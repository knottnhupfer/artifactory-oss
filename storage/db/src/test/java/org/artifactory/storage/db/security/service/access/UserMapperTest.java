/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.storage.db.security.service.access;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.EnumUtils;
import org.artifactory.api.security.UserInfoBuilder;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.model.xstream.security.UserProperty;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.SaltedPassword;
import org.artifactory.security.UserInfo;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.test.ArtifactoryHomeStub;
import org.fest.assertions.MapAssert;
import org.jfrog.access.model.Realm;
import org.jfrog.access.model.UserStatus;
import org.jfrog.access.rest.group.GroupResponse;
import org.jfrog.access.rest.user.*;
import org.jfrog.security.crypto.*;
import org.jfrog.security.crypto.result.DecryptionStatusHolder;
import org.jfrog.security.file.SecurityFolderHelper;
import org.joda.time.format.ISODateTimeFormat;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.artifactory.storage.db.security.service.access.UserMapper.ArtifactoryBuiltInUserProperty.*;
import static org.fest.assertions.Assertions.assertThat;
import static org.testng.Assert.*;

/**
 * Unit tests for {@link UserMapper}.
 *
 * @author Yossi Shaul
 */
@Test
public class UserMapperTest extends ArtifactoryHomeBoundTest {

    @BeforeMethod
    public void setup() {
        ArtifactoryHome artifactoryHome = new ArtifactoryHomeStub();
        artifactoryHome.getArtifactoryKey().delete();
        SecurityFolderHelper.createKeyFile(artifactoryHome.getArtifactoryKey(), CipherAlg.AES128);
        ArtifactoryHome.bind(artifactoryHome);
    }

    public void fullUserToUserInfo() throws Exception {
        User u = sampleAccessUser();

        UserInfo i = UserMapper.toArtifactoryUser(u);

        assertEquals(i.getUsername(), u.getUsername());
        assertEquals(i.getEmail(), u.getEmail());
        assertEquals(i.isAccountNonLocked(), true); // ?
        assertEquals(i.isAnonymous(), false);
        assertEquals(i.isAdmin(), true);
        assertEquals(i.isGroupAdmin(), false);
        assertEquals(i.isCredentialsExpired(), false);
        assertEquals(i.isEnabled(), true);
        assertEquals(i.isLocked(), false);
        assertEquals(i.isTransientUser(), false);
        assertEquals(i.isUpdatableProfile(), false);
        assertEquals(i.getGroups(), Sets.newHashSet(InfoFactoryHolder.get().createUserGroup("northmen", "internal"),
                InfoFactoryHolder.get().createUserGroup("kings", "internal")));
        assertEquals(i.getBintrayAuth(), u.getCustomData("bintray_auth"));
        assertEquals(i.getPrivateKey(), u.getCustomData("private_key"));
        assertEquals(i.getPublicKey(), u.getCustomData("public_key"));
        assertThat(i.getUserProperties()).hasSize(3).containsOnly(
                new UserProperty("charismatic", "true"),
                new UserProperty("bold", "true"),
                new UserProperty("height", "210"));
    }

    public void fullToAccessUserWithArtifactoryEncryptedValues() throws Exception {
        String encryptedData = CryptoHelper.encryptIfNeeded(ArtifactoryHome.get(), "data");
        assertFalse(encryptedData.equals("data"));

        MutableUserInfo u = sampleUserInfo()
                .addProp(new UserProperty("encrypted", encryptedData))
                .addProp(new UserProperty("decrypted", "newdata"))
                .build();

        EncodedKeyPair keyPair = createKeyPairForUser(u, ArtifactoryHome.get().getArtifactoryEncryptionWrapper());
        DecodedKeyPair decoded = keyPair.decode(ArtifactoryHome.get().getArtifactoryEncryptionWrapper(), new DecryptionStatusHolder());
        EncodedKeyPair toValidate = new EncodedKeyPair(decoded, null);

        UserRequest i = UserMapper.toAccessUser(u, true);

        assertEquals(i.getCustomData("encrypted").getValue(), "data");
        assertEquals(i.getCustomData("encrypted").isSensitive(), true);
        assertEquals(i.getCustomData("encrypted_shash").getValue().length(), 6);
        assertEquals(i.getCustomData("encrypted_shash").isSensitive(), false);
        assertEquals(i.getCustomData("decrypted").getValue(), "newdata");
        assertEquals(i.getCustomData("decrypted").isSensitive(), true);
        assertEquals(i.getCustomData("decrypted_shash").getValue().length(), 6);
        assertEquals(i.getCustomData("decrypted_shash").isSensitive(), false);
        assertEquals(i.getCustomData(UserMapper.ArtifactoryBuiltInUserProperty.private_key.name()).getValue(), toValidate.getEncodedPrivateKey());
        assertEquals(i.getCustomData(UserMapper.ArtifactoryBuiltInUserProperty.private_key.name()).isSensitive(), true);
        assertEquals(i.getCustomData(UserMapper.ArtifactoryBuiltInUserProperty.public_key.name()).getValue(), toValidate.getEncodedPublicKey());
        assertEquals(i.getCustomData(UserMapper.ArtifactoryBuiltInUserProperty.public_key.name()).isSensitive(), true);
    }

