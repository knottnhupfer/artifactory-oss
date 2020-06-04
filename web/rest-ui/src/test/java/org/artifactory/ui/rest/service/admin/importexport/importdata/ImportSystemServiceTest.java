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
public class ImportSystemServiceTest extends ArtifactoryHomeBoundTest {

    @Mock
    private ArtifactoryRestRequest request;

    private ArtifactoryRestResponse response;

    private ImportSystemService importSystemService;

    @BeforeMethod
    public void setup() {
        initMocks(this);
        response = new ArtifactoryRestResponse();
        importSystemService = new ImportSystemService();
    }

    @Test(testName = "test execute when systemImportEnabled is false")
    public void testExecuteWhenRepositoryImportDisabled() {
        ArtifactoryHome.get()
                .getArtifactoryProperties()
                .setProperty(ConstantValues.systemImportEnabled.getPropertyName(), Boolean.toString(false));

        importSystemService.execute(request, response);
        assertEquals(response.getEntity(), "{\"error\":\"System Import is disabled.\"}");
        assertEquals(response.getResponseCode(), HttpStatus.SC_FORBIDDEN);
    }
}
