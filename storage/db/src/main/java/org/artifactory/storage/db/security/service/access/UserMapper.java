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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.artifactory.api.security.UserInfoBuilder;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.model.xstream.security.UserProperty;
import org.artifactory.security.*;
import org.jfrog.access.model.Realm;
import org.jfrog.access.model.UserStatus;
import org.jfrog.access.rest.imports.ImportUserRequest;
import org.jfrog.access.rest.user.*;
import org.jfrog.access.rest.user.User;
import org.jfrog.common.ClockUtils;
import org.jfrog.security.crypto.DecodedKeyPair;
import org.jfrog.security.crypto.EncodedKeyPair;
import org.jfrog.security.crypto.result.DecryptionStatusHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.artifactory.storage.db.security.service.access.GroupMapper.fromAccessRealm;
import static org.artifactory.storage.db.security.service.access.GroupMapper.toAccessRealm;
import static org.artifactory.storage.db.security.service.access.UserMapper.ArtifactoryBuiltInUserProperty.*;
import static org.artifactory.storage.db.security.service.access.UserPropertiesSearchHelper.getSearchableProp;
import static org.artifactory.storage.db.security.service.access.UserPropertiesSearchHelper.isSearchableHelperProperty;

/**
 * Helper class to map Artifactory user to Access user details and vice versa.
 *
 * @author Yossi Shaul
 */
public class UserMapper {

    private static final Logger log = LoggerFactory.getLogger(UserMapper.class);

    public static boolean isFieldSensitive(String propKey) {
        return Arrays.stream(ArtifactoryBuiltInUserProperty.values())
                .filter(property -> property.name().equals(propKey))
                .findFirst()
                .map(ArtifactoryBuiltInUserProperty::isSensitive)
                .orElse(true);
    }

    /**
     * List of custom data which maps to build in user info properties in Artifactory
     */
    public enum ArtifactoryBuiltInUserProperty {
        artifactory_admin(false),
        private_key(true),
        public_key(true),
        gen_password_key(true),
        updatable_profile(false),
        bintray_auth(true),
        disabled_password(false);

        private boolean sensitive;

        ArtifactoryBuiltInUserProperty(boolean sensitive) {
            this.sensitive = sensitive;
        }

        public static boolean contains(String value) {
            return Arrays.stream(ArtifactoryBuiltInUserProperty.values())
                    .anyMatch(property -> property.name().equals(value));
        }

        public boolean isSensitive() {
            return sensitive;
        }
    }

    /**
     * Converts an Access user to Artifactory {@link UserInfo}.
     *
     * @param user User model from Access server
     * @return User info built from the Access user
     */
    @Nonnull
    public static MutableUserInfo toArtifactoryUser(@Nonnull UserBase user) {
        //TODO: [by YS] other custom properties (also encrypted)
        UserInfoBuilder builder = new UserInfoBuilder(user.getUsername())
                .email(user.getEmail())
                .admin(user.getBooleanCustomData(artifactory_admin.name()))
                .privateKey(user.getCustomData(private_key.name()))
                .publicKey(user.getCustomData(public_key.name()))
                .updatableProfile(user.getBooleanCustomData(updatable_profile.name()))
                .bintrayAuth(user.getCustomData(bintray_auth.name()))
                .lastLogin(user.getLastLoginTime(), user.getLastLoginIp())
                .credentialsExpired(user.isPasswordExpired())
                .genPasswordKey(user.getCustomData(gen_password_key.name()))
                .groups(groupsFromUser(user))
                .groupAdmin(isGroupAdmin(user))
                .passwordDisabled(user.getBooleanCustomData(disabled_password.name()))
                .password(new SaltedPassword(user.getPasswordHash(), null))
                .locked(UserStatus.LOCKED.equals(user.getStatus()))
                .realm(fromAccessRealm(user.getRealm()));

        user.getCustomData().entrySet().stream()
                .filter(entry -> !ArtifactoryBuiltInUserProperty.contains(entry.getKey()))
                .filter(entry -> !isSearchableHelperProperty(user, entry.getKey()))
                .map(entry -> new UserProperty(entry.getKey(), entry.getValue()))
                .forEach(builder::addProp);

        return builder.build();
    }

    private static Set<UserGroupInfo> groupsFromUser(UserBase user) {
        return Stream.of(user)
                .flatMap(UserMapper::getUserGroups)
                .map((nameRealm) -> InfoFactoryHolder.get().createUserGroup(nameRealm.getLeft(), fromAccessRealm(nameRealm.getRight())))
                .collect(Collectors.toSet());
    }

