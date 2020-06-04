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

import com.google.common.collect.Lists;
import org.apache.commons.compress.utils.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.build.*;
import org.artifactory.api.build.model.diff.BuildParams;
import org.artifactory.api.jackson.JacksonReader;
import org.artifactory.api.rest.build.ContinueBuildFilter;
import org.artifactory.build.BuildId;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.repo.trash.TrashService;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.build.dao.utils.BuildsDaoUtils;
import org.artifactory.storage.db.build.entity.*;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.storage.db.util.querybuilder.ArtifactoryQueryWriter;
import org.jfrog.storage.util.DbUtils;
import org.jfrog.storage.util.querybuilder.QueryWriter;
import org.jfrog.storage.wrapper.BlobWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.jfrog.storage.util.DbUtils.tableExists;

/**
 * Date: 10/30/12
 * Time: 12:44 PM
 *
 * @author freds
 */
@Repository
public class BuildsDao extends BaseDao {
    private static final Logger log = LoggerFactory.getLogger(BuildsDao.class);

    private static final int PROP_VALUE_MAX_SIZE = 2048;
    private static final String DELETE_JSON_BY_BUILD_ID = "DELETE FROM build_jsons WHERE build_id=?";
    private static final String BUILD_JSONS_TABLE = "build_jsons";

    private ArtifactoryDbProperties dbProperties;
    private DbService dbService;

    @Autowired
    public BuildsDao(JdbcHelper jdbcHelper, ArtifactoryDbProperties dbProperties, DbService dbService) {
        super(jdbcHelper);
        this.dbProperties = dbProperties;
        this.dbService = dbService;
    }

    public int createBuild(BuildEntity build, BlobWrapper jsonBlob) throws SQLException {
        int res = createBuild(build);
        res += createBuildJsonIfNeeded(build.getBuildId(), jsonBlob, false);
        res = createBuildProps(build, res);
        res = createPromotions(build, res);
        return res;
    }

    private int createBuild(BuildEntity b) throws SQLException {
        return jdbcHelper.executeUpdate("INSERT INTO builds " +
                            "(build_id, " +
                            "build_name, build_number, build_date, " +
                            "ci_url, created, created_by, " +
                            "modified, modified_by) " +
                            "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    b.getBuildId(),
                    b.getBuildName(), b.getBuildNumber(), b.getBuildDate(),
                    b.getCiUrl(), b.getCreated(), b.getCreatedBy(),
                    nullIfZero(b.getModified()), b.getModifiedBy());
    }

    private int createBuildProps(BuildEntity b, int res) throws SQLException {
        int maxPropValue = getDbIndexedValueMaxSize(PROP_VALUE_MAX_SIZE);
        int nbProps = b.getProperties().size();
        if (nbProps != 0) {
            for (BuildProperty bp : b.getProperties()) {
                String propValue = bp.getPropValue();
                if (propValue != null && propValue.length() > maxPropValue) {
                    log.info("Trimming property value to {} characters {}",maxPropValue, bp.getPropKey());
                    log.debug("Trimming property value to {} characters {}: {}", maxPropValue,bp.getPropKey(), bp.getPropValue());
                    propValue = StringUtils.substring(propValue, 0, maxPropValue);
                }
                res += jdbcHelper.executeUpdate("INSERT INTO build_props " +
                                "(prop_id, build_id, prop_key, prop_value) " +
                                "VALUES (?,?,?,?)",
                        bp.getPropId(), bp.getBuildId(), bp.getPropKey(), propValue);
            }
        }
        return res;
    }

    private int createPromotions(BuildEntity b, int res) throws SQLException {
        int nbPromotions = b.getPromotions().size();
        if (nbPromotions != 0) {
            for (BuildPromotionStatus bp : b.getPromotions()) {
                res += jdbcHelper.executeUpdate("INSERT INTO build_promotions " +
                                "(build_id, created, created_by, status, repo, promotion_comment, ci_user) " +
                                "VALUES (?,?,?,?,?,?,?)",
                        bp.getBuildId(), bp.getCreated(), bp.getCreatedBy(),
                        bp.getStatus(), bp.getRepository(), bp.getComment(), bp.getCiUser());
            }
        }
        return res;
    }

    public int rename(long buildId, String newName, BlobWrapper jsonBlob, String currentUser, long currentTime) throws SQLException {
        int res = jdbcHelper.executeUpdate("UPDATE builds SET" +
                " build_name = ?, modified = ?, modified_by = ?" +
                " WHERE build_id = ?", newName, currentTime, currentUser, buildId);
        res += createBuildJsonIfNeeded(buildId, jsonBlob, true);
        return res;
    }

    public int addPromotionStatus(long buildId, BuildPromotionStatus promotionStatus, BlobWrapper jsonBlob, String currentUser, long currentTime) throws SQLException {
        int res = jdbcHelper.executeUpdate("UPDATE builds SET" +
                " modified = ?, modified_by = ?" +
                " WHERE build_id = ?", currentTime, currentUser, buildId);
        res += createBuildJsonIfNeeded(buildId, jsonBlob, true);
        res += jdbcHelper.executeUpdate("INSERT INTO build_promotions " +
                        "(build_id, created, created_by, status, repo, promotion_comment, ci_user) " +
                        "VALUES (?,?,?,?,?,?,?)",
                promotionStatus.getBuildId(), promotionStatus.getCreated(), promotionStatus.getCreatedBy(),
                promotionStatus.getStatus(), promotionStatus.getRepository(), promotionStatus.getComment(),
                promotionStatus.getCiUser());
        return res;
    }

