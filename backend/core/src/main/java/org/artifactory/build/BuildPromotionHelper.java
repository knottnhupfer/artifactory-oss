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

package org.artifactory.build;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.build.BuildAddon;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.rest.artifact.PromotionResult;
import org.artifactory.common.StatusEntry;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.FileInfo;
import org.artifactory.md.Properties;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.security.AccessLogger;
import org.artifactory.util.DoesNotExistException;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.builder.PromotionStatusBuilder;
import org.jfrog.build.api.release.BuildArtifactsMapping;
import org.jfrog.build.api.release.Promotion;
import org.jfrog.client.util.PathUtils;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Noam Y. Tenne
 */
public class BuildPromotionHelper extends BaseBuildPromoter {
    private static final Logger log = LoggerFactory.getLogger(BuildPromotionHelper.class);

    public PromotionResult promoteBuild(BuildRun buildRun, Promotion promotion) {
        handleVirtualRepo(promotion);
        Build build;
        try {
            build = getBuild(buildRun);
        } catch (IllegalArgumentException e) {
            throw new DoesNotExistException("Unable to find build '" + buildRun.getName() + "' #" +
                    buildRun.getNumber() + ".", e);
        }

        PromotionResult promotionResult = new PromotionResult();

        String targetRepo = promotion.getTargetRepo();
        BasicStatusHolder statusHolder = new BasicStatusHolder();

        Set<FileInfo> itemsToMove = null;
        Set<RepoPath> allModifiedItems = Sets.newHashSet();
        Set<RepoPath> allSourceItems = Sets.newHashSet();

        if (StringUtils.isBlank(targetRepo)) {
            if (isNull(promotion.getMappings()) || promotion.getMappings().isEmpty()) {
                statusHolder.status("Skipping build item relocation: no target repository selected.", log);
            } else {
                multiPromoteBuild(build, promotion, statusHolder, allSourceItems, allModifiedItems);
                if (!promotion.isCopy() && !promotion.isDryRun() && !allSourceItems.isEmpty()) {
                    statusHolder.merge(delete(allSourceItems));
                }
            }
        } else {
            assertRepoExists(targetRepo);
            itemsToMove = collectItems(build, promotion, statusHolder);
            promoteBuildItems(promotion, statusHolder, BuildServiceUtils.toRepoPath(itemsToMove));
        }
        /*
        *  A list of properties to attach to the build's artifacts (regardless if "targetRepo" is used).
        * */
        Properties properties = (Properties) InfoFactoryHolder.get().createProperties();
        Map<String, Collection<String>> promotionProperties = promotion.getProperties();
        if ((promotionProperties != null) && !promotionProperties.isEmpty()) {
            for (Map.Entry<String, Collection<String>> entry : promotionProperties.entrySet()) {
                properties.putAll(entry.getKey(), entry.getValue());
            }
        }
        if (!properties.isEmpty()) {
            Set<RepoPath> modifiedItems;
            if (isMultiPromotion(promotion)) {
                modifiedItems = allModifiedItems;
            } else {
                modifiedItems = collectModifiedItems(promotion, build, targetRepo, statusHolder,
                        BuildServiceUtils.toRepoPath(itemsToMove));
            }
            if (!modifiedItems.isEmpty()) {
                tagBuildItemsWithProperties(modifiedItems, properties, promotion.isFailFast(), promotion.isDryRun(),
                        statusHolder);
            }
        }

        performPromotionIfNeeded(statusHolder, build, promotion);
        appendMessages(promotionResult, statusHolder);

        return promotionResult;
    }