    private static Stream<Pair<String, Realm>> getUserGroups(UserBase user) {
        if (user instanceof User) {
            return ((User) user).getGroups().stream()
                    .map((group) -> Pair.of(group, Realm.INTERNAL));
        } else if (user instanceof UserWithGroups) {
            return ((UserWithGroups) user).getGroups().stream()
                    .map((group) -> Pair.of(group.getName(), group.getRealm()));
        }

        throw new IllegalArgumentException(user.getClass().getCanonicalName() +
                " is not a supported child of " + UserBase.class.getCanonicalName());
    }

    private static Boolean isGroupAdmin(UserBase user) {
        if (user instanceof UserWithGroups) {
            return ((UserWithGroups) user).getGroups().stream().map(GroupMapper::toArtifactoryGroup)
                    .anyMatch(GroupInfo::isAdminPrivileges);
        }
        return null;

    }

    static UpdateUserRequest toUpdateUserRequest(MutableUserInfo user, boolean includeCustomProperties) {
        return toAccessUser(UpdateUserRequest.create(), user, includeCustomProperties)
                .passwordExpired(user.isCredentialsExpired());
    }

    /**
     * Converts an Artifactory user to Access {@link UserRequest} with or without any extra Artifactory user properties.
     *
     * @param user User model from Artifactory
     * @return User request built from the Artifactory user
     */
    @Nonnull
    public static UserRequest toAccessUser(@Nonnull UserInfo user, boolean includeCustomProperties) {
        return toAccessUser(UserRequest.create(), user, includeCustomProperties);
    }

    @Nonnull
    private static <T extends UserRequest> T toAccessUser(T builder, @Nonnull UserInfo user,
                                                          boolean includeCustomProperties) {
        builder.username(user.getUsername())
                .email(user.getEmail())
                .realm(toAccessRealm(user.getRealm()))
                .status(toAccessStatus(user))
                .groups(user.getGroups().stream().map(UserGroupInfo::getGroupName).collect(Collectors.toSet()));
        addCustomData(builder, user, includeCustomProperties);

        if (isUpdate(builder)) {
            if (user.getPassword() != null) {
                builder.password(user.getPassword());
            }
        } else {
            builder.password(user.getPassword());
        }
        return builder;
    }

    /**
     * Converts an Artifactory user to full Access user for import purposes (including any extra Artifactory user properties).
     *
     * @param user User model from Artifactory
     * @return Import User request built from the Artifactory user
     */
    @Nonnull
    public static ImportUserRequest toFullAccessUser(@Nonnull UserInfo user) {
        ImportUserRequest.Builder builder = ImportUserRequest.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .email(user.getEmail())
                .realm(toAccessRealm(user.getRealm()))
                .status(toAccessStatus(user))
                .created(ClockUtils.epochMillis())
                .modified(ClockUtils.epochMillis())
                .lastLoginTime(user.getLastLoginTimeMillis())
                .lastLoginIp(user.getLastLoginClientIp())
                .statusLastModified(ClockUtils.epochMillis())
                .passwordLastModified(user.isCredentialsExpired() ? 0 : getPasswordCreated(user))
                .groups(toUserGroups(user.getGroups()));

        addCustomData(builder, user, true);

        return builder.build();
    }

    private static long getPasswordCreated(UserInfo user) {
        return user.getUserProperty("passwordCreated").filter(StringUtils::isNumeric).map(Long::parseLong)
                .orElseGet(ClockUtils::epochMillis);
    }

    private static UserStatus toAccessStatus(@Nonnull UserInfo user) {
        return user.isEnabled() ? (user.isLocked() ? UserStatus.LOCKED : UserStatus.ENABLED) : UserStatus.DISABLED;
    }