    /**
     * In legacy mode (up until the converter finishes) all nodes write both into database and repo.
     * This means adding/replacing in the build jsons table is still required
     */
    private int createBuildJsonIfNeeded(long buildId, BlobWrapper jsonBlob, boolean delete) throws SQLException {
        int res = 0;
        if (jsonBlob != null) {
            if (delete) {
                res += jdbcHelper.executeUpdate(DELETE_JSON_BY_BUILD_ID, buildId);
            }
            res += jdbcHelper.executeUpdate("INSERT INTO build_jsons (build_id, build_info_json) VALUES(?, ?)", buildId, jsonBlob);
        }
        return res;
    }

    public int deleteAllBuilds(boolean legacy) throws SQLException {
        int res = jdbcHelper.executeUpdate("DELETE FROM build_props");
        if (legacy) {
            res += jdbcHelper.executeUpdate("DELETE FROM build_jsons");
        }
        res += jdbcHelper.executeUpdate("DELETE FROM build_promotions");
        res += jdbcHelper.executeUpdate("DELETE FROM builds");
        return res;
    }

    public int deleteBuild(long buildId, boolean legacy) throws SQLException {
        int res = jdbcHelper.executeUpdate("DELETE FROM build_props WHERE build_id=?", buildId);
        if (legacy) {
            res += jdbcHelper.executeUpdate(DELETE_JSON_BY_BUILD_ID, buildId);
        }
        res += jdbcHelper.executeUpdate("DELETE FROM build_promotions WHERE build_id=?", buildId);
        res += jdbcHelper.executeUpdate("DELETE FROM builds WHERE build_id=?", buildId);
        return res;
    }

    public <T> T getJsonBuild(long buildId, Class<T> clazz) throws SQLException {
        ResultSet rs = null;
        InputStream jsonStream = null;
        try {
            rs = jdbcHelper.executeSelect("SELECT build_info_json FROM build_jsons WHERE build_id = ?", buildId);
            if (rs.next()) {
                jsonStream = rs.getBinaryStream(1);
                if (CharSequence.class.isAssignableFrom(clazz)) {
                    //noinspection unchecked
                    return (T) IOUtils.toString(jsonStream, Charsets.UTF_8.name());
                }
                return JacksonReader.streamAsClass(jsonStream, clazz);
            }
        } catch (IOException e) {
            throw new SQLException("Failed to read JSON data for build '" + buildId + "' due to: " + e.getMessage(), e);
        } finally {
            DbUtils.close(rs);
            IOUtils.closeQuietly(jsonStream);
        }
        return null;
    }

    public BuildEntity getBuild(long buildId) throws SQLException {
        BuildEntityRecord build = getBuildEntityRecord(buildId);
        if (build == null) {
            return null;
        }
        return new BuildEntity(build,
            findBuildProperties(build.getBuildId()),
            findBuildPromotions(build.getBuildId()));
    }

    /**
     * return only the  build record without properties which aren't set yet.
     */
    private BuildEntityRecord getBuildEntityRecord(long buildId) throws SQLException {
        ResultSet rs = null;
        BuildEntity build = null;
        try {
            rs = jdbcHelper.executeSelect("SELECT * FROM builds WHERE build_id = ?", buildId);
            if (rs.next()) {
                build = resultSetToBuild(rs);
            }
        } finally {
            DbUtils.close(rs);
        }
        return build;
    }

    /**
     * returns a list of previous builds
     */
    public List<GeneralBuild> getPrevBuildsList(String buildName, String buildDate) {
        ResultSet rs = null;
        List<GeneralBuild> buildList = new ArrayList<>();
        try {
            String buildsQuery = "SELECT builds.*, build_promotions.status " +
                    "FROM builds " +
                    "LEFT JOIN build_promotions on build_promotions.build_id = builds.build_id  " +
                    "WHERE build_name = ? and build_date < ? " +
                    "ORDER BY build_number DESC";
            rs = jdbcHelper.executeSelect(buildsQuery, buildName, Long.parseLong(buildDate));
            while (rs.next()) {
                GeneralBuild buildEntity = resultSetToGeneralBuild(rs, rs.getLong(1));
                buildEntity.setStatus(rs.getString(10));
                buildList.add(buildEntity);
            }
        } catch (Exception e) {
            log.error(e.toString());
        } finally{
            DbUtils.close(rs);
        }
        return buildList;
    }

    public List<GeneralBuild> getBuildForName(String buildName, ContinueBuildFilter continueBuildFilter) throws SQLException {
        ResultSet rs = null;
        List<GeneralBuild> buildList;
        if (continueBuildFilter.getLimit() <= 0) {
            return Collections.emptyList();
        }
        try {

            List<Object> params = new ArrayList<>();
            QueryWriter buildsQuery = new QueryWriter(dbService.getDatabaseType());
            buildsQuery.select("builds.* , \n" +
                                "         '0' as module_cnt ,\n" +
                                "         '0' as artifact_cnt,\n" +
                                "         '0' as dependency_cnt ")
                        .from("builds ")
                        .orderBy("build_date DESC, build_number DESC")
                        .where("build_name = ?")
                        .limit(continueBuildFilter.getLimit());
            params.add(buildName);
            BuildId continueBuild = continueBuildFilter.getContinueBuildId();
            if (continueBuild != null) {
                String whereAddition = " AND (build_date < ? OR (build_date = ? AND build_number < ? )) ";
                buildsQuery.where("build_name = ? " + whereAddition);
                params.add(continueBuild.getStartedDate().getTime());
                params.add(continueBuild.getStartedDate().getTime());
                params.add(continueBuild.getNumber());
            }

            rs = jdbcHelper.executeSelect(buildsQuery.build(), params.toArray());
            buildList = resultSetToGeneralBuildList(rs);
        } finally {
            DbUtils.close(rs);
        }
        return Optional.ofNullable(buildList).orElse(Collections.emptyList());
    }

