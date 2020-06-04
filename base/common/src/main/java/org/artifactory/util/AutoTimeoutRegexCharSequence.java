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

import javax.annotation.Nonnull;

/**
 * A {@link CharSequence} that has the ability to abort an operation running on it (i.e. regex matching) after the
 * specified {@param timeoutMillis} has passed.
 *
 * @author Dan Feldman
 */
public class AutoTimeoutRegexCharSequence implements CharSequence {
    private final CharSequence inner;
    private final int timeoutMillis;
    private final long timeoutTime;
    private final String stringToMatch;
    private final String regexPattern;

    public AutoTimeoutRegexCharSequence(CharSequence inner, String stringToMatch, String regexPattern,
            int timeoutMillis) {
        super();
        this.inner = inner;
        this.timeoutMillis = timeoutMillis;
        this.stringToMatch = stringToMatch;
        this.regexPattern = regexPattern;
        timeoutTime = System.currentTimeMillis() + timeoutMillis;
    }

    public char charAt(int index) {
        long currentTime = System.currentTimeMillis();
        if (currentTime > timeoutTime) {
            throw new IllegalStateException("Timeout occurred after " + (currentTime - timeoutMillis) + " ms while " +
                    "processing regex '" + regexPattern + "' on input '" + stringToMatch);
        }
        return inner.charAt(index);
    }

    public int length() {
        return inner.length();
    }

    public CharSequence subSequence(int start, int end) {
        return new AutoTimeoutRegexCharSequence(inner.subSequence(start, end), stringToMatch, regexPattern, timeoutMillis);
    }

    @Override
    @Nonnull
    public String toString() {
        return inner.toString();
    }
}
