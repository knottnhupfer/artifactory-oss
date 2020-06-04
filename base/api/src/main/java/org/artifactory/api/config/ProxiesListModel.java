package org.artifactory.api.config;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.artifactory.descriptor.repo.ProxyDescriptor;

import java.util.List;


/**
 * @author Lior Gur
 */
@Data
@NoArgsConstructor
public class ProxiesListModel {

    List<ProxyModel> proxies = Lists.newArrayList();

    public ProxiesListModel(List<ProxyDescriptor> proxyDescriptors) {
        for (ProxyDescriptor proxyDescriptor : proxyDescriptors) {
            proxies.add(new ProxyModel(proxyDescriptor));
        }
    }
}

