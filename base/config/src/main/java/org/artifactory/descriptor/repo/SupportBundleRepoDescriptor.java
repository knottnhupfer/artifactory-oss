package org.artifactory.descriptor.repo;

/**
 * @author Tamir Hadad
 */
public class SupportBundleRepoDescriptor extends LocalRepoDescriptor {
   public static String SUPPORT_BUNDLE_REPO_NAME = "jfrog-support-bundle";

    public SupportBundleRepoDescriptor(String repoKey, RepoLayout simpleLayout) {
        setKey(repoKey);
        setType(RepoType.Support);
        setRepoLayout(simpleLayout);
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public boolean isCache() {
        return false;
    }
}
