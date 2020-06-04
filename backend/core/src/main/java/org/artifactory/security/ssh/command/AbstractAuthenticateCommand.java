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

package org.artifactory.security.ssh.command;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.sshd.common.RuntimeSshException;
import org.apache.sshd.common.SshConstants;
import org.apache.sshd.common.io.IoWriteFuture;
import org.apache.sshd.common.util.Buffer;
import org.apache.sshd.server.*;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.session.ServerSession;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.rest.restmodel.JsonUtil;
import org.artifactory.api.security.access.CreatedTokenInfo;
import org.artifactory.api.security.access.UserTokenSpec;
import org.artifactory.security.access.AccessService;
import org.artifactory.storage.security.service.UserGroupStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

/**
 * @author Chen Keinan
 */
public abstract class AbstractAuthenticateCommand implements Command, SessionAware, ChannelSessionAware {

    private static final Logger log = LoggerFactory.getLogger(AbstractAuthenticateCommand.class);
    protected static final String HREF = "href";

    protected CentralConfigService centralConfigService;
    protected UserGroupStoreService userGroupStoreService;
    private InputStream inputStream;
    private OutputStream outputStream;
    private OutputStream errStream;
    private ExitCallback callback;
    protected ServerSession serverSession;
    protected ChannelSession channelSession;

    public AbstractAuthenticateCommand(CentralConfigService centralConfigService,
            UserGroupStoreService userGroupStoreService, String command) {
        this.centralConfigService = centralConfigService;
        this.userGroupStoreService = userGroupStoreService;
        parseCommandDetails(command);
    }

    /**
     * parse command details send by ssh client
     *
     * @param command - command send by ssh client
     */
    protected abstract void parseCommandDetails(String command);

    @Override
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void setErrorStream(OutputStream errStream) {
        this.errStream = errStream;
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        this.callback = callback;
    }

    @Override
    public void start(Environment env) throws IOException {
        log.debug("Starting command");
        String urlBase = centralConfigService.getDescriptor().getUrlBase();
        if (StringUtils.isBlank(urlBase)) {
            throw new RuntimeSshException("Artifactory base url is not configure");
        }
        // start command action
        sendAuthHeader();
        sendEofSignal();
        sendExitCodeSignal();
        sendCloseSignal();
        // end command action
        log.debug("Finished command");
    }

    /**
     * update authentication header for response
     */
    protected abstract void sendAuthHeader() throws IOException;

    @Override
    public void destroy() {
        log.debug("Destroying command");
    }

    @Override
    public void setSession(ServerSession serverSession) {
        this.serverSession = serverSession;
    }

    @Override
    public void setChannelSession(ChannelSession channelSession) {
        this.channelSession = channelSession;
    }


    /**
     * get configure base url or use default
     *
     * @return base url
     */
    protected String getBaseUrl() {
        String urlBase = centralConfigService.getDescriptor().getUrlBase();
        if (StringUtils.isBlank(urlBase)) {
            urlBase = "http://127.0.0.1:8080/artifactory";
        }
        return urlBase;
    }

    /**
     * write response to output stream
     *
     * @param response - hhs response
     * @param header   - header with http data
     * @throws IOException
     */
    protected void writeResponse(Map<String, Object> response, Map<String, Object> header) throws IOException {
        updateResponseHeader(response, header);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
        outputStreamWriter.write((JsonUtil.jsonToString(response)));
        outputStreamWriter.flush();
    }

    protected void updateResponseHeader(Map<String, Object> response, Map<String, Object> header) {
        response.put("header", header);
    }

    /**
     * send end of file signal to server session
     *
     * @throws IOException
     */
    private void sendEofSignal() throws IOException {
        Buffer eofBuffer = serverSession.createBuffer(SshConstants.SSH_MSG_CHANNEL_EOF);
        eofBuffer.putInt(channelSession.getRecipient());
        IoWriteFuture eofPacket = serverSession.writePacket(eofBuffer);
        eofPacket.verify();
    }

    /**
     * send exist code signal to server session
     *
     * @throws IOException
     */
    private void sendExitCodeSignal() throws IOException {
        Buffer exitBuffer = serverSession.createBuffer(SshConstants.SSH_MSG_CHANNEL_REQUEST);
        exitBuffer.putInt(channelSession.getRecipient());
        exitBuffer.putString("exit-status");
        exitBuffer.putBoolean(false);
        exitBuffer.putInt(0);
        IoWriteFuture exitPacket = serverSession.writePacket(exitBuffer);
        exitPacket.verify();
    }

    /**
     * send close signal to server session
     *
     * @throws IOException
     */
    private void sendCloseSignal() throws IOException {
        Buffer closeBuffer = serverSession.createBuffer(SshConstants.SSH_MSG_CHANNEL_CLOSE);
        closeBuffer.putInt(channelSession.getRecipient());
        IoWriteFuture closePacket = serverSession.writePacket(closeBuffer);
        closePacket.verify();
    }

    /**
     * generate unique auth token
     *
     * @param username - user name to associate token
     * @return - unique token
     */
    protected CreatedTokenInfo generateUniqueToken(String username) {
        UserTokenSpec tokenSpec = UserTokenSpec.create(username)
                .refreshable(false)
                .scope(Lists.newArrayList("member-of-groups:*"));
                // Expiry is 3600 (default value )
        CreatedTokenInfo token = ContextHelper.get().beanForType(AccessService.class).createToken(tokenSpec);
        if(token != null) {
            return token;
        }
        throw new RuntimeException("error generating new token for authentication");
    }

}
