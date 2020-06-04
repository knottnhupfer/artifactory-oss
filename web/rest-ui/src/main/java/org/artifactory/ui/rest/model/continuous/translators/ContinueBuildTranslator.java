package org.artifactory.ui.rest.model.continuous.translators;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.jackson.JacksonFactory;
import org.artifactory.api.rest.build.ContinueBuildFilter;
import org.artifactory.api.rest.common.model.continues.ContinuePage;
import org.artifactory.api.rest.common.model.continues.util.Direction;
import org.artifactory.build.BuildId;
import org.artifactory.storage.db.build.service.BuildIdImpl;
import org.artifactory.ui.rest.model.builds.GeneralBuildInfo;
import org.artifactory.ui.rest.model.continuous.dtos.ContinueBuildDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.jfrog.common.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Base64;

import static org.apache.commons.lang.StringUtils.isNotBlank;

@UtilityClass
public class ContinueBuildTranslator {

    private static final Logger log = LoggerFactory.getLogger(ContinueBuildTranslator.class);

    public static ContinueBuildFilter toBuildFilter(ContinueBuildDto continueBuildDto) {
        ContinuePage continuePage = ContinuePageTranslator.toContinuePage(continueBuildDto);
        ContinueBuildFilter continueBuildFilter = new ContinueBuildFilter(continuePage);
        continueBuildFilter.setSearchStr(continueBuildDto.getSearchStr());
        if (StringUtils.isNotBlank(continueBuildDto.getOrderBy())) {
            continueBuildFilter.setOrderBy(ContinueBuildFilter.OrderBy.valueOf(continueBuildDto.getOrderBy().toUpperCase()));
        }
        if (continueBuildDto.getDirection() == null && continueBuildFilter.getOrderBy() == ContinueBuildFilter.OrderBy.BUILD_DATE) {
            continueBuildFilter.setDirection(Direction.DESC);
        }

        BuildId continueBuildId = base64ToBuildId(continueBuildDto.getContinueState());
        if (continueBuildId != null) {
            continueBuildFilter.setContinueBuildId(continueBuildId);
        }

        return continueBuildFilter;
    }



    static GeneralBuildInfo base64ToGeneralBuildInfo(String b64) {
        return isNotBlank(b64) ?
                JsonUtils.getInstance().readValue(Base64.getDecoder().decode(b64), GeneralBuildInfo.class) :
                null;
    }

    static BuildId base64ToBuildId(String b64) {
        GeneralBuildInfo generalBuildInfo = base64ToGeneralBuildInfo(b64);
        return generalBuildInfo != null ?
                new BuildIdImpl(-1, generalBuildInfo.getBuildName(),
                        generalBuildInfo.getBuildNumber(), generalBuildInfo.getTime()) : null;
    }

    public static String buildIdToBase64(GeneralBuildInfo buildId) {

        if (buildId == null) {
            return null;
        }
        try {
            ObjectMapper objectMapper = JacksonFactory.createObjectMapper();
            String json = objectMapper.writeValueAsString(buildId);
            return Base64.getEncoder().encodeToString(json.getBytes());
        } catch (IOException e) {
            log.error("Failed to write Value for build id '{}'", buildId);
            return null;
        }

    }

}