    private void multiPromoteBuild(Build build, Promotion promotion, BasicStatusHolder statusHolder, Set<RepoPath> allSourceItems,
            Set<RepoPath> allModifiedItems) {

        BuildAddon buildAddon = ContextHelper.get().beanForType(AddonsManager.class).addonByType(BuildAddon.class);
        Set<FileInfo> allBuildArtifacts = collectItems(build, promotion, statusHolder);

        for (BuildArtifactsMapping mapping : promotion.getMappings()) {
            try {
                rewriteMappingIfNeeded(mapping);
                Set<FileInfo> matchedBuildArtifacts = buildAddon.filterBuildArtifactsByPattern(allBuildArtifacts, mapping);
                if (StringUtils.isBlank(mapping.getOutput())) {
                    allModifiedItems.addAll(BuildServiceUtils.toRepoPath(matchedBuildArtifacts));
                } else {
                    Map<FileInfo, String> mappingResult = buildAddon
                            .mapBuildArtifactsToOutputPaths(matchedBuildArtifacts, mapping);
                    promoteBuildItems(promotion, statusHolder, mappingResult, allSourceItems, allModifiedItems);
                }
            } catch (Exception e) {
                statusHolder.error("Error occurred while promoting: " + e.getMessage(), e, log);
                if (promotion.isFailFast()) {
                    return;
                }
            }
        }
    }

    private void rewriteMappingIfNeeded(BuildArtifactsMapping mapping) {
        if (StringUtils.isBlank(mapping.getInput()) || StringUtils.isBlank(mapping.getOutput())) {
            return;
        }
        Pattern mappingInputPattern = Pattern.compile(mapping.getInput());
        int inputPatternNum = mappingInputPattern.matcher("").groupCount();
        if (inputPatternNum <= 0) {
            mapping.setInput(PathUtils.trimTrailingSlashes(mapping.getInput()) + "/(.+)");
            mapping.setOutput(PathUtils.trimTrailingSlashes(mapping.getOutput()) + "/$1");
            return;
        }
        Pattern placeHolderPattern = Pattern.compile("\\$([1-9]\\d*)");
        Matcher placeHoldersMatcher = placeHolderPattern.matcher(mapping.getOutput());
        if (!placeHoldersMatcher.find()) {
            String path = RepoPathFactory.create(mapping.getInput()).getPath();
            for (int i = 1; i < inputPatternNum + 1; i++) {
                path = path.replaceFirst("\\(.+?\\)", "\\$" + String.valueOf(i));
            }
            mapping.setOutput(PathUtils.trimTrailingSlashes(mapping.getOutput()) + "/" + path);
        }
    }

    private void handleVirtualRepo(Promotion promotion) {
        if (nonNull(promotion)) {
            String promotionTargetRepo = promotion.getTargetRepo();
            String repoKey = ContextHelper.get().beanForType(InternalRepositoryService.class)
                    .getDefaultDeploymentRepoKey(promotionTargetRepo);
            promotion.setTargetRepo(repoKey);
        }
    }

    private void handleVirtualRepo(Map.Entry<RepoPath, RepoPath> entry) {
        String newRepoKey = ContextHelper.get().beanForType(InternalRepositoryService.class)
                .getDefaultDeploymentRepoKey(entry.getValue().getRepoKey());
        entry.setValue(RepoPathFactory.create(newRepoKey, entry.getValue().getPath()));
    }

    private Set<RepoPath> collectModifiedItems(Promotion promotion, Build build, String targetRepo,
            BasicStatusHolder statusHolder, Set<RepoPath> itemsToMove) {
        //Collect artifacts only from the target repository
        Set<RepoPath> modifiedItems = Sets.newHashSet();
        if (!StringUtils.isBlank(targetRepo)) {
            if (itemsToMove != null) {
                for (RepoPath item : itemsToMove) {
                    modifiedItems.add(new RepoPathImpl(targetRepo, item.getPath()));
                }
            }
        } else {
            //In case the target repository is not defined, collect the items form the source.
            Set<FileInfo> result = collectItems(build, promotion, statusHolder);
            modifiedItems = BuildServiceUtils.toRepoPath(result);
        }
        return modifiedItems;
    }

