package org.artifactory.ui.rest.service.admin.security.permissions;

import org.artifactory.api.rest.common.model.continues.ContinueResult;
import org.artifactory.api.security.SearchStringPermissionFilter;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.service.security.permissions.RestSecurityRequestHandlerV2;
import org.artifactory.security.permissions.PermissionTargetModel;
import org.artifactory.ui.rest.model.admin.security.permissions.AllPermissionTargetsUIModel;
import org.artifactory.ui.rest.model.continuous.dtos.ContinuePermissionDto;
import org.artifactory.ui.rest.model.continuous.translators.SearchStringTranslator;
import org.jfrog.common.StreamSupportUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.artifactory.security.PermissionTargetNaming.NAMING_UI;

/**
 * Used in main Permissions page
 *
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetAllPermissionTargetsService implements RestService {

    @Autowired
    private RestSecurityRequestHandlerV2 securityRequestHandler;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        ContinuePermissionDto continuePermissionDto = new ContinuePermissionDto(request);
        SearchStringPermissionFilter searchStringPermissionFilter = SearchStringTranslator.toSearchStringFilter(continuePermissionDto);
        //These are all of the permission targets the current user has permission to manage
        ContinueResult<PermissionTargetModel> allPermissionTargets = securityRequestHandler.getPagingPermissionTargets(NAMING_UI,
                searchStringPermissionFilter);
        List<AllPermissionTargetsUIModel> modelList = StreamSupportUtils.stream(allPermissionTargets.getData())
                .map(AllPermissionTargetsUIModel::new)
                .collect(Collectors.toList());

        response.iModel(new ContinueResult<>( allPermissionTargets.getContinueState(), modelList));
    }

}
