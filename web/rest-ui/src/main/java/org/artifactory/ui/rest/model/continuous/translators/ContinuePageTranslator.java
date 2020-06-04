package org.artifactory.ui.rest.model.continuous.translators;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.rest.common.model.continues.ContinueIndexPage;
import org.artifactory.api.rest.common.model.continues.ContinuePage;
import org.artifactory.ui.rest.model.continuous.dtos.ContinuePageDto;

@UtilityClass
class ContinuePageTranslator {

    static ContinuePage toContinuePage(ContinuePageDto continuePageDto) {
        ContinuePage continuePage = new ContinuePage();
        continuePage.setMustInclude(continuePageDto.getMustInclude());
        if (continuePageDto.getDirection() != null) {
            continuePage.setDirection(continuePageDto.getDirection());
        }
        if (continuePageDto.getLimit() != null) {
            continuePage.setLimit(continuePageDto.getLimit());
        }
        return continuePage;
    }

    static ContinueIndexPage toContinueIndex(ContinuePageDto continuePageDto) {
        ContinuePage continuePage = toContinuePage(continuePageDto);
        ContinueIndexPage continueIndex = new ContinueIndexPage(continuePage);
        if (StringUtils.isNotBlank(continuePageDto.getContinueState())) {
            continueIndex.setContinueIndex(Long.parseLong(continuePageDto.getContinueState()));
        }
        return continueIndex;
    }

}
