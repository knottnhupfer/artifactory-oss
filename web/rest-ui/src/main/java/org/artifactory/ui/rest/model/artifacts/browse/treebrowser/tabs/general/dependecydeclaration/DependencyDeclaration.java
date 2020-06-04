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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.dependecydeclaration;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.maven.MavenArtifactInfo;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.module.ModuleInfoBuilder;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.util.RepoLayoutUtils;

import java.util.EnumSet;

/**
 * @author Chen Keinan
 */
public class DependencyDeclaration extends BaseModel {

    private static final EnumSet<RepoType> REPO_TYPES_SUPPORTING_MAVEN =
            EnumSet.of(RepoType.Maven, RepoType.Ivy, RepoType.Gradle, RepoType.SBT, RepoType.Generic);
    private String[] types;
    private String dependencyData;

    public String[] getTypes() {
        return types;
    }

    public void setTypes(String[] types) {
        this.types = types;
    }

    public String getDependencyData() {
        return dependencyData;
    }

    public void setDependencyData(String dependencyData) {
        this.dependencyData = dependencyData;
    }

    public void updateDependencyDeclaration(String buildTool, RepoPath repoPath, LocalRepoDescriptor localRepoDescriptor) {
        RepoLayout repoLayout = localRepoDescriptor.getRepoLayout();
        ModuleInfo moduleInfo = getModuleInfo(repoPath, repoLayout);
        if (moduleInfo != null && moduleInfo.isValid() && isMavenSupported(localRepoDescriptor)) {
            if (StringUtils.isNotBlank(buildTool)) {
                updateDependencyDeclarationData(buildTool , moduleInfo);
            }
            String[] buildTypes = {"Maven", "Ivy", "Gradle", "Sbt"};
            types = buildTypes;
        }
    }

    private boolean isMavenSupported(LocalRepoDescriptor localRepoDescriptor) {
        return REPO_TYPES_SUPPORTING_MAVEN.contains(localRepoDescriptor.getType());
    }

    /**
     * get declaration data by type and update declaration dependency model
     *
     * @param moduleInfo             - artifact module data
     * @return dependency declaration instance
     */
    private void updateDependencyDeclarationData(String buildTool, ModuleInfo moduleInfo) {
        String declaration;
        DependencyDeclaration dependencyDeclaration = this;
        switch (buildTool) {
            case "maven":
                declaration = this.getMavenDependencyDeclaration(moduleInfo);
                break;
            case "gradle":
                declaration = this.getGradleDependencyDeclaration(moduleInfo);
                break;
            case "ivy":
                declaration = this.getIvyDependencyDeclaration(moduleInfo);
                break;
            case "sbt":
                declaration = this.getSbtDependency(moduleInfo);
                break;
            default:
                declaration = this.getMavenDependencyDeclaration(moduleInfo);
                break;
        }
        dependencyDeclaration.setDependencyData(declaration);
    }

    private ModuleInfo getModuleInfo(RepoPath repoPath, RepoLayout repoLayout) {
        ModuleInfo moduleInfo = null;
        if (RepoLayoutUtils.isDefaultM2(repoLayout)) {
            MavenArtifactInfo mavenArtifactInfo = MavenArtifactInfo.fromRepoPath(repoPath);
            if (mavenArtifactInfo.isValid()) {
                moduleInfo = new ModuleInfoBuilder()
                        .organization(mavenArtifactInfo.getGroupId())
                        .module(mavenArtifactInfo.getArtifactId())
                        .baseRevision(mavenArtifactInfo.getVersion())
                        .classifier(mavenArtifactInfo.getClassifier())
                        .ext(mavenArtifactInfo.getType())
                        .build();
            }
        }
        if (moduleInfo == null) {
            moduleInfo = ContextHelper.get().getRepositoryService().getItemModuleInfo(repoPath);
        }
        return moduleInfo;
    }

