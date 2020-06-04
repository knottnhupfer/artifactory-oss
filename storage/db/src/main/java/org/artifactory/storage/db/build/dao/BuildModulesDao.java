/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.storage.db.build.dao;

import org.apache.commons.lang.StringUtils;
import org.artifactory.storage.db.build.entity.BuildModule;
import org.artifactory.storage.db.build.entity.ModuleProperty;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.JdbcHelper;
import org.jfrog.storage.util.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Date: 10/30/12
 * Time: 12:44 PM
 *
 * @author freds
 */
@Repository
public class BuildModulesDao extends BaseDao {

    private static final Logger log = LoggerFactory.getLogger(BuildModulesDao.class);
    private static final int PROP_VALUE_MAX_SIZE = 2048;

    @Autowired
    public BuildModulesDao(JdbcHelper jdbcHelper) {
        super(jdbcHelper);
    }

    public int createBuildModules(List<BuildModule> bms) throws SQLException {
        int res = 0;
        for (BuildModule bm : bms) {
            res += createBuildModule(bm);
        }
        return res;
    }

    public int createBuildModule(BuildModule bm) throws SQLException {
        int res = jdbcHelper.executeUpdate("INSERT INTO build_modules VALUES(" +
                "?, ?, ?)",
                bm.getModuleId(), bm.getBuildId(), bm.getModuleNameId());

        int maxPropValue = getDbIndexedValueMaxSize(PROP_VALUE_MAX_SIZE);
        for (ModuleProperty bmp : bm.getProperties()) {
            String propValue = bmp.getPropValue();
            if (propValue != null && propValue.length() > maxPropValue) {
                log.info("Trimming property value to {} characters {}", maxPropValue, bmp.getPropKey());
                log.debug("Trimming property value to {} characters {}: {}", maxPropValue, bmp.getPropKey(),
                        bmp.getPropValue());
                propValue = StringUtils.substring(bmp.getPropValue(), 0, maxPropValue);
            }
            res += jdbcHelper.executeUpdate("INSERT INTO module_props VALUES(" +
                    "?, ?, ?, ?)",
                    bmp.getPropId(), bmp.getModuleId(), bmp.getPropKey(), propValue);
        }
        return res;
    }

    public int deleteBuildModules(long buildId) throws SQLException {
        List<Long> moduleIdsForBuild = findModuleIdsForBuild(buildId);
        int res = 0;
        if (!moduleIdsForBuild.isEmpty()) {
            res = jdbcHelper.executeUpdate("DELETE FROM module_props WHERE module_id IN (#)", moduleIdsForBuild);
        }
        res += jdbcHelper.executeUpdate("DELETE FROM build_modules WHERE build_id = ?", buildId);
        return res;
    }

    public int deleteAllBuildModules() throws SQLException {
        int res = jdbcHelper.executeUpdate("DELETE FROM module_props");
        res += jdbcHelper.executeUpdate("DELETE FROM build_modules");
        return res;
    }

    public List<Long> findModuleIdsForBuild(long buildId) throws SQLException {
        ResultSet rs = null;
        List<Long> moduleIds = null;
        try {
            rs = jdbcHelper.executeSelect("SELECT module_id FROM build_modules WHERE build_id = ?", buildId);
            moduleIds = new ArrayList<>();
            while (rs.next()) {
                moduleIds.add(rs.getLong(1));
            }
        } finally {
            DbUtils.close(rs);
        }
        return moduleIds;
    }

    public List<BuildModule> findModulesForBuild(long buildId) throws SQLException {
        ResultSet resultSet = null;
        List<BuildModule> modules = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT * FROM build_modules WHERE build_id = ?", buildId);
            modules = new ArrayList<>();
            while (resultSet.next()) {
                modules.add(resultSetToBuildModule(resultSet));
            }
        } finally {
            DbUtils.close(resultSet);
        }
        for (BuildModule module : modules) {
            module.setProperties(findModuleProperties(module.getModuleId()));
        }
        return modules;
    }

    private Set<ModuleProperty> findModuleProperties(long moduleId) throws SQLException {
        Set<ModuleProperty> props = new HashSet<>(3);
        ResultSet rs = null;
        try {
            rs = jdbcHelper.executeSelect("SELECT * FROM module_props WHERE module_id = ?", moduleId);
            while (rs.next()) {
                props.add(resultSetToModuleProperty(rs));
            }
        } finally {
            DbUtils.close(rs);
        }
        return props;
    }

    private ModuleProperty resultSetToModuleProperty(ResultSet rs) throws SQLException {
        return new ModuleProperty(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getString(4));
    }

    private BuildModule resultSetToBuildModule(ResultSet rs) throws SQLException {
        return new BuildModule(rs.getLong(1), rs.getLong(2), rs.getString(3));
    }
}
