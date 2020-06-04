package org.artifactory.repo;

import org.artifactory.descriptor.repo.RepoType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation purpose is to map the fields in {@link RepositoryConfigurationBase},
 * {@link LocalRepositoryConfigurationImpl}, {@link HttpRepositoryConfigurationImpl} and {@link VirtualRepositoryConfigurationImpl}
 * to their specific package type.
 *
 * @author Inbar Tal
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface IncludeTypeSpecific {
    RepoType[] packageType() default {};
    RepoDetailsType[] repoType() default {};
}