    private List<GeneralBuild> resultSetToGeneralBuildList(ResultSet rs) throws SQLException {
        List<GeneralBuild> answer = new ArrayList<>();
        while (rs.next()) {
            Long id = rs.getLong("build_id");
            GeneralBuild buildEntity = resultSetToGeneralBuild(rs, id);
            buildEntity.setNumOfModules(Integer.toString(rs.getInt("module_cnt")));
            buildEntity.setNumOfArtifacts(Integer.toString(rs.getInt("artifact_cnt")));
            buildEntity.setNumOfDependencies(Integer.toString(rs.getInt("dependency_cnt")));
            answer.add(buildEntity);
        }
        return answer;
    }

    /**
     * get Module Artifact diff with paging
     *
     * @return
     */
    public List<ModuleArtifact> getModuleArtifactsForDiffWithPaging(BuildParams buildParams) {
        ResultSet rs = null;
        List<ModuleArtifact> artifacts = new ArrayList<>();
        Map<String, ModuleArtifact> artifactMap = new HashMap<>();
        ResultSet rsArtCurr = null;
        ResultSet rsArtPrev = null;
        try {
            Object[] diffParams = getArtifatBuildQueryParam(buildParams);
            String buildQuery = getArtifactBuildDiffQuery(buildParams);
            rs = jdbcHelper.executeSelect(buildQuery, diffParams);
            while (rs.next()) {
                ModuleArtifact artifact = new ModuleArtifact(null, null, rs.getString(1), rs.getString(2), rs.getString(3));
                artifact.setStatus(rs.getString(4));
                if (buildParams.isAllArtifact()) {
                    artifact.setModule(rs.getString(5));
                }
                artifacts.add(artifact);
            }
            // update artifact repo path data
            if (!artifacts.isEmpty()) {
                rsArtCurr = getArtifactNodes(buildParams.getBuildName(), buildParams.getCurrBuildNum(), artifactMap);
                if (buildParams.isAllArtifact()) {
                    rsArtPrev = getArtifactNodes(buildParams.getBuildName(), buildParams.getComperedBuildNum(), artifactMap);
                }
                for (ModuleArtifact artifact : artifacts) {
                    ModuleArtifact moduleArtifact = artifactMap.get(artifact.getSha1());
                    if (moduleArtifact != null) {
                        artifact.setRepoKey(moduleArtifact.getRepoKey());
                        artifact.setPath(moduleArtifact.getPath());
                    }
                }
            }
        } catch (SQLException e) {
            log.error(e.toString(), e);
        } finally {
            DbUtils.close(rsArtCurr);
            DbUtils.close(rsArtPrev);
            DbUtils.close(rs);
        }
        return artifacts;
    }

    private String getArtifactBuildDiffQuery(BuildParams buildParams) {
        if (!buildParams.isAllArtifact()) {
            return BuildQueries.MODULE_ARTIFACT_DIFF_QUERY;
        } else {
            return BuildQueries.BUILD_ARTIFACT_DIFF_QUERY;
        }
    }

    /**
     * return diif param for artifact diff query
     * @param buildParams
     * @return
     */
    private Object[] getArtifatBuildQueryParam(BuildParams buildParams) {
        if (!buildParams.isAllArtifact()) {
            return new Object[]{buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(), buildParams.getBuildModuleId()};
        } else {
            return new Object[]{buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate()};
        }
    }

    /**
     * get BuildProps through distinct filter.
     * @param buildParams
     * @return
     */
    public List<BuildProps> getBuildPropsList(BuildParams buildParams) {
        String buildPropsQuery;
        if (buildParams.isEnvProps()) {
            buildPropsQuery = BuildQueries.BUILD_ENV_PROPS_BY_BUILD_ID;
        } else {
            buildPropsQuery = BuildQueries.BUILD_SYSTEM_PROPS_BY_BUILD_ID;
        }
        Long buildId = getBuildId(buildParams);
        return getBuildPropsList(buildPropsQuery, buildId);
    }

    /**
     * get build props - ditinct programqtically.
     * would be slow on large list but several thousands should be fine.
     * @param buildPropsQuery
     * @param buildId
     * @return
     */
    public List<BuildProps> getBuildPropsList(String buildPropsQuery, Long buildId) {
        List<BuildProps> buildPropsList = new ArrayList<>();
        if (buildId==null) return  buildPropsList;

        ResultSet rs = null;
        try {
            rs = jdbcHelper.executeSelect(buildPropsQuery, new Object[]{buildId});
            while(rs.next()){
                buildPropsList.add(resultSetToBuildProps(rs));
            }
        } catch (SQLException e) {
            log.error(e.toString());
        }finally {
            DbUtils.close(rs);
        }
        return buildPropsList;
    }

