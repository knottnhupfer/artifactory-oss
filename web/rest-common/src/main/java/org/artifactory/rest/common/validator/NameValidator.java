package org.artifactory.rest.common.validator;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.exception.ValidationException;

import java.util.List;
import java.util.Optional;


/**
 * @author omriz
 */
public class NameValidator {

    private static final List<Character> illegalCharacters = Lists
            .newArrayList('/', '\\', ':', '|', '?', '*', '"', '<', '>');

    public static Optional<String> validate(String name) {
        if (StringUtils.isBlank(name)) {
            return Optional.of("Name cannot be blank");
        }

        if (name.equals(".") || name.equals("..") || name.equals("&")) {
            return Optional.of("Name cannot be empty link: '" + name + "'");
        }

        char[] nameChars = name.toCharArray();

        if(name.chars().anyMatch(chr -> illegalCharacters.contains((char) chr))) {
            return Optional.of("Illegal name : '/,\\,:,|,?,<,>,*,\"' is not allowed");
        }
        return Optional.empty();
    }

}
