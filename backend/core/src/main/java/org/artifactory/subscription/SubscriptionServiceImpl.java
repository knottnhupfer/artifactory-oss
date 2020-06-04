package org.artifactory.subscription;

import org.artifactory.addon.subscription.SubscriptionService;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.api.rest.subscription.Subscription;
import org.artifactory.descriptor.subscription.SubscriptionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.HashSet;

/**
 * @author dudim
 */
@Component
public class SubscriptionServiceImpl implements SubscriptionService {

    private CentralConfigService configService;

    @Autowired
    public SubscriptionServiceImpl(CentralConfigService centralConfigService) {
        this.configService = centralConfigService;
    }

    @Override
    public void setSubscription(Subscription subscription) {
        SubscriptionConfig subscriptionConfig = new SubscriptionConfig();
        subscriptionConfig.setEmails(subscription.getEmails());
        MutableCentralConfigDescriptor mutableDescriptor = configService.getMutableDescriptor();
        mutableDescriptor.setSubscriptionConfig(subscriptionConfig);
        configService.saveEditedDescriptorAndReload(mutableDescriptor);

    }

    @Override
    @Nullable
    public Subscription getSubscription() {
        SubscriptionConfig subscriptionConfig = configService.getMutableDescriptor().getSubscriptionConfig();
        Subscription subscription = new Subscription();
        if (subscriptionConfig == null) {
            subscription.setEmails(new HashSet<>());
        } else {
            subscription.setEmails(subscriptionConfig.getEmails());
        }
        return subscription;
    }
}
