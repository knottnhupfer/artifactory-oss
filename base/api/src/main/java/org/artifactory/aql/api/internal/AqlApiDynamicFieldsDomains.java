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

package org.artifactory.aql.api.internal;

import com.google.common.collect.Lists;
import org.artifactory.aql.model.*;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gidi Shabat
 */
public class AqlApiDynamicFieldsDomains {
    public static class AqlApiItemDynamicFieldsDomains<T extends AqlBase> {

        private List<AqlDomainEnum> domains = Lists.newArrayList();

        public AqlApiItemDynamicFieldsDomains(List<AqlDomainEnum> domains) {
            this.domains.addAll(domains);
        }

        public AqlApiComparator<T> size() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.itemSize, domains);
        }

        public AqlApiComparator<T> updated() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.itemUpdated, domains);
        }

        public AqlApiComparator<T> itemId() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.itemId, domains);
        }

        public AqlApiComparator<T> repo() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.itemRepo, domains);
        }

        public AqlApiComparator<T> path() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.itemPath, domains);
        }

        public AqlApiComparator<T> name() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.itemName, domains);
        }

        public AqlApiComparator<T> type() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.itemType, domains);
        }

        public AqlApiComparator<T> created() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.itemCreated, domains);
        }

        public AqlApiComparator<T> createdBy() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.itemCreatedBy, domains);
        }

        public AqlApiComparator<T> modified() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.itemModified, domains);
        }

        public AqlApiComparator<T> modifiedBy() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.itemModifiedBy, domains);
        }

        public AqlApiComparator<T> sha1Actual() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.itemActualSha1, domains);
        }

        public AqlApiComparator<T> sha1Original() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.itemOriginalSha1, domains);
        }

        public AqlApiComparator<T> md5Actual() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.itemActualMd5, domains);
        }

        public AqlApiComparator<T> md5Orginal() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.itemOriginalMd5, domains);
        }

        public AqlApiComparator<T> depth() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.itemDepth, domains);
        }

        public AqlApiComparator<T> sha2() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.itemSha2, domains);
        }

        public AqlApiComparator<T> repoPathChecksum() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.repoPathChecksum, domains);
        }


        public AqlApiField<T> virtualRepos() {
            return new AqlApiField<>(AqlLogicalFieldEnum.itemVirtualRepos, domains);
        }

        public AqlApiArchiveDynamicFieldsDomains<T> archive() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.archives);
            return new AqlApiArchiveDynamicFieldsDomains<>(tempDomains);
        }

        public AqlApiItemPropertyDynamicFieldsDomains<T> property() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.properties);
            return new AqlApiItemPropertyDynamicFieldsDomains<>(tempDomains);
        }

        public AqlApiStatisticDynamicFieldsDomains<T> statistic() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.statistics);
            return new AqlApiStatisticDynamicFieldsDomains<>(tempDomains);
        }

        public AqlApiArtifactDynamicFieldsDomains<T> artifact() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.artifacts);
            return new AqlApiArtifactDynamicFieldsDomains<>(tempDomains);
        }

        public AqlApiDependencyDynamicFieldsDomains<T> dependency() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.dependencies);
            return new AqlApiDependencyDynamicFieldsDomains<>(tempDomains);
        }

        public AqlApiReleaseBundleDynamicFieldsDomains<T> bundledFile() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.releaseBundleFiles);
            return new AqlApiReleaseBundleDynamicFieldsDomains<>(tempDomains);
        }

    }

    public static class AqlApiArchiveDynamicFieldsDomains<T extends AqlBase> {
        private List<AqlDomainEnum> domains = Lists.newArrayList();

        public AqlApiArchiveDynamicFieldsDomains(List<AqlDomainEnum> domains) {
            this.domains.addAll(domains);
        }

        public AqlApiItemDynamicFieldsDomains<T> item() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.items);
            return new AqlApiItemDynamicFieldsDomains<>(tempDomains);
        }

        public AqlApiArchiveEntryDynamicFieldsDomains<T> entry() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.entries);
            return new AqlApiArchiveEntryDynamicFieldsDomains<>(tempDomains);
        }

    }

    public static class AqlApiArchiveEntryDynamicFieldsDomains<T extends AqlBase> {
        private List<AqlDomainEnum> domains = Lists.newArrayList();

        public AqlApiArchiveEntryDynamicFieldsDomains(List<AqlDomainEnum> domains) {
            this.domains.addAll(domains);
        }

        public AqlApiComparator<T> name() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.archiveEntryName, domains);
        }

        public AqlApiComparator<T> path() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.archiveEntryPath, domains);
        }

        public AqlApiArchiveDynamicFieldsDomains<T> archive() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.archives);
            return new AqlApiArchiveDynamicFieldsDomains<>(tempDomains);
        }

    }

    public static class AqlApiBuildDynamicFieldsDomains<T extends AqlBase> {

        private List<AqlDomainEnum> domains = Lists.newArrayList();

        public AqlApiBuildDynamicFieldsDomains(List<AqlDomainEnum> domains) {
            this.domains.addAll(domains);
        }

        public AqlApiComparator<T> name() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.buildName, domains);
        }

        public AqlApiComparator<T> number() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.buildNumber, domains);
        }

        public AqlApiModuleDynamicFieldsDomains<T> module() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.modules);
            return new AqlApiModuleDynamicFieldsDomains<>(tempDomains);
        }

        public AqlApiBuildPropertyDynamicFieldsDomains<T> property() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.buildProperties);
            return new AqlApiBuildPropertyDynamicFieldsDomains<>(tempDomains);
        }

        public AqlApiBuildPromotionDynamicFieldsDomains<T> promotion() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.buildPromotions);
            return new AqlApiBuildPromotionDynamicFieldsDomains<>(tempDomains);
        }

    }

    public static class AqlApiArtifactDynamicFieldsDomains<T extends AqlBase> {
        private ArrayList<AqlDomainEnum> domains = Lists.newArrayList();

        public AqlApiArtifactDynamicFieldsDomains(ArrayList<AqlDomainEnum> domains) {
            this.domains.addAll(domains);
        }

        public AqlApiComparator<T> name() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.buildArtifactName, domains);
        }

        public AqlApiComparator<T> type() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.buildArtifactType, domains);
        }

        public AqlApiComparator<T> sha1() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.buildArtifactSha1, domains);
        }

        // Kicked out build artifacts sha2 from db because of performance
        /*public AqlApiComparator<T> sha2() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.buildArtifactSha2, domains);
        }*/

        public AqlApiComparator<T> md5() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.buildArtifactMd5, domains);
        }

        public AqlApiModuleDynamicFieldsDomains<T> module() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.modules);
            return new AqlApiModuleDynamicFieldsDomains<>(tempDomains);
        }

        public AqlApiItemDynamicFieldsDomains<T> item() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.items);
            return new AqlApiItemDynamicFieldsDomains<>(tempDomains);
        }
    }

    public static class AqlApiDependencyDynamicFieldsDomains<T extends AqlBase> {

        private ArrayList<AqlDomainEnum> domains = Lists.newArrayList();

        public AqlApiDependencyDynamicFieldsDomains(ArrayList<AqlDomainEnum> domains) {
            this.domains.addAll(domains);
        }

        public AqlApiComparator<T> name() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.buildDependencyName, domains);
        }

        public AqlApiComparator<T> scope() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.buildDependencyScope, domains);
        }

        public AqlApiComparator<T> type() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.buildDependencyType, domains);
        }

        public AqlApiComparator<T> sha1() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.buildDependencySha1, domains);
        }

        // Kicked out build artifacts sha2 from db because of performance
        /*public AqlApiComparator<T> sha2() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.buildDependencySha2, domains);
        }*/

        public AqlApiComparator<T> md5() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.buildDependencyMd5, domains);
        }

        public AqlApiItemDynamicFieldsDomains<T> item() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.items);
            return new AqlApiItemDynamicFieldsDomains<>(tempDomains);
        }

        public AqlApiModuleDynamicFieldsDomains<T> module() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.modules);
            return new AqlApiModuleDynamicFieldsDomains<>(tempDomains);
        }
    }

    public static class AqlApiModuleDynamicFieldsDomains<T extends AqlBase> {
        private List<AqlDomainEnum> domains = Lists.newArrayList();

        public AqlApiModuleDynamicFieldsDomains(List<AqlDomainEnum> domains) {
            this.domains.addAll(domains);
        }

        public AqlApiComparator<T> name() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.moduleName, domains);
        }

        public AqlApiBuildDynamicFieldsDomains<T> build() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.builds);
            return new AqlApiBuildDynamicFieldsDomains<>(tempDomains);
        }

        public AqlApiArtifactDynamicFieldsDomains<T> artifact() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.artifacts);
            return new AqlApiArtifactDynamicFieldsDomains<>(tempDomains);
        }

        public AqlApiDependencyDynamicFieldsDomains<T> dependecy() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.dependencies);
            return new AqlApiDependencyDynamicFieldsDomains<>(tempDomains);
        }
    }

    public static class AqlApiItemPropertyDynamicFieldsDomains<T extends AqlBase> {
        private final List<AqlDomainEnum> domains = Lists.newArrayList();

        public AqlApiItemPropertyDynamicFieldsDomains(ArrayList<AqlDomainEnum> domains) {
            this.domains.addAll(domains);
        }

        public AqlApiComparator<T> propertyId() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.propertyId, domains);
        }

        public AqlApiComparator<T> propertyItemId() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.propertyItemId, domains);
        }

        public AqlBase.PropertyCriteriaClause<T> property(String key, AqlComparatorEnum comparator, String value) {
            return new AqlBase.PropertyCriteriaClause<>(key, comparator, value, domains);
        }

        public AqlApiComparator<T> key() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.propertyKey, domains);
        }

        public AqlApiComparator<T> value() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.propertyValue, domains);
        }

        public AqlApiItemDynamicFieldsDomains<T> item() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.items);
            return new AqlApiItemDynamicFieldsDomains<>(tempDomains);
        }
    }

    public static class AqlApiBuildPropertyDynamicFieldsDomains<T extends AqlBase> {
        private final List<AqlDomainEnum> domains = Lists.newArrayList();

        public AqlApiBuildPropertyDynamicFieldsDomains(List<AqlDomainEnum> domains) {
            this.domains.addAll(domains);
        }

        public AqlApiComparator<T> key() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.buildPropertyKey, domains);
        }

        public AqlApiComparator<T> value() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.buildPropertyValue, domains);
        }

        public AqlApiBuildDynamicFieldsDomains<T> build() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.builds);
            return new AqlApiBuildDynamicFieldsDomains<>(tempDomains);
        }
    }

    public static class AqlApiBuildPromotionDynamicFieldsDomains<T extends AqlBase> {
        private final List<AqlDomainEnum> domains = Lists.newArrayList();

        public AqlApiBuildPromotionDynamicFieldsDomains(List<AqlDomainEnum> domains) {
            this.domains.addAll(domains);
        }

        public AqlApiComparator<T> created() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.buildPromotionCreated, domains);
        }

        public AqlApiComparator<T> createdBy() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.buildPromotionCreatedBy, domains);
        }

        public AqlApiComparator<T> userName() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.buildPromotionUserName, domains);
        }

        public AqlApiComparator<T> comment() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.buildPromotionComment, domains);
        }

        public AqlApiComparator<T> repo() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.buildPromotionRepo, domains);
        }

        public AqlApiComparator<T> status() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.buildPromotionStatus, domains);
        }

        public AqlApiBuildDynamicFieldsDomains<T> build() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.builds);
            return new AqlApiBuildDynamicFieldsDomains<>(tempDomains);
        }
    }

    public static class AqlApiStatisticDynamicFieldsDomains<T extends AqlBase> {
        private final List<AqlDomainEnum> domains = Lists.newArrayList();

        public AqlApiStatisticDynamicFieldsDomains(ArrayList<AqlDomainEnum> domains) {
            this.domains.addAll(domains);
        }

        public AqlApiComparator<T> statId() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.statId, domains);
        }

        public AqlApiComparator<T> downloads() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.statDownloads, domains);
        }

        public AqlApiComparator<T> downloaded() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.statDownloaded, domains);
        }

        public AqlApiComparator<T> downloadedBy() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.statDownloadedBy, domains);
        }

        public AqlApiComparator<T> remoteDownloads() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.statRemoteDownloadedBy, domains);
        }

        public AqlApiComparator<T> remoteDownloaded() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.statRemoteDownloaded, domains);
        }

        public AqlApiComparator<T> remoteDownloadedBy() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.statRemoteDownloadedBy, domains);
        }

        public AqlApiComparator<T> remoteOrigin() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.statRemoteDownloadedBy, domains);
        }

        public AqlApiComparator<T> remotePath() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.statRemoteDownloadedBy, domains);
        }

        public AqlApiItemDynamicFieldsDomains<T> item() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.items);
            return new AqlApiItemDynamicFieldsDomains<>(tempDomains);
        }
    }

    public static class AqlApiReleaseBundleDynamicFieldsDomains<T extends AqlBase> {
        private final List<AqlDomainEnum> domains = Lists.newArrayList();

        public AqlApiReleaseBundleDynamicFieldsDomains(ArrayList<AqlDomainEnum> domains) {
            this.domains.addAll(domains);
        }

        public AqlApiComparator<T> id() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.releaseBundleId, domains);
        }

        public AqlApiComparator<T> name() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.releaseBundleName, domains);
        }

        public AqlApiComparator<T> version() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.releaseBundleVersion, domains);
        }

        public AqlApiComparator<T> status() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.releaseBundleStatus, domains);
        }

        public AqlApiComparator<T> created() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.releaseBundleCreated, domains);
        }

        public AqlApiComparator<T> signature() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.releaseBundleSignature, domains);
        }

        public AqlApiComparator<T> bundleType() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.releaseBundleType, domains);
        }

        public AqlApiReleaseBundleFileDynamicFieldsDomains<T> releaseArtifact() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.releaseBundleFiles);
            return new AqlApiReleaseBundleFileDynamicFieldsDomains<>(tempDomains);
        }
    }

    public static class AqlApiReleaseBundleFileDynamicFieldsDomains<T extends AqlBase> {
        private final List<AqlDomainEnum> domains = Lists.newArrayList();

        public AqlApiReleaseBundleFileDynamicFieldsDomains(ArrayList<AqlDomainEnum> domains) {
            this.domains.addAll(domains);
        }

        public AqlApiComparator<T> id() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.releaseBundleFileId, domains);
        }

        public AqlApiComparator<T> nodeId() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.releaseBundleFileNodeId, domains);
        }

        public AqlApiComparator<T> bundleId() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.releaseBundleFileBundleId, domains);
        }

        public AqlApiComparator<T> repoPath() {
            return new AqlApiComparator<>(AqlPhysicalFieldEnum.releaseBundleFileRepoPath, domains);
        }

        public AqlApiReleaseBundleDynamicFieldsDomains<T> release() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.releaseBundles);
            return new AqlApiReleaseBundleDynamicFieldsDomains<>(tempDomains);
        }

        public AqlApiItemDynamicFieldsDomains<T> item() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.items);
            return new AqlApiItemDynamicFieldsDomains<>(tempDomains);
        }
    }

    public static class AqlApiField<T extends AqlBase> {
        protected final List<AqlDomainEnum> domains = Lists.newArrayList();
        private final AqlFieldEnum fieldEnum;

        public AqlApiField(AqlFieldEnum fieldEnum, List<AqlDomainEnum> domains) {
            this.fieldEnum = fieldEnum;
            this.domains.addAll(domains);
        }

        public AqlFieldEnum getFieldEnum() {
            return fieldEnum;
        }

        public List<AqlDomainEnum> getDomains() {
            return domains;
        }
    }

    public static class AqlApiComparator<T extends AqlBase> extends AqlApiField<T> {
        private final AqlPhysicalFieldEnum fieldEnum;

        public AqlApiComparator(AqlPhysicalFieldEnum fieldEnum, List<AqlDomainEnum> domains) {
            super(fieldEnum, domains);
            this.fieldEnum = fieldEnum;
        }

        @Override
        public AqlPhysicalFieldEnum getFieldEnum() {
            return this.fieldEnum;
        }

        public AqlBase.CriteriaClause<T> matches(int value) {
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.matches, "" + value);
        }

        public AqlBase.CriteriaClause<T> matches(long value) {
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.matches, "" + value);
        }

        public AqlBase.CriteriaClause<T> matches(DateTime value) {
            String dateString = convertDateToString(value);
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.matches, dateString);
        }

        public AqlBase.CriteriaClause<T> matches(String value) {
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.matches, value);
        }

        public AqlBase.CriteriaClause<T> notMatches(int value) {
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.notMatches, "" + value);
        }

        public AqlBase.CriteriaClause<T> notMatches(long value) {
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.notMatches, "" + value);
        }

        public AqlBase.CriteriaClause<T> notMatches(DateTime value) {
            String dateString = convertDateToString(value);
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.notMatches, dateString);
        }

        public AqlBase.CriteriaClause<T> notMatches(String value) {
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.notMatches, value);
        }


        public AqlBase.CriteriaClause<T> equals(int value) {
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.equals, "" + value);
        }

        public AqlBase.CriteriaClause<T> equals(long value) {
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.equals, "" + value);
        }

        public AqlBase.CriteriaClause<T> equals(DateTime value) {
            String dateString = convertDateToString(value);
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.equals, dateString);
        }

        public AqlBase.CriteriaClause<T> equal(String value) {
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.equals, value);
        }


        public AqlBase.CriteriaClause<T> notEquals(int value) {
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.notEquals, "" + value);
        }

        public AqlBase.CriteriaClause<T> notEquals(long value) {
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.notEquals, "" + value);
        }

        public AqlBase.CriteriaClause<T> notEquals(DateTime value) {
            String dateString = convertDateToString(value);
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.notEquals, dateString);
        }

        public AqlBase.CriteriaClause<T> notEquals(String value) {
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.notEquals, value);
        }


        public AqlBase.CriteriaClause<T> greater(int value) {
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.greater, "" + value);
        }

        public AqlBase.CriteriaClause<T> greater(long value) {
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.greater, "" + value);
        }

        public AqlBase.CriteriaClause<T> greater(DateTime value) {
            String dateString = convertDateToString(value);
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.greater, dateString);
        }

        public AqlBase.CriteriaClause<T> greater(String value) {
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.greater, value);
        }


        public AqlBase.CriteriaClause<T> greaterEquals(int value) {
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.greaterEquals, "" + value);
        }

        public AqlBase.CriteriaClause<T> greaterEquals(long value) {
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.greaterEquals, "" + value);
        }

        public AqlBase.CriteriaClause<T> greaterEquals(DateTime value) {
            String dateString = convertDateToString(value);
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.greaterEquals, dateString);
        }

        public AqlBase.CriteriaClause<T> greaterEquals(String value) {
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.greaterEquals, value);
        }


        public AqlBase.CriteriaClause<T> less(int value) {
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.less, "" + value);
        }

        public AqlBase.CriteriaClause<T> less(long value) {
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.less, "" + value);
        }

        public AqlBase.CriteriaClause<T> less(DateTime value) {
            String dateString = convertDateToString(value);
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.less, dateString);
        }

        public AqlBase.CriteriaClause<T> less(String value) {
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.less, value);
        }

        public AqlBase.CriteriaClause<T> lessEquals(int value) {
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.lessEquals, "" + value);
        }

        public AqlBase.CriteriaClause<T> lessEquals(long value) {
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.lessEquals, "" + value);
        }

        public AqlBase.CriteriaClause<T> lessEquals(DateTime value) {
            String dateString = convertDateToString(value);
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.lessEquals, dateString);
        }

        public AqlBase.CriteriaClause<T> lessEquals(String value) {
            return new AqlBase.CriteriaClause<>(fieldEnum, domains, AqlComparatorEnum.lessEquals, value);
        }

        private static String convertDateToString(DateTime date) {
            if (date == null) {
                return null;
            }
            return ISODateTimeFormat.dateTime().print(date);
        }

    }
}