    public Long getBuildId(BuildParams buildParams) {
        Object[] diffParams = getBuildPropsParam(buildParams);
        ResultSet rs = null;
        try {
            rs = jdbcHelper.executeSelect(BuildQueries.BUILD_BY_NAME_NUMBER_DATE, diffParams);
            if (rs.next()) {
                Long res = rs.getLong(1);
                if (rs.next()) {
                    // more then one build found ...
                    log.error("More then one build by number: " + diffParams[0] + " date:" + diffParams[1]);
                }
                return res;
            }
        } catch (SQLException e) {
            log.error(e.toString());
        } finally {
            DbUtils.close(rs);
        }
        return null;
    }

    /**
     * get build props (env or system) param to be included in sql query
     *
     * @param buildParams - build params
     * @return list of build props param
     */
    private Object[] getBuildPropsParam(BuildParams buildParams) {
        return new Object[]{buildParams.getBuildName(), buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate()};
    }

    /**
     * get Module Artifact diff with paging
     *
     * @return
     */
    public List<ModuleDependency> getModuleDependencyForDiffWithPaging(BuildParams buildParams) {
        ResultSet rs = null;
        ResultSet rsDep = null;
        ResultSet rsDepCompared = null;
        List<ModuleDependency> dependencies = new ArrayList<>();
        Map<String, ModuleDependency> moduleDependencyMap = new HashMap<>();
        try {
            StringBuilder builder = new StringBuilder(getBaseDependencyQuery(buildParams));
            Object[] diffParams = getBuildDependencyParams(buildParams);
            /// update query with specific conditions
            updateQueryWithSpecificConditions(buildParams, builder);
            String buildQuery = builder.toString();
            rs = jdbcHelper.executeSelect(buildQuery, diffParams);
            Map<String, String> tempDependencyMap = new HashMap<>();
            StringBuilder inClauseBuilder = new StringBuilder();
            inClauseBuilder.append("(");
            while (rs.next()) {
                String sha1 = rs.getString(3);
                if (tempDependencyMap.get(sha1) == null) {
                    tempDependencyMap.put(sha1, sha1);
                    ModuleDependency dependency = new ModuleDependency(null, null, rs.getString(1),
                            rs.getString(2), rs.getString(4), sha1);
                    dependency.setStatus(rs.getString(5));
                    if (buildParams.isAllArtifact()) {
                        dependency.setModule(rs.getString(6));
                    }
                    dependencies.add(dependency);
                }
                inClauseBuilder.append("'" + sha1 + "'").append(",");
            }
            String inClause = inClauseBuilder.toString();
            inClause = inClause.substring(0, inClause.length() - 1);
            inClause = inClause + ")";
            // update dependencies repo path data
            if (!dependencies.isEmpty()) {
                rsDep = getModuleDependencyNodes(moduleDependencyMap, inClause);
                if (buildParams.isAllArtifact()) {
                    rsDepCompared = getModuleDependencyNodes(moduleDependencyMap, inClause);
                }
                dependencies.forEach(dependency -> {
                    ModuleDependency moduleDependency = moduleDependencyMap.get(dependency.getSha1());
                    if (moduleDependency != null) {
                        dependency.setRepoKey(moduleDependency.getRepoKey());
                        String path = moduleDependency.getPath();
                        String name = moduleDependency.getName();
                        if (path != null) {
                            dependency.setPath(path.equals(".") ? name : path + "/" + name);
                        }
                    }
                });
            }
        } catch (SQLException e) {
            log.error(e.toString());
        } finally {
            DbUtils.close(rsDep);
            DbUtils.close(rsDepCompared);
            DbUtils.close(rs);
        }
        return dependencies;
    }

    /**
     * update build with specific condition for exclude and full build diff
     *
     * @param buildParams - build params
     * @param builder - build diff query writer
     */
    private void updateQueryWithSpecificConditions(BuildParams buildParams, StringBuilder builder) {
        if (buildParams.isExcludeInternalDependencies()) {
            // exclude internal dependencies
            builder.append(" where c not in (select build_modules.module_name_id  from build_modules \n" +
                    "inner join builds on builds.build_id = build_modules.build_id\n" +
                    " where builds.build_number=? and builds.build_date=?)");
        }
    }

    /**
     * get build dependency query params for all build dependency diff or build module dependency diff
     *
     * @param buildParams - build diff param
     * @return - build dependency param for diff query
     */
    private Object[] getBuildDependencyParams(BuildParams buildParams) {
        // build params for all build artifact query
        if (!buildParams.isAllArtifact()) {
            return new Object[]{buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(), buildParams.getBuildModuleId()};
        } else {// build params for module build artifact query
            if (buildParams.isExcludeInternalDependencies()) {
                return new Object[]{buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                        buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(),
                        buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(),
                        buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                        buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                        buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(),
                        buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                        buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(),
                        buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate()};
            } else {
                return new Object[]{buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                        buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(),
                        buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(),
                        buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                        buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                        buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(),
                        buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                        buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate()};
            }
        }
    }

    /**
     * get build dependency query for all build dependency diff or build module dependency diff
     *
     * @param buildParams - build diff param
     * @return - build dependency query for diff query
     */
    private String getBaseDependencyQuery(BuildParams buildParams) {
        String baseQuery;
        if (!buildParams.isAllArtifact()) {
            baseQuery = BuildQueries.MODULE_DEPENDENCY_DIFF_QUERY;
        } else {
            baseQuery = BuildQueries.BUILD_DEPENDENCY_DIFF_QUERY;
        }
        return baseQuery;
    }

