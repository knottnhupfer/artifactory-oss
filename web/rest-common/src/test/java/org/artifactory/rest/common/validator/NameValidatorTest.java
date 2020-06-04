package org.artifactory.rest.common.validator;

import com.google.common.collect.Lists;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author barh
 */
public class NameValidatorTest {

    private static final List<Character> illegalCharacters = Lists
            .newArrayList('/', '\\', ':', '|', '?', '*', '"', '<', '>');

    @Test
    public void validateBadCharacters() {
        illegalCharacters.forEach(chr -> assertTrue(
                NameValidator.validate("blah" + chr + "blah").isPresent()));
    }

    @Test
    public void validateGoodCharacters() {
        assertFalse(NameValidator.validate("legal").isPresent());
    }
}