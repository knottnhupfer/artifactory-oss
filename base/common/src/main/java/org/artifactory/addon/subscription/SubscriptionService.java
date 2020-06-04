package org.artifactory.addon.subscription;

import org.artifactory.api.rest.subscription.Subscription;

/**
 * @author dudim
 */
public interface SubscriptionService {

     void setSubscription(Subscription subscriptionMail);

     Subscription getSubscription();
}
