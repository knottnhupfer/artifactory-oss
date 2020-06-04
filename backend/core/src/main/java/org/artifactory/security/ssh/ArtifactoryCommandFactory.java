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

package org.artifactory.security.ssh;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.command.UnknownCommand;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.gitlfs.GitLfsAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.security.ssh.command.CliAuthenticateCommand;
import org.artifactory.storage.security.service.UserGroupStoreService;

/**
 * @author Noam Y. Tenne
 * @author Chen Keinan
 */
public class ArtifactoryCommandFactory implements CommandFactory {

    private CentralConfigService centralConfigService;
    private UserGroupStoreService userGroupStoreService;

    public ArtifactoryCommandFactory(CentralConfigService centralConfigService,
            UserGroupStoreService userGroupStoreService) {
        this.centralConfigService = centralConfigService;
        this.userGroupStoreService = userGroupStoreService;
    }

    @Override
    public Command createCommand(String command) {
        GitLfsAddon gitLfsAddon = ContextHelper.get().beanForType(AddonsManager.class).addonByType(GitLfsAddon.class);
        if (gitLfsAddon.isGitLfsCommand(command)) {
            return gitLfsAddon.createGitLfsCommand(command);
        }
        if (isCliCommand(command)) {
            return new CliAuthenticateCommand(centralConfigService, userGroupStoreService, command);
        }
        return new UnknownCommand(command);
    }

    private boolean isCliCommand(String command) {
        return CliAuthenticateCommand.COMMAND_NAME.startsWith(command);
    }
}
