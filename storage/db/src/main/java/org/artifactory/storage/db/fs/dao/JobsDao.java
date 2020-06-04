package org.artifactory.storage.db.fs.dao;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.artifactory.addon.jobs.JobsQuery;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.storage.db.util.querybuilder.ArtifactoryQueryWriter;
import org.jfrog.storage.util.DbUtils;
import org.jfrog.storage.util.querybuilder.QueryWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.artifactory.common.ConstantValues.jobsTableFetchLimit;


/**
 * @author Yoaz Menda
 */
@Repository
public class JobsDao extends BaseDao {

    private static final String JOBS_TABLE = "jobs";

    @Autowired
    public JobsDao(JdbcHelper jdbcHelper) {
        super(jdbcHelper);
    }

    public List<JobRecord> getJobs(JobsQuery jobsQuery) throws SQLException {
        ResultSet resultSet = null;
        List<JobRecord> entries = Lists.newArrayList();
        try {
            resultSet = executeQuery(jobsQuery);
            while (resultSet.next()) {
                entries.add(jobFromResultSet(resultSet));
            }
            return entries;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public void create(long jobId, String jobType, String jobStatus, Long started, Long finished,
            String additionalDetails) throws SQLException {
        jdbcHelper.executeUpdate("INSERT INTO " + JOBS_TABLE + " " +
                "(job_id, job_type, job_status, started, finished, additional_details) " +
                "VALUES(?, ?, ?, ?, ?, ?)", jobId, jobType, jobStatus, started, finished, additionalDetails);
    }

    public int deleteJobsStartedBefore(long startedBefore) throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM " + JOBS_TABLE + " WHERE started < ?", startedBefore);
    }

    private JobRecord jobFromResultSet(ResultSet resultSet) throws SQLException {
        return new JobRecord(
                resultSet.getLong("job_id"),
                resultSet.getString("job_type"),
                resultSet.getString("job_status"),
                resultSet.getLong("started"),
                resultSet.getLong("finished"),
                resultSet.getString("additional_details"));
    }

    public JobRecord getJob(long jobId) throws SQLException {
        ResultSet resultSet = null;
        JobRecord job = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT * from " + JOBS_TABLE + " WHERE job_id = ?", jobId);
            if (resultSet.next()) {
                job = jobFromResultSet(resultSet);
            }
            return job;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public void updateJob(long jobId, String newStatus, String newAdditionalDetails) throws SQLException {
        jdbcHelper.executeUpdate("UPDATE " + JOBS_TABLE + " SET" +
                        " finished = ?, additional_details = ?, job_status = ?" +
                        " WHERE job_id = ?",
                Instant.now().toEpochMilli(), newAdditionalDetails, newStatus, jobId);
    }

    private ResultSet executeQuery(JobsQuery jobsQuery) throws SQLException {
        QueryWriter sql = new ArtifactoryQueryWriter()
                .from(" " + JOBS_TABLE + " ")
                .orderBy(" started Desc ")
                .limit(jobsTableFetchLimit.getLong());
        List<Pair<String, Object>> conditions = extractConditions(jobsQuery);
        if (conditions.isEmpty()) {
            return jdbcHelper.executeSelect(sql.build());
        }
        StringBuilder where = new StringBuilder(" ");
        Object[] whereParams = new Object[conditions.size()];
        boolean isFirst = true;
        Iterator<Pair<String, Object>> iterator = conditions.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            if (!isFirst) {
                where.append(" AND ");
            }
            Pair<String, Object> next = iterator.next();
            where.append(next.getLeft());
            whereParams[i++] =next.getRight();
            isFirst = false;
        }
        sql.where(where.toString());
        return jdbcHelper.executeSelect(sql.build(), whereParams);
    }

    private List<Pair<String, Object>> extractConditions(JobsQuery jobsQuery) {
        List<Pair<String, Object>> params = new ArrayList<>();
        if (jobsQuery.getFinishedBefore() != null) {
            params.add(Pair.of(" ((finished is NOT NULL) AND (finished < ?)) ", jobsQuery.getFinishedBefore().toEpochMilli()));
        }
        if (jobsQuery.getStartedAfter() != null) {
            params.add(Pair.of(" started >= ? ", jobsQuery.getStartedAfter().toEpochMilli()));
        }
        if (jobsQuery.getJobStatus() != null) {
            params.add(Pair.of(" job_status = ? ", jobsQuery.getJobStatus().toString()));
        }
        if (jobsQuery.getJobType() != null) {
            params.add(Pair.of(" job_type = ? ", jobsQuery.getJobType().toString()));
        }
        return params;
    }
}
