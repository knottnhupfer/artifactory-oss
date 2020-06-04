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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.docker;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * @author Chen Keinan
 */
public class DockerMetadata {

    @JsonProperty("id")
    public String id;
    @JsonProperty("parent")
    public String parent;
    @JsonProperty("created")
    public String created;
    @JsonProperty("container")
    public String container;
    @JsonProperty("docker_version")
    public String dockerVersion;
    @JsonProperty("author")
    public String author;
    @JsonProperty("config")
    public Config config;
    @JsonProperty("architecture")
    public String architecture;
    @JsonProperty("os")
    public String os;
    @JsonProperty("Size")
    public long size;

    public static class Config implements Serializable {
        @JsonProperty("Hostname")
        public String hostname;
        @JsonProperty("Domainname")
        public String domainname;
        @JsonProperty("User")
        public String user;
        @JsonProperty("Memory")
        public long memory;
        @JsonProperty("MemorySwap")
        public long memorySwap;
        @JsonProperty("CpuShares")
        public long cpuShares;
        @JsonProperty("CpuSet")
        public String cpuSet;
        @JsonProperty("AttachStdin")
        public boolean attachStdin;
        @JsonProperty("AttachStdout")
        public boolean attachStdout;
        @JsonProperty("AttachStderr")
        public boolean attachStderr;
        @JsonProperty("PortSpecs")
        public List<String> portSpecs;
        @JsonProperty("ExposedPorts")
        public JsonNode exposedPorts;
        @JsonProperty("Tty")
        public boolean tty;
        @JsonProperty("OpenStdin")
        public boolean openStdin;
        @JsonProperty("StdinOnce")
        public boolean stdinOnce;
        @JsonProperty("Env")
        public List<String> env;
        @JsonProperty("Cmd")
        public List<String> cmd;
        @JsonProperty("Image")
        public String image;
        @JsonProperty("Volumes")
        public JsonNode volumes;
        @JsonProperty("WorkingDir")
        public String workingDir;
        @JsonProperty("Entrypoint")
        public List<String> entrypoint;
        @JsonProperty("NetworkDisabled")
        public boolean networkDisabled;
        @JsonProperty("OnBuild")
        public List<String> onBuild;
    }
}