    public void fullToAccessUserWithDecryptedPrivatePublicKeys() throws Exception {
        EncodedKeyPair keyPair = JFrogCryptoHelper.encodeKeyPair(JFrogCryptoHelper.generateKeyPair());
        MutableUserInfo u = sampleUserInfo()
                .privateKey(keyPair.getEncodedPrivateKey())
                .publicKey(keyPair.getEncodedPublicKey())
                .build();

        UserRequest i = UserMapper.toAccessUser(u, true);

        assertEquals(i.getCustomData(UserMapper.ArtifactoryBuiltInUserProperty.private_key.name()).getValue(), keyPair.getEncodedPrivateKey());
        assertEquals(i.getCustomData(UserMapper.ArtifactoryBuiltInUserProperty.private_key.name()).isSensitive(), true);
        assertEquals(i.getCustomData(UserMapper.ArtifactoryBuiltInUserProperty.public_key.name()).getValue(), keyPair.getEncodedPublicKey());
        assertEquals(i.getCustomData(UserMapper.ArtifactoryBuiltInUserProperty.public_key.name()).isSensitive(), true);
    }

    public void fullToAccessUserWithKeysNewEncryptedValues() throws Exception {
        MutableUserInfo u = sampleUserInfo().build();

        EncodedKeyPair keyPair = createKeyPairForUser(u, null);
        DecodedKeyPair decoded = keyPair.decode(null, new DecryptionStatusHolder());
        EncodedKeyPair toValidate = new EncodedKeyPair(decoded, null);

        UserRequest i = UserMapper.toAccessUser(u, true);

        assertEquals(i.getCustomData(UserMapper.ArtifactoryBuiltInUserProperty.private_key.name()).getValue(), toValidate.getEncodedPrivateKey());
        assertEquals(i.getCustomData(UserMapper.ArtifactoryBuiltInUserProperty.private_key.name()).isSensitive(), true);
        assertEquals(i.getCustomData(UserMapper.ArtifactoryBuiltInUserProperty.public_key.name()).getValue(), toValidate.getEncodedPublicKey());
        assertEquals(i.getCustomData(UserMapper.ArtifactoryBuiltInUserProperty.public_key.name()).isSensitive(), true);
    }

    private EncodedKeyPair createKeyPairForUser(MutableUserInfo user, EncryptionWrapper keyWrapper) {
        EncodedKeyPair encodedKeyPair;
        DecodedKeyPair decodedKeyPair = new DecodedKeyPair(JFrogCryptoHelper.generateKeyPair());
        encodedKeyPair = new EncodedKeyPair(decodedKeyPair, keyWrapper);
        user.setPrivateKey(encodedKeyPair.getEncodedPrivateKey());
        user.setPublicKey(encodedKeyPair.getEncodedPublicKey());
        return encodedKeyPair;
    }

    public void fullGroupAdminToUserInfo() {
        UserWithGroups u = sampleAccessGroupAdminUser();
        UserInfo i = UserMapper.toArtifactoryUser(u);
        assertEquals(i.getUsername(), u.getUsername());
        assertEquals(i.getEmail(), u.getEmail());
        assertEquals(i.isAccountNonLocked(), true); // ?
        assertEquals(i.isAnonymous(), false);
        assertEquals(i.isAdmin(), false);
        assertEquals(i.isGroupAdmin(), true);
        assertEquals(i.isCredentialsExpired(), false);
        assertEquals(i.isEnabled(), true);
        assertEquals(i.isLocked(), false);
        assertEquals(i.isTransientUser(), false);
        assertEquals(i.isUpdatableProfile(), false);
    }

