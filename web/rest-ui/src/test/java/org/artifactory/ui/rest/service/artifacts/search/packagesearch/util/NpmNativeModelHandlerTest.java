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

package org.artifactory.ui.rest.service.artifacts.search.packagesearch.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Sets;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.result.PackageNativeModel;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.result.PackageNativeSearchResult;
import org.artifactory.ui.rest.model.artifacts.search.versionsearch.result.NativeSummaryModel;
import org.artifactory.ui.rest.model.artifacts.search.versionsearch.result.VersionNativeModel;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeConstants.*;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Lior Gur
 */
@Test
public class NpmNativeModelHandlerTest {

    private PackageNativeModelHandler nativeModelHandler = PackageNativeModelHandlersFactory
            .getModelHandler(RepoType.Npm.getType());

    @Test
    public void testMergePackageFields() throws ParseException {
        PackageNativeSearchResult packageNativeSearchResult = createMockPackageNativeSearchResult("npm_pkg", "1.0.0",
                "[\"key1\", \"key2\", \"key3\"]", "npm-local", "Thu Jul 26 16:46:31 IDT 2018",
                null, null, null);

        PackageNativeModel packageNativeModel = new PackageNativeModel();
        setPackageNativeModel(packageNativeModel, Sets.newHashSet("npm-local-2"),
                "[\"key5\", \"key6\"]", getDateFromString("Thu Jul 26 16:46:31 IDT 2018").getTime(), "npm_pkg");

        nativeModelHandler.mergeFields(packageNativeModel, packageNativeSearchResult);

        assertPackage(packageNativeModel, Sets.newHashSet("npm-local", "npm-local-2"),
                Sets.newHashSet("key1", "key2", "key3", "key5", "key6"),
                "npm_pkg", "Thu Jul 26 16:46:31 IDT 2018");
    }

    @Test
    public void testMergePackageFieldsResultsIsLatest() throws ParseException {
        PackageNativeSearchResult packageSearchResult = createMockPackageNativeSearchResult("npm_pkg", "1.0.0",
                "[\"bla\", \"blabla\"]",  "npm-local",
                "Thu Jul 26 17:46:31 IDT 2018", null, null, null);

        PackageNativeModel packageNativeModel = new PackageNativeModel();
        setPackageNativeModel(packageNativeModel, Sets.newHashSet("npm-local-2"),
                null, getDateFromString("Thu Jul 26 16:46:31 IDT 2018").getTime(), "npm_pkg");

        nativeModelHandler.mergeFields(packageNativeModel, packageSearchResult);

        assertPackage(packageNativeModel, Sets.newHashSet("npm-local", "npm-local-2"),
                Sets.newHashSet("bla", "blabla"), "npm_pkg", "Thu Jul 26 17:46:31 IDT 2018");
    }

    @Test
    public void testMergePackageFieldsSameVersion() throws ParseException {
        PackageNativeSearchResult packageSearchResult = createMockPackageNativeSearchResult("npm_pkg", "2.3.0",
                "no_content", "npm-local",
                "Thu Jul 26 17:46:31 IDT 2018", null, null, null);

        PackageNativeModel packageNativeModel = new PackageNativeModel();
        setPackageNativeModel(packageNativeModel, Sets.newHashSet("npm-local-2"),
                "[\"bla\", \"blabla\"]", getDateFromString("Thu Jul 26 16:46:31 IDT 2018").getTime(), "npm_pkg");

        nativeModelHandler.mergeFields(packageNativeModel, packageSearchResult);

        assertPackage(packageNativeModel, Sets.newHashSet("npm-local", "npm-local-2"),
                Sets.newHashSet("bla", "blabla"), "npm_pkg", "Thu Jul 26 17:46:31 IDT 2018");
    }

    @Test
    public void testMergePackageFieldsKeywordsIsEmptyArray() throws ParseException {
        PackageNativeSearchResult packageSearchResult = createMockPackageNativeSearchResult("npm_pkg", "2.3.0",
                "[]", "npm-local",
                "Thu Jul 26 17:46:31 IDT 2018", null, null, null);

        PackageNativeModel packageNativeModel = new PackageNativeModel();
        setPackageNativeModel(packageNativeModel, Sets.newHashSet("npm-local-2"),
                "no_content", getDateFromString("Thu Jul 26 16:46:31 IDT 2018").getTime(), "npm_pkg");

        nativeModelHandler.mergeFields(packageNativeModel, packageSearchResult);

        assertPackage(packageNativeModel, Sets.newHashSet("npm-local", "npm-local-2"),
                Sets.newHashSet(), "npm_pkg", "Thu Jul 26 17:46:31 IDT 2018");
    }

