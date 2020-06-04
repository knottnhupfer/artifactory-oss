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

package org.artifactory.version;

import org.artifactory.version.converter.NamespaceConverter;
import org.artifactory.version.converter.SnapshotUniqueVersionConverter;
import org.artifactory.version.converter.XmlConverter;
import org.artifactory.version.converter.v100.BackupToElementConverter;
import org.artifactory.version.converter.v100.RepositoriesKeysConverter;
import org.artifactory.version.converter.v110.SnapshotNonUniqueValueConverter;
import org.artifactory.version.converter.v120.AnonAccessNameConverter;
import org.artifactory.version.converter.v130.AnnonAccessUnderSecurityConverter;
import org.artifactory.version.converter.v130.BackupListConverter;
import org.artifactory.version.converter.v130.LdapSettings130Converter;
import org.artifactory.version.converter.v131.LdapAuthenticationPatternsConverter;
import org.artifactory.version.converter.v132.BackupKeyConverter;
import org.artifactory.version.converter.v132.LdapListConverter;
import org.artifactory.version.converter.v134.BackupExcludedVirtualRepoConverter;
import org.artifactory.version.converter.v135.ProxyNTHostConverter;
import org.artifactory.version.converter.v136.IndexerCronRemoverConverter;
import org.artifactory.version.converter.v136.RepositoryTypeConverter;
import org.artifactory.version.converter.v141.ProxyDefaultConverter;
import org.artifactory.version.converter.v1410.GcSystemPropertyConverter;
import org.artifactory.version.converter.v1412.IndexerCronExpPropertyConverter;
import org.artifactory.version.converter.v1414.ArchiveBrowsingConverter;
import org.artifactory.version.converter.v1414.AssumedOfflineConverter;
import org.artifactory.version.converter.v1414.CleanupConfigConverter;
import org.artifactory.version.converter.v142.RepoIncludeExcludePatternsConverter;
import org.artifactory.version.converter.v143.RemoteChecksumPolicyConverter;
import org.artifactory.version.converter.v144.MultiLdapXmlConverter;
import org.artifactory.version.converter.v144.ServerIdXmlConverter;
import org.artifactory.version.converter.v147.DefaultRepoLayoutConverter;
import org.artifactory.version.converter.v147.JfrogRemoteRepoUrlConverter;
import org.artifactory.version.converter.v147.UnusedArtifactCleanupSwitchConverter;
import org.artifactory.version.converter.v149.ReplicationElementNameConverter;
import org.artifactory.version.converter.v152.BlackDuckProxyConverter;
import org.artifactory.version.converter.v153.VirtualCacheCleanupConverter;
import org.artifactory.version.converter.v160.AddonsDefaultLayoutConverter;
import org.artifactory.version.converter.v160.MavenIndexerConverter;
import org.artifactory.version.converter.v160.SingleRepoTypeConverter;
import org.artifactory.version.converter.v160.SuppressConsitencyConverter;
import org.artifactory.version.converter.v162.FolderDownloadConfigConverter;
import org.artifactory.version.converter.v166.SourceDeletedDetectionConverter;
import org.artifactory.version.converter.v167.TrashcanConfigConverter;
import org.artifactory.version.converter.v167.UserLockConfigConverter;
import org.artifactory.version.converter.v168.PasswordPolicyConverter;
import org.artifactory.version.converter.v169.PasswordMaxAgeConverter;
import org.artifactory.version.converter.v171.SimpleLayoutConverter;
import org.artifactory.version.converter.v172.BlockMismatchingMimeTypesConverter;
import org.artifactory.version.converter.v175.DockerForceAuthRemovalConverter;
import org.artifactory.version.converter.v177.LdapPoisoningProtectionConverter;
import org.artifactory.version.converter.v178.SigningKeysConverter;
import org.artifactory.version.converter.v180.ExternalProvidersRemovalConverter;
import org.artifactory.version.converter.v180.XrayRepoConfigConverter;
import org.artifactory.version.converter.v181.ComposerDefaultLayoutConverter;
import org.artifactory.version.converter.v182.ConanDefaultLayoutConverter;
import org.artifactory.version.converter.v201.PuppetDefaultLayoutConverter;
import org.artifactory.version.converter.v204.AccessTokenSettingsRenameToAccessClientSettingsConverter;
import org.artifactory.version.converter.v204.EventBasedRemoteReplicationConverter;
import org.artifactory.version.converter.v205.YumEnableFilelistsIndexingForExistingLocalReposConverter;
import org.artifactory.version.converter.v207.FolderDownloadForAnonymousConfigConverter;
import org.artifactory.version.converter.v207.RemoveAccessAdminCredentialsConverter;
import org.artifactory.version.converter.v208.AddReplicationKey;
import org.artifactory.version.converter.v211.XrayMinBlockedSeverityAndBlockUnscannedConverter;
import org.artifactory.version.converter.v212.PyPIRegistryUrlConverter;
import org.artifactory.version.converter.v213.AllowCrowdUsersToAccessProfilePageConverter;
import org.artifactory.version.converter.v213.GoDefaultLayoutConverter;
import org.artifactory.version.converter.v215.ConanDefaultDistributionRuleConverter;
import org.artifactory.version.converter.v215.ConanFixDefaultLayoutConverter;
import org.artifactory.version.converter.v218.AnonAccessToBuildsConverter;
import org.artifactory.version.converter.v218.BackupSettingConvert;
import org.artifactory.version.converter.v218.BuildDefaultLayoutConverter;
import org.artifactory.version.converter.v219.ConanV2DefaultLayoutConverter;
import org.artifactory.version.converter.v220.DownloadRedirectConverter;
import org.artifactory.version.converter.v221.PyPIRegistrySuffixConverter;
import org.artifactory.version.converter.v224.EnablePushingSchema1DockerConverter;
import org.artifactory.version.v214.NuGetV3Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * NOTE! each version declares the converters that should run when moving forward *from* it, meaning for example that
 * v180 starts at 4.12.1 (denoted v4121) up to 4.13.2 and the converter that needs to run for it
 * is XrayRepoConfigConverter (which is declared in v171) --> when moving forward from it to v181
 * ComposerDefaultLayoutConverter will run.
 *
 * @author freds
 * @author Yossi Shaul
 */