    private void performPromotionIfNeeded(BasicStatusHolder statusHolder, Build build, Promotion promotion) {
        String status = promotion.getStatus();

        if (promotion.isFailFast() && (statusHolder.hasErrors() || statusHolder.hasWarnings())) {
            statusHolder.status("Skipping promotion status update: item promotion was completed with errors " +
                    "and warnings.", log);
            return;
        }

        if (StringUtils.isBlank(status)) {
            statusHolder.status("Skipping promotion status update: no status received.", log);
            return;
        }

        PromotionStatusBuilder statusBuilder = new PromotionStatusBuilder(status).
                user(authorizationService.currentUsername()).repository(promotion.getTargetRepo()).
                comment(promotion.getComment()).ciUser(promotion.getCiUser());

        String timestamp = promotion.getTimestamp();

        if (StringUtils.isNotBlank(timestamp)) {
            try {
                ISODateTimeFormat.dateTime().parseMillis(timestamp);
            } catch (Exception e) {
                statusHolder.error("Skipping promotion status update: invalid\\unparsable timestamp " + timestamp +
                        ".", log);
                return;
            }
            statusBuilder.timestamp(timestamp);
        } else {
            statusBuilder.timestampDate(new Date());
        }

        if (promotion.isDryRun()) {
            return;
        }

        buildService.addPromotionStatus(build, statusBuilder.build());
        String withErrors = (statusHolder.hasErrors() || statusHolder.hasWarnings()) ? "with errors" : "successfully";
        log.info("Promotion completed {} for build name '{}' and number '{}' with status of '{}'", withErrors,
                build.getName(), build.getNumber(), status);
        AccessLogger.buildPromote(build.getName());
    }

    private boolean isMultiPromotion(Promotion promotion) {
        return promotion.getMappings() != null && !promotion.getMappings().isEmpty();
    }

    private void promoteBuildItems(Promotion promotion, BasicStatusHolder status, Map<FileInfo, String> mappingResult,
            Set<RepoPath> allSourceItems, Set<RepoPath> allModifiedItems) {
        boolean dryRun = promotion.isDryRun();
        boolean isFailFast = promotion.isFailFast();

        if (mappingResult != null && !mappingResult.isEmpty()) {
            Map<RepoPath, RepoPath> mappedBuildArtifacts = getResultMap(mappingResult);

            if (!dryRun && !mappedBuildArtifacts.isEmpty()) {
                for (Map.Entry<RepoPath, RepoPath> entry : mappedBuildArtifacts.entrySet()) {
                    handleVirtualRepo(entry);
                    assertRepoExists(entry.getValue().getRepoKey());

                    allSourceItems.add(entry.getKey());
                    allModifiedItems.add(entry.getValue());
                }
            }
            status.merge(copy(mappedBuildArtifacts, dryRun, isFailFast));
        }
    }

    private Map<RepoPath, RepoPath> getResultMap(Map<FileInfo, String> mappedBuildArtifacts) {
        Map<RepoPath, RepoPath> resultMap = Maps.newHashMap();
        for (Map.Entry<FileInfo, String> entry : mappedBuildArtifacts.entrySet()) {
            resultMap.put(entry.getKey().getRepoPath(), RepoPathFactory.create(entry.getValue()));
        }
        return resultMap;
    }

    private void promoteBuildItems(Promotion promotion, BasicStatusHolder status, Set<RepoPath> itemsToMove) {
        String targetRepo = promotion.getTargetRepo();
        boolean dryRun = promotion.isDryRun();
        boolean isFailFast = promotion.isFailFast();
        if (itemsToMove != null && !itemsToMove.isEmpty()) {
            if (promotion.isCopy()) {
                try {
                    status.merge(copy(itemsToMove, targetRepo, dryRun, isFailFast));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    status.error("Error occurred while copying: " + e.getMessage(), e, log);
                }
            } else {
                try {
                    status.merge(move(itemsToMove, targetRepo, dryRun, isFailFast));
                } catch (Exception e) {
                    status.error("Error occurred while moving: " + e.getMessage(), e, log);
                }
            }
        }
    }

    private void appendMessages(PromotionResult promotionResult, BasicStatusHolder statusHolder) {
        for (StatusEntry statusEntry : statusHolder.getEntries()) {
            promotionResult.messages.add(new PromotionResult.PromotionResultMessages(statusEntry));
        }
    }
}
