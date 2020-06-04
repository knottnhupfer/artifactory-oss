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

package org.artifactory.repo.remote.browse;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.jfrog.common.ResourceUtils.getResourceAsString;
import static org.testng.Assert.assertEquals;

/**
 * Test {@link HtmlRepositoryBrowser}.
 *
 * @author Tomer Cohen
 */
@Test
public class HtmlRepositoryBrowserTest extends ArtifactoryHomeBoundTest {

    private HtmlRepositoryBrowser urlLister;
    private String baseUrl;
    private static final String testResourcesPath = "/org/artifactory/repo/remote/browse/html";

    @BeforeMethod
    public void setUp() {
        final CloseableHttpClient hc = HttpClients.createDefault();
        HttpExecutor httpExecutor = hc::execute;
        urlLister = new HtmlRepositoryBrowser(httpExecutor);
        baseUrl = "http://blabla";
    }

    public void listAllInvalid() throws IOException {
        String html = getResourceAsString(testResourcesPath + "/invalidHtml.html");
        List<RemoteItem> urls = urlLister.parseHtml(html, baseUrl);
        assertEquals(urls.size(), 0, "there should be no URLs in this html");
    }

    public void listAllNoHrefs() throws IOException {
        String html = getResourceAsString(testResourcesPath + "/noHrefs.html");
        List<RemoteItem> urls = urlLister.parseHtml(html, baseUrl);
        assertEquals(urls.size(), 0, "there should be no URLs in this html");
    }

    public void listAll() throws IOException {
        String html = getResourceAsString(testResourcesPath + "/simple.html");
        String validHtml = createValidHtml(html);
        List<RemoteItem> urls = urlLister.parseHtml(validHtml, baseUrl);
        assertEquals(urls.size(), 1);
    }

    public void listAllFromArtifactorySimpleBrowsingHtml() throws IOException {
        String html = getResourceAsString(testResourcesPath + "/artifactory-simple.html");
        // base url that matches the links un the test html file
        String baseUrl = "http://localhost:8081/artifactory/libs-releases-local/org/jfrog/test/";
        List<RemoteItem> children = urlLister.parseHtml(html, baseUrl);
        assertEquals(children.size(), 4, "Found: " + children);
        assertEquals(children.get(0), new RemoteItem(baseUrl + "multi1/", true));
        assertEquals(children.get(3), new RemoteItem(baseUrl + "multi.pom", false));
    }

    public void listAllFromArtifactoryListBrowsingHtml() throws IOException {
        String html = getResourceAsString(testResourcesPath + "/artifactory-list.html");
        List<RemoteItem> children = urlLister.parseHtml(html, baseUrl);
        assertEquals(children.size(), 3, "Found: " + children);
        assertEquals(children.get(0).getUrl(), baseUrl + "/multi1/");
        assertEquals(children.get(1).getUrl(), baseUrl + "/multi2/");
        assertEquals(children.get(2).getUrl(), baseUrl + "/multi3/");
    }

    public void listAllFromBintrayListBrowsingHtml() throws IOException {
        baseUrl = "http://jcenter.bintray.com";
        String html = getResourceAsString(testResourcesPath + "/bintray.html");
        List<RemoteItem> children = urlLister.parseHtml(html, baseUrl);
        assertEquals(children.size(), 1, "Found: " + children);
        assertEquals(children.get(0).getUrl(), baseUrl + "/ojdbc/");
    }

    private String createValidHtml(String source) throws IOException {
        File tempFile = File.createTempFile("artifactory", "html");
        return source.replace("{placeHolder}", tempFile.toURI().toURL().toExternalForm());
    }

    public void listAllFromArtifactoryListBrowsingHtmlWithSingleQuotes() throws IOException {
        String html = getResourceAsString(testResourcesPath + "/hrefExample.html");
        List<RemoteItem> children = urlLister.parseHtml(html, baseUrl);
        assertEquals(children.size(), 4, "Found: " + children);
        assertEquals(children.get(0).getUrl(), baseUrl + "/file.jar");
        assertEquals(children.get(0).getName(), "file.jar");
        assertEquals(children.get(1).getUrl(), baseUrl + "/file1");
        assertEquals(children.get(1).getName(), "file1");
        assertEquals(children.get(2).getUrl(), baseUrl + "/file2");
        assertEquals(children.get(2).getName(), "file2");
        assertEquals(children.get(3).getUrl(), baseUrl + "/index.html");
        assertEquals(children.get(3).getName(), "index.html");
    }

    public void listAllWhenHtmlTextEndsWithDots() throws IOException {
        String html = getResourceAsString(testResourcesPath + "/htmlTextEndsWithDots.html");
        List<RemoteItem> children = urlLister.parseHtml(html, baseUrl);
        assertEquals(children.size(), 4, "Found: " + children);
        assertEquals(children.get(0).getUrl(), baseUrl + "/long-test-resource-name-1.0.0-RELEASE-source.jar");
        assertEquals(children.get(0).getName(), "long-test-resource-name-1.0.0-RELEASE-source.jar");
        assertEquals(children.get(1).getUrl(), baseUrl + "/long-test-resource-name-1.0.0-RELEASE-source.jar.asc");
        assertEquals(children.get(1).getName(), "long-test-resource-name-1.0.0-RELEASE-source.jar.asc");
        assertEquals(children.get(2).getUrl(), baseUrl + "/long-test-resource-name-1.0.0-RELEASE-source.jar.md5");
        assertEquals(children.get(2).getName(), "long-test-resource-name-1.0.0-RELEASE-source.jar.md5");
        assertEquals(children.get(3).getUrl(), baseUrl + "/long-test-resource-name-1.0.0-RELEASE-source.jar.sha1");
        assertEquals(children.get(3).getName(), "long-test-resource-name-1.0.0-RELEASE-source.jar.sha1");
    }

    public void listAllWhenHtmlTextWithSpecialChars() throws IOException {
        String html = getResourceAsString(testResourcesPath + "/htmlTextWithSpecialChars.html");
        List<RemoteItem> children = urlLister.parseHtml(html, baseUrl);
        assertEquals(children.size(), 6, "Found: " + children);
        assertEquals(children.get(0).getUrl(), baseUrl + "/conan/");
        assertEquals(children.get(0).getName(), "conan");
        assertEquals(children.get(1).getUrl(), baseUrl + "/user/");
        assertEquals(children.get(1).getName(), "user");
        assertEquals(children.get(2).getUrl(), baseUrl + "/abc!@#$%^&().json");
        assertEquals(children.get(2).getName(), "abc!@#$%^&().json");
        assertEquals(children.get(3).getUrl(), baseUrl + "/ésdasdé.txt");
        assertEquals(children.get(3).getName(), "ésdasdé.txt");
        assertEquals(children.get(4).getUrl(), baseUrl + "/why space.json");
        assertEquals(children.get(4).getName(), "why space.json");
        assertEquals(children.get(5).getUrl(), baseUrl + "/bla+blo.json");
        assertEquals(children.get(5).getName(), "bla+blo.json");
    }

}
