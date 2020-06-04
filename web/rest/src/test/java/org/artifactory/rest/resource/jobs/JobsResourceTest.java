package org.artifactory.rest.resource.jobs;

import org.apache.commons.io.IOUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.jobs.JobStatus;
import org.artifactory.addon.replication.ReplicationAddon;
import org.artifactory.addon.replication.ReplicationStrategy;
import org.artifactory.addon.replication.ReplicationType;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.rest.common.exception.mapper.BadRequestExceptionMapper;
import org.artifactory.test.ArtifactoryHomeStub;
import org.artifactory.version.ArtifactoryVersion;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.http.HttpStatus.*;
import static org.artifactory.api.rest.constant.JobsConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;


/**
 * @author Yoaz Menda
 */
public class JobsResourceTest extends JerseyTestNg.ContainerPerClassTest {

    @Mock
    private AddonsManager addonsManager;
    @Mock
    private ReplicationAddon replicationAddon;

    private ArtifactoryHomeStub homeStub;

    @Override
    protected Application configure() {
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.refresh();
        MockitoAnnotations.initMocks(this);
        when(addonsManager.addonByType(ReplicationAddon.class)).thenReturn(replicationAddon);
        when(replicationAddon
                .getReplicationJobs(anyString(), anyString(), any(JobStatus.class), any(ReplicationType.class), any(
                        ReplicationStrategy.class), anyString(),
                        anyString())).thenReturn(
                Collections.EMPTY_LIST);
        final ResourceConfig resourceConfig = new ResourceConfig()
                .register(new JobsResource(addonsManager))
                .register(new BadRequestExceptionMapper());
        resourceConfig.property("contextConfig", context);
        return resourceConfig;
    }

    @BeforeClass
    public void setup() {
        ArtifactoryHome.bind(getOrCreateArtifactoryHomeStub());
    }

    @AfterClass
    public void tearDown() {
        ArtifactoryHome.unbind();
    }

    private ArtifactoryHomeStub getOrCreateArtifactoryHomeStub() {
        if (homeStub == null) {
            homeStub = new ArtifactoryHomeStub();
            loadAndBindArtifactoryProperties(homeStub);
        }
        return homeStub;
    }

    private void loadAndBindArtifactoryProperties(ArtifactoryHomeStub artifactory) {
        artifactory.loadSystemProperties();
        artifactory.setProperty(ConstantValues.artifactoryVersion, ArtifactoryVersion.getCurrent().getVersion());
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new InMemoryTestContainerFactory();
    }

    @Test(dataProvider = "options")
    public void testReplicationResourceOK(String status, String type, String strategy, String sourceRepoKey,
            String targetURL,
            String from, String to) {
        Response response = target("/" + JOBS + "/" + REPLICATION_JOBS)
                .queryParam(STARTED_AFTER, from)
                .queryParam(FINISHED_BEFORE, to)
                .queryParam(JOB_STATUS, status)
                .queryParam(REPLICATION_TYPE, type)
                .queryParam(REPLICATION_STRATEGY, strategy)
                .queryParam(SOURCE_REPO, sourceRepoKey)
                .queryParam(TARGET_URL, targetURL)
                .request().get();
        assertNotNull(response);
        assertEquals(response.getStatus(), SC_OK);
    }

    @Test
    public void testJobStatusInValid() {
        Response response = target("/" + JOBS + "/" + REPLICATION_JOBS)
                .queryParam(STARTED_AFTER, "2014-12-12T10:39:40Z")
                .queryParam(FINISHED_BEFORE, "2014-12-12T10:45:40Z")
                .queryParam(REPLICATION_TYPE, "PUSH")
                .queryParam(SOURCE_REPO, "libs-release")
                .queryParam(TARGET_URL, "http://artifactory.com:445/artifactory/libs-release")
                .queryParam(JOB_STATUS, "WHAT_IS_THIS!!!")
                .request().get();
        assertNotNull(response);
        assertEquals(response.getStatus(), SC_NOT_FOUND);
    }

