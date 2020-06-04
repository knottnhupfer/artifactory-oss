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

package org.artifactory.util;

import org.jfrog.client.util.PathUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Tests the PathUtils.
 *
 * @author Yossi Shaul
 */
@Test
public class PathUtilsTest {

    public void formatSimplePath() {
        String formatted = PathUtils.formatPath("\\this\\is\\a/path");
        assertEquals(formatted, "/this/is/a/path");
    }

    public void formatFormattedPath() {
        String formatted = PathUtils.formatPath("/this/is/a/path");
        assertEquals(formatted, "/this/is/a/path");
    }

    public void formatNullPath() {
        String formatted = PathUtils.formatPath(null);
        assertEquals(formatted, "");
    }

    public void stripExtensionSimple() {
        String result = PathUtils.stripExtension("file.ext");
        assertEquals(result, "file");
    }

    public void stripExtensionMultipleExtensions() {
        String result = PathUtils.stripExtension("file.ext.ext2");
        assertEquals(result, "file.ext");
    }

    public void stripExtensionPathWithNoExtension() {
        String result = PathUtils.stripExtension("file");
        assertEquals(result, "file");
    }

    public void stripExtensionPathWithDotAtEnd() {
        String result = PathUtils.stripExtension("file.");
        assertEquals(result, "file");
    }

    public void stripExtensionNullPath() {
        String result = PathUtils.stripExtension(null);
        assertNull(result);
    }

    public void stripExtensionEmptyPath() {
        String result = PathUtils.stripExtension("");
        assertEquals(result, "");
    }

    public void injectStringInMiddle() {
        String result = PathUtils.injectString("Arttory", "ifac", 3);
        assertEquals(result, "Artifactory");
    }

    public void injectStringAtTheBeginning() {
        String result = PathUtils.injectString("rtifactory", "A", 0);
        assertEquals(result, "Artifactory");
    }

    public void injectStringAtTheEnd() {
        String result = PathUtils.injectString("Artifactor", "y", 10);
        assertEquals(result, "Artifactory");
    }

    public void injectStringEmpty() {
        String result = PathUtils.injectString("Artifactory", "", 15);
        assertEquals(result, "Artifactory");
    }

    public void injectStringNull() {
        String result = PathUtils.injectString("Artifactory", null, 9);
        assertEquals(result, "Artifactory");
    }

    public void parentPathOfPathWithParent() {
        String result = PathUtils.getParent("/a/b/c");
        assertEquals(result, "/a/b");
    }

    public void parentPathOfRoot() {
        String result = PathUtils.getParent("/");
        assertEquals(result, "");
    }

    public void parentPathOfEmptyString() {
        String result = PathUtils.getParent("");
        assertEquals(result, "");
    }

    public void getPathElementsAbsolutePath() {
        String[] result = PathUtils.getPathElements("/a/b/c");
        assertEquals(result, new String[]{"a", "b", "c"});
    }

    public void getPathElementsRelativePath() {
        String[] result = PathUtils.getPathElements("a/b/c");
        assertEquals(result, new String[]{"a", "b", "c"});
    }

    public void getPathElementsPathWithTrailingSlash() {
        String[] result = PathUtils.getPathElements("a/b/");
        assertEquals(result, new String[]{"a", "b"});
    }

    public void getPathElementsRootPath() {
        String[] result = PathUtils.getPathElements("/");
        assertEquals(result, new String[]{""});
    }

    public void getPathElementsEmptyPath() {
        String[] result = PathUtils.getPathElements("");
        assertEquals(result, new String[]{""});
    }

    public void getPathElementsNullPath() {
        String[] result = PathUtils.getPathElements(null);
        assertEquals(result, new String[0]);
    }

    public void getFirstPathElementsAbsolutePath() {
        String result = PathUtils.getFirstPathElement("/a/b/c");
        assertEquals(result, "a");
    }

    public void getFirstPathElementsRelativePath() {
        String result = PathUtils.getFirstPathElement("a/b/c");
        assertEquals(result, "a");
    }

    public void getFirstPathElementsRootPath() {
        String result = PathUtils.getFirstPathElement("/");
        assertEquals(result, "");
    }