    public void accessUserToArtifactoryUserWithAllProperties() {
        User u = sampleAccessUser();
        UserInfo userInfo = UserMapper.toArtifactoryUser(u);
        // expect only the 4 custom data properties which aren't mapped to domain fields in UserInfo
        assertThat(userInfo.getUserProperties()).hasSize(3).containsOnly(
                new UserProperty("charismatic", "true"),
                new UserProperty("bold", "true"),
                new UserProperty("height", "210")
        );
    }

    public void fullUserInfoToAccessUser() throws Exception {
        UserInfo i = sampleUserInfo().build();

        UserRequest u = UserMapper.toAccessUser(i, true);

        assertEquals(u.getUsername(), i.getUsername());
        assertEquals(u.getPassword(), i.getPassword());
        assertEquals(u.getEmail(), i.getEmail());
        assertEquals(Boolean.parseBoolean(u.getCustomData("artifactory_admin").getValue()), true);
        assertEquals(Boolean.parseBoolean(u.getCustomData("updatable_profile").getValue()), false);
        assertEquals(u.getRealm().getName(), i.getRealm());
        assertEquals(u.getGroups(), Sets.newHashSet("northmen", "kings"));
        assertEquals(u.getCustomData("bintray_auth").getValue(), i.getBintrayAuth());
        assertEquals(u.getCustomData("private_key").getValue(), i.getPrivateKey());
        assertEquals(u.getCustomData("public_key").getValue(), i.getPublicKey());
        assertTrue(u.getCustomData("bintray_auth").isSensitive());
        assertTrue(u.getCustomData("private_key").isSensitive());
        assertTrue(u.getCustomData("public_key").isSensitive());

        assertEquals(u.getCustomData("charismatic").getValue(), "true");
        assertEquals(u.getCustomData("height").getValue(), "210");
        assertTrue(u.getCustomData("charismatic").isSensitive());
        assertTrue(u.getCustomData("height").isSensitive());
    }

    public void userInfoToAccessUserWithProperties() {
        UserInfo artiUser = sampleUserInfo().build();
        UserRequest accessUser = UserMapper.toAccessUser(artiUser, false);
        assertNotNull(accessUser);
        // make sure custom data include only the Artifactory built in custom properties
        Map<String, CustomDatumRequest> customData = accessUser.getCustomData();
        for (String s : customData.keySet()) {
            assertTrue(EnumUtils.isValidEnum(UserMapper.ArtifactoryBuiltInUserProperty.class, s));
        }
    }

    public void userInfoToAccessUserWithNullProperties() {
        UserInfo artiUser = sampleUserInfo().build();
        UserRequest accessUser = UserMapper.toAccessUser(artiUser, true);
        assertNotNull(accessUser);
        assertThat(toMapOfStrings(accessUser.getCustomData())).includes(MapAssert.entry("charismatic", "true"),
                MapAssert.entry("charismatic", "true"), MapAssert.entry("height", "210"));
    }

    private Map<String, String> toMapOfStrings(Map<String, CustomDatumRequest> customData) {
        HashMap<String, String> map = new HashMap<>(); // Bad java!
        customData.forEach((key, value) -> map.put(key, value.getValue()));
        return map;
    }

    public void testNotSavingEmptyCustomProperties() {
        UserInfo artiUser = sampleUserInfo().addProp(new UserProperty("something", "")).bintrayAuth("").build();
        UserRequest accessUser = UserMapper.toAccessUser(artiUser, true);
        assertNotNull(accessUser);
        Map<String, String> customData = toMapOfStrings(accessUser.getCustomData());
        assertThat(customData).includes(MapAssert.entry("charismatic", "true"),
                MapAssert.entry("charismatic", "true"), MapAssert.entry("height", "210"));
        assertThat(customData.get("something")).isNull();
        assertThat(customData.get("bintray_auth")).isNull();
    }

    public void testDeleteExistPropertiesOnUpdate() {
        MutableUserInfo artiUser = sampleUserInfo().addProp(new UserProperty("something", ""))
                .bintrayAuth("").build();

        UserRequest accessUser = UserMapper.toUpdateUserRequest(artiUser, true);
        assertNotNull(accessUser);
        Map<String, CustomDatumRequest> customData = accessUser.getCustomData();
        assertThat(customData.get("something").getValue()).isNull();
        assertThat(customData.get("bintray_auth").getValue()).isNull();
    }