    @Test
    public void testStartedAfterIstooEarly() throws IOException {
        Response response = target("/" + JOBS + "/" + REPLICATION_JOBS)
                .queryParam(STARTED_AFTER, "1999-12-12T10:39:40Z")
                .queryParam(FINISHED_BEFORE, "2014-12-12T10:45:40Z")
                .queryParam(REPLICATION_TYPE, "PUSH")
                .queryParam(SOURCE_REPO, "libs-release")
                .queryParam(TARGET_URL, "http://artifactory.com:445/artifactory/libs-release")
                .queryParam(JOB_STATUS, "RUNNING")
                .request().get();
        assertNotNull(response);
        assertEquals(response.getStatus(), SC_BAD_REQUEST);
        ByteArrayInputStream byteArrayInputStream = (ByteArrayInputStream) (response.getEntity());
        String result = IOUtils.toString(byteArrayInputStream);
        Pattern pattern = Pattern.compile(".*'" + STARTED_AFTER + "' parameter must not be under 7 days from now.*");
        Matcher matcher = pattern.matcher(result);
        assertTrue(matcher.matches());
    }

    @Test
    public void testReplicationTypeInValid() {
        Response response = target("/" + JOBS + "/" + REPLICATION_JOBS)
                .queryParam(STARTED_AFTER, "2014-12-12T10:39:40Z")
                .queryParam(FINISHED_BEFORE, "2014-12-12T10:45:40Z")
                .queryParam(REPLICATION_TYPE, "PUSH")
                .queryParam(SOURCE_REPO, "libs-release")
                .queryParam(TARGET_URL, "http://artifactory.com:445/artifactory/libs-release")
                .queryParam(JOB_STATUS, "Running")
                .queryParam(REPLICATION_TYPE, "fdsa")
                .request().get();
        assertNotNull(response);
        assertEquals(response.getStatus(), SC_NOT_FOUND);
    }

    @Test
    public void testReplicationStrategyInValid() {
        Response response = target("/" + JOBS + "/" + REPLICATION_JOBS)
                .queryParam(STARTED_AFTER, "2014-12-12T10:39:40Z")
                .queryParam(FINISHED_BEFORE, "2014-12-12T10:45:40Z")
                .queryParam(REPLICATION_TYPE, "PUSH")
                .queryParam(SOURCE_REPO, "libs-release")
                .queryParam(TARGET_URL, "http://artifactory.com:445/artifactory/libs-release")
                .queryParam(JOB_STATUS, "RUNNING")
                .queryParam(REPLICATION_STRATEGY, "asdhfg")
                .request().get();
        assertNotNull(response);
        assertEquals(response.getStatus(), SC_NOT_FOUND);
    }

    @Test
    public void testReplicationToAfterFrom() {
        Response response = target("/" + JOBS + "/" + REPLICATION_JOBS)
                .queryParam(FINISHED_BEFORE, "2014-12-12T10:39:40Z")
                .queryParam(STARTED_AFTER, "2014-12-12T10:45:40Z")
                .queryParam(REPLICATION_TYPE, "PUSH")
                .queryParam(SOURCE_REPO, "libs-release")
                .queryParam(TARGET_URL, "http://artifactory.com:445/artifactory/libs-release")
                .queryParam(JOB_STATUS, "RUNNING")
                .request().get();
        assertNotNull(response);
        assertEquals(response.getStatus(), SC_BAD_REQUEST);
    }

    @Test
    public void testReplicationBadTimeFormat() {
        Response response = target("/" + JOBS + "/" + REPLICATION_JOBS)
                .queryParam(STARTED_AFTER, "this is not parseable")
                .queryParam(FINISHED_BEFORE, "2014-12-12T10:45:40Z")
                .queryParam(REPLICATION_TYPE, "PUSH")
                .queryParam(SOURCE_REPO, "libs-release")
                .queryParam(TARGET_URL, "http://artifactory.com:445/artifactory/libs-release")
                .queryParam(JOB_STATUS, "RUNNING")
                .request().get();
        assertNotNull(response);
        assertEquals(response.getStatus(), SC_BAD_REQUEST);
        response = target("/" + JOBS + "/" + REPLICATION_JOBS)
                .queryParam(STARTED_AFTER, "2014-12-12T10:45:40Z")
                .queryParam(FINISHED_BEFORE, "this is not parseable")
                .queryParam(REPLICATION_TYPE, "PUSH")
                .queryParam(SOURCE_REPO, "libs-release")
                .queryParam(TARGET_URL, "http://artifactory.com:445/artifactory/libs-release")
                .queryParam(JOB_STATUS, "RUNNING")
                .request().get();
        assertNotNull(response);
        assertEquals(response.getStatus(), SC_BAD_REQUEST);
    }