    public void getFirstPathElementsEmptyPath() {
        String result = PathUtils.getFirstPathElement("");
        assertEquals(result, "");
    }

    public void getFirstPathElementsNullPath() {
        String result = PathUtils.getFirstPathElement(null);
        assertNull(result);
    }

    public void trimLeadingSlashes() {
        String result = PathUtils.trimLeadingSlashes("////a/b/c");
        assertEquals(result, "a/b/c");
    }

    public void trimTrailingSlashes() {
        String result = PathUtils.trimTrailingSlashes("a/b/c///");
        assertEquals(result, "a/b/c");
    }

    public void trimTrailingSlashesChars() {
        CharSequence sequence = PathUtils.trimTrailingSlashesChars("a/b/c///");
        assertNotNull(sequence);
        assertEquals(sequence.toString(), "a/b/c");
    }

    public void trimLeadingSlashesChars() {
        CharSequence sequence = PathUtils.trimLeadingSlashChars("////a/b/c");
        assertNotNull(sequence);
        assertEquals(sequence.toString(), "a/b/c");
    }

    public void stripFirstPathElement() {
        assertEquals(PathUtils.stripFirstPathElement("/a/b/c"), "b/c");
        assertEquals(PathUtils.stripFirstPathElement("a/b/c/"), "b/c/");
        assertEquals(PathUtils.stripFirstPathElement("/a"), "");
        assertEquals(PathUtils.stripFirstPathElement("/"), "");
        assertEquals(PathUtils.stripFirstPathElement(""), "");
        assertNull(PathUtils.stripFirstPathElement(null));
    }

    public void addTrailingSlash() {
        assertEquals(PathUtils.addTrailingSlash("abc"), "abc/");
        assertEquals(PathUtils.addTrailingSlash("/a/b/c/"), "/a/b/c/");
        assertEquals(PathUtils.addTrailingSlash("  a/b  "), "a/b/");
        assertEquals(PathUtils.addTrailingSlash(""), "/");
        assertEquals(PathUtils.addTrailingSlash(null), null);
    }

    public void normalizeSlashes() {
        assertEquals(PathUtils.normalizeSlashes("a//b/c///d"), "a/b/c/d");
        assertEquals(PathUtils.normalizeSlashes("/a///////b///c///"), "/a/b/c/");
        assertEquals(PathUtils.normalizeSlashes("///a///////b///c/"), "/a/b/c/");
        assertNull(PathUtils.normalizeSlashes(null));
    }

    public void checksumTargetFile() {
        assertEquals(PathUtils.stripExtension("/a/b/c.jar.sha1"), "/a/b/c.jar");
        assertEquals(PathUtils.stripExtension("a.pom.md5"), "a.pom");
        assertEquals(PathUtils.stripExtension("/a/b/c.jar"), "/a/b/c");
        assertEquals(PathUtils.stripExtension("/a/b/c"), "/a/b/c");
    }

    // Check slashes and backslashes
    private final String B = "\\";
    private final String[] toCheckABC = {
            "/a///b/c/",
            "///a//" + B + B + B + "/////b///c/",
            "//////////////////a///////b///c/" + B,
            B + "///a///////b///c/",
            "///a///////b///c/",
            "///a///\\\\////b///c/"};

    private final String[] toCheckBlank = {null, "", "//", " ", "\n", "\\", "\\//", "//\\", "\\\\\\\\", "////////////"};

    private static String normalizePathBefore(String path) {
        if (path == null) {
            return null;
        }
        path = PathUtils.replaceBackslashesWithSlashes(path);
        path = PathUtils.trimSlashes(path);//.toString();
        path = PathUtils.removeDuplicateSlashes(path);
        return path;
    }

    /**
     * @param path A file like path.
     * @return The name of the file from the path)
     */
    private static String getFileNameBefore(String path) {
        if (path == null) {
            return null;
        }
        path = PathUtils.normalizePath(path);
        if (path.length() == 0) {
            return "";
        }
        int index = path.lastIndexOf("/");// returns -1 if n '/' exists
        return path.substring(index + 1);
    }

