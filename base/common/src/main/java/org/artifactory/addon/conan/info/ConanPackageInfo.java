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

package org.artifactory.addon.conan.info;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Yinon Avraham
 */
public class ConanPackageInfo {

    private String os;
    private String arch;
    private String buildType;
    private String compiler;
    private String compilerVersion;
    private String compilerRuntime;
    private final Map<String, String> settings = Maps.newHashMap();
    private final Map<String, String> options = Maps.newHashMap();
    private final List<String> requires = Lists.newArrayList();

    private ConanPackageInfo() { }

    public String getOs() {
        return os;
    }

    public String getArchitecture() {
        return arch;
    }

    public String getBuildType() {
        return buildType;
    }

    public String getCompiler() {
        return compiler;
    }

    public String getCompilerVersion() {
        return compilerVersion;
    }

    public String getCompilerRuntime() {
        return compilerRuntime;
    }

    public Map<String, String> getSettings() {
        return Collections.unmodifiableMap(settings);
    }

    public Map<String, String> getOptions() {
        return Collections.unmodifiableMap(options);
    }

    public List<String> getRequires() {
        return Collections.unmodifiableList(requires);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ConanPackageInfo packageInfo = new ConanPackageInfo();

        private Builder() {}

        public Builder os(String os) {
            packageInfo.os = os;
            return this;
        }

        public Builder arch(String arch) {
            packageInfo.arch = arch;
            return this;
        }

        public Builder buildType(String buildType) {
            packageInfo.buildType = buildType;
            return this;
        }

        public Builder compiler(String compiler) {
            packageInfo.compiler = compiler;
            return this;
        }

        public Builder compilerVersion(String compilerVersion) {
            packageInfo.compilerVersion = compilerVersion;
            return this;
        }

        public Builder compilerRuntime(String compilerRuntime) {
            packageInfo.compilerRuntime = compilerRuntime;
            return this;
        }

        public Builder setting(String name, String value) {
            packageInfo.settings.put(name, value);
            return this;
        }

        public Builder settings(Map<String, String> settings) {
            packageInfo.settings.putAll(settings);
            return this;
        }

        public Builder option(String name, String value) {
            packageInfo.options.put(name, value);
            return this;
        }

        public Builder options(Map<String, String> options) {
            packageInfo.options.putAll(options);
            return this;
        }

        public Builder require(String require) {
            packageInfo.requires.add(require);
            return this;
        }

        public Builder requires(List<String> requires) {
            packageInfo.requires.addAll(requires);
            return this;
        }

        public ConanPackageInfo create() {
            ConanPackageInfo result = packageInfo;
            packageInfo = null; // ensure this builder can no longer modify the instance
            return result;
        }
    }
}
