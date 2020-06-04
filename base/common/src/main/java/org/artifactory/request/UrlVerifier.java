package org.artifactory.request;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Verifies that the given URL is not blocked {@see RTFACT-18385} for remote repository URL.
 * - Loopback URLs are blocked (prefixes can be whitelisted by ConstantValue)
 * - site/link local address (10/8,172.16/12,192.168/16 prefixes and 169.254/16) are enabled by default.
 *
 * @author Nadav Yogev
 */
@Component
public class UrlVerifier {
    private static final Logger log = LoggerFactory.getLogger(UrlVerifier.class);

    private List<String> whiteListedRemoteRepositoryPrefixes;
    private boolean isDev;
    private AddonsManager addonsManager;

    @Autowired
    public UrlVerifier(AddonsManager addonsManager) {
        this.addonsManager = addonsManager;
        String whitelistedPrefixes = ConstantValues.whitelistRemoteRepoUrls.getString();
        if (whitelistedPrefixes != null) {
            whiteListedRemoteRepositoryPrefixes = Arrays.stream(StringUtils.split(whitelistedPrefixes, ","))
                    .map(String::trim).collect(Collectors.toList());
        } else {
            whiteListedRemoteRepositoryPrefixes = Lists.newArrayList();
        }
        isDev =  ConstantValues.isDevOrTest(ArtifactoryHome.get());
    }

    public boolean isRemoteRepoBlocked(String url, String repoKey) {
        try {
            if (!addonsManager.addonByType(CoreAddons.class).isAol() || isDev ||
                    whiteListedRemoteRepositoryPrefixes.stream().anyMatch(url::startsWith)) {
                return false;
            }
            InetAddress byName = InetAddress.getByName(new URL(url).getHost());
            if (byName.isLoopbackAddress()) {
                log.error("Can't connect to {} URL {} - URL is a loopback, which is configured as blocked.",
                        repoKey, url);
                return true;
            }
            boolean isSiteLocalBlocked = (byName.isSiteLocalAddress()  || byName.isLinkLocalAddress()) &&
                    ConstantValues.remoteRepoBlockUrlStrictPolicy.getBoolean();
            if (isSiteLocalBlocked) {
                log.error("Can't connect to {} URL {} - URL is a site/link local address, which is configured as blocked",
                        repoKey, url);
                return true;
            }
            return false;
        } catch (UnknownHostException | MalformedURLException e) {
            log.warn("Unable to resolve {} URL address of {}", repoKey, url);
            log.debug("Unable to resolve {} URL address of {}", repoKey, url, e);
        }
        return ConstantValues.remoteRepoBlockUrlStrictPolicy.getBoolean();
    }
}