    @Test
    public void testMergePackageFieldsSameRepoKey() throws ParseException {
        PackageNativeSearchResult packageSearchResult = createMockPackageNativeSearchResult("npm_pkg", "2.3.0",
                "no_content", "npm-local",
                "Thu Jul 26 17:46:31 IDT 2018", null, null, null);
        PackageNativeModel packageNativeModel = new PackageNativeModel();
        setPackageNativeModel(packageNativeModel, Sets.newHashSet("npm-local"),
                null, getDateFromString("Thu Jul 26 16:46:31 IDT 2018").getTime(), "npm_pkg");

        nativeModelHandler.mergeFields(packageNativeModel, packageSearchResult);

        assertPackage(packageNativeModel, Sets.newHashSet("npm-local"),
                Sets.newHashSet(), "npm_pkg", "Thu Jul 26 17:46:31 IDT 2018");
    }

    @Test
    public void testMergeVersionFieldsResultIsLatest() throws ParseException {
        PackageNativeSearchResult packageSearchResult = createMockPackageNativeSearchResult("npm_pkg", "1.0.0",
                "[\"key1\", \"key2\", \"key3\"]","npm-local", "Thu Jul 26 16:46:31 IDT 2018",
                new RepoPathImpl("npm-local", "npm_pkg/-/npm_pkg-1.0.0.tgz"), null, null);

        VersionNativeModel versionNativeModel = new VersionNativeModel("1.0.0",
                getDateFromString("Thu Jul 25 15:46:31 IDT 2018").getTime(), 2,
                new RepoPathImpl("npm-local-2", "npm_pkg/-/npm_pkg-1.0.0.tgz").toPath(),
                Sets.newHashSet("npm-local", "npm-local-2"));

        nativeModelHandler.mergeVersionFields(versionNativeModel, packageSearchResult);

        assertVersion(versionNativeModel, Sets.newHashSet("npm-local", "npm-local-2"),
                Sets.newHashSet("key1", "key2", "key3", "key10"),
                "1.0.0", "Thu Jul 26 16:46:31 IDT 2018",
                new RepoPathImpl("npm-local", "npm_pkg/-/npm_pkg-1.0.0.tgz").toPath());
    }

    @Test
    public void testMergeVersionFieldsCurrentVersionIsLatest() throws ParseException {
        PackageNativeSearchResult packageSearchResult = createMockPackageNativeSearchResult("npm_pkg", "1.0.0",
                "[\"key1\", \"key2\", \"key3\"]","npm-local", "Thu Jul 26 16:46:31 IDT 2018",
                new RepoPathImpl("npm-local", "npm_pkg/-/npm_pkg-1.0.0.tgz"), null, null);

        VersionNativeModel versionNativeModel = new VersionNativeModel("1.0.0",
                getDateFromString("Thu Jul 28 15:46:31 IDT 2018").getTime(), 2,
                new RepoPathImpl("npm-local-2", "npm_pkg/-/npm_pkg-1.0.0.tgz").toPath(),
                Sets.newHashSet("npm-local", "npm-local-2"));

        nativeModelHandler.mergeVersionFields(versionNativeModel, packageSearchResult);

        assertVersion(versionNativeModel, Sets.newHashSet("npm-local", "npm-local-2"),
                Sets.newHashSet("key1", "key2", "key3", "key10"),
                "1.0.0", "Thu Jul 28 15:46:31 IDT 2018",
                new RepoPathImpl("npm-local-2", "npm_pkg/-/npm_pkg-1.0.0.tgz").toPath());
    }

