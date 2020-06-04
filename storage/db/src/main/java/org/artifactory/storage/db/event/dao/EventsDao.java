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

package org.artifactory.storage.db.event.dao;

import com.google.common.collect.Lists;
import org.artifactory.storage.db.event.entity.EventRecord;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.storage.db.util.querybuilder.ArtifactoryQueryWriter;
import org.artifactory.storage.event.EventType;
import org.jfrog.common.ResultSetStream;
import org.jfrog.storage.util.DbUtils;
import org.jfrog.storage.util.querybuilder.QueryWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * A data access object for the events table.
 *
 * @author Yossi Shaul
 */
@Repository
public class EventsDao extends BaseDao {

    @Autowired
    public EventsDao(JdbcHelper jdbcHelper) {
        super(jdbcHelper);
    }

    public void create(EventRecord e) throws SQLException {
        jdbcHelper.executeUpdate("INSERT INTO node_events " +
                        "(event_id, timestamp, event_type, path) VALUES(?, ?, ?, ?)",
                e.getEventId(), e.getTimestamp(), e.getType().code(), e.getPath());
    }

    /**
     * Loads all the events newer from the given timestamp
     *
     * @param timestamp Timestamp in millis
     * @return Events newer than the given timestamp ordered by timestamp
     */
    @Nonnull
    public List<EventRecord> loadNewer(long timestamp, String repoKey) throws SQLException {
        return loadEventsSince(timestamp, repoKey, false);
    }

    /**
     * Loads all the events more recent than the given timestamp
     *
     * @param timestamp Timestamp in millis
     * @return Events more recent than the given timestamp ordered by timestamp
     */
    @Nonnull
    public List<EventRecord> loadNewer(long timestamp) throws SQLException {
        return loadEventsSince(timestamp, false);
    }


    /**
     * Loads all the events newer or same as the given timestamp
     *
     * @param timestamp Timestamp in millis
     * @return Events newer than the given timestamp ordered by timestamp
     */
    public List<EventRecord> loadNewerOrSame(long timestamp, String repoKey) throws SQLException {
        return loadEventsSince(timestamp, repoKey, true);
    }

    public List<EventRecord> loadNewerOrSame(long timestamp) throws SQLException {
        return loadEventsSince(timestamp, true);
    }

    @Nonnull
    public List<EventRecord> loadEventsByTimestamp(long timestamp) throws SQLException {
        ResultSet rs = null;
        try {
            List<EventRecord> entries = Lists.newArrayList();
            rs = jdbcHelper.executeSelect("SELECT * FROM node_events " +
                    "WHERE timestamp = ? ORDER BY timestamp ASC, event_id ASC", timestamp);
            while (rs.next()) {
                entries.add(eventFromResultSet(rs));
            }
            return entries;
        } finally {
            DbUtils.close(rs);
        }
    }

    @Nonnull
    private List<EventRecord> loadEventsSince(long timestamp, String repoKey, boolean inclusive) throws SQLException {
        ResultSet rs = null;
        try {
            String comparator = inclusive ? ">=" : ">"; // whether to include or not events with the same timestamp
            List<EventRecord> entries = Lists.newArrayList();
            rs = jdbcHelper.executeSelect("SELECT * FROM node_events " +
                    "WHERE timestamp " + comparator + " ? " +
                    "AND path like ? " +
                    "ORDER BY timestamp ASC, event_id ASC", timestamp, repoKey + "/%");
            while (rs.next()) {
                entries.add(eventFromResultSet(rs));
            }
            return entries;
        } finally {
            DbUtils.close(rs);
        }
    }

    @Nonnull
    private List<EventRecord> loadEventsSince(long timestamp, boolean inclusive) throws SQLException {
        ResultSet rs = null;
        try {
            String comparator = inclusive ? ">=" : ">"; // whether to include or not events with the same timestamp
            List<EventRecord> entries = Lists.newArrayList();
            rs = jdbcHelper.executeSelect("SELECT * FROM node_events " +
                    "WHERE timestamp " + comparator + " ? " +
                    "ORDER BY timestamp ASC, event_id ASC", timestamp);
            while (rs.next()) {
                entries.add(eventFromResultSet(rs));
            }
            return entries;
        } finally {
            DbUtils.close(rs);
        }
    }

    /**
     * Loads all the events newer from the given timestamp
     *
     * @param timestamp Timestamp in millis
     * @return Events newer than the given timestamp ordered by timestamp
     */
    @Nonnull
    public Stream<EventRecord> loadNewerStream(long timestamp, String repoKey,
            CompletableFuture<SQLException> onException) {
        return loadEventsSinceStream(timestamp, repoKey, false, onException);
    }

