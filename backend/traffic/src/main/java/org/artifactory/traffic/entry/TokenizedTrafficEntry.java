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

package org.artifactory.traffic.entry;

import org.apache.commons.lang.StringUtils;
import org.artifactory.traffic.TrafficAction;

import java.util.Arrays;

/**
 * The main implementation of a string tokenized TrafficEntry
 *
 * @author Noam Tenne
 */
public abstract class TokenizedTrafficEntry extends TrafficEntryBase {

    /**
     * Separates the columns of the textual entries
     */
    public static final String COLUMN_SEPARATOR = "|";


    protected TokenizedTrafficEntry(long duration) {
        super(duration);
    }

    /**
     * Parses the given textual entry and sets the object fields accordingly
     */
    public TokenizedTrafficEntry() {
    }

    @Override
    public String toString() {
        String[] tokens = initTokens();

        return buildEntry(tokens);
    }

    abstract String[] initTokens();

    /**
     * Returns the number of info columns this type of entry should hold
     *
     * @return int - Entry's number of info columns
     */
    protected abstract int getColumnsCount();

    /**
     * Builds and returns a textual entry using the entry fields
     *
     * @return String - Textual entry
     */
    static private String buildEntry(String[] tokens) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            builder.append(token);
            if (i != (tokens.length - 1)) {
                builder.append(COLUMN_SEPARATOR);
            }
        }
        return builder.toString();
    }

    /**
     * Parses a textual entry and sets the entry fields accordingly
     *
     * @param entry - Textual entry
     */
    protected String[] parseEntry(String entry) {
        if (StringUtils.isEmpty(entry)) {
            throw new IllegalArgumentException("Entry is empty");
        }

        String[] tokens = StringUtils.split(entry, COLUMN_SEPARATOR);


        if (tokens.length == 0) {
            throw new IllegalArgumentException("No tokens found in entry");
        }

        TrafficAction action = getAction();
        // check length fits
        boolean valid = (tokens.length == getColumnsCount());
        if (!valid && ( TrafficAction.UPLOAD.equals(action) || TrafficAction.DOWNLOAD.equals(action) ) ) {
            // old deprecated entry without IP field.
            valid = ((getColumnsCount() - 1) == tokens.length);
        }

        if (!valid) {
            throw new IllegalArgumentException(
                    "Number of entry columns does not match the entry specification." +
                            "Expected: " + getColumnsCount() + " got: " + tokens.length + " tokens: " +
                            Arrays.toString(tokens));
        }

        // Date should always be located in the first token
        String dateToken = tokens[0];
        time = ENTRY_DATE_FORMATTER.parseMillis(dateToken);
        // Duration should always be located in the second token
        String durationToken = tokens[1];
        try {
            duration = Long.parseLong(durationToken);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid duration token: " + durationToken);
        }
        return tokens;
    }
}
