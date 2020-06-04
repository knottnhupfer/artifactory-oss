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

package org.artifactory.aql.result;

import org.apache.commons.lang.StringUtils;
import org.artifactory.aql.AqlException;
import org.artifactory.aql.action.AqlAction;
import org.artifactory.aql.action.AqlActionException;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlRepoProvider;
import org.artifactory.aql.model.DomainSensitiveField;
import org.artifactory.aql.result.rows.AqlRowResult;
import org.artifactory.aql.result.rows.InflatableRow;
import org.artifactory.aql.result.rows.populate.PhysicalFieldResultPopulators;
import org.artifactory.aql.result.rows.populate.ResultPopulationContext;
import org.artifactory.aql.result.rows.populate.RowPopulation;
import org.artifactory.aql.result.rows.populate.RowPopulationContext;
import org.artifactory.aql.util.AqlUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

/**
 * @author Gidi Shabat
 *         The class converts the AqlLazyResult to in-memory Aql Json result
 *         The Max number of rows allowed by this result is the actually artifactory searchUserQueryLimit:
 *         (ConstantValues.searchUserQueryLimit)
 */
public class AqlJsonStreamer extends AqlRestResult implements Cloneable {
    private static final Logger log = LoggerFactory.getLogger(AqlJsonStreamer.class);

    private static final String QUERY_PREFIX = "\n{\n\"results\" : [ ";
    private static final String NUMBER_OF_ROWS = "<NUMBER_OF_ROWS>";
    private static final String QUERY_POSTFIX = " ],\n\"range\" : " + NUMBER_OF_ROWS + "\n}\n";
    private final ResultSet resultSet;
    private final List<DomainSensitiveField> fields;
    private final long limit;
    private final long offset;
    private final AqlRepoProvider repoProvider;
    private final AqlDomainEnum domain;
    private final boolean skipResultWrapper; //used for the user plugin where we don't want the wrappers as a part of the JSON
    private final ObjectMapper mapper;
    private final AqlAction action;
    private long rowsCount;
    private Buffer buffer = new Buffer();
    private boolean ended;
    private String mainId = null;
    private Row mainRow;

    public AqlJsonStreamer(AqlLazyResult<? extends AqlRowResult> lazyResult) {
        this(lazyResult, false);
    }

    public AqlJsonStreamer(AqlLazyResult<? extends AqlRowResult> lazyResult, boolean skipResultWrapper) {
        super(lazyResult.getPermissionProvider());
        repoProvider = lazyResult.getRepoProvider();
        resultSet = lazyResult.getResultSet();
        fields = lazyResult.getFields();
        limit = lazyResult.getLimit();
        offset = lazyResult.getOffset();
        domain = lazyResult.getDomain();
        action = lazyResult.getAction();
        mapper = createObjectMapper();
        this.skipResultWrapper = skipResultWrapper;
        if (!skipResultWrapper) {
            buffer.push(QUERY_PREFIX.getBytes());
        }
    }

    /**
     * Read the ResultSet from db:
     * 1. In case of multi domain result the method merge multi rows into Json multi layer json result
     * 2. In case of single domain result the class convert single row into flat json.
     */
    private Row inflateRow() {
        try {
            ResultPopulationContext resultContext = new ResultPopulationContext(resultSet, fields, repoProvider);
            while (resultSet.next()) {
                InflatableRow row = new InflatableRow();
                RowPopulationContext populationContext = new RowPopulationContext(resultContext, row);
                RowPopulation.populatePhysicalFields(populationContext, PhysicalFieldResultPopulators.forJson);
                RowPopulation.populateLogicalFields(populationContext);
                if (!canRead(domain, resultSet)) {
                    continue;
                }
                Map<String, Row> map = row.inflate();
                String newId = map.keySet().iterator().next();
                Row newRow = map.values().iterator().next();
                if (mainId == null) {
                    mainId = newId;
                    mainRow = newRow;
                } else {
                    if (!mainId.equals(newId)) {
                        Row temp = mainRow;
                        mainRow = newRow;
                        mainId = newId;
                        Row currentRow = actOnRow(temp.build());
                        if (currentRow != null) {
                            return currentRow;
                        }
                    } else {
                        mainRow.merge(newRow);
                    }
                }
            }
        } catch (Exception e) {
            throw new AqlException("Failed to fetch Aql result", e);
        }
        if (mainRow != null) {
            Row row = mainRow;
            mainRow = null;
            return actOnRow(row.build());
        } else {
            return null;
        }
    }

    /**
     * Performs the {@link AqlAction} this streamer was inited with.
     * @return the current row if the action was successful, null otherwise.
     */
    @Nullable
    private Row actOnRow(Row row) {
        try {
            return action.doAction(row);
        } catch (AqlActionException aae) {
            String warn = (action.isDryRun() ? "** AQL dry run mode ** " : "") + aae.getReason().code + " : " + aae.getMessage();
            log.warn(warn);
            log.debug("", aae);
        } catch (Exception e) {
            log.warn(String.format("Failed to execute action %s : {}", action.getName()), e.getMessage());
            log.debug("", e);
        }
        // Don't return rows that failed the action.
        return null;
    }

    /**
     * Reads Single row from The Json result
     * The method return null to signal end of stream
     *
     * @return Json row in byte array
     */
    @Override
    public byte[] read() {
        // Use the data in the buffer, reloading  the buffer is allowed only if it is empty
        if (!buffer.isEmpty()) {
            return buffer.getData();
        }
        // Fill the buffer from result-set
        byte[] data;
        if ((data = getNewRowFromDb()) != null) {
            rowsCount++;
            buffer.push(data);
            return buffer.getData();
        }
        // Fill the buffer from post fix
        if (!ended && !skipResultWrapper) {
            appendEndSection();
            return buffer.getData();
        }
        return null;
    }

    private void appendEndSection() {
        try {
            if (!ended) {
                String range = generateRangeJson();
                String summary = StringUtils.replace(QUERY_POSTFIX, NUMBER_OF_ROWS, "" + range);
                buffer.push(summary.getBytes());
                ended = true;
            }
        } catch (IOException e) {
            log.error("Failed to generate Aql result summery.", e);
        }
    }

    public long getLimit() {
        return limit;
    }

    public long getOffset() {
        return offset;
    }

    public long getRowsCount() {
        return rowsCount;
    }

    private String generateRangeJson() throws IOException {
        Range range = new Range(offset, rowsCount, rowsCount, limit);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(range);
    }

    private byte[] getNewRowFromDb() {
        boolean isFirstElement = mainId == null;
        Row row = inflateRow();
        if (row != null) {
            try {
                String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(row);
                json = isFirstElement || skipResultWrapper ? "" + json : "," + json;
                return json.getBytes();
            } catch (Exception e) {
                throw new AqlException("Failed to convert Aql Result to JSON", e);
            }
        }
        return null;
    }

    @Override
    public void close() {
        AqlUtils.closeResultSet(resultSet);
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.getSerializationConfig().withSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        mapper.setVisibility(JsonMethod.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
        return mapper;
    }

    /**
     * Simplify the work with the stream during the read
     */
    private class Buffer {
        private byte[] buffer;

        public void push(byte[] bytes) {
            buffer = bytes;
        }

        public byte[] getData() {
            byte[] temp = buffer;
            buffer = null;
            return temp;
        }

        public boolean isEmpty() {
            return buffer == null;
        }
    }

}