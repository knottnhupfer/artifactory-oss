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

package org.artifactory.storage.jobs.migration.sha256;

import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.service.RepositoryServiceImpl;
import org.artifactory.spring.ArtifactoryApplicationContext;
import org.artifactory.storage.binstore.service.BinaryService;
import org.artifactory.storage.binstore.service.InternalBinaryService;
import org.artifactory.storage.db.binstore.dao.BinariesDao;
import org.artifactory.storage.db.binstore.service.BinaryInfoImpl;
import org.artifactory.storage.db.fs.dao.NodesDao;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.jfrog.storage.binstore.common.BinaryElementImpl;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * @author Uriah Levy
 */
public class Sha256MigrationJobTestBase extends ArtifactoryHomeBoundTest {

    @Mock
    private ArtifactoryApplicationContext artifactoryContext;
    @Mock
    private BinaryService binaryService;
    @Mock
    private InternalRepositoryService internalRepositoryService;
    @Mock
    private InternalBinaryService internalBinaryService;
    @Mock
    private BinariesDao binariesDao;
    @Mock
    private NodesDao nodesDao;

    RepositoryServiceImpl repoService = new RepositoryServiceImpl();

    @BeforeClass
    public void beforeClass() {
        MockitoAnnotations.initMocks(this);
        ArtifactoryContextThreadBinder.bind(artifactoryContext);
        // Context
        mockServices();
    }

    private void mockServices() {
        when(artifactoryContext.beanForType(BinaryService.class)).thenReturn(binaryService);
        when(artifactoryContext.beanForType(InternalBinaryService.class)).thenReturn(internalBinaryService);
        when(artifactoryContext.beanForType(InternalRepositoryService.class)).thenReturn(internalRepositoryService);
        when(artifactoryContext.beanForType(BinariesDao.class)).thenReturn(binariesDao);
        when(artifactoryContext.beanForType(NodesDao.class)).thenReturn(nodesDao);
    }

    /**
     * Dynamically init required method stubs per every test-case with different params
     */
    void initStubs(String sha1, String sha2, String md5, String repoKey, BinaryElementImpl binaryElement,
                   RepoPath repoPath, Throwable exception) throws SQLException {
        services(sha1, sha2, md5, binaryElement);
        daos(sha1, sha2);
        // define stub exception behavior based on input
        exceptions(sha2, repoPath, exception);
    }

    private void exceptions(String sha2, RepoPath repoPath, Throwable exception) {
        if(exception.getClass().isAssignableFrom(ItemNotFoundRuntimeException.class)) {
            doThrow(exception)
                    .when(internalRepositoryService).updateSha2ForPath(repoPath, sha2);
        } else if (exception.getClass().isAssignableFrom(RuntimeException.class)){
            doThrow(exception)
                    .when(internalRepositoryService).updateSha2ForPath(repoPath, sha2);
        }
    }

    private void services(String sha1, String sha2, String md5, BinaryElementImpl binaryElement) throws SQLException {
        when(binaryService.findBinary(ChecksumType.sha1, sha1)).thenReturn(new BinaryInfoImpl(sha1, null, md5,
                10));
        when(internalBinaryService.createBinaryElement(sha1, null, null, -1))
                .thenReturn(binaryElement);
        when(internalBinaryService.getBinary(binaryElement)).thenReturn(new ByteArrayInputStream(sha1.getBytes()));
        when(internalBinaryService.updateSha2ForSha1(sha1, sha2)).thenReturn(true);
    }

    private void daos(String sha1, String sha2) throws SQLException {
        when(binariesDao.insertSha2(sha1, sha2)).thenReturn(true);
        when(nodesDao.getMissingSha2ArtifactCount()).thenReturn(1);
        when(binariesDao.getSha1ForMissingSha2Count()).thenReturn(0);
    }

    static int getErrorMapFieldSize(Sha256MigrationJob sha256MigrationJob, String fieldName) throws NoSuchFieldException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Field noneFatalNodeErrorsField = sha256MigrationJob.getClass().getDeclaredField(fieldName);
        noneFatalNodeErrorsField.setAccessible(true);
        Object fieldValue = noneFatalNodeErrorsField.get(sha256MigrationJob);
        Method sizeMethod = fieldValue.getClass().getDeclaredMethod("size", null);
        return (int) sizeMethod.invoke(fieldValue);
    }
}