public enum ArtifactoryConfigVersion implements VersionWrapper {
    v100("http://artifactory.jfrog.org/xsd/1.0.0",
            "http://www.jfrog.org/xsd/artifactory-v1_0_0.xsd",
            ArtifactoryVersionProvider.v122rc0.get(),
            new SnapshotUniqueVersionConverter(),
            new BackupToElementConverter(),
            new RepositoriesKeysConverter()),
    v110("http://artifactory.jfrog.org/xsd/1.1.0",
            "http://www.jfrog.org/xsd/artifactory-v1_1_0.xsd",
            ArtifactoryVersionProvider.v125.get(),
            new SnapshotNonUniqueValueConverter()),
    v120("http://artifactory.jfrog.org/xsd/1.2.0",
            "http://www.jfrog.org/xsd/artifactory-v1_2_0.xsd",
            ArtifactoryVersionProvider.v125u1.get(),
            new AnonAccessNameConverter()),
    v130("http://artifactory.jfrog.org/xsd/1.3.0",
            "http://www.jfrog.org/xsd/artifactory-v1_3_0.xsd",
            ArtifactoryVersionProvider.v130beta1.get(),
            new BackupListConverter(), new AnnonAccessUnderSecurityConverter(),
            new LdapSettings130Converter()),
    v131("http://artifactory.jfrog.org/xsd/1.3.1",
            "http://www.jfrog.org/xsd/artifactory-v1_3_1.xsd",
            ArtifactoryVersionProvider.v130beta3.get(),
            new LdapAuthenticationPatternsConverter()),
    v132("http://artifactory.jfrog.org/xsd/1.3.2",
            "http://www.jfrog.org/xsd/artifactory-v1_3_2.xsd",
            ArtifactoryVersionProvider.v130beta4.get(),
            new BackupKeyConverter(), new LdapListConverter()),
    v133("http://artifactory.jfrog.org/xsd/1.3.3",
            "http://www.jfrog.org/xsd/artifactory-v1_3_3.xsd",
            ArtifactoryVersionProvider.v130beta5.get()),
    v134("http://artifactory.jfrog.org/xsd/1.3.4",
            "http://www.jfrog.org/xsd/artifactory-v1_3_4.xsd",
            ArtifactoryVersionProvider.v130rc1.get(),
            new BackupExcludedVirtualRepoConverter()),
    v135("http://artifactory.jfrog.org/xsd/1.3.5",
            "http://www.jfrog.org/xsd/artifactory-v1_3_5.xsd",
            ArtifactoryVersionProvider.v130rc2.get(),
            new ProxyNTHostConverter()),
    v136("http://artifactory.jfrog.org/xsd/1.3.6",
            "http://www.jfrog.org/xsd/artifactory-v1_3_6.xsd",
            ArtifactoryVersionProvider.v206.get(),
            new IndexerCronRemoverConverter(), new RepositoryTypeConverter()),
    v140("http://artifactory.jfrog.org/xsd/1.4.0",
            "http://www.jfrog.org/xsd/artifactory-v1_4_0.xsd",
            ArtifactoryVersionProvider.v210.get(),
            new ProxyDefaultConverter()),
    v141("http://artifactory.jfrog.org/xsd/1.4.1",
            "http://www.jfrog.org/xsd/artifactory-v1_4_1.xsd",
            ArtifactoryVersionProvider.v211.get(),
            new RepoIncludeExcludePatternsConverter()),
    v142("http://artifactory.jfrog.org/xsd/1.4.2",
            "http://www.jfrog.org/xsd/artifactory-v1_4_2.xsd",
            ArtifactoryVersionProvider.v213.get(),
            new RemoteChecksumPolicyConverter()),
    v143("http://artifactory.jfrog.org/xsd/1.4.3",
            "http://www.jfrog.org/xsd/artifactory-v1_4_3.xsd",
            ArtifactoryVersionProvider.v222.get(),
            new MultiLdapXmlConverter(), new ServerIdXmlConverter()),
    v144("http://artifactory.jfrog.org/xsd/1.4.4",
            "http://www.jfrog.org/xsd/artifactory-v1_4_4.xsd",
            ArtifactoryVersionProvider.v224.get()),
    v145("http://artifactory.jfrog.org/xsd/1.4.5",
            "http://www.jfrog.org/xsd/artifactory-v1_4_5.xsd",
            ArtifactoryVersionProvider.v230.get()),
    v146("http://artifactory.jfrog.org/xsd/1.4.6",
            "http://www.jfrog.org/xsd/artifactory-v1_4_6.xsd",
            ArtifactoryVersionProvider.v231.get(),
            new JfrogRemoteRepoUrlConverter(), new DefaultRepoLayoutConverter(),
            new UnusedArtifactCleanupSwitchConverter()),
    v147("http://artifactory.jfrog.org/xsd/1.4.7",
            "http://www.jfrog.org/xsd/artifactory-v1_4_7.xsd",
            ArtifactoryVersionProvider.v232.get()),
    v148("http://artifactory.jfrog.org/xsd/1.4.8",
            "http://www.jfrog.org/xsd/artifactory-v1_4_8.xsd",
            ArtifactoryVersionProvider.v233.get(),
            new ReplicationElementNameConverter()),
    v149("http://artifactory.jfrog.org/xsd/1.4.9",
            "http://www.jfrog.org/xsd/artifactory-v1_4_9.xsd",
            ArtifactoryVersionProvider.v234.get(),
             new GcSystemPropertyConverter()),
    v1410("http://artifactory.jfrog.org/xsd/1.4.10",
            "http://www.jfrog.org/xsd/artifactory-v1_4_10.xsd",
            ArtifactoryVersionProvider.v240.get()),
    v1411("http://artifactory.jfrog.org/xsd/1.4.11",
            "http://www.jfrog.org/xsd/artifactory-v1_4_11.xsd",
            ArtifactoryVersionProvider.v250.get(),
            new IndexerCronExpPropertyConverter()),
    v1412("http://artifactory.jfrog.org/xsd/1.4.12",
            "http://www.jfrog.org/xsd/artifactory-v1_4_12.xsd",
            ArtifactoryVersionProvider.v251.get()),
    v1413("http://artifactory.jfrog.org/xsd/1.4.13",
            "http://www.jfrog.org/xsd/artifactory-v1_4_13.xsd",
            ArtifactoryVersionProvider.v252.get(),
            new CleanupConfigConverter(), new AssumedOfflineConverter(),
            new ArchiveBrowsingConverter()),
    v1414("http://artifactory.jfrog.org/xsd/1.4.14",
            "http://www.jfrog.org/xsd/artifactory-v1_4_14.xsd",
            ArtifactoryVersionProvider.v260.get()),
    v1415("http://artifactory.jfrog.org/xsd/1.4.15",
            "http://www.jfrog.org/xsd/artifactory-v1_4_15.xsd",
            ArtifactoryVersionProvider.v262.get()),
    v1416("http://artifactory.jfrog.org/xsd/1.4.16",
            "http://www.jfrog.org/xsd/artifactory-v1_4_16.xsd",
            ArtifactoryVersionProvider.v264.get()),
    v1417("http://artifactory.jfrog.org/xsd/1.4.17",
            "http://www.jfrog.org/xsd/artifactory-v1_4_17.xsd",
            ArtifactoryVersionProvider.v265.get()),
    v1418("http://artifactory.jfrog.org/xsd/1.4.18",
            "http://www.jfrog.org/xsd/artifactory-v1_4_18.xsd",
            ArtifactoryVersionProvider.v266.get()),
    v150("http://artifactory.jfrog.org/xsd/1.5.0",
            "http://www.jfrog.org/xsd/artifactory-v1_5_0.xsd",
            ArtifactoryVersionProvider.v300.get()),
    v151("http://artifactory.jfrog.org/xsd/1.5.1",
            "http://www.jfrog.org/xsd/artifactory-v1_5_1.xsd",
            ArtifactoryVersionProvider.v3021.get()),
    v152("http://artifactory.jfrog.org/xsd/1.5.2",
            "http://www.jfrog.org/xsd/artifactory-v1_5_2.xsd",
            ArtifactoryVersionProvider.v304.get()
    , new BlackDuckProxyConverter()),
    v153("http://artifactory.jfrog.org/xsd/1.5.3",
            "http://www.jfrog.org/xsd/artifactory-v1_5_3.xsd",
            ArtifactoryVersionProvider.v310.get(),
            new VirtualCacheCleanupConverter()),
    v154("http://artifactory.jfrog.org/xsd/1.5.4",
            "http://www.jfrog.org/xsd/artifactory-v1_5_4.xsd",
            ArtifactoryVersionProvider.v320.get()),
    v155("http://artifactory.jfrog.org/xsd/1.5.5",
            "http://www.jfrog.org/xsd/artifactory-v1_5_5.xsd",
            ArtifactoryVersionProvider.v330.get()),
    v156("http://artifactory.jfrog.org/xsd/1.5.6",
            "http://www.jfrog.org/xsd/artifactory-v1_5_6.xsd",
            ArtifactoryVersionProvider.v340.get()),
    v157("http://artifactory.jfrog.org/xsd/1.5.7",
            "http://www.jfrog.org/xsd/artifactory-v1_5_7.xsd",
            ArtifactoryVersionProvider.v342.get()),
    v158("http://artifactory.jfrog.org/xsd/1.5.8",
            "http://www.jfrog.org/xsd/artifactory-v1_5_8.xsd",
            ArtifactoryVersionProvider.v350.get()),
    v159("http://artifactory.jfrog.org/xsd/1.5.9",
            "http://www.jfrog.org/xsd/artifactory-v1_5_9.xsd",
            ArtifactoryVersionProvider.v351.get()),
    v1510("http://artifactory.jfrog.org/xsd/1.5.10",
            "http://www.jfrog.org/xsd/artifactory-v1_5_10.xsd",
            ArtifactoryVersionProvider.v360.get()),
    v1511("http://artifactory.jfrog.org/xsd/1.5.11",
            "http://www.jfrog.org/xsd/artifactory-v1_5_11.xsd",
            ArtifactoryVersionProvider.v370.get()),
    v1512("http://artifactory.jfrog.org/xsd/1.5.12",
            "http://www.jfrog.org/xsd/artifactory-v1_5_12.xsd",
            ArtifactoryVersionProvider.v380.get()),
    v1513("http://artifactory.jfrog.org/xsd/1.5.13",
            "http://www.jfrog.org/xsd/artifactory-v1_5_13.xsd",
            ArtifactoryVersionProvider.v390.get(), new AddonsDefaultLayoutConverter(), new SingleRepoTypeConverter(),
            new SuppressConsitencyConverter(),new MavenIndexerConverter()),
    v160("http://artifactory.jfrog.org/xsd/1.6.0",
            "http://www.jfrog.org/xsd/artifactory-v1_6_0.xsd",
            ArtifactoryVersionProvider.v400.get()),
    v161("http://artifactory.jfrog.org/xsd/1.6.1",
            "http://www.jfrog.org/xsd/artifactory-v1_6_1.xsd",
            ArtifactoryVersionProvider.v401.get(),
             new FolderDownloadConfigConverter()),
    v162("http://artifactory.jfrog.org/xsd/1.6.2",
            "http://www.jfrog.org/xsd/artifactory-v1_6_2.xsd",
            ArtifactoryVersionProvider.v410.get()),
    v163("http://artifactory.jfrog.org/xsd/1.6.3",
            "http://www.jfrog.org/xsd/artifactory-v1_6_3.xsd",
            ArtifactoryVersionProvider.v413.get()),
    v164("http://artifactory.jfrog.org/xsd/1.6.4",
            "http://www.jfrog.org/xsd/artifactory-v1_6_4.xsd",
            ArtifactoryVersionProvider.v430.get()),
    v165("http://artifactory.jfrog.org/xsd/1.6.5",
            "http://www.jfrog.org/xsd/artifactory-v1_6_5.xsd",
            ArtifactoryVersionProvider.v431.get(),
             new SourceDeletedDetectionConverter()),
    v166("http://artifactory.jfrog.org/xsd/1.6.6",
            "http://www.jfrog.org/xsd/artifactory-v1_6_6.xsd",
            ArtifactoryVersionProvider.v433.get(),
             new UserLockConfigConverter(), new TrashcanConfigConverter()),
    v167("http://artifactory.jfrog.org/xsd/1.6.7",
            "http://www.jfrog.org/xsd/artifactory-v1_6_7.xsd",
            ArtifactoryVersionProvider.v440.get(),
             new PasswordPolicyConverter()),
    v168("http://artifactory.jfrog.org/xsd/1.6.8",
            "http://www.jfrog.org/xsd/artifactory-v1_6_8.xsd",
            ArtifactoryVersionProvider.v441.get(),
             new PasswordMaxAgeConverter()),
    v169("http://artifactory.jfrog.org/xsd/1.6.9",
            "http://www.jfrog.org/xsd/artifactory-v1_6_9.xsd",
            ArtifactoryVersionProvider.v442.get()),
    v170("http://artifactory.jfrog.org/xsd/1.7.0",
            "http://www.jfrog.org/xsd/artifactory-v1_7_0.xsd",
            ArtifactoryVersionProvider.v443.get(), new SimpleLayoutConverter()),
    v171("http://artifactory.jfrog.org/xsd/1.7.1",
            "http://www.jfrog.org/xsd/artifactory-v1_7_1.xsd",
            ArtifactoryVersionProvider.v460.get(),new BlockMismatchingMimeTypesConverter()),
    v172("http://artifactory.jfrog.org/xsd/1.7.2",
            "http://www.jfrog.org/xsd/artifactory-v1_7_2.xsd",
            ArtifactoryVersionProvider.v461.get()),
    v173("http://artifactory.jfrog.org/xsd/1.7.3",
            "http://www.jfrog.org/xsd/artifactory-v1_7_3.xsd",
            ArtifactoryVersionProvider.v471.get()),
    v174("http://artifactory.jfrog.org/xsd/1.7.4",
            "http://www.jfrog.org/xsd/artifactory-v1_7_4.xsd",
            ArtifactoryVersionProvider.v480.get(),
             new DockerForceAuthRemovalConverter()),
    v175("http://artifactory.jfrog.org/xsd/1.7.5",
            "http://www.jfrog.org/xsd/artifactory-v1_7_5.xsd",
            ArtifactoryVersionProvider.v490.get()),
    v176("http://artifactory.jfrog.org/xsd/1.7.6",
            "http://www.jfrog.org/xsd/artifactory-v1_7_6.xsd",
            ArtifactoryVersionProvider.v4100.get(),
             new LdapPoisoningProtectionConverter()),
    v177("http://artifactory.jfrog.org/xsd/1.7.7",
            "http://www.jfrog.org/xsd/artifactory-v1_7_7.xsd",
            ArtifactoryVersionProvider.v4110.get(),
             new SigningKeysConverter()),
    v178("http://artifactory.jfrog.org/xsd/1.7.8",
            "http://www.jfrog.org/xsd/artifactory-v1_7_8.xsd",
            ArtifactoryVersionProvider.v4111.get()),
    v179("http://artifactory.jfrog.org/xsd/1.7.9",
            "http://www.jfrog.org/xsd/artifactory-v1_7_9.xsd",
            ArtifactoryVersionProvider.v4120.get(),
             new XrayRepoConfigConverter()),
    v180("http://artifactory.jfrog.org/xsd/1.8.0",
            "http://www.jfrog.org/xsd/artifactory-v1_8_0.xsd",
            ArtifactoryVersionProvider.v4121.get(),
             new ComposerDefaultLayoutConverter()),
    v181("http://artifactory.jfrog.org/xsd/1.8.1",
            "http://www.jfrog.org/xsd/artifactory-v1_8_1.xsd",
            ArtifactoryVersionProvider.v4140.get(),
             new ConanDefaultLayoutConverter()),
    v182("http://artifactory.jfrog.org/xsd/1.8.2",
            "http://www.jfrog.org/xsd/artifactory-v1_8_2.xsd",
            ArtifactoryVersionProvider.v4150.get(),
             new ExternalProvidersRemovalConverter()),
    v200("http://artifactory.jfrog.org/xsd/2.0.0",
            "http://www.jfrog.org/xsd/artifactory-v2_0_0.xsd",
            ArtifactoryVersionProvider.v500beta1.get(),
             new PuppetDefaultLayoutConverter()),
    v201("http://artifactory.jfrog.org/xsd/2.0.1",
            "http://www.jfrog.org/xsd/artifactory-v2_0_1.xsd",
            ArtifactoryVersionProvider.v500rc6.get()),
    v202("http://artifactory.jfrog.org/xsd/2.0.2",
            "http://www.jfrog.org/xsd/artifactory-v2_0_2.xsd",
            ArtifactoryVersionProvider.v521m003.get()),
    v203("http://artifactory.jfrog.org/xsd/2.0.3",
            "http://www.jfrog.org/xsd/artifactory-v2_0_3.xsd",
            ArtifactoryVersionProvider.v522m001.get(),
             new AccessTokenSettingsRenameToAccessClientSettingsConverter()),
    v204("http://artifactory.jfrog.org/xsd/2.0.4",
            "http://www.jfrog.org/xsd/artifactory-v2_0_4.xsd",
            ArtifactoryVersionProvider.v540m001.get(),
            new YumEnableFilelistsIndexingForExistingLocalReposConverter()),
    v205("http://artifactory.jfrog.org/xsd/2.0.5",
            "http://www.jfrog.org/xsd/artifactory-v2_0_5.xsd",
            ArtifactoryVersionProvider.v540.get(),
             new EventBasedRemoteReplicationConverter()),
    v206("http://artifactory.jfrog.org/xsd/2.0.6",
            "http://www.jfrog.org/xsd/artifactory-v2_0_6.xsd",
            ArtifactoryVersionProvider.v550m001.get()),
    v207("http://artifactory.jfrog.org/xsd/2.0.7",
            "http://www.jfrog.org/xsd/artifactory-v2_0_7.xsd",
            ArtifactoryVersionProvider.v552m001.get(),
             new RemoveAccessAdminCredentialsConverter(),
            new FolderDownloadForAnonymousConfigConverter()),
    v208("http://artifactory.jfrog.org/xsd/2.0.8",
            "http://www.jfrog.org/xsd/artifactory-v2_0_8.xsd",
            ArtifactoryVersionProvider.v560m001.get(),
             new AddReplicationKey()),
    v209("http://artifactory.jfrog.org/xsd/2.0.9",
            "http://www.jfrog.org/xsd/artifactory-v2_0_9.xsd",
            ArtifactoryVersionProvider.v570m001.get()),
    v210("http://artifactory.jfrog.org/xsd/2.1.0",
            "http://www.jfrog.org/xsd/artifactory-v2_1_0.xsd",
            ArtifactoryVersionProvider.v580.get(),
             new XrayMinBlockedSeverityAndBlockUnscannedConverter()),
    v211("http://artifactory.jfrog.org/xsd/2.1.1",
            "http://www.jfrog.org/xsd/artifactory-v2_1_1.xsd",
            ArtifactoryVersionProvider.v5100m009.get(),
            new PyPIRegistryUrlConverter()),
    v212("http://artifactory.jfrog.org/xsd/2.1.2",
            "http://www.jfrog.org/xsd/artifactory-v2_1_2.xsd",
            ArtifactoryVersionProvider.v5103.get(),
            new AllowCrowdUsersToAccessProfilePageConverter(),
            new GoDefaultLayoutConverter()),
    v213("http://artifactory.jfrog.org/xsd/2.1.3",
            "http://www.jfrog.org/xsd/artifactory-v2_1_3.xsd",
            ArtifactoryVersionProvider.v5110.get(),
            new NuGetV3Converter()),
    v214("http://artifactory.jfrog.org/xsd/2.1.4",
            "http://www.jfrog.org/xsd/artifactory-v2_1_4.xsd",
            ArtifactoryVersionProvider.v600m023.get(),
            new ConanFixDefaultLayoutConverter(), new ConanDefaultDistributionRuleConverter()),
    v215("http://artifactory.jfrog.org/xsd/2.1.5",
            "http://www.jfrog.org/xsd/artifactory-v2_1_5.xsd",
            ArtifactoryVersionProvider.v610m002.get()),
    v216("http://artifactory.jfrog.org/xsd/2.1.6",
            "http://www.jfrog.org/xsd/artifactory-v2_1_6.xsd",
            ArtifactoryVersionProvider.v640m007.get()),
    v217("http://artifactory.jfrog.org/xsd/2.1.7",
            "http://www.jfrog.org/xsd/artifactory-v2_1_7.xsd",
            ArtifactoryVersionProvider.v658.get(),
            new BackupSettingConvert(), new AnonAccessToBuildsConverter(), new BuildDefaultLayoutConverter()),
    v218("http://artifactory.jfrog.org/xsd/2.1.8",
            "http://www.jfrog.org/xsd/artifactory-v2_1_8.xsd",
            ArtifactoryVersionProvider.v660m001.get()),
    v219("http://artifactory.jfrog.org/xsd/2.1.9",
            "http://www.jfrog.org/xsd/artifactory-v2_1_9.xsd",
            ArtifactoryVersionProvider.v665.get(),
            new DownloadRedirectConverter(), new ConanV2DefaultLayoutConverter()),
    v220("http://artifactory.jfrog.org/xsd/2.2.0",
            "http://www.jfrog.org/xsd/artifactory-v2_2_0.xsd",
            ArtifactoryVersionProvider.v690m001.get(), new PyPIRegistrySuffixConverter()),
    v221("http://artifactory.jfrog.org/xsd/2.2.1",
            "http://www.jfrog.org/xsd/artifactory-v2_2_1.xsd",
            ArtifactoryVersionProvider.v6103.get()),
    v222("http://artifactory.jfrog.org/xsd/2.2.2",
            "http://www.jfrog.org/xsd/artifactory-v2_2_2.xsd",
            ArtifactoryVersionProvider.v6110m001.get()),
    v223("http://artifactory.jfrog.org/xsd/2.2.3",
            "http://www.jfrog.org/xsd/artifactory-v2_2_3.xsd",
            ArtifactoryVersionProvider.v6120.get(),
            new EnablePushingSchema1DockerConverter()),
    v224("http://artifactory.jfrog.org/xsd/2.2.4",
            "http://www.jfrog.org/xsd/artifactory-v2_2_4.xsd",
            ArtifactoryVersionProvider.v6150.get()),
    v225("http://artifactory.jfrog.org/xsd/2.2.5",
            "http://www.jfrog.org/xsd/artifactory-v2_2_5.xsd",
            ArtifactoryVersionProvider.v6190.get());

