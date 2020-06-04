package org.artifactory.security.ssh;

import org.apache.sshd.common.Cipher;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.cipher.*;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SshAuthServiceImplTest extends ArtifactoryHomeBoundTest {
    @Test
    public void testCipherFiltering() {
        bindArtifactoryHome();
        List<NamedFactory<Cipher>> avail = new LinkedList<>();
        avail.add(new AES128CTR.Factory());
        avail.add(new AES192CTR.Factory());
        avail.add(new AES256CTR.Factory());
        avail.add(new ARCFOUR256.Factory());
        avail.add(new AES128CBC.Factory());
        avail.add(new TripleDESCBC.Factory());
        avail.add(new BlowfishCBC.Factory());
        avail.add(new AES192CBC.Factory());
        avail.add(new AES256CBC.Factory());
        List<NamedFactory<Cipher>> ciphers = SshAuthServiceImpl.filterCiphersByBlackList(avail);
        List<String> filteredCipherNames = ciphers.stream().map(NamedFactory::getName).collect(Collectors.toList());
        Assert.assertFalse(filteredCipherNames.contains(new ARCFOUR256.Factory().getName()));
    }

}