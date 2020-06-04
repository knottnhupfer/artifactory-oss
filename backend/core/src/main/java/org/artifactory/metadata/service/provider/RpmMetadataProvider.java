package org.artifactory.metadata.service.provider;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.jfrog.client.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Uriah Levy
 */
@Component
public class RpmMetadataProvider extends AbstractMetadataProvider {
    private static final Logger log = LoggerFactory.getLogger(RpmMetadataProvider.class);
    private static final RepoType type = RepoType.YUM;
    private static final String RPM = "rpm";
    private static final String LEAD_FILE_EXTENSION = RPM;
    public static final Pattern EL_VERSION = Pattern.compile("\\d+(\\.el|\\.rhel)(?<enterpriseLinuxVersion>\\d+)+");

    public RpmMetadataProvider() {
        super(type);
        setTypeSpecificMetadataTemplate(new RpmTypeSpecificMetadataTemplate());
    }

    @Override
    public boolean isLeadArtifact(RepoPath repoPath) {
        String extension = PathUtils.getExtension(repoPath.getPath());
        return StringUtils.isNotBlank(extension) && extension.equals(LEAD_FILE_EXTENSION);
    }

    @Override
    public Optional<AqlApiItem> buildVersionFilesQuery(@Nonnull RepoPath repoPath) {
        return Optional.empty();
    }

    @Override
    protected String getType() {
        return RPM;
    }

    @Data
    class RpmTypeSpecificMetadataTemplate implements TypeSpecificMetadataTemplate {
        private static final String METADATA_PREFIX = "rpm.metadata";
        private static final String NAME_SUFFIX = ".name";
        private static final String VERSION_SUFFIX = ".version";
        private static final String DESCRIPTION_SUFFIX = ".summary";
        private static final String ARCH_SUFFIX = ".arch";
        private static final String EPOCH_SUFFIX = ".epoch";
        private static final String RELEASE_SUFFIX = ".release";
        private final Map<String, String> qualifierKeys = ImmutableMap
                .of(METADATA_PREFIX + ARCH_SUFFIX, "rpm_architecture", METADATA_PREFIX + ".group", "rpm_group");
        private Set<String> excludedUserProperties = ImmutableSet
                .of(METADATA_PREFIX + NAME_SUFFIX, METADATA_PREFIX + VERSION_SUFFIX,
                        METADATA_PREFIX + DESCRIPTION_SUFFIX);

        @Nullable
        @Override
        public String getPkgidSuffix(RepoPath repoPath, Properties properties) {
            return getNameKey().map(nameKey -> {
                String name = valueForKey(properties, nameKey);
                String version = valueForKey(properties, METADATA_PREFIX + VERSION_SUFFIX);
                if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(version)) {
                    String fullSuffix = "";
                    // Enterprise Linux version
                    String release = valueForKey(properties, METADATA_PREFIX + RELEASE_SUFFIX);
                    String enterpriseLinuxVersion = tryToGetEnterpriseLinuxVersion(release);
                    if (StringUtils.isNotBlank(enterpriseLinuxVersion)) {
                        fullSuffix = fullSuffix + enterpriseLinuxVersion + COLON;
                    }
                    // Name
                    fullSuffix = fullSuffix + name;
                    // RPM Epoch
                    String epoch = valueForKey(properties, METADATA_PREFIX + EPOCH_SUFFIX);
                    if (StringUtils.isNotBlank(epoch)) {
                        fullSuffix = fullSuffix + COLON + epoch;
                    }
                    // Version
                    fullSuffix = fullSuffix + COLON + version;
                    // Release
                    if (StringUtils.isNotBlank(release)) {
                        fullSuffix = fullSuffix + "-" + release;
                    }
                    return fullSuffix;
                }
                log.debug("Unable to resolve RPM package ID for '{}', either name or version are missing.", repoPath);
                return null;
            }).orElse(null);
        }

        @Override
        public Optional<String> getNameKey() {
            return Optional.of(METADATA_PREFIX + NAME_SUFFIX);
        }

        @Override
        public Optional<String> getVersionKey() {
            return Optional.of(METADATA_PREFIX + VERSION_SUFFIX);
        }

        @Override
        public Optional<String> getDescriptionKey() {
            return Optional.of(METADATA_PREFIX + DESCRIPTION_SUFFIX);
        }

        @Override
        public Map<String, String> getQualifierKeysToMdsKeys() {
            return qualifierKeys;
        }

        @Override
        public Set<String> getLeadFileExtensions() {
            return ImmutableSet.of(LEAD_FILE_EXTENSION);
        }

        private String tryToGetEnterpriseLinuxVersion(String release) {
            Matcher elVersionMatcher = EL_VERSION.matcher(release);
            if (StringUtils.isNotBlank(release) && elVersionMatcher.matches()) {
                return elVersionMatcher.group("enterpriseLinuxVersion");
            }
            return null;
        }
    }
}
