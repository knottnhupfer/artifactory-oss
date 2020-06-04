package org.artifactory.build;

import org.artifactory.api.rest.build.ContinueBuildFilter;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.property.ArtifactorySystemProperties;
import org.artifactory.storage.db.build.entity.BuildIdEntity;
import org.artifactory.storage.db.build.service.BuildIdImpl;
import org.glassfish.jersey.internal.guava.Predicates;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 *
 * @author Omri Ziv
 */

@Test
public class BuildServiceUtilsTest {

    private long now = System.currentTimeMillis();

    @Mock
    ArtifactoryHome artifactoryHome;
    @Mock
    ArtifactorySystemProperties artifactorySystemProperties;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ArtifactoryHome.bind(artifactoryHome);
        when(artifactoryHome.getArtifactoryProperties()).thenReturn(artifactorySystemProperties);
        when(artifactorySystemProperties.getLongProperty(any(ConstantValues.class)))
                .thenAnswer(invocationOnMock -> {
                    ConstantValues constantValue = invocationOnMock.getArgument(0);
                    return Long.parseLong(constantValue.getDefValue().trim());
                });
    }

    public void testIterateFetchFromDb() {
        long now = System.currentTimeMillis();
        ContinueBuildFilter continueBuildFilter = new ContinueBuildFilter();
        continueBuildFilter.setLimit(10L);
        List<BuildIdEntity> list = BuildServiceUtils.iterateFetchFromDb(this::fetchFunction1, Predicates.alwaysTrue(),
                    buildIdEntity -> new BuildIdImpl(buildIdEntity.getBuildId(), buildIdEntity.getBuildName(),
                            buildIdEntity.getBuildNumber(), buildIdEntity.getBuildDate()),
                continueBuildFilter);
        assertEquals(10, list.size());

    }

    public void testIterateFetchFromDb0Limit() {
        long now = System.currentTimeMillis();
        ContinueBuildFilter continueBuildFilter = new ContinueBuildFilter();
        continueBuildFilter.setLimit(0L);
        List<BuildIdEntity> list = BuildServiceUtils.iterateFetchFromDb(this::fetchFunction1, Predicates.alwaysTrue(),
                    buildIdEntity -> new BuildIdImpl(buildIdEntity.getBuildId(), buildIdEntity.getBuildName(),
                            buildIdEntity.getBuildNumber(), buildIdEntity.getBuildDate()),
                continueBuildFilter);
        assertEquals(0, list.size());
    }

    public void testIterateFetchFromDbNoResults() {
        long now = System.currentTimeMillis();
        ContinueBuildFilter continueBuildFilter = new ContinueBuildFilter();
        continueBuildFilter.setLimit(0L);
        List<BuildIdEntity> list = BuildServiceUtils.iterateFetchFromDb(x -> Collections.emptyList(), Predicates.alwaysTrue(),
                    buildIdEntity -> new BuildIdImpl(buildIdEntity.getBuildId(), buildIdEntity.getBuildName(),
                            buildIdEntity.getBuildNumber(), buildIdEntity.getBuildDate()),
                continueBuildFilter);
        assertEquals(0, list.size());
    }


    public void testIterateFetchFromDbFilterOdds() {
        long now = System.currentTimeMillis();
        ContinueBuildFilter continueBuildFilter = new ContinueBuildFilter();
        continueBuildFilter.setLimit(10L);
        List<BuildIdEntity> list = BuildServiceUtils.iterateFetchFromDb(this::fetchFunction1, build -> build.getBuildId() % 2 == 0,
                    buildIdEntity -> new BuildIdImpl(buildIdEntity.getBuildId(), buildIdEntity.getBuildName(),
                            buildIdEntity.getBuildNumber(), buildIdEntity.getBuildDate()),
                continueBuildFilter);
        assertEquals(10, list.size());
        assertEquals(2, list.get(0).getBuildId());
        assertEquals(20, list.get(9).getBuildId());

    }

    public void testIterateFetchFromDbFilterOddsStartFrom() {
        long now = System.currentTimeMillis();
        ContinueBuildFilter continueBuildFilter = new ContinueBuildFilter();
        continueBuildFilter.setLimit(10L);
        BuildId continueBuildId = new BuildIdImpl(16, "build16", "16", now);
        continueBuildFilter.setContinueBuildId(continueBuildId);
        List<BuildIdEntity> list = BuildServiceUtils.iterateFetchFromDb(this::fetchFunction1, build -> build.getBuildId() % 2 == 0,
                buildIdEntity -> new BuildIdImpl(buildIdEntity.getBuildId(), buildIdEntity.getBuildName(),
                        buildIdEntity.getBuildNumber(), buildIdEntity.getBuildDate()),
                continueBuildFilter);
        assertEquals(10, list.size());
        assertEquals(18, list.get(0).getBuildId());
        assertEquals(36, list.get(9).getBuildId());

    }


    private List<BuildIdEntity> fetchFunction1(ContinueBuildFilter continueBuildFilter) {
        return IntStream.rangeClosed(1, 100)
                .mapToObj(number -> new BuildIdEntity(number, "build-" + number, number + "", now))
                .filter(buildIdEntity -> {
                    return continueBuildFilter.getContinueBuildId() == null ||
                            Integer.parseInt(buildIdEntity.getBuildNumber()) > Integer.parseInt(continueBuildFilter.getContinueBuildId().getNumber());
                })
                .limit(continueBuildFilter.getLimit())
                .collect(Collectors.toList());
    }

}
