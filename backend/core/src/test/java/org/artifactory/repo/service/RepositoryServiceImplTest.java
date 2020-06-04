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

package org.artifactory.repo.service;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.descriptor.config.CentralConfigDescriptorImpl;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.interceptor.StorageInterceptors;
import org.artifactory.storage.spring.ArtifactoryStorageContext;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.util.ReflectionUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

/**
 * @author Lior Gur
 */
public class RepositoryServiceImplTest {

    public static final String TEST_REPO = "testRepo";
    private MutableCentralConfigDescriptor configDescriptor;
    private RepositoryServiceImpl repositoryService;

    @Mock
    private CentralConfigService centralConfigService;

    @Mock
    private ArtifactoryStorageContext artifactoryContext;

    @Mock
    private StorageInterceptors storageInterceptors;


    @BeforeMethod
    public void init() {
        MockitoAnnotations.initMocks(this);
        ArtifactoryContextThreadBinder.bind(artifactoryContext);

        repositoryService = new RepositoryServiceImpl();

        Field field = ReflectionUtils.findField(RepositoryServiceImpl.class, "centralConfigService");

        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, repositoryService,centralConfigService);

        configDescriptor = new CentralConfigDescriptorImpl();
        configDescriptor.addLocalRepository(new LocalRepoDescriptor() {{
            setKey("testRepo");
            setType(RepoType.Generic);
        }});
    }

    /**
     * RTFACT-14083
     * steps when deleting repository :
     * 1  blackout the repo in config descriptor
     * 2. save descriptor
     * 3. delete all files in militarisation
     * 4. remove repo from config descriptor
     * 5. save descriptor
     * 6. delete all files in repo (include repository himself)
     *
     * the test tests if step 4 is failing the repo remains blackedOut
     */
    @Test
    public void testRemoveRepository() {
        when(artifactoryContext.beanForType(StorageInterceptors.class)).thenReturn(storageInterceptors);
        when(centralConfigService.getMutableDescriptor()).thenReturn(configDescriptor);

        assertFalse(configDescriptor.getLocalRepositoriesMap().get("testRepo").isBlackedOut());
        try {
            repositoryService.removeRepository(TEST_REPO);
        } catch (NullPointerException ex) {
            //nothing to do here, we expected this exception
        }

        assertTrue(configDescriptor.getLocalRepositoriesMap().get("testRepo").isBlackedOut());
    }
}