    /**
     * Making sure we didn't change existing behaviour
     */
    public void testNormalizePathImprovement() {
        for (String st : toCheckBlank) {
            assertEquals(normalizePathBefore(st), PathUtils.normalizePath(st));
        }
        for (String st : toCheckABC) {
            assertEquals(normalizePathBefore(st), PathUtils.normalizePath(st));
            assertEquals("a/b/c", PathUtils.normalizePath(st));
        }
    }

    public void testGetFileNameImprovement() {
        for (String st : toCheckBlank) {
            assertEquals(getFileNameBefore(st), PathUtils.getFileName(st));
        }
        for (String st : toCheckABC) {
            assertEquals(getFileNameBefore(st), PathUtils.getFileName(st));
            assertEquals("c", PathUtils.getFileName(st));
        }
        assertEquals("ccccccc", PathUtils.getFileName("///aaaa///b/bb/b/b/b////d/d/sa/scccc/////ccccccc/////"));
    }


    public void testGetFileName() {
        assertEquals("hello.world", PathUtils.getFileName("hello.world"));
        assertEquals("hello.world", PathUtils.getFileName("/hello.world"));
        assertEquals("hello.world", PathUtils.getFileName("//hello.world"));
        assertEquals("hello.world", PathUtils.getFileName("/x/hello.world"));
        assertEquals("hello.world", PathUtils.getFileName("x/x/hello.world"));
    }

    @Test(dataProvider = "urls")
    public void testNormalizeRegistryUrl(String urlBefore, String expectedUrl) {
        assertEquals(PathUtils.normalizeUrl(urlBefore), expectedUrl);
    }

    @DataProvider
    public Object[][] urls() {
        return new Object[][]{
                {"https://localhost:8080", "https://localhost:8080"},
                {"https://localhost:8080/", "https://localhost:8080"},
                {"https://localhost:8080//", "https://localhost:8080"},
                {"https://localhost:8080///", "https://localhost:8080"},
                {"http://www.google.com", "http://www.google.com"},
                {"https://www.google.com", "https://www.google.com"},
                {"http://www.google.com/", "http://www.google.com"},
                {"https://www.google.com/", "https://www.google.com"},
                {"www.google.com", "www.google.com"},
                {"www.goo%3Dgle.com", "www.goo%3Dgle.com"},
                {"www.google.com/asdf/asdf/asdf", "www.google.com/asdf/asdf/asdf"},
                {"www.google.com/asdf/asdf/asdf/", "www.google.com/asdf/asdf/asdf"},
                {"", ""},
                {"aaaaaaa", "aaaaaaa"},
                {"128.9.91", "128.9.91"},
                {"http://128.9.91", "http://128.9.91"},
                {"http://128.9.91:443", "http://128.9.91:443"},
                {"http://128.9.91:8080", "http://128.9.91:8080"},
                {"http://128.9.91:8080/", "http://128.9.91:8080"},
                {"http://128.9.91:8080//", "http://128.9.91:8080"},
                {"https://128.9.91:8080//", "https://128.9.91:8080"},
                {"http://128.9.91:8080/test/a/b/c/", "http://128.9.91:8080/test/a/b/c"},
                {"יועז.com", "יועז.com"},

        };
    }

    public void testSplitZipResourcePathIfExist() {
        String[] expected = {"commons-lang/commons-lang/2.6/commons-lang-2.6.jar", "META-INF/LICENSE.txt"};
        String[] actual = PathUtils.splitZipResourcePathIfExist(
                expected[0] + "!/" + expected[1],
                false);
        assertEquals(actual[0], expected[0]);
        assertEquals(actual[1], expected[1]);
    }

    @Test(dataProvider = "getPaths")
    public void testDontSplitPathIfNotZipResource(String path) {
        String[] expected = {path};
        String[] actual = PathUtils.splitZipResourcePathIfExist(expected[0], false);
        assertEquals(actual.length, expected.length);
        assertEquals(actual[0], expected[0]);
    }

    @DataProvider
    public static Object[][] getPaths() {
        return new Object[][]{
                {"folder/Downloads.!!!"},
                {"folde!r/file.rotem"},
                {"conda-forge/osx-64/x264-1!152.20180717-h470a237_0.tar.bz2"}};
    }
}
