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

package org.artifactory.api.bintray.distribution;

import org.artifactory.api.bintray.distribution.model.BintrayDistInfoModel;
import org.artifactory.api.bintray.distribution.reporting.DistributionReporter;
import org.artifactory.api.bintray.distribution.resolver.DistributionCoordinatesResolver;
import org.artifactory.api.bintray.distribution.resolver.DistributionRuleFilterType;
import org.artifactory.descriptor.repo.distribution.rule.DistributionRule;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * This bean services all common actions that are required both by the REST api and the ui for distribution repo actions.
 *
 * @author Dan Feldman
 */
public interface DistributionService {

    /**
     * Creates a list of {@link RepoPath} from the given full path list {@param distPaths}
     */
    List<RepoPath> inputPathsToRepoPaths(List<String> distPaths, DistributionReporter status);

    /**
     * @return a mapping of {@param paths} and the properties set on them, layout tokens according to {@param } and
     * the product name token if {@param targetPath} has any returns an empty (not null) {@link Properties} object for
     * paths that had no info
     */
    Map<RepoPath, Properties> getPathInformation(List<RepoPath> paths, @Nullable String productName, DistributionReporter status);

    /**
     * Used by the UI to populate capture groups from {@param rule} into {@param resolver}
     */
    boolean addCaptureGroupsToRuleResolverIfMatches(DistributionRule rule, DistributionRuleFilterType filterType,
            String filterRegex, String textToMatch, DistributionCoordinatesResolver resolver, DistributionReporter status);

    /**
     * Used by the UI to display Bintray information on each tree node item in a distribution repo
     */
    BintrayDistInfoModel buildInfoModel(RepoPath repoPath) throws IOException;
}
