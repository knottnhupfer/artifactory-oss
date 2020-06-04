package org.artifactory.metadata.service.provider;

import com.google.common.collect.ImmutableList;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.model.xstream.fs.FileInfoImpl;
import org.artifactory.model.xstream.fs.PropertiesImpl;
import org.artifactory.repo.Repo;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.storage.db.event.service.metadata.model.MutableMetadataEntityBOM;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

/**
 * @author Uriah Levy
 */
@Test
public class AbstractMetadataProviderTest {

    public void testExcludedUserProperties() {
        NpmMetadataProvider npmMetadataProvider = new NpmMetadataProvider();
        MutableMetadataEntityBOM bom = new MutableMetadataEntityBOM();
        PropertiesImpl props = new PropertiesImpl();
        props.put("npm.description", "the gift is a cool movie");
        props.put("npm.keywords", "Gordo, Weirdo");
        props.put("npm.name", "Bojangles");

        npmMetadataProvider.userPropsResolver().apply(bom, props, null, null);

        assertEquals(0, bom.getUserProperties().size());

        RpmMetadataProvider rpmMetadataProvider = new RpmMetadataProvider();
        bom = new MutableMetadataEntityBOM();
        props = new PropertiesImpl();
        props.put("rpm.metadata.name", "Robyn");
        props.put("rpm.metadata.summary", "the gift is a strange movie");

        rpmMetadataProvider.userPropsResolver().apply(bom, props, null, null);

        assertEquals(bom.getUserProperties().size(), 0);
    }

    public void testQualifierResolver() {
        NpmMetadataProvider npmMetadataProvider = new NpmMetadataProvider();
        MutableMetadataEntityBOM bom = new MutableMetadataEntityBOM();
        PropertiesImpl props = new PropertiesImpl();
        props.put("npm.name", "@foo/bar");

        npmMetadataProvider.qualifiersResolver().apply(bom, props, null, null);

        assertEquals(1, bom.getQualifiers().size());
        assertEquals(bom.getQualifiers().get("npm_scope"), "foo");
    }

    public void testNpmTagsResolver() {
        NpmMetadataProvider npmMetadataProvider = new NpmMetadataProvider();
        MutableMetadataEntityBOM bom = new MutableMetadataEntityBOM();
        PropertiesImpl props = new PropertiesImpl();
        props.put("npm.keywords", "foo, bar");

        npmMetadataProvider.tagsResolver().apply(bom, props, null, null);

        assertEquals(2, bom.getTags().size());
        assertTrue(bom.getTags().containsAll(ImmutableList.of("foo", "bar")));

        props.put("npm.keywords", "\f\\oo//\\, /ba/r//");
        bom.getTags().clear();
        npmMetadataProvider.tagsResolver().apply(bom, props, null, null);

        assertEquals(2, bom.getTags().size());
        assertTrue(bom.getTags().containsAll(ImmutableList.of("foo", "bar")));
    }

    public void testNugetTagsResolver() {
        NugetMetadataProvider nugetMetadataProvider = new NugetMetadataProvider();
        MutableMetadataEntityBOM bom = new MutableMetadataEntityBOM();
        PropertiesImpl props = new PropertiesImpl();
        props.put("nuget.tags", "foo bar");

        nugetMetadataProvider.tagsResolver().apply(bom, props, null, null);

        assertEquals(2, bom.getTags().size());
        assertTrue(bom.getTags().containsAll(ImmutableList.of("foo", "bar")));
    }

    public void descriptionResolver() {
        NpmMetadataProvider npmMetadataProvider = new NpmMetadataProvider();
        MutableMetadataEntityBOM bom = new MutableMetadataEntityBOM();
        PropertiesImpl props = new PropertiesImpl();
        props.put("npm.description", "darn cool package");

        npmMetadataProvider.packageDescriptionResolver().apply(bom, props, null, null);

        assertEquals(bom.getDescription(), "darn cool package");

        RpmMetadataProvider rpmMetadataProvider = new RpmMetadataProvider();
        bom = new MutableMetadataEntityBOM();
        props = new PropertiesImpl();
        props.put("rpm.metadata.name", "Robyn");
        props.put("rpm.metadata.summary", "the gift is a strange movie");

        rpmMetadataProvider.packageDescriptionResolver().apply(bom, props, null, null);

        assertEquals(bom.getDescription(), "the gift is a strange movie");

        props.clear();
        bom.setDescription(null);
        npmMetadataProvider.packageDescriptionResolver().apply(bom, props, null, null);

        assertNull(bom.getDescription());
    }

    public void testNpmPkgIdResolver() {
        NpmMetadataProvider npmMetadataProvider = new NpmMetadataProvider();
        MutableMetadataEntityBOM bom = new MutableMetadataEntityBOM();
        FileInfoImpl fileInfo = new FileInfoImpl(new RepoPathImpl("npm-local", "express/-/express-1.0.0.tgz"));
        PropertiesImpl props = new PropertiesImpl();
        props.put("npm.name", "express");

        npmMetadataProvider.pkgIdResolver().apply(bom, props, fileInfo, null);

        assertEquals(bom.getPkgid(), "npm://express");

        props.replaceValues("npm.name", ImmutableList.of(new String(new byte[500])));
        npmMetadataProvider.pkgIdResolver().apply(bom, props, fileInfo, null);

        assertTrue(bom.getPkgid().startsWith("npm://"));
        assertEquals(bom.getPkgid().length(), 255);
    }

