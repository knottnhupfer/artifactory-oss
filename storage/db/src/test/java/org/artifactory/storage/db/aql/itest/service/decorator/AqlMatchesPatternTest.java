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

package org.artifactory.storage.db.aql.itest.service.decorator;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Yinon Avraham
 */
public class AqlMatchesPatternTest {

    @Test
    public void testStar() {
        AqlMatchesPattern pattern = AqlMatchesPattern.compile("*");
        assertTrue(pattern.matches(""));
        assertTrue(pattern.matches("foo"));
        assertTrue(pattern.matches("bar foo"));
        assertTrue(pattern.matches("bla+bla="));
    }

    @Test
    public void testStartsWith() {
        AqlMatchesPattern pattern = AqlMatchesPattern.compile("abc*");
        assertTrue(pattern.matches("abc"));
        assertTrue(pattern.matches("abcd"));
        assertTrue(pattern.matches("abc def"));
        assertFalse(pattern.matches(""));
        assertFalse(pattern.matches(" abc"));
        assertFalse(pattern.matches(" abcd"));
    }

    @Test
    public void testEndsWith() {
        AqlMatchesPattern pattern = AqlMatchesPattern.compile("*abc");
        assertTrue(pattern.matches("abc"));
        assertTrue(pattern.matches("dabc"));
        assertTrue(pattern.matches("def abc"));
        assertFalse(pattern.matches(""));
        assertFalse(pattern.matches("abc "));
        assertFalse(pattern.matches("dabc "));
    }

    @Test
    public void testContains() {
        AqlMatchesPattern pattern = AqlMatchesPattern.compile("*abc*");
        assertTrue(pattern.matches("abc"));
        assertTrue(pattern.matches("dabc"));
        assertTrue(pattern.matches("abcd"));
        assertTrue(pattern.matches(" abc "));
        assertTrue(pattern.matches("foo abc bar"));
        assertFalse(pattern.matches(""));
        assertFalse(pattern.matches("a bc "));
        assertFalse(pattern.matches(" dab c"));
    }

    @Test
    public void testQuestionMark() {
        AqlMatchesPattern pattern = AqlMatchesPattern.compile("?");
        assertTrue(pattern.matches(" "));
        assertTrue(pattern.matches("a"));
        assertTrue(pattern.matches("1"));
        assertTrue(pattern.matches(""));
        assertFalse(pattern.matches("ab"));
        assertFalse(pattern.matches("foo bar"));
    }

    @Test
    public void testFixedString() {
        AqlMatchesPattern pattern = AqlMatchesPattern.compile("abc");
        assertTrue(pattern.matches("abc"));
        assertFalse(pattern.matches("a"));
        assertFalse(pattern.matches("ab"));
        assertFalse(pattern.matches("abc "));
        assertFalse(pattern.matches(" abc"));
        assertFalse(pattern.matches("1abcd"));
    }

    @Test
    public void testMixedPattern() {
        AqlMatchesPattern pattern = AqlMatchesPattern.compile("foo+bar*is+the+b?st");
        assertTrue(pattern.matches("foo+bar is+the+best"));
        assertTrue(pattern.matches("foo+baris+the+best"));
        assertTrue(pattern.matches("foo+barblablais+the+bist"));
        assertTrue(pattern.matches("foo+bar is+the+bst"));
        assertFalse(pattern.matches(""));
        assertFalse(pattern.matches("foo+bar s+the+best"));
        assertFalse(pattern.matches("foo+bar is+the+best "));
    }

}