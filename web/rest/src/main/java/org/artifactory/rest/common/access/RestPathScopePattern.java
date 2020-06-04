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
package org.artifactory.rest.common.access;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.container.ContainerRequestContext;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * REST URI pattern matcher based on a scope token in the format:
 * <p><code>&lt;prefix&gt;:&lt;path-pattern&gt;</code></p>
 * <p>Where:<ul>
 * <li><code>&lt;prefix&gt;</code> is one of the predefined prefixes: <code>api</code>, <code>ui</code><br></li>
 * <li><code>&lt;path-pattern&gt;</code> is the path to match (after the prefix). The pattern supports the <code>'*'</code> wildcard.</li>
 * </ul>
 * </p>
 * <p>For example:<br>
 * The scope token <code>"api:*"</code> will match paths such as <code>"/api/foo/bar"</code> but not <code>"/ui/foo/bar"</code><br>
 * The scope token <code>"api:foo/*"</code> will match paths such as <code>"/api/foo/bar"</code> but not <code>"/api/bar"</code><br>
 * The scope token <code>"api:foo/bar"</code> will match only the path <code>"/api/foo/bar"</code>
 * </p>
 * <p>Usage:<pre>
 * ContainerRequest request = ...
 * RestPathScopePattern pattern = RestPathScopePattern.parse("api:foo/*");
 * if (pattern.matches(request)) {
 *     //do something
 * }
 * </pre></p>
 * @author Yinon Avraham.
 */
class RestPathScopePattern {

    private static final Logger log = LoggerFactory.getLogger(RestPathScopePattern.class);

    private static final Set<String> PATH_PREFIXES = Collections.unmodifiableSet(Sets.newHashSet("api", "ui"));
    private static final Pattern PATH_PATTERN_GROUPS = Pattern.compile("(\\*)|([^*]+)");

    private final String prefix;
    private final Pattern pattern;

    private RestPathScopePattern(@Nonnull String prefix, @Nonnull Pattern pattern) {
        this.prefix = requireNonNull(prefix, "prefix is required");
        this.pattern = requireNonNull(pattern, "pattern is required");
    }

    /**
     * Check whether a given request URI matches this pattern
     * @param request the request to check
     * @return <code>true</code> if the request matches, <code>false</code> otherwise.
     */
    boolean matches(ContainerRequestContext request) {
        String basePath = request.getUriInfo().getBaseUri().getPath();
        String path = request.getUriInfo().getPath(false);
        boolean result = basePath.endsWith("/" + prefix + "/") && pattern.matcher(path).matches();
        log.debug("Matching request '{}' with path scope pattern '{}:{}', result: {}",
                request.getUriInfo().getAbsolutePath(), prefix, pattern, result);
        return result;
    }

    /**
     * Parse the scope token and compile a REST path pattern
     * @param scopeToken the scope token to parse
     * @return the compiled pattern
     * @throws IllegalArgumentException if the given scope token could not be parsed
     */
    @Nonnull
    static RestPathScopePattern parse(@Nonnull String scopeToken) {
        return parseOptional(scopeToken)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Could not parse scope token '" + scopeToken + "' as a path pattern"));
    }

    /**
     * Parse the scope token and compile a REST path pattern
     * @param scopeToken the scope token to parse
     * @return an optional with the compiled pattern or an empty optional if the scope token was not a REST path pattern
     */
    @Nonnull
    static Optional<RestPathScopePattern> parseOptional(@Nonnull String scopeToken) {
        int colonIndex = scopeToken.indexOf(":");
        if (colonIndex > 0) {
            String prefix = scopeToken.substring(0, colonIndex);
            if (PATH_PREFIXES.contains(prefix)) {
                try {
                    Pattern pattern = compilePathPattern(scopeToken.substring((prefix + ":").length()));
                    RestPathScopePattern pathScopePattern = new RestPathScopePattern(prefix, pattern);
                    return Optional.of(pathScopePattern);
                } catch (Exception e) {
                    log.debug("Failed to compile path pattern from scope token '{}'.", scopeToken, e);
                }
            }
        }
        return Optional.empty();
    }

    private static Pattern compilePathPattern(String pattern) {
        StringBuilder regex = new StringBuilder("^");
        Matcher matcher = PATH_PATTERN_GROUPS.matcher(pattern);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                regex.append(".*");
            } else if (matcher.group(2) != null) {
                regex.append(Pattern.quote(matcher.group(2)));
            } else {
                throw new IllegalStateException("Unexpected state - check the pattern");
            }
        }
        regex.append("$");
        return Pattern.compile(regex.toString());
    }
}