    public void testDebianPkgIdResolver() {
        DebianMetadataProvider debianMetadataProvider = new DebianMetadataProvider();
        MutableMetadataEntityBOM bom = new MutableMetadataEntityBOM();
        FileInfoImpl fileInfo = new FileInfoImpl(new RepoPathImpl("debian-local", "lucid/i386/acl-1.0.0.deb"));
        PropertiesImpl props = new PropertiesImpl();
        props.put("deb.name", "acl");

        debianMetadataProvider.pkgIdResolver().apply(bom, props, fileInfo, null);

        assertEquals(bom.getPkgid(), "deb://acl");
    }

    public void testTrimValueIfNeeded() {
        RpmMetadataProvider rpmMetadataProvider = new RpmMetadataProvider();
        String normalizedValue = rpmMetadataProvider
                .trimValueIfNeeded("rpm.metadata.summary", new String(new byte[5000]));
        assertEquals(normalizedValue.length(), 5000);

        normalizedValue = rpmMetadataProvider
                .trimValueIfNeeded("rpm.metadata.tooLong", new String(new byte[5000]));
        assertEquals(normalizedValue.length(), 255);

        normalizedValue = rpmMetadataProvider
                .trimValueIfNeeded("rpm.metadata.notTooLong", new String(new byte[5]));
        assertEquals(normalizedValue.length(), 5);

        DockerMetadataProvider dockerMetadataProvider = new DockerMetadataProvider();
        normalizedValue = dockerMetadataProvider
                .trimValueIfNeeded("docker.hasNoDescriptionKey", new String(new byte[5000]));
        assertEquals(normalizedValue.length(), 255);

        normalizedValue = dockerMetadataProvider
                .trimValueIfNeeded("docker.hasNoDescriptionKey", new String(new byte[4]));
        assertEquals(normalizedValue.length(), 4);

        NpmMetadataProvider npmMetadataProvider = new NpmMetadataProvider();
        normalizedValue = npmMetadataProvider.trimValueIfNeeded("npm.keywords", new String(new byte[5000]));
        assertEquals(normalizedValue.length(), 5000);

        normalizedValue = npmMetadataProvider.trimValueIfNeeded("npm.description", new String(new byte[5000]));
        assertEquals(normalizedValue.length(), 5000);
    }

    public void testRpmPkgIdResolver() {
        RpmMetadataProvider rpmMetadataProvider = new RpmMetadataProvider();
        MutableMetadataEntityBOM bom = new MutableMetadataEntityBOM();
        FileInfoImpl fileInfo = new FileInfoImpl(new RepoPathImpl("rpm-local", "ImageMagick.rpm"));
        PropertiesImpl props = new PropertiesImpl();
        props.put("rpm.metadata.name", "ImageMagick");
        props.put("rpm.metadata.version", "1.5.3");
        props.put("rpm.metadata.epoch", "1");
        props.put("rpm.metadata.release", "2.el7");

        rpmMetadataProvider.pkgIdResolver().apply(bom, props, fileInfo, null);

        assertEquals(bom.getPkgid(), "rpm://7:ImageMagick:1:1.5.3-2.el7");

        props = new PropertiesImpl();
        props.put("rpm.metadata.name", "ImageMagick");
        props.put("rpm.metadata.version", "1.5.3");
        props.put("rpm.metadata.epoch", "1");
        props.put("rpm.metadata.release", "2.el7.notAValidElVersion");

        rpmMetadataProvider.pkgIdResolver().apply(bom, props, fileInfo, null);

        assertEquals(bom.getPkgid(), "rpm://ImageMagick:1:1.5.3-2.el7.notAValidElVersion");

        props = new PropertiesImpl();
        props.put("rpm.metadata.name", "ImageMagick");
        props.put("rpm.metadata.version", "1.5.3");
        props.put("rpm.metadata.epoch", "1");

        rpmMetadataProvider.pkgIdResolver().apply(bom, props, fileInfo, null);

        assertEquals(bom.getPkgid(), "rpm://ImageMagick:1:1.5.3");
    }

    public void testLayoutBasedResolvers() {
        InternalRepositoryService internalRepositoryService = mock(InternalRepositoryService.class);
        MavenMetadataProvider provider = new MavenMetadataProvider(internalRepositoryService);
        MutableMetadataEntityBOM bom = new MutableMetadataEntityBOM();
        FileInfoImpl fileInfo = new FileInfoImpl(
                new RepoPathImpl("libs-release-local", "org/jfrog/artifactory/1.0.0/artifactory-1.0.0.pom"));

        Repo repo = mock(Repo.class);
        when(internalRepositoryService.repositoryByKey("libs-release-local")).thenReturn(repo);
        when(repo.getDescriptor()).thenReturn(new LocalRepoDescriptor());
        when(repo.getArtifactModuleInfo("org/jfrog/artifactory/1.0.0/artifactory-1.0.0.pom"))
                .thenReturn(new ModuleInfo("org/jfrog", "artifactory", "1.0.0", null, null, null, "pom", "pom",
                        Collections.emptyMap()));

        provider.nameResolver().apply(bom, null, fileInfo, null);
        provider.versionResolver().apply(bom, null, fileInfo, null);
        provider.pkgIdResolver().apply(bom, null, fileInfo, null);

        assertEquals(bom.getName(), "artifactory");
        assertEquals(bom.getVersion(), "1.0.0");
        assertEquals(bom.getPkgid(), "gav://org.jfrog:artifactory");
    }
}