    private final String xsdUri;
    private final String xsdLocation;
    private ArtifactoryVersion version;
    private final XmlConverter[] converters;

    /**
     * @param version       The artifactory version this config version was first used
     * @param converters A list of converters to use to move from <b>this</b> config version to the <b>next</b> config
     *                   version
     */
    ArtifactoryConfigVersion(String xsdUri, String xsdLocation, ArtifactoryVersion version,
                             XmlConverter... converters) {
        this.xsdUri = xsdUri;
        this.xsdLocation = xsdLocation;
        this.version = version;
        this.converters = converters;
    }

    public boolean isLast() {
        return this.ordinal() == last().ordinal();
    }

    public String convert(String in) {
        // First create the list of converters to apply
        List<XmlConverter> converters = new ArrayList<>();

        // First thing to do is to change the namespace and schema location
        converters.add(new NamespaceConverter());

        // All converters of versions above me needs to be executed in sequence
        ArtifactoryConfigVersion[] versions = ArtifactoryConfigVersion.values();
        for (ArtifactoryConfigVersion version : versions) {
            if (version.ordinal() >= ordinal() && version.getConverters() != null) {
                converters.addAll(Arrays.asList(version.getConverters()));
            }
        }
        return XmlConverterUtils.convert(converters, in);
    }

    public String getXsdUri() {
        return xsdUri;
    }

    public String getXsdLocation() {
        return xsdLocation;
    }

    public XmlConverter[] getConverters() {
        return converters;
    }

    @Override
    public ArtifactoryVersion getVersion() {
        return version;
    }

    public static ArtifactoryConfigVersion getConfigVersion(String configXml) {
        // Find correct version by schema URI
        ArtifactoryConfigVersion[] configVersions = values();
        for (ArtifactoryConfigVersion configVersion : configVersions) {
            if (configXml.contains("\"" + configVersion.getXsdUri() + "\"")) {
                return configVersion;
            }
        }
        return null;
    }

    public static ArtifactoryConfigVersion last() {
        return ArtifactoryConfigVersion.values()[ArtifactoryConfigVersion.values().length-1];
    }
}
