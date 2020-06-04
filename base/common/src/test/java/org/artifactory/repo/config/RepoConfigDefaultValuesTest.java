package org.artifactory.repo.config;

import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.config.RepoConfigDefaultValues.DefaultUrl;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.testng.Assert.fail;

/**
 * @author Dan Feldman
 */
@Test
public class RepoConfigDefaultValuesTest {

    public void verifyDefaultUrls() {
        Set<RepoType> uniqueTypes = Arrays.stream(DefaultUrl.values())
                .map(DefaultUrl::getType)
                .collect(Collectors.toSet());
        if (uniqueTypes.size() != DefaultUrl.values().length) {
            fail("There can only be one default url per package type.");
        }
    }
}