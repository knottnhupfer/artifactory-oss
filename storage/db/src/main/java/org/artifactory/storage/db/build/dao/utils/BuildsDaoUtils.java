package org.artifactory.storage.db.build.dao.utils;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.rest.build.ContinueBuildFilter;
import org.artifactory.build.BuildId;
import org.artifactory.storage.db.build.entity.BuildIdEntity;
import org.artifactory.storage.db.build.service.BuildIdImpl;
import org.mapstruct.ap.internal.util.Strings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.artifactory.api.rest.build.ContinueBuildFilter.OrderBy.BUILD_NAME;
import static org.artifactory.api.rest.common.model.continues.util.Direction.ASC;

public class BuildsDaoUtils {

    private BuildsDaoUtils() {

    }

    public static String createWhereClauseFromContinueBuildFilter(ContinueBuildFilter continueBuildFilter,
            List<Object> sqlParametersList) {
        BuildId continueBuild = continueBuildFilter.getContinueBuildId();
        List<String> whereClauses = new ArrayList<>();
        if (continueBuild != null) {
            Object continueValue = getContinueValue(continueBuildFilter);
            char directionChar = continueBuildFilter.getDirection() == ASC ? '>' : '<';
            if (continueBuildFilter.getOrderBy() == BUILD_NAME) {
                whereClauses.add(String.format("d.build_name %s ?", directionChar));
                sqlParametersList.add(continueBuild.getName());
            }
            else {
                whereClauses.add(String.format("(d.%s %s ? OR (d.%s = ? AND d.build_name %s ?))",
                        continueBuildFilter.getOrderByStr(), directionChar, continueBuildFilter.getOrderByStr(), directionChar));
                sqlParametersList.add(continueValue);
                sqlParametersList.add(continueValue);
                sqlParametersList.add(((BuildIdImpl) continueBuild).getName());
            }
        }
        if (StringUtils.isNotBlank(continueBuildFilter.getSearchStr())) {
            whereClauses.add("(d.build_name like ?)");
            sqlParametersList.add("%" + continueBuildFilter.getSearchStr() + "%");
        }

        String separator = " AND ";
        return Strings.join(whereClauses, separator);
    }

    public static List<BuildIdEntity> populateBuildIdEntryList(ResultSet rs) throws SQLException {
        List<BuildIdEntity> buildIdEntities = new ArrayList<>();
        while (rs.next()) {
            BuildIdEntity buildId = new BuildIdEntity(rs.getLong("build_id"), rs.getString("build_name"), rs.getString("build_number"), rs.getLong("build_date"));
            buildIdEntities.add(buildId);
        }
        return buildIdEntities;
    }

    public static String createOrderByStr(ContinueBuildFilter continueBuildFilter) {
        String orderByStr;
        if (continueBuildFilter.getOrderBy() == BUILD_NAME) {
            orderByStr = String.format("build_name %s", continueBuildFilter.getDirection());
        } else {
            orderByStr = String.format("%s %s, build_name %s", continueBuildFilter.getOrderByStr(),
                    continueBuildFilter.getDirection(), continueBuildFilter.getDirection());
        }
        return orderByStr;
    }

    private static Object getContinueValue(ContinueBuildFilter continueBuildFilter) {

        BuildId buildId = continueBuildFilter.getContinueBuildId();
        switch (continueBuildFilter.getOrderBy()) {
            case BUILD_NAME:
                return buildId.getName();
            case BUILD_NUMBER:
                return buildId.getNumber();
            case BUILD_DATE:
            default:
                return buildId.getStartedDate().getTime();
        }
    }

}