    public long findBuildId(String name, String number, long startDate) throws SQLException {
        ResultSet rs = null;
        try {
            rs = jdbcHelper.executeSelect("SELECT build_id FROM builds WHERE" +
                    " build_name = ? AND build_number = ? AND build_date = ?",
                    name, number, startDate);
            if (rs.next()) {
                return rs.getLong(1);
            }
        } finally {
            DbUtils.close(rs);
        }
        return 0L;
    }

    public BuildEntity findBuild(String name, String number, long startDate) throws SQLException {
        long buildId = findBuildId(name, number, startDate);
        if (buildId > 0L) {
            return getBuild(buildId);
        }
        return null;
    }

    public BuildEntity getLatestBuild(String buildName) throws SQLException {
        long latestBuildDate = 0L;
        ResultSet rs = null;
        try {
            rs = jdbcHelper.executeSelect("SELECT max(build_date) FROM builds WHERE build_name = ?", buildName);
            if (rs.next()) {
                latestBuildDate = rs.getLong(1);
            }
        } finally {
            DbUtils.close(rs);
            rs = null;
        }
        BuildEntity buildEntity = null;
        if (latestBuildDate > 0L) {
            try {
                rs = jdbcHelper.executeSelect("SELECT * FROM builds " +
                        "WHERE build_name = ? AND build_date = ?", buildName, latestBuildDate);
                if (rs.next()) {
                    buildEntity = resultSetToBuild(rs);
                }
            } finally {
                DbUtils.close(rs);
            }
        }
        if (buildEntity != null) {
            buildEntity.setProperties(findBuildProperties(buildEntity.getBuildId()));
            buildEntity.setPromotions(findBuildPromotions(buildEntity.getBuildId()));
        }
        return buildEntity;
    }

    public long findLatestBuildDate(String buildName, String buildNumber) throws SQLException {
        long latestBuildDate = 0L;
        ResultSet rs = null;
        try {
            rs = jdbcHelper.executeSelect("SELECT max(build_date) FROM builds WHERE" +
                    " build_name = ? AND build_number = ?",
                    buildName, buildNumber);
            if (rs.next()) {
                latestBuildDate = rs.getLong(1);
            }
        } finally {
            DbUtils.close(rs);
        }
        return latestBuildDate;
    }

    public List<Long> findBuildIds(String buildName) throws SQLException {
        ResultSet rs = null;
        List<Long> buildIds = new ArrayList<>();
        try {
            rs = jdbcHelper.executeSelect("SELECT build_id FROM builds WHERE" +
                    " build_name = ? ORDER BY build_date DESC",
                    buildName);
            while (rs.next()) {
                buildIds.add(rs.getLong(1));
            }
        } finally {
            DbUtils.close(rs);
        }
        return buildIds;
    }

    public List<Long> findBuildIds(String buildName, String buildNumber) throws SQLException {
        ResultSet rs = null;
        List<Long> buildIds = new ArrayList<>();
        try {
            rs = jdbcHelper.executeSelect("SELECT build_id FROM builds WHERE" +
                    " build_name = ? AND build_number = ? ORDER BY build_date DESC",
                    buildName, buildNumber);
            while (rs.next()) {
                buildIds.add(rs.getLong(1));
            }
        } finally {
            DbUtils.close(rs);
        }
        return buildIds;
    }

    public List<String> getAllBuildNames() throws SQLException {
        ResultSet rs = null;
        List<String> buildNames = new ArrayList<>();
        try {
            rs = jdbcHelper.executeSelect(
                    "SELECT build_name, max(build_date) d FROM builds GROUP BY build_name ORDER BY d");
            while (rs.next()) {
                buildNames.add(rs.getString(1));
            }
        } finally {
            DbUtils.close(rs);
        }
        return buildNames;
    }

    /**
     * get build modules with paging
     * @param buildNumber - build name
     * @param date - date
     */
    public List<PublishedModule> getBuildModule(String buildNumber, String date) throws SQLException {
        ResultSet rs = null;
        String buildQuery = "SELECT build_modules.module_name_id,\n" +
                "(select count(*) from build_artifacts where build_artifacts.module_id =  build_modules.module_id ) as num_of_art ,\n" +
                "(select count(*) from build_dependencies where build_dependencies.module_id =  build_modules.module_id ) as num_of_dep FROM build_modules\n" +
                "left join builds on builds.build_id=build_modules.build_id \n" +
                "where  builds.build_number=? and builds.build_date=?";

        List<PublishedModule> modules = new ArrayList<>();
        try {
            rs = jdbcHelper.executeSelect(buildQuery, buildNumber, Long.parseLong(date));
            while (rs.next()) {
                PublishedModule module = new PublishedModule();
                module.setId(rs.getString(1));
                module.setNumOfArtifact(rs.getString(2));
                module.setNumOfDependencies(rs.getString(3));
                modules.add(module);
            }
        } finally {
            DbUtils.close(rs);
        }
        return modules;
    }


    /**
     * get build modules artifact with paging
     *
     * @param buildNumber - build name
     * @param date        - build date
     * @return
     * @throws SQLException
     */
    public List<ModuleArtifact> getModuleArtifact(String buildName, String buildNumber, String date,
            String moduleId) throws SQLException {
        ResultSet rsArtifact = null;
        ResultSet rs = null;
        List<ModuleArtifact> artifacts = new ArrayList<>();
        Map<String, ModuleArtifact> artifactMap = new HashMap<>();
        try {
            // get artifact info
            rs = getPaginatedArtifact(buildNumber, Long.parseLong(date), moduleId);
            while (rs.next()) {
                artifacts.add(new ModuleArtifact(null, null, rs.getString(1), rs.getString(2), rs.getString(3)));
            }
            if (!artifacts.isEmpty()) {
                // query for artifact nodes
                rsArtifact = getArtifactNodes(buildName, buildNumber, artifactMap);
                for (ModuleArtifact artifact : artifacts) {
                    ModuleArtifact moduleArtifact = artifactMap.get(artifact.getSha1());
                    if (moduleArtifact != null) {
                        artifact.setRepoKey(moduleArtifact.getRepoKey());
                        artifact.setPath(moduleArtifact.getPath());
                    }
                }
            }

        } finally {
            DbUtils.close(rsArtifact);
            DbUtils.close(rs);
        }
        return artifacts;
    }