    private UserResponse sampleAccessUser() {
        return new UserResponse()
                .username("bethod")
                .firstName("Bethod")
                .lastName("The King")
                .email("bethod@northmen.com")
                .status(UserStatus.ENABLED)
                .allowedIp("*")
                .created(fromIsoDateString("1978-05-15T09:15:56.003Z"))
                .modified(fromIsoDateString("1978-05-15T09:15:56.003Z"))
                .lastLoginTime(fromIsoDateString("1980-05-15T09:15:56.003Z"))
                .lastLoginIp("10.0.0.2")
                .groups(Sets.newHashSet("northmen", "kings"))
                .realm(Realm.INTERNAL)
                .addCustomData(gen_password_key.name(), "ZW4gTmluZWZpbmdlcnMTG9n")
                .addCustomData(updatable_profile.name(), "false")
                .addCustomData(bintray_auth.name(), "bethodn")
                .addCustomData(private_key.name(), "TG9nZW4gTmluZWZpbmdlcnM=")
                .addCustomData(public_key.name(), "VGhlIEJsb29keS1OaW5l")
                .addCustomData(artifactory_admin.name(), "true")
                .addCustomData("charismatic", "true")
                .addCustomData("bold", "true")
                .addCustomData("height", "210");
    }

    private UserWithGroupsResponse sampleAccessGroupAdminUser() {
        return new UserWithGroupsResponse()
                .username("bethod")
                .firstName("Bethod")
                .lastName("The King")
                .email("bethod@northmen.com")
                .status(UserStatus.ENABLED)
                .allowedIp("*")
                .created(fromIsoDateString("1978-05-15T09:15:56.003Z"))
                .modified(fromIsoDateString("1978-05-15T09:15:56.003Z"))
                .lastLoginTime(fromIsoDateString("1980-05-15T09:15:56.003Z"))
                .lastLoginIp("10.0.0.2")
                .groups(getAdminUserGroups())
                .realm(Realm.INTERNAL)
                .addCustomData(gen_password_key.name(), "ZW4gTmluZWZpbmdlcnMTG9n")
                .addCustomData(updatable_profile.name(), "false")
                .addCustomData(bintray_auth.name(), "bethodn")
                .addCustomData(private_key.name(), "TG9nZW4gTmluZWZpbmdlcnM=")
                .addCustomData(public_key.name(), "VGhlIEJsb29keS1OaW5l")
                .addCustomData("charismatic", "true")
                .addCustomData("bold", "true")
                .addCustomData("height", "210");
    }

    private ArrayList<GroupResponse> getAdminUserGroups() {
        Map<String, String> customData = Maps.newHashMap();
        customData.put(artifactory_admin.name(), "true");
        GroupResponse group1 = new GroupResponse().name("group1").realm(Realm.INTERNAL).customData(customData);
        return Lists.newArrayList(group1);
    }

    private static long fromIsoDateString(String dateTime) {
        return ISODateTimeFormat.dateTime().parseMillis(dateTime);
    }

    private UserInfoBuilder sampleUserInfo() {
        return new UserInfoBuilder("bethod")
                .password(new SaltedPassword("calder", "scale"))
                .email("bethod@northmen.com")
                .enabled(true)
                //.created("1978-05-15T09:15:56.003Z")
                //.lastModified("1978-05-15T09:15:56.003Z")
                .groups(Sets.newHashSet(InfoFactoryHolder.get().createUserGroup("northmen", "internal"),
                        InfoFactoryHolder.get().createUserGroup("kings", "internal")))
                .credentialsExpired(false)
                .updatableProfile(false)
                .bintrayAuth("bethodn")
                .privateKey("TG9nZW4gTmluZWZpbmdlcnM=")
                .publicKey("VGhlIEJsb29keS1OaW5l")
                .admin(true)
                .realm("internal")
                .lastLogin(fromIsoDateString("1980-05-15T09:15:56.003Z"), "10.0.0.2")
                .addProp(new UserProperty("charismatic", "true"))
                .addProp(new UserProperty("bold", "true"))
                .addProp(new UserProperty("height", "210"));
    }

}