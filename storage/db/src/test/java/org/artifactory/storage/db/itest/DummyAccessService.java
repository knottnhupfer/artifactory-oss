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

package org.artifactory.storage.db.itest;

import org.artifactory.api.security.access.CreatedTokenInfo;
import org.artifactory.api.security.access.TokenInfo;
import org.artifactory.api.security.access.TokenSpec;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.security.SecurityInfo;
import org.artifactory.security.access.AccessService;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.access.client.AccessClient;
import org.jfrog.access.client.token.TokenResponse;
import org.jfrog.access.client.token.TokenVerifyResult;
import org.jfrog.access.common.ServiceId;
import org.jfrog.access.token.JwtAccessToken;
import org.jfrog.common.config.diff.DataDiff;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * @author Yinon Avraham.
 */
public class DummyAccessService implements AccessService {
    
    @Override
    public void initAccessService(String oldServiceId) {
        throw unexpectedMethodCallException();
    }

    @Nonnull
    @Override
    public CreatedTokenInfo createToken(@Nonnull TokenSpec tokenSpec) {
        throw unexpectedMethodCallException();
    }

    @Nonnull
    @Override
    public CreatedTokenInfo createToken(@Nonnull TokenSpec tokenSpec, boolean skipUserCanCreateLongLivedTokenAssertion) {
        throw unexpectedMethodCallException();
    }

    @Nonnull
    @Override
    public TokenResponse createTokenWithAccessAdminCredentials(@Nonnull String serviceId) {
        return null;
    }

    @Nonnull
    @Override
    public CreatedTokenInfo createNoPermissionToken(@Nonnull List<String> scope,
            @Nonnull TokenSpec tokenSpec, @Nullable String extraData) {
        throw unexpectedMethodCallException();
    }

    @Nonnull
    @Override
    public CreatedTokenInfo refreshToken(@Nonnull TokenSpec tokenSpec, @Nonnull String tokenValue, @Nonnull String refreshToken) {
        throw unexpectedMethodCallException();
    }

    @Nullable
    @Override
    public String extractSubjectUsername(@Nonnull JwtAccessToken accessToken) {
        throw unexpectedMethodCallException();
    }

    @Nonnull
    @Override
    public Collection<String> extractAppliedGroupNames(@Nonnull JwtAccessToken accessToken) {
        throw unexpectedMethodCallException();
    }

    @Override
    public void revokeToken(@Nonnull String tokenValue) {
        throw unexpectedMethodCallException();
    }

    @Override
    public void revokeTokenById(@Nonnull String tokenId) {
        throw unexpectedMethodCallException();
    }

    @Nonnull
    @Override
    public JwtAccessToken parseToken(@Nonnull String tokenValue) throws IllegalArgumentException {
        throw unexpectedMethodCallException();
    }

    @Override
    public boolean verifyToken(@Nonnull JwtAccessToken accessToken) {
        throw unexpectedMethodCallException();
    }

    @Override
    public boolean verifyTokenIfServiceIdChanged(@Nonnull JwtAccessToken accessToken) {
        return false;
    }

    @Override
    public TokenVerifyResult verifyAndGetResult(@Nonnull JwtAccessToken accessToken) {
        throw unexpectedMethodCallException();
    }

    @Nonnull
    @Override
    public ServiceId getArtifactoryServiceId() {
        throw unexpectedMethodCallException();
    }

    @Override
    public boolean isTokenAppliesScope(@Nonnull JwtAccessToken accessToken, @Nonnull String requiredScope) {
        throw unexpectedMethodCallException();
    }

    @Override
    public void registerAcceptedScopePattern(@Nonnull Pattern pattern) {
        throw unexpectedMethodCallException();
    }

    @Nonnull
    @Override
    public List<TokenInfo> getTokenInfos() {
        throw unexpectedMethodCallException();
    }

    @Override
    public AccessClient getAccessClient() {
        throw unexpectedMethodCallException();
    }

    @Override
    public void encryptOrDecrypt(boolean encrypt) {
        throw unexpectedMethodCallException();
    }

    @Override
    public void importSecurityEntities(SecurityInfo securityInfo, boolean override) {
        throw unexpectedMethodCallException();
    }

    @Override
    public void ping() {
        throw unexpectedMethodCallException();
    }

    @Override
    public void afterImport(ImportSettings settings) {
        throw unexpectedMethodCallException();
    }

    @Override
    public boolean isUsingBundledAccessServer() {
        return false;
    }

    @Override
    public void revokeAllForUserAndScope(String username, String scope) {

    }

    @Override
    public boolean isAdminUsingOldDefaultPassword() {
        return false;
    }

    @Override
    public <T> T ensureAuth(Callable<T> call) {
        return null;
    }

    @Override
    public void exportTo(ExportSettings settings) {
        throw unexpectedMethodCallException();
    }

    @Override
    public void importFrom(ImportSettings settings) {
        throw unexpectedMethodCallException();
    }

    @Override
    public void unregisterFromRouterIfNeeded() {
        throw unexpectedMethodCallException();
    }

    private RuntimeException unexpectedMethodCallException() {
        return new UnsupportedOperationException("This is a dummy implementation, none of its methods should be called!");
    }

    @Override
    public void init() {

    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor, List<DataDiff<?>> configDiff) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {

    }
}