    @Test
    public void testReplicationResourceNoFrom() {
        Response response = target("/" + JOBS + "/" + REPLICATION_JOBS)
                .queryParam(FINISHED_BEFORE, "2014-12-12T10:45:40Z")
                .queryParam(REPLICATION_TYPE, "PUSH")
                .queryParam(SOURCE_REPO, "libs-release")
                .queryParam(TARGET_URL, "http://artifactory.com:445/artifactory/libs-release")
                .queryParam(JOB_STATUS, "RUNNING")
                .request().get();
        assertNotNull(response);
        assertEquals(response.getStatus(), SC_BAD_REQUEST);
    }

    //status, type, sourceRepoKey, targetURL, from, to
    @DataProvider(name = "options")
    public Object[][] options() {
        String startedAfter = Instant.now().minusMillis(5 * 1000).toString();
        String finishedBefore = Instant.now().toString();
        return new Object[][]{
                {"RUNNING", "PUSH", "FULL", "libs-release", "http://localhost:8080/artifactory/libs-release", startedAfter, finishedBefore},
                {"FINISHED", "PUSH", "FULL", "libs-release", "http://localhost:8080/artifactory/libs-release", startedAfter, finishedBefore},
                {"STOPPED", "PUSH", "FULL", "libs-release", "http://localhost:8080/artifactory/libs-release", startedAfter, finishedBefore},
                {"STOPPED", "PUSH", "FULL", "libs-release", "http://localhost:8080/artifactory/libs-release", startedAfter, finishedBefore},
                {"STOPPED", "PUSH", "FULL", "libs-release", "http://localhost:8080/artifactory/libs-release", startedAfter, ""},
                {"", "PUSH", "FULL", "libs-release", "http://localhost:8080/artifactory/libs-release", startedAfter, finishedBefore},
                {"STOPPED", "", "FULL", "", "http://localhost:8080/artifactory/libs-release", startedAfter, finishedBefore},
                {"STOPPED", "PUSH", "FULL", "libs-release", "", startedAfter, finishedBefore},
                {"RUNNING", "PULL", "FULL", "libs-release", "http://localhost:8080/artifactory/libs-release", startedAfter, finishedBefore},
                {"FINISHED", "PULL", "FULL", "libs-release", "http://localhost:8080/artifactory/libs-release", startedAfter, finishedBefore},
                {"STOPPED", "PULL", "FULL", "libs-release", "http://localhost:8080/artifactory/libs-release", startedAfter, finishedBefore},
                {"", "", "FULL", "", "", startedAfter, ""},
                {"RUNNING", "PUSH", "INCREMENTAL", "libs-release", "http://localhost:8080/artifactory/libs-release", startedAfter, finishedBefore},
                {"FINISHED", "PUSH", "INCREMENTAL", "libs-release", "http://localhost:8080/artifactory/libs-release", startedAfter, finishedBefore},
                {"STOPPED", "PUSH", "INCREMENTAL", "libs-release", "http://localhost:8080/artifactory/libs-release", startedAfter, finishedBefore},
                {"STOPPED", "PUSH", "INCREMENTAL", "libs-release", "http://localhost:8080/artifactory/libs-release", startedAfter, finishedBefore},
                {"STOPPED", "PUSH", "INCREMENTAL", "libs-release", "http://localhost:8080/artifactory/libs-release", startedAfter, ""},
                {"", "PUSH", "INCREMENTAL", "libs-release", "http://localhost:8080/artifactory/libs-release", startedAfter, finishedBefore},
                {"STOPPED", "", "INCREMENTAL", "", "http://localhost:8080/artifactory/libs-release", startedAfter, finishedBefore},
                {"STOPPED", "PUSH", "INCREMENTAL", "libs-release", "", startedAfter, finishedBefore},
                {"RUNNING", "PULL", "INCREMENTAL", "libs-release", "http://localhost:8080/artifactory/libs-release", startedAfter, finishedBefore},
                {"FINISHED", "PULL", "", "libs-release", "http://localhost:8080/artifactory/libs-release", startedAfter, finishedBefore},
                {"STOPPED", "PULL", "INCREMENTAL", "libs-release", "http://localhost:8080/artifactory/libs-release", startedAfter, finishedBefore},
                {"", "", "INCREMENTAL", "", "", startedAfter, ""},

        };
    }

}