    /**
     * Loads all the events newer from the fromTimeStamp and older than the toTimestamp
     *
     * @param fromTimestamp Beginning of the interval non-inclusive timestamp in millis
     * @param toTimestamp End of the interval non-inclusive timestamp in millis
     * @return Events newer than the given fromTimestamp and older then the toTimestamp ordered by timestamp
     */
    @Nonnull
    public Stream<EventRecord> loadIntervalStream(long fromTimestamp, long toTimestamp, String repoKey, long limit,
                                               CompletableFuture<SQLException> onException) {
        QueryWriter queryWriter = new ArtifactoryQueryWriter();
        queryWriter.select().from("node_events")
                .where("timestamp > ? AND timestamp < ? AND path like ?")
                .limit(limit)
                .orderBy("timestamp ASC, event_id ASC");
        try {
            ResultSet rs = jdbcHelper.executeSelect(queryWriter.build(), fromTimestamp, toTimestamp, repoKey + "/%");
            return ResultSetStream.asStream(rs, this::eventFromResultSet, onException);
        } catch (SQLException e) {
            onException.complete(e);
            return Stream.empty();
        }
    }


    @Nonnull
    public Stream<EventRecord> loadNewerStream(long timestamp, CompletableFuture<SQLException> onException) {
        return loadEventsSinceStream(timestamp, false, onException);
    }

    private Stream<EventRecord> loadEventsSinceStream(long timestamp, boolean inclusive,
            CompletableFuture<SQLException> onException) {
        String comparator = inclusive ? ">=" : ">"; // whether or not to include events with the same timestamp
        try {
            ResultSet rs = jdbcHelper.executeSelect("SELECT * FROM node_events " +
                    "WHERE timestamp " + comparator + " ? " +
                    "ORDER BY timestamp ASC, event_id ASC", timestamp);
            return ResultSetStream.asStream(rs, this::eventFromResultSet, onException);
        } catch (SQLException e) {
            onException.complete(e);
            return Stream.empty();
        }
    }

    /**
     * Loads all the events newer or same as the given timestamp
     *
     * @param timestamp Timestamp in millis
     * @return Events newer than the given timestamp ordered by timestamp
     */
    public Stream<EventRecord> loadNewerOrSameStream(long timestamp, String repoKey, CompletableFuture<SQLException> onException) {
        return loadEventsSinceStream(timestamp, repoKey, true, onException);
    }

    @Nonnull
    private Stream<EventRecord> loadEventsSinceStream(long timestamp, String repoKey, boolean inclusive, CompletableFuture<SQLException> onException) {
        String comparator = inclusive ? ">=" : ">"; // whether to include or not events with the same timestamp
        try {
            ResultSet rs = jdbcHelper.executeSelect("SELECT * FROM node_events " +
                    "WHERE timestamp " + comparator + " ? " +
                    "AND path like ? " +
                    "ORDER BY timestamp ASC, event_id ASC", timestamp, repoKey + "/%");
            return ResultSetStream.asStream(rs, this::eventFromResultSet, onException);
        } catch (SQLException e) {
            onException.complete(e);
            return Stream.empty();
        }
    }

    public long getEventsCount() throws SQLException {
        return jdbcHelper.executeSelectCount("SELECT COUNT(*) FROM node_events");
    }

    public int countEventsAfter(long timestamp) throws SQLException {
        return jdbcHelper.executeSelectCount("SELECT COUNT(*) FROM node_events WHERE timestamp >= ?", timestamp);
    }


    public long getFirstEventTimestamp() throws SQLException {
        return jdbcHelper.executeSelectLong("SELECT MIN(timestamp) FROM node_events");
    }

    public long getLastEventTimestamp() throws SQLException {
        return jdbcHelper.executeSelectLong("SELECT MAX(timestamp) FROM node_events");
    }

    public List<EventRecord> loadAll() throws SQLException {
        ResultSet rs = null;
        try {
            List<EventRecord> entries = Lists.newArrayList();
            rs = jdbcHelper.executeSelect("SELECT * FROM node_events ORDER BY timestamp ASC, event_id ASC");
            while (rs.next()) {
                entries.add(eventFromResultSet(rs));
            }
            return entries;
        } finally {
            DbUtils.close(rs);
        }
    }

    public Stream<EventRecord> loadAllStream(CompletableFuture<SQLException> onException) throws SQLException {
        return ResultSetStream.asStream(
                jdbcHelper.executeSelect("SELECT * FROM node_events ORDER BY timestamp ASC, event_id ASC"),
                this::eventFromResultSet,
                onException);
    }

    /**
     * Delete all events from the event log. Internal use only!
     */
    public void deleteAll() throws SQLException {
        jdbcHelper.executeUpdate("DELETE FROM node_events");
    }

    /**
     * Delete all events from in range.
     */
    public void deleteRange(long startInclusive, long endExclusive) throws SQLException {
        jdbcHelper.executeUpdate("DELETE FROM node_events WHERE timestamp < ? AND timestamp >= ?", endExclusive, startInclusive);
    }

    private EventRecord eventFromResultSet(ResultSet rs) throws SQLException {
        return new EventRecord(rs.getLong("event_id"), rs.getLong("timestamp"),
                EventType.fromCode(rs.getShort("event_type")), rs.getString("path"));
    }
}
