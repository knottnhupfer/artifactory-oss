package org.artifactory.ui.rest.service.builds.buildsinfo.tabs.permissions;

import com.google.common.base.Strings;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.BuildAcl;
import org.artifactory.ui.rest.service.common.EffectivePermissionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static org.artifactory.build.BuildInfoUtils.formatBuildTime;

/**
 * Used in Effective Permission tab when the number of permission targets per entity (user/group) is larger than 5.
 * Then we don't show all the permissions, but only when user is requesting it specifically.
 *
 * @author Yuval Reches
 */
@Component
public class GetBuildEffectivePermissionsByEntityService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetBuildEffectivePermissionsByEntityService.class);

    private UserGroupService userGroupService;
    private AuthorizationService authService;
    private EffectivePermissionHelper effectivePermissionHelper;

    @Autowired
    public GetBuildEffectivePermissionsByEntityService(UserGroupService userGroupService,
            AuthorizationService authService) {
        this.userGroupService = userGroupService;
        this.authService = authService;
        this.effectivePermissionHelper = new EffectivePermissionHelper(userGroupService);
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String buildName = request.getQueryParamByKey("buildName");
        String buildNumber = request.getQueryParamByKey("buildNumber");
        String buildStarted = formatBuildTime(request.getQueryParamByKey("buildDate"));
        boolean canManageBuild = authService.canManageBuild(buildName, buildNumber, buildStarted);
        if (canManageBuild) {
            EntityInfo entityInfo = getEntityInfo(request, buildName);
            List<String> permissionsByEntity = getPermissionsByEntity(entityInfo, buildNumber, buildStarted);
            if (permissionsByEntity != null) {
                response.iModel(permissionsByEntity);
            }
        } else {
            response.responseCode(HttpServletResponse.SC_FORBIDDEN).buildResponse();
            log.error(String.format("Forbidden UI REST call from user: '%s'", authService.currentUsername()));
        }
    }

    /**
     * Returns all permissions names of a given entity
     */
    private List<String> getPermissionsByEntity(EntityInfo entityInfo, String buildNumber, String buildStarted) {
        List<BuildAcl> buildNameAcls = userGroupService.getBuildPathAcls(entityInfo.buildName, buildNumber,
                buildStarted);
        boolean isGroup = entityInfo.isGroup;
        String entityName = entityInfo.entityName;
        return effectivePermissionHelper.getPermissionByEntity(buildNameAcls, isGroup, entityName);
    }

    private EntityInfo getEntityInfo(ArtifactoryRestRequest request, String buildName) {
        EntityInfo entityInfo = new EntityInfo();
        entityInfo.buildName = buildName;
        entityInfo.entityName = request.getPathParamByKey("username");
        if (Strings.isNullOrEmpty(entityInfo.entityName)) {
            entityInfo.entityName = request.getPathParamByKey("groupname");
            entityInfo.isGroup = true;
        }
        return entityInfo;
    }

    private class EntityInfo {
        boolean isGroup = false;
        String entityName;
        String buildName;
    }
}