    /**
     * get Gradle Dependency Declaration
     * @param moduleInfo  - item module info
     * @return Dependency Declaration as String
     */
    public String getGradleDependencyDeclaration(ModuleInfo moduleInfo) {
        StringBuilder sb = new StringBuilder("compile(group: '").append(moduleInfo.getOrganization()).
                append("', name: '").append(moduleInfo.getModule()).append("', version: '").
                append(moduleInfo.getBaseRevision());

        String artifactRevisionIntegration = moduleInfo.getFileIntegrationRevision();
        if (StringUtils.isNotBlank(artifactRevisionIntegration)) {
            sb.append("-").append(artifactRevisionIntegration);
        }
        sb.append("'");

        String classifier = moduleInfo.getClassifier();
        if (StringUtils.isNotBlank(classifier)) {
            sb.append(", classifier: '").append(classifier).append("'");
        }

        String ext = moduleInfo.getExt();
        if (StringUtils.isNotBlank(ext) && !"jar".equalsIgnoreCase(ext)) {
            sb.append(", ext: '").append(ext).append("'");
        }
        return sb.append(")").toString();
    }

    /**
     * get sbt Dependency Declaration
     *
     * @param moduleInfo - item module info
     * @return Dependency Declaration as String
     */
    public String getSbtDependency(ModuleInfo moduleInfo) {
        StringBuilder sb = new StringBuilder("libraryDependencies += ").
                append("\"").append(moduleInfo.getOrganization()).append("\"").
                append(" % ").
                append("\"").append(moduleInfo.getModule()).append("\"").
                append(" % ").
                append("\"").append(moduleInfo.getBaseRevision()).append("\"");
        return sb.toString();
    }

    /**
     * get Ivy Dependency Declaration
     * @param moduleInfo  - item module info
     * @return Dependency Declaration as String
     */
    public String getIvyDependencyDeclaration(ModuleInfo moduleInfo) {
        String module = moduleInfo.getModule();

        StringBuilder sb = new StringBuilder("<dependency org=\"").append(moduleInfo.getOrganization()).append("\" ")
                .append("name=\"").append(module).append("\" ").append("rev=\"").append(moduleInfo.getBaseRevision());

        String artifactRevisionIntegration = moduleInfo.getFileIntegrationRevision();
        if (StringUtils.isNotBlank(artifactRevisionIntegration)) {
            sb.append("-").append(artifactRevisionIntegration);
        }
        sb.append("\"");

        String classifier = moduleInfo.getClassifier();
        String type = moduleInfo.getType();

        boolean validClassifier = StringUtils.isNotBlank(classifier);
        boolean validType = StringUtils.isNotBlank(type);

        if (validClassifier || !"jar".equals(type)) {
            sb.append(">\n")
                    .append("    <artifact name=\"").append(module).append("\"");

            if (validType && (validClassifier || !"jar".equals(type))) {
                sb.append(" type=\"").append(type).append("\"");
            }

            if (validClassifier) {
                sb.append(" m:classifier=\"").append(classifier).append("\"");
            }

            sb.append(" ext=\"").append(moduleInfo.getExt()).append("\"/>\n")
                    .append("</dependency>");
        } else {
            sb.append("/>");
        }
        return sb.toString();
    }


    /**
     * get Maven Dependency Declaration
     * @param moduleInfo  - item module info
     * @return Dependency Declaration as String
     */
    public String getMavenDependencyDeclaration(ModuleInfo moduleInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("<dependency>\n");
        sb.append("    <groupId>").append(moduleInfo.getOrganization()).append("</groupId>\n");
        sb.append("    <artifactId>").append(moduleInfo.getModule()).append("</artifactId>\n");
        sb.append("    <version>").append(moduleInfo.getBaseRevision());

        String artifactRevisionIntegration = moduleInfo.getFileIntegrationRevision();
        if (StringUtils.isNotBlank(artifactRevisionIntegration)) {
            sb.append("-").append(artifactRevisionIntegration);
        }
        sb.append("</version>\n");

        String classifier = moduleInfo.getClassifier();
        if (StringUtils.isNotBlank(classifier)) {
            sb.append("    <classifier>").append(classifier).append("</classifier>\n");
        }

        String ext = moduleInfo.getExt();
        if (StringUtils.isNotBlank(ext) && !"jar".equalsIgnoreCase(ext)) {
            sb.append("    <type>").append(moduleInfo.getExt()).append("</type>\n");
        }

        return sb.append("</dependency>").toString();
    }
}
