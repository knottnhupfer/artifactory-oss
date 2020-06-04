package org.artifactory.ui.rest.service.admin.importexport.importdata;

import org.apache.http.HttpStatus;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.ArtifactoryRestResponse;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;

/**
 * @author Yevdo Abramov
 * Created on 04/02/2020
 */
public class ImportRepositoryServiceTest extends ArtifactoryHomeBoundTest {

    @Mock
    private ArtifactoryRestRequest request;

    private ArtifactoryRestResponse response;

    private ImportRepositoryService importRepositoryService;

    @BeforeMethod
    public void setup() {
        initMocks(this);
        response = new ArtifactoryRestResponse();
        importRepositoryService = new ImportRepositoryService();
    }

    @Test(testName = "test execute when repositoryImportEnabled is false")
    public void testExecuteWhenRepositoryImportDisabled() {
        ArtifactoryHome.get()
                .getArtifactoryProperties()
                .setProperty(ConstantValues.repositoryImportEnabled.getPropertyName(), Boolean.toString(false));

        importRepositoryService.execute(request, response);
        assertEquals(response.getEntity(), "{\"error\":\"Repositories Import is disabled.\"}");
        assertEquals(response.getResponseCode(), HttpStatus.SC_FORBIDDEN);
    }
}