    @Test
    public void testMergeVersionFieldsSameLastUpdated() throws ParseException {
        PackageNativeSearchResult packageSearchResult = createMockPackageNativeSearchResult("npm_pkg", "1.0.0",
                "[\"key1\", \"key2\", \"key3\"]","npm-local", "Thu Jul 26 16:46:31 IDT 2018",
                new RepoPathImpl("npm-local", "npm_pkg/-/npm_pkg-1.0.0.tgz"), null, null);

        VersionNativeModel versionNativeModel = new VersionNativeModel("1.0.0",
                getDateFromString("Thu Jul 26 16:46:31 IDT 2018").getTime(), 2,
                new RepoPathImpl("npm-local-2", "npm_pkg/-/npm_pkg-1.0.0.tgz").toPath(),
                Sets.newHashSet("npm-local", "npm-local-2"));

        nativeModelHandler.mergeVersionFields(versionNativeModel, packageSearchResult);

        assertVersion(versionNativeModel, Sets.newHashSet("npm-local", "npm-local-2"),
                Sets.newHashSet("key1", "key2", "key3", "key10"),
                "1.0.0", "Thu Jul 26 16:46:31 IDT 2018",
                new RepoPathImpl("npm-local-2", "npm_pkg/-/npm_pkg-1.0.0.tgz").toPath());
    }

    @Test
    public void testMergePackageSummaryFields() throws ParseException {
        PackageNativeSearchResult packageSearchResult = createMockPackageNativeSearchResult("npm_pkg", "1.0.0",
                null, "npm-local", "Thu Jul 26 16:46:31 IDT 2018",
                new RepoPathImpl("npm-local", "npm_pkg/-/npm_pkg-1.0.0.tgz"), "lic1", "my description");

        NativeSummaryModel packageNativeSummaryModel = new NativeSummaryModel("npm_pkg",
                getDateFromString("Thu Jul 28 16:46:31 IDT 2018").getTime(), "another description",
                "npm_pkg/-/npm_pkg-4.1.0.tgz");

        nativeModelHandler.mergeSummaryFields(packageNativeSummaryModel, packageSearchResult);

        assertSummary(packageNativeSummaryModel, "npm_pkg",
                "Thu Jul 28 16:46:31 IDT 2018", "another description",
                "npm_pkg/-/npm_pkg-4.1.0.tgz");
    }

    @Test
    public void testMergePackageSummaryFieldsResultIsLatest() throws ParseException {
        PackageNativeSearchResult packageSearchResult = createMockPackageNativeSearchResult("npm_pkg", "1.0.0",
                null, "npm-local", "Thu Jul 26 18:46:31 IDT 2018",
                new RepoPathImpl("npm-local", "npm_pkg/-/npm_pkg-1.0.0.tgz"), "lic1", "my description");

        NativeSummaryModel packageNativeSummaryModel = new NativeSummaryModel("npm_pkg",
                getDateFromString("Thu Jul 26 16:46:31 IDT 2018").getTime(), "another description",
                "npm-local-3/npm_pkg/-/npm_pkg-3.0.0.tgz");

        nativeModelHandler.mergeSummaryFields(packageNativeSummaryModel, packageSearchResult);

        assertSummary(packageNativeSummaryModel, "npm_pkg",
                "Thu Jul 26 18:46:31 IDT 2018", "my description",
                "npm-local/npm_pkg/-/npm_pkg-1.0.0.tgz");
    }

    @Test
    public void testMergeVersionSummaryFields() throws ParseException {
        PackageNativeSearchResult packageSearchResult = createMockPackageNativeSearchResult("npm_pkg", "1.0.0",
                null,"npm-local", "Thu Jul 26 16:46:31 IDT 2018",
                new RepoPathImpl("npm-local", "npm_pkg/-/npm_pkg-1.0.0.tgz"), "lic1", "my description");

        NativeSummaryModel versionNativeSummaryModel = new NativeSummaryModel("1.0.0",
                getDateFromString("Thu Jul 28 16:46:31 IDT 2018").getTime(), "another description",
                "npm-local-2/npm_pkg/-/npm_pkg-1.0.0.tgz");

        nativeModelHandler.mergeSummaryFields(versionNativeSummaryModel, packageSearchResult);

        assertSummary(versionNativeSummaryModel, "1.0.0", "Thu Jul 28 16:46:31 IDT 2018",
                "another description", "npm-local-2/npm_pkg/-/npm_pkg-1.0.0.tgz");
    }

