package org.artifactory.ui.rest.service.admin.security.permissions;

import com.google.common.collect.Sets;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.service.security.permissions.RestSecurityRequestHandlerV2;
import org.artifactory.security.permissions.PermissionTargetModel;
import org.artifactory.security.permissions.RepoPermissionTargetModel;
import org.jfrog.common.StreamSupportUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.artifactory.security.PermissionTargetNaming.NAMING_UI;

/**
 * Used in Permissions page
 *
 * @author Omri Ziv
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetPermissionTargetGroupsService implements RestService {

    @Autowired
    private RestSecurityRequestHandlerV2 securityRequestHandler;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String permissionTargetName = request.getPathParamByKey("name");
        PermissionTargetModel backendModel = securityRequestHandler.getPermissionTarget(permissionTargetName, NAMING_UI);
        if (backendModel == null) {
            response.responseCode(HttpServletResponse.SC_NOT_FOUND).error("Permission target '" + permissionTargetName + "' not found!");
            return;
        }
        TreeSet<String> groupNames = new TreeSet<>();
        groupNames.addAll(getGroupsFromPermissonTargetModel(backendModel.getRepo()));
        groupNames.addAll(getGroupsFromPermissonTargetModel(backendModel.getBuild()));
        groupNames.addAll(getGroupsFromPermissonTargetModel(backendModel.getReleaseBundle()));
        response.iModelList(groupNames);
    }

    private Set<String> getGroupsFromPermissonTargetModel(RepoPermissionTargetModel permissionTargetModel) {
        if (permissionTargetModel != null && permissionTargetModel.getActions() != null) {
            return StreamSupportUtils.mapEntriesStream(permissionTargetModel.getActions().getGroups())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
        }
        return Sets.newHashSet();
    }
}