    private static void addCustomData(CustomDataBuilder builder, UserInfo user, boolean includeCustomProperties) {
        // Might be already decrypted by access/artifactory, even though there are some cases like converters/import/etc...
        // that the data is saved in the old way
        UserInfo decryptedUser = decryptUser(user);

        addCustomData(builder, artifactory_admin, decryptedUser.isAdmin());
        addCustomData(builder, private_key, decryptedUser.getPrivateKey());
        addCustomData(builder, public_key, decryptedUser.getPublicKey());
        addCustomData(builder, gen_password_key, decryptedUser.getGenPasswordKey());
        addCustomData(builder, updatable_profile, decryptedUser.isUpdatableProfile());
        addCustomData(builder, bintray_auth, decryptedUser.getBintrayAuth());
        addCustomData(builder, disabled_password, decryptedUser.isPasswordDisabled());

        if (includeCustomProperties) {
            decryptedUser.getUserProperties()
                    .forEach(p -> builder.addCustomData(p.getPropKey(),
                            StringUtils.isBlank(p.getPropValue()) ? null : p.getPropValue(), true));
            //TODO [RK] they should have a special prefix so that they could be differentiated when we get user from access to RT
            // add search hints
                decryptedUser.getUserProperties().stream()
                        .map(p -> getSearchableProp(p.getPropKey(), p.getPropValue()))
                        .filter(Objects::nonNull)
                        .forEach(p -> builder.addCustomData(p.getKey(), p.getValue(), false));
        }
    }

    private static UserInfo decryptUser(UserInfo user) {
        return UserPropertiesSearchHelper.decryptUserProperties(decryptKeys(user));
    }

    private static UserInfo decryptKeys(UserInfo user) {
        if (user.getPrivateKey() != null && user.getPublicKey() != null) {
            EncodedKeyPair keyPair = new EncodedKeyPair(user.getPrivateKey(), user.getPublicKey());

            try {
                // All good?
                keyPair.decode(null, new DecryptionStatusHolder());
            } catch (Exception e) {
                if (ArtifactoryHome.get().getArtifactoryEncryptionWrapper() != null) {
                    log.trace("Keypair is not in the expected format. reformatting", e);
                    return firstFallbackOldArtifactoryFormat(user, keyPair);
                }
            }
        }

        return user;
    }

    private static UserInfo firstFallbackOldArtifactoryFormat(UserInfo user, EncodedKeyPair keyPair) {
        try {
            EncodedKeyPair encodedKeyPair = keyPair.toSaveEncodedKeyPair(ArtifactoryHome.get().getArtifactoryEncryptionWrapper());

            if (encodedKeyPair != null) {
                DecodedKeyPair decoded = encodedKeyPair.decode(ArtifactoryHome.get().getArtifactoryEncryptionWrapper(), new DecryptionStatusHolder());
                return reencodeUser(user, decoded);
            }
        } catch (Exception e) {
            log.trace("Artifactory old format converting failed", e);
        }

        return secondFallbackArtifactoryAESFormat(user, keyPair);
    }

    private static UserInfo secondFallbackArtifactoryAESFormat(UserInfo user, EncodedKeyPair keyPair) {
        try {
            DecodedKeyPair decoded = keyPair.decode(ArtifactoryHome.get().getArtifactoryEncryptionWrapper(), new DecryptionStatusHolder());
            return reencodeUser(user, decoded);
        } catch (Exception e) {
            log.trace("Last fallback failed");
            return user;
        }
    }

    private static UserInfo reencodeUser(UserInfo user, DecodedKeyPair decoded) {
        EncodedKeyPair encodedWithoutKey = new EncodedKeyPair(decoded, null);
        MutableUserInfo userInfo = InfoFactoryHolder.get().copyUser(user);
        userInfo.setPrivateKey(encodedWithoutKey.getEncodedPrivateKey());
        userInfo.setPublicKey(encodedWithoutKey.getEncodedPublicKey());
        return userInfo;
    }

    private static void addCustomData(CustomDataBuilder builder, ArtifactoryBuiltInUserProperty property, boolean value) {
        if (value || isUpdate(builder)) {
            builder.addCustomData(property.name(), value ? "true" : null, property.sensitive);
        }
    }

    private static void addCustomData(CustomDataBuilder builder, ArtifactoryBuiltInUserProperty property,
                                      String value) {
        if (!StringUtils.isBlank(value)) {
            builder.addCustomData(property.name(), value, property.sensitive);
        } else if (isUpdate(builder)) {
            builder.addCustomData(property.name(), null, false);
        }
    }

    private static Set<ImportUserRequest.UserGroup> toUserGroups(Set<UserGroupInfo> groups) {
        return groups.stream()
                .map(group -> new ImportUserRequest.UserGroup(group.getGroupName(), toAccessRealm(group.getRealm())))
                .collect(Collectors.toSet());
    }


    private static boolean isUpdate(Object builder) {
        return (builder instanceof UpdateUserRequest);
    }
}