    @Test
    public void testMergeVersionSummaryFieldsResultIsLatest() throws ParseException {
        PackageNativeSearchResult packageSearchResult = createMockPackageNativeSearchResult("1.0.0", "1.0.0",
                null, "npm-local", "Thu Jul 26 16:58:31 IDT 2018",
                new RepoPathImpl("npm-local", "npm_pkg/-/npm_pkg-1.0.0.tgz"), "lic1", "my description");

        NativeSummaryModel versionNativeSummaryModel = new NativeSummaryModel("1.0.0",
                getDateFromString("Thu Jul 26 16:46:31 IDT 2018").getTime(), "another description",
                "");

        nativeModelHandler.mergeSummaryFields(versionNativeSummaryModel, packageSearchResult);

        assertSummary(versionNativeSummaryModel, "1.0.0",
                "Thu Jul 26 16:58:31 IDT 2018", "my description",
                "npm-local/npm_pkg/-/npm_pkg-1.0.0.tgz");
    }

    private void assertPackage(PackageNativeModel packageNativeModel, Set<String> repositories, Set<String> keywords,
            String name, String lastModified) throws ParseException {
        assertEquals(packageNativeModel.getName(), name);
        assertEquals(packageNativeModel.getLastModified(), getDateFromString(lastModified).getTime());
        assertEquals(packageNativeModel.getNumOfRepos(), repositories.size());
        assertTrue(packageNativeModel.getRepositories().containsAll(repositories));
        assertTrue(keywords.containsAll(packageNativeModel.getKeywords()));
        assertEquals(packageNativeModel.getKeywords().size(), keywords.size());
    }

    private void assertSummary(NativeSummaryModel packageNativeSummaryModel,
            String expectedName, String expectedLastModified, String expectedDescription, String expectedPath) throws ParseException {
        assertEquals(packageNativeSummaryModel.getName(), expectedName);
        assertEquals(packageNativeSummaryModel.getLastModified(), getDateFromString(expectedLastModified).getTime());
        assertEquals(packageNativeSummaryModel.getDescription(), expectedDescription);
        assertEquals(packageNativeSummaryModel.getLatestPath(), expectedPath);
    }

    private void assertVersion(VersionNativeModel packageNativeModel, Set<String> repositories, Set<String> keywords,
            String name, String lastModified, String repoPath) throws ParseException {
        assertTrue(packageNativeModel.getRepositories().containsAll(repositories));
        assertEquals(packageNativeModel.getName(), name);
        assertEquals(packageNativeModel.getLastModified(), getDateFromString(lastModified).getTime());
        assertEquals(packageNativeModel.getNumOfRepos(), repositories.size());
        assertEquals(packageNativeModel.getLatestPath(), repoPath);
    }

    private PackageNativeModel setPackageNativeModel(PackageNativeModel packageNativeModel, Set<String> repositories,
            String keywords, long lastModified, String name) {
        packageNativeModel.setRepositories(repositories);
        packageNativeModel.addKeywords(keywords);
        packageNativeModel.setLastModified(lastModified);
        packageNativeModel.setName(name);
        return packageNativeModel;
    }

    private PackageNativeSearchResult createMockPackageNativeSearchResult(String name, String version,
            String keywords, String repoKey, String lastModified, RepoPath repoPath, String lic, String description) throws ParseException {
        PackageNativeSearchResult mockPackageNativeSearchResult = Mockito.mock(PackageNativeSearchResult.class);

        HashMultimap<String, String> extraFields = HashMultimap.create();
        setExtraFields(extraFields, name, version, keywords, lic, description);

        when(mockPackageNativeSearchResult.getExtraFields()).thenReturn(extraFields);
        when(mockPackageNativeSearchResult.getRepoKey()).thenReturn(repoKey);
        when(mockPackageNativeSearchResult.getModifiedDate()).thenReturn(getDateFromString(lastModified).getTime());
        when(mockPackageNativeSearchResult.getRepoPath()).thenReturn(repoPath);

        return mockPackageNativeSearchResult;
    }

    private void setExtraFields(HashMultimap<String, String> extraFields, String name, String version, String keywords,
            String lic, String description) {
        extraFields.put(NPM_NAME, name);
        extraFields.put(NPM_VERSION, version);

        if (keywords != null) {
            extraFields.put(NPM_KEYWORDS, keywords);
        }

        if (lic != null) {
            extraFields.put(ARTIFACTORY_LIC, lic);
        }
        if (description != null) {
            extraFields.put(NPM_DESCRIPTION, description);
        }
    }

    private Date getDateFromString(String date) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        return dateFormat.parse(date);
    }
}