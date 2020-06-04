package org.artifactory.storage.jobs.migration.buildinfo;

import org.artifactory.api.jackson.JacksonReader;
import org.artifactory.api.jackson.JacksonWriter;
import org.artifactory.storage.db.build.entity.BuildEntity;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.jfrog.build.api.Build;
import org.jfrog.common.JsonParsingException;
import org.jfrog.common.JsonUtils;
import org.jfrog.common.ResourceUtils;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.fest.assertions.Assertions.assertThat;

@Test
public class BuildInfoAlignmentTest extends ArtifactoryHomeBoundTest {

    private final static BuildEntity buildEntity = Mockito.mock(BuildEntity.class);
    private final static String BUILDS_PATH = "/org/artifactory/buildinfo/";
    private final static Logger logger = LoggerFactory.getLogger(BuildInfoAlignmentTest.class);

    public void testJsonAlignmentNormalProps() {
        Build build = getBuild("normal-props.json", true);
        assertThat(build).isNotNull();
        assertThat(build.getProperties().getProperty("p1")).isEqualTo("p1");
        assertThat(build.getProperties().getProperty("p2")).isEqualTo("p2");
        assertThat(build.getProperties().getProperty("p3")).isEqualTo("p3");
    }

    public void testJsonAlignmentMultiTypeProps() {
        Build build = getBuild("multitype-props.json", true);
        assertThat(build).isNotNull();
        assertThat(build.getProperties().getProperty("p1")).isEqualTo("1");
        assertThat(build.getProperties().getProperty("p2")).isEqualTo("2.11");
        assertThat(build.getProperties().getProperty("p3")).isEqualTo("true");
    }

    public void testJsonAlignmentNestedMap() {
        Build build = getBuild("nested-map.json", false);
        assertThat(build).isNotNull();
        assertThat(build.getProperties().getProperty("p1")).isEqualTo("{\"np1\":1}");
    }

    public void testJsonAlignmentWithArray() {
        Build build = getBuild("build-array-prop.json", false);
        assertThat(build.getProperties().getProperty("testing")).isEqualTo("[\"true\"]");
    }

    public void testJsonAlignmentWithProps() {
        Build build = getBuild("build-no-props-empty-map.json", true);
        assertThat(build.getProperties()).isNotNull();
        assertThat(build.getProperties().size()).isEqualTo(0);
    }

    public void testJsonAlignmentWithNullProps() {
        Build build = getBuild("build-null-props.json", true);
        assertThat(build).isNotNull();
        assertThat(build.getProperties()).isNull();
    }

    public void testJsonAlignmentWithNoProps() {
        Build build = getBuild("build-no-props.json", true);
        assertThat(build).isNotNull();
        assertThat(build.getProperties()).isNull();
    }

    public void testJsonAlignmentWithStringArray() {
        Build build = getBuild("build-string-array-prop.json", true);
        assertThat(build).isNotNull();
        assertThat(build.getProperties().getProperty("testing")).isEqualTo("[true]");
    }

    private Build getBuild(String fileName, boolean shouldLog) {
        String buildJson = ResourceUtils.getResourceAsString(BUILDS_PATH + fileName);
        Build build = shouldLog ? JsonUtils.getInstance()
                .readValue(BuildMigrationUtils.fixBuildProperties(buildJson, buildEntity, logger), Build.class) :
                JsonUtils.getInstance()
                        .readValue(BuildMigrationUtils.fixBuildProperties(buildJson, buildEntity, null), Build.class);
        assertThat(buildJson).isNotNull();
        return build;
    }

}