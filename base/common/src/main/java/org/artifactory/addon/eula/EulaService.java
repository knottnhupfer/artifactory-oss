package org.artifactory.addon.eula;

/**
 * @author Omri Ziv
 */
public interface EulaService {

    void accept();

    byte[] getEulaFile();

    boolean isRequired();
}