    /**
     * get module artifact info
     *
     * @param buildNumber - build number
     * @param date        - build date
     * @param moduleId    - module id
     * @return query result set
     */
    private ResultSet getPaginatedArtifact(String buildNumber, Long date, String moduleId) throws SQLException {
        ResultSet rs;
        String buildQuery = "SELECT distinct build_artifacts.artifact_name as name,build_artifacts.artifact_type as type,build_artifacts.sha1 FROM build_artifacts\n" +
                "left join build_modules on build_modules.module_id=build_artifacts.module_id\n" +
                "left join builds on  build_modules.build_id = builds.build_id\n" +
                "where builds.build_number = ? and builds.build_date = ? and build_modules.module_name_id = ?";

        rs = jdbcHelper.executeSelect(buildQuery, buildNumber, date, moduleId);

        return rs;
    }

    /**
     * get Artifact nodes data by build name and number
     * Note - who ever use this method must be responsible for close the result set
     *
     * @param buildName   - build name
     * @param buildNumber - build number
     * @param artifactMap - map of data , key = sha1 , value = module artifact
     * @return query result set
     * @throws SQLException
     */
    private ResultSet getArtifactNodes(String buildName, String buildNumber, Map<String, ModuleArtifact> artifactMap) throws SQLException {
        ResultSet rsArtifact;
        rsArtifact = jdbcHelper.executeSelect("select distinct  n.repo,n.node_path,n.node_name,n.node_id,n.depth,n.sha1_actual,n.sha1_original,n.md5_actual,n.md5_original  \n" +
                "from  nodes n left outer join node_props np100 on np100.node_id = n.node_id left outer join node_props np101 on np101.node_id = n.node_id \n" +
                "where (( np100.prop_key = 'build.name' and  np100.prop_value = ?) and( np101.prop_key = 'build.number' and  np101.prop_value =?)) and n.repo != '" + TrashService.TRASH_KEY + "' and n.node_type = 1", buildName, buildNumber);

        while (rsArtifact.next()) {
            String sha1 = rsArtifact.getString(6);
            if (artifactMap.get(sha1) == null) {
                artifactMap.put(sha1, new ModuleArtifact(rsArtifact.getString(1), rsArtifact.getString(2), rsArtifact.getString(3), null, null));
            }
        }
        return rsArtifact;
    }

    /**
     * get build modules dependencies with paging
     *
     * @param buildNumber - build name
     * @param date        - date
     * @return
     * @throws SQLException
     */
    public List<ModuleDependency> getModuleDependency(String buildNumber, String date, String moduleId) throws SQLException {
        ResultSet rs = null;
        ResultSet rsDep = null;
        Map<String, ModuleDependency> moduleDependencyMap = new HashMap<>();
        String buildQuery = "SELECT distinct build_dependencies.dependency_name_id as id," +
                "build_dependencies.dependency_type as type,build_dependencies.dependency_scopes as scope," +
                "build_dependencies.sha1 FROM build_dependencies\n" +
                "left join build_modules on build_modules.module_id=build_dependencies.module_id\n" +
                "left join builds on  build_modules.build_id = builds.build_id\n" +
                "where builds.build_number = ? and builds.build_date = ? and build_modules.module_name_id = ?";

        List<ModuleDependency> dependencies = new ArrayList<>();
        try {
            rs = jdbcHelper.executeSelect(buildQuery, buildNumber, Long.parseLong(date), moduleId);
            StringBuilder inClauseBuilder = new StringBuilder();
            inClauseBuilder.append("(");
            while (rs.next()) {
                String sha1 = rs.getString(4);
                dependencies.add(new ModuleDependency(null, null, rs.getString(1), rs.getString(2), rs.getString(3),
                        sha1));
                inClauseBuilder.append("'" + sha1 + "'").append(",");
            }
            String inClause = inClauseBuilder.toString();
            inClause = inClause.substring(0, inClause.length() - 1);
            inClause = inClause + ")";

            if (!dependencies.isEmpty()) {
                // get repo key and path data for dependency
                rsDep = getModuleDependencyNodes(moduleDependencyMap, inClause);
                dependencies.forEach(dependency -> {
                    ModuleDependency moduleDependency = moduleDependencyMap.get(dependency.getSha1());
                    if (moduleDependency != null) {
                        dependency.setRepoKey(moduleDependency.getRepoKey());
                        String path = moduleDependency.getPath();
                        String name = moduleDependency.getName();
                        if (path != null) {
                            dependency.setPath(path.equals(".") ? name : path + "/" + name);
                        }
                    }
                });
            }
        } finally {
            DbUtils.close(rsDep);
            DbUtils.close(rs);
        }
        return dependencies;
    }

