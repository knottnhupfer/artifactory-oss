package org.artifactory.event.provider;

/**
 * @author Uriah Levy
 * A finite event provider (a provider that reads from a finite source, such as an obsolete db table).
 */
public interface FiniteEventProvider<T> extends EventProvider<T> {
    void onFinish();
}
