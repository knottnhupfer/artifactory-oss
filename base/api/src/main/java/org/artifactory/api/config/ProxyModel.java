package org.artifactory.api.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProxyModel{
    private String key;
    private String host;
    private int port;
    private String username;
    private String password;
    @JsonProperty("nt_host")
    private String ntHost;
    private String domain;
    @JsonProperty("default_proxy")
    private boolean defaultProxy;
    @JsonProperty("redirected_to_hosts")
    private String redirectedToHosts;

    public ProxyModel(ProxyDescriptor proxyDescriptor) {
        this.defaultProxy = proxyDescriptor.isDefaultProxy();
        this.domain = proxyDescriptor.getDomain();
        this.host = proxyDescriptor.getHost();
        this.key = proxyDescriptor.getKey();
        this.ntHost = proxyDescriptor.getNtHost();
        this.password = CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(),proxyDescriptor.getPassword());
        this.port = proxyDescriptor.getPort();
        this.redirectedToHosts = proxyDescriptor.getRedirectedToHosts();
        this.username = proxyDescriptor.getUsername();
    }
}