    private ResultSet getModuleDependencyNodes(Map<String, ModuleDependency> moduleDependencyMap, String inClause)
            throws SQLException {
        ResultSet rsDep = jdbcHelper.executeSelect(
                "SELECT distinct nodes.repo,nodes.node_path,nodes.node_name,nodes.sha1_actual FROM nodes\n" +
                        "                where nodes.repo != '" + TrashService.TRASH_KEY + "' and nodes.sha1_actual in " + inClause);
        while (rsDep.next()) {
            String sha1 = rsDep.getString(4);
            if (moduleDependencyMap.get(sha1) == null) {
                moduleDependencyMap.put(sha1, new ModuleDependency(rsDep.getString(1), rsDep.getString(2), rsDep.getString(3), null, null, null));
            }
        }
        return rsDep;
    }

    public List<BuildIdEntity> getLatestBuildIds(ContinueBuildFilter continueBuildFilter) throws SQLException {
        ResultSet rs = null;
        List<BuildIdEntity> buildNames = new ArrayList<>();
        if (continueBuildFilter.getLimit() <= 0) {
            return buildNames;
        }
        try {
            QueryWriter queryWriter = new QueryWriter(dbService.getDatabaseType());
            queryWriter.select("d.build_id, d.build_name, d.build_number, d.build_date")
                    .from("builds d ")
                    .innerJoin("(select c.build_name, max(c.build_date) build_time from builds c group by c.build_name ) k",
                            "k.build_name=d.build_name AND k.build_time=d.build_date")
                    .limit(continueBuildFilter.getLimit());
            String orderByStr = BuildsDaoUtils.createOrderByStr(continueBuildFilter);
            queryWriter.orderBy(orderByStr);
            List<Object> sqlParametersList = new ArrayList<>();
            String whereClause = BuildsDaoUtils.createWhereClauseFromContinueBuildFilter(continueBuildFilter, sqlParametersList);
            if (StringUtils.isNotBlank(whereClause)) {
                queryWriter.where(whereClause);
            }
            rs = jdbcHelper.executeSelect(queryWriter.build(), sqlParametersList.toArray());
            buildNames = BuildsDaoUtils.populateBuildIdEntryList(rs);
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DbUtils.close(rs);
        }
        return buildNames;
    }

    /**
     * Default sorting is by date desc
     */
    @Nonnull
    public List<BuildIdEntity> getLatestBuildIds(String buildName, String fromDate, String toDate, String orderBy, String direction) throws SQLException {
        ResultSet rs = null;
        List<BuildIdEntity> builds = new ArrayList<>();
        List<Object> whereParams = new LinkedList<>();
        try {
            String query = "SELECT a.build_id, a.build_name, a.build_number, a.build_date\n" +
                    "  FROM builds a\n" +
                    "  INNER JOIN\n" +
                    "    (SELECT build_name, max(build_date) build_time\n" +
                    "    FROM builds\n" +
                    "    WHERE build_name in\n" +
                    "          (SELECT build_name FROM builds\n" +
                                buildWhereClause(whereParams, buildName, fromDate, toDate) +
                    "          GROUP BY build_name)\n" +
                    "      GROUP BY build_name) b\n" +
                    "  ON a.build_name = b.build_name AND a.build_date = build_time\n" +
                        buildOrderByClause(orderBy, direction);
            rs = jdbcHelper.executeSelect(query, whereParams);
            while (rs.next()) {
                builds.add(new BuildIdEntity(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getLong(4)));
            }
        } finally {
            DbUtils.close(rs);
        }
        return builds;
    }

    private String buildWhereClause(List<Object> whereParams, String buildName, String fromDate, String toDate) {
        StringBuilder where = new StringBuilder();
        if (StringUtils.isNotBlank(buildName)) {
            String name = buildName.replaceAll("\\?", "_").replaceAll("\\*", "%");
            where.append("build_name LIKE ?");
            whereParams.add("%" + name + "%");
        }
        if (StringUtils.isNotBlank(fromDate)) {
            if (where.length() > 0) {
                where.append(" AND ");
            }
            where.append("build_date >= ?");
            whereParams.add(Long.valueOf(fromDate));
        }
        if (StringUtils.isNotBlank(toDate)) {
            if (where.length() > 0) {
                where.append(" AND ");
            }
            where.append("build_date <= ?");
            whereParams.add(Long.valueOf(toDate));
        }
        return where.length() > 0 ? " WHERE " + where.toString() : "";
    }

    private String buildOrderByClause(String orderBy, String direction) throws SQLException {
        String sortingDirection = "asc".equalsIgnoreCase(direction) ? "ASC" : "DESC";
        StringBuilder orderClause = new StringBuilder();
        if (StringUtils.isNotBlank(orderBy)) {
            switch (orderBy) {
                case "name":    orderClause.append("build_name");
                                break;
                case "number":  orderClause.append("build_number");
                                break;
                case "date":    orderClause.append("build_date");
                                break;
                default:        throw new SQLException("Cannot order by: " + orderBy + ". Unknown column.");
            }
        } else {
            orderClause.append("build_date");
        }
        return " ORDER BY a." + orderClause.append(" ").append(sortingDirection).toString();
    }

