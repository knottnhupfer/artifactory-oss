package org.artifactory.rest.common.validator;

import com.google.common.collect.Lists;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.rest.common.exception.RepoConfigException;
import org.junit.Assert;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/*
 * @author Omri Ziv
 */
public class RepositoryNameValidatorTest {
    private static final List<Character> illegalCharacters = Lists
            .newArrayList('/', '\\', ':', '|', '?', '*', '"', '<', '>');

    private CentralConfigService centralConfig = mock(CentralConfigService.class);
    private MutableCentralConfigDescriptor mutableCentralConfigDescriptor = mock(MutableCentralConfigDescriptor.class);

    @Test
    public void validateBadCharacters() {
        illegalCharacters.forEach(chr -> {
            try {
                RepositoryNameValidator.validateRepoName("repo" + chr + "name","remote", centralConfig);
            } catch (RepoConfigException e) {
                assertEquals(e.getMessage(), "Illegal Repository key : '/,\\,:,|,?,<,>,*,\"' is not allowed");
                return;
            }
            Assert.fail();
        });
    }

    @Test(expectedExceptions = RepoConfigException.class)
    public void negativeEmptyRepoKey() throws RepoConfigException {
        try {
            RepositoryNameValidator.validateRepoName("", "local", centralConfig);
        } catch (RepoConfigException e) {
            assertEquals(e.getMessage(), "Repository key cannot be empty");
            throw e;
        }
    }
    @Test(expectedExceptions = RepoConfigException.class)
    public void negativeMaxLengthRepo() throws RepoConfigException {
        try {
            RepositoryNameValidator.validateRepoName("very-very-l0000000oooooooooooooooooooooooooooooooooooong-remote-repo","local", centralConfig);
        } catch (RepoConfigException e) {
            assertTrue(e.getMessage().contains("Repository key exceeds maximum length"));
            throw e;
        }
    }

    @Test(expectedExceptions = RepoConfigException.class)
    public void negativeMaxLengthRemoteRepo() throws RepoConfigException {
        try {
            RepositoryNameValidator.validateRepoName("very-very-loooooooooooooooooooooooooooooooooooong-remote-repo","remote", centralConfig);
        } catch (RepoConfigException e) {
            assertTrue(e.getMessage().contains("Repository key exceeds maximum length"));
            throw e;
        }
    }

    @Test
    public void validateGoodCharacters() throws RepoConfigException {
        when(centralConfig.getMutableDescriptor()).thenReturn(mutableCentralConfigDescriptor);
        when(mutableCentralConfigDescriptor.isKeyAvailable(anyString())).thenReturn(true);
        RepositoryNameValidator.validateRepoName("valid-repo-name", "remote", centralConfig);
    }
}