    public Collection<BuildEntity> findBuildsForArtifactChecksum(ChecksumType type, String checksum) throws SQLException {
        Collection<BuildEntity> results = Lists.newArrayList();
        ResultSet rs = null;
        if (ChecksumType.sha256.equals(type)) {
            // Kicked out build artifacts sha2 from db because of performance
            log.debug("Build artifact sha256 search not supported.");
            return results;
        }
        try {
            rs = jdbcHelper.executeSelect("SELECT DISTINCT b.* FROM builds b, build_artifacts ba, build_modules bm" +
                    " WHERE b.build_id = bm.build_id" +
                    " AND bm.module_id = ba.module_id" +
                    " AND ba." + type.name() + " = ?" +
                    " AND ba.module_id = bm.module_id", checksum);
            while (rs.next()) {
                results.add(resultSetToBuild(rs));
            }
        } finally {
            DbUtils.close(rs);
        }
        for (BuildEntity buildEntity : results) {
            buildEntity.setProperties(findBuildProperties(buildEntity.getBuildId()));
            buildEntity.setPromotions(findBuildPromotions(buildEntity.getBuildId()));
        }
        return results;
    }

    public Collection<BuildEntity> findBuildsForDependencyChecksum(ChecksumType type, String checksum) throws SQLException {
        Collection<BuildEntity> results = Lists.newArrayList();
        ResultSet rs = null;
        if (ChecksumType.sha256.equals(type)) {
            // Kicked out build artifacts sha2 from db because of performance
            log.debug("Build dependencies sha256 search not supported.");
            return results;
        }
        try {
            rs = jdbcHelper.executeSelect("SELECT DISTINCT b.* FROM builds b, build_dependencies bd, build_modules bm" +
                    " WHERE b.build_id = bm.build_id" +
                    " AND bm.module_id = bd.module_id" +
                    " AND bd." + type.name() + " = ?" +
                    " AND bd.module_id = bm.module_id", checksum);
            while (rs.next()) {
                results.add(resultSetToBuild(rs));
            }
        } finally {
            DbUtils.close(rs);
        }
        for (BuildEntity buildEntity : results) {
            buildEntity.setProperties(findBuildProperties(buildEntity.getBuildId()));
            buildEntity.setPromotions(findBuildPromotions(buildEntity.getBuildId()));
        }
        return results;
    }

    private Set<BuildProperty> findBuildProperties(long buildId) throws SQLException {
        ResultSet rs = null;
        Set<BuildProperty> buildProperties = new HashSet<>();
        try {
            rs = jdbcHelper.executeSelect("SELECT * FROM build_props WHERE" +
                    " build_id = ?",
                    buildId);
            while (rs.next()) {
                buildProperties.add(resultSetToBuildProperty(rs));
            }
        } finally {
            DbUtils.close(rs);
        }
        return buildProperties;
    }

    private SortedSet<BuildPromotionStatus> findBuildPromotions(long buildId) throws SQLException {
        ResultSet rs = null;
        SortedSet<BuildPromotionStatus> buildPromotions = new TreeSet<>();
        try {
            rs = jdbcHelper.executeSelect("SELECT * FROM build_promotions WHERE" +
                    " build_id = ?",
                    buildId);
            while (rs.next()) {
                buildPromotions.add(resultSetToBuildPromotion(rs));
            }
        } finally {
            DbUtils.close(rs);
        }
        return buildPromotions;
    }

    private static BuildProperty resultSetToBuildProperty(ResultSet rs) throws SQLException {
        return new BuildProperty(rs.getLong(1), rs.getLong(2),
                rs.getString(3), rs.getString(4));
    }

    private static  BuildProps resultSetToBuildProps(ResultSet rs) throws SQLException {
        return new BuildProps(rs.getString(1), rs.getString(2), null);
    }

    private BuildPromotionStatus resultSetToBuildPromotion(ResultSet rs) throws SQLException {
        return new BuildPromotionStatus(rs.getLong(1), rs.getLong(2),
                rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6),
                rs.getString(7));
    }

    private BuildEntity resultSetToBuild(ResultSet rs) throws SQLException {
        return new BuildEntity(rs.getLong(1),
                rs.getString(2), rs.getString(3), rs.getLong(4),
                rs.getString(5), rs.getLong(6), rs.getString(7),
                zeroIfNull(rs.getLong(8)), rs.getString(9)
        );
    }

    private GeneralBuild resultSetToGeneralBuild(ResultSet rs, Long id) throws SQLException {
        return new GeneralBuild(id,
                rs.getString("build_name"), rs.getString("build_number"), rs.getLong("build_date"),
                rs.getString("ci_url"), rs.getLong("created"), rs.getString("created_by"),
                zeroIfNull(rs.getLong("modified")), rs.getString("modified_by")
        );
    }

    /**
     * Used to determine how many entries in the build_jsons table (for the migration job)
     */
    public int getNumberOfBuildJsons() throws SQLException {
        int num = 0;
        if (tableExists(jdbcHelper, dbProperties.getDbType(), BUILD_JSONS_TABLE)) {
            num = jdbcHelper.executeSelectCount("SELECT COUNT(*) FROM " + BUILD_JSONS_TABLE);
        }
        return num;
    }

    /**
     * Gets the next {@param limit} build ids ordered (for the migration job)
     */
    public List<Long> getBuildIdsStartingAt(long lastBuildId, int limit) throws SQLException {
        ResultSet rs = null;
        List<Long> buildIds = new ArrayList<>();
        String query = new ArtifactoryQueryWriter().select("build_id ")
                .from(BUILD_JSONS_TABLE + " ")
                .where("build_id > ? ")
                .orderBy("build_id ASC ")
                .limit((long) limit)
                .build();
        try {
            rs = jdbcHelper.executeSelect(query, lastBuildId);
            while (rs.next()) {
                buildIds.add(rs.getLong("build_id"));
            }
        } finally {
            DbUtils.close(rs);
        }
        return buildIds;
    }
}
