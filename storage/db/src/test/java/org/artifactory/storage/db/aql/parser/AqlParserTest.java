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

package org.artifactory.storage.db.aql.parser;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gidi Shabat
 */
@Test
public class AqlParserTest {

    @Test
    public void findActionParseSuccess() throws Exception {
        AqlParser sm = new AqlParser();
        assertValid(sm, "items.find({\"repo\":{\"$match\":\"jc*\"}})");
        assertValid(sm, "items.find({\"original_sha1\" :\"sha1_orig\"})");
        assertValid(sm, "items.find({\"actual_sha1\" :\"sha1_act\"})");
        assertValid(sm, "items.find({\"original_md5\" :\"md5_orig\"})");
        assertValid(sm, "items.find({\"actual_md5\" :\"md5_act\"})");
        assertValid(sm, "items.find({\"sha256\" :\"sha2\"})");
        assertValid(sm, "items.find({\"@license\":{\"$eq\":\"GPL\"}})");
        assertValid(sm, "items.find({\"@license\":{\"$eq\":\"GPL\"}})");
        assertValid(sm, "items.find({\"@license\":{\"$eq\":\"GPL\"},\"@license\":{\"$eq\":\"GPL\"},\"$or\":[{\"@license\":{\"$eq\":\"GPL\"}}]})");
        assertValid(sm, "items.find({\"@license\":{\"$eq\":\"GPL\"},\"@license\":{\"$lt\":\"GPL\"},\"$or\":[{\"@license\":{\"$eq\":\"GPL\"},\"@license\":{\"$eq\":\"GPL\"}}]})");
        assertValid(sm, "items.find({\"@license\":{\"$eq\":\"GPL\"},\"$or\":[{\"@license\":{\"$eq\":\"GPL\"},\"@license\":{\"$eq\":\"GPL\"},\"@license\":{\"$eq\":\"GPL\"}}]})");
        assertValid(sm, "items.find({\"$or\":[{\"@license\":\"GPL\",\"@license\":\"GPL\"}]})");
        assertValid(sm, "items.find({\"$or\":[{\"repo\":\"jcentral\"},{\"type\":1},{\"$or\":[{\"$or\":[{\"@version\":\"1,1,1\"},{\"type\":1}]},{\"@version\":\"2.2.2\"}]}]})");
        assertValid(sm, "items.find({\"$msp\":[{\"repo\":\"jcentral\"},{\"type\":1}]})");
        assertValid(sm, "items.find({\"$msp\":[{\"repo\":\"jcentral\",\"type\":1}]})");
        assertValid(sm, "items.find({\"repo\":\"jcentral\",\"type\":1})");
        assertValid(sm, "items.find({\"repo\":\"jcentral\"},{\"type\":1})");
        assertValid(sm, "items.find({\"$msp\":[{\"repo\":\"jcentral\"},{\"type\":1}]})");
        assertValid(sm, "items.find({\"$msp\":[{\"repo\":\"jcentral\"},{\"type\":1}]}).sort({\"$asc\" : [\"name\", \"repo\" ]})");
        assertValid(sm, "items.find({\"@*\" : {\"$eq\" : \"1.1*\"}})");
        assertValid(sm, "items.find({\"@ver*\" : {\"$eq\" : \"*\" }})");
        assertValid(sm, "items.find({\"@ver*\" : {\"$eq\" : \"*\"}})");
        assertValid(sm, "items.find()");
        assertValid(sm, "items.find({\"@*\" : {\"$eq\" : \"1.1*\"}}).limit(10)");
        assertValid(sm, "items.find({\"archive.entry.name\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "items.find({\"artifact.module.name\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "items.find({\"artifact.module.build.name\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "builds.find({\"module.dependency.name\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "builds.find({\"module.dependency.item.repo\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "builds.find({\"module.dependency.item.name\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "modules.find({\"name\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "modules.find({\"artifact.item.name\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "modules.find({\"artifact.item.@test\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "modules.find({\"build.number\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "dependencies.find({\"item.name\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "dependencies.find({\"scope\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "dependencies.find({\"module.name\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "build.properties.find({\"key\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "build.properties.find({\"build.number\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "build.properties.find({\"value\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "properties.find({\"item.repo\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "items.find({\"stat.downloads\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "artifacts.find({\"type\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "artifacts.find({\"module.name\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "artifacts.find({\"item.path\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "builds.find({\"module.artifact.item.@path\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "items.find({\"path\" : {\"$eq\" : \"a.a\"},\"path\" :\"path\" }).limit(10)");
        assertValid(sm, "items.find({\"path\" : {\"$eq\" : \"a.a\"},\"@path\" :\"path\" }).limit(10)");
        assertValid(sm, "items.find({\"path\" : {\"$eq\" : \"a.a\"},\"@path\" :\"path\" }).include(\"repo\",\"archive.entry.name\").limit(10)");
        assertValid(sm, "items.find({\"path\" : {\"$eq\" : \"a.a\"},\"@path\" :\"path\" }).include(\"@repo\",\"property.value\").limit(10)");
        assertValid(sm, "items.find({\"path\" : {\"$eq\" : \"a.a\"},\"@path\" :\"path\" }).include(\"repo\",\"@repo\",\"property.value\").limit(10)");
        assertValid(sm, "items.find({\"path\" : {\"$eq\" : \"a.a\"},\"@path\" :\"path\" }).include(\"archive.entry.name\").limit(10)");
        assertValid(sm, "items.find({\"path\" : \"a.a\"}).include(\"repo\",\"path\",\"name\",\"virtual_repos\")");
        assertValid(sm, "items.find({\"@*\" : {\"$eq\" : \"1.1*\"}}).limit(10)");
        assertValid(sm, "items.find({\"@*\" : {\"$eq\" : null}}).limit(10)");
        assertValid(sm, "items.find({\"@*\" : {\"$eq\" : null}}).offset(145).limit(10)");
        assertValid(sm, "items.find({\"@*\" : {\"$eq\" : \"test\"}}).offset(145).limit(10)");
        assertValid(sm, "builds.find({\"module.artifact.item.@*\" : {\"$eq\" : \"test\"}}).offset(145).limit(10)");
        assertValid(sm, "builds.find({\"module.artifact.item.@key\" : {\"$eq\" : \"*\"}}).offset(145).limit(10)");
        assertValid(sm, "build.properties.find({\"build.module.artifact.item.@key\" : {\"$eq\" : \"*\"}}).offset(145).limit(10)");
        assertValid(sm, "build.properties.find({\"module.artifact.item.@key\" : {\"$eq\" : \"*\"}}).offset(145).limit(10)");
        assertValid(sm, "build.properties.find({\"@key\" : {\"$eq\" : \"*\"}}).offset(145).limit(10)");
        assertValid(sm, "build.properties.find({\"@*\" : {\"$eq\" : \"*\"}}).offset(145).limit(10)");
        assertValid(sm, "build.properties.find({\"@*\" : {\"$eq\" : \"sdsdsdsd*\"}}).offset(145).limit(10)");
        assertValid(sm, "module.properties.find({\"module.artifact.item.@key\" : {\"$eq\" : \"*\"}}).offset(145).limit(10)");
        assertValid(sm, "module.properties.find({\"@key\" : {\"$eq\" : \"*\"}}).offset(145).limit(10)");
        assertValid(sm, "module.properties.find({\"*\" : {\"$eq\" : \"*\"}}).offset(145).limit(10)");
        assertValid(sm, "module.properties.find({\"*\" : {\"$eq\" : \"sdsdsdsd*\"}}).offset(145).limit(10)");
        assertValid(sm, "modules.find({\"@*\" : {\"$eq\" : \"sdsdsdsd*\"}}).offset(145).limit(10)");
        assertValid(sm, "build.properties.find({\"module.number\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "build.properties.find({\"repo\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "archive.entries.find({\"archive.item.repo\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "build.promotions.find({\"repo\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "archive.entries.find({\"archive.item.artifact.module.build.created\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "archive.entries.find({\"archive.item.artifact.module.build.created\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertValid(sm, "items.find({\"created\" : {\"$before\" : \"5D\"}}).limit(10)");
        assertValid(sm, "items.find({\"created\" : {\"$before\" : \"5Y\"}}).limit(10)");
        assertValid(sm, "archive.entries.find({\"archive.item.artifact.module.build.created\" : {\"$before\" : \"10Days\"}}).limit(10)");
        assertValid(sm, "archive.entries.find({\"archive.item.artifact.module.build.created\" : {\"$before\" : \"10years\"}}).limit(10)");
        assertValid(sm, "archive.entries.find({\"archive.item.artifact.module.build.created\" : {\"$before\" : \"10Minutes\"}}).limit(10)");
        assertValid(sm, "archive.entries.find({\"archive.item.artifact.module.build.created\" : {\"$before\" : \"10S\"}}).limit(10)");
        assertValid(sm, "archive.entries.find({\"archive.item.artifact.module.build.created\" : {\"$last\" : \"10D\"}}).limit(10)");
        assertValid(sm, "archive.entries.find({\"archive.item.artifact.module.build.created\" : {\"$last\" : \"10S\"}}).limit(10)");
        assertValid(sm, "archive.entries.find({\"archive.item.artifact.module.build.created\" : {\"$last\" : \"10Minute\"}}).limit(10)");
        assertValid(sm, "archive.entries.find({\"archive.item.artifact.module.build.created\" : {\"$last\" : \"10second\"}}).limit(10)");
        assertValid(sm, "stats.find({\"downloads\":{\"$gt\":\"7\"}})");
        assertValid(sm, "stats.find({\"remote_downloads\":{\"$gt\":\"7\"}})");
        assertValid(sm, "stats.find({\"item.repo\":\"repo1\"}).include(\"downloads\",\"downloaded\",\"downloaded_by\")");
        assertValid(sm, "stats.find({\"item.repo\":\"repo1\"}).include(\"remote_downloads\",\"remote_downloaded\",\"remote_downloaded_by\",\"remote_origin\",\"remote_path\")");
        assertValid(sm, "items.find({\"repo\":\"repo1\",\"stat.remote_origin\":\"other\"}).include(\"repo\",\"path\",\"name\",\"stat.remote_downloads\",\"stat.downloads\")");
        assertValid(sm, "releases.find({\"id\":\"2\"})");
        assertValid(sm, "releases.find({\"release_artifact.path\":\"2\"})");
        assertValid(sm, "release_artifacts.find({\"id\":\"2\"})");
        assertValid(sm, "release_artifacts.find({\"release.name\":\"2\"})");
        assertValid(sm, "release_artifacts.find({\"item.name\":\"2\"})");
        assertValid(sm, "items.find({\"release_artifact.id\":\"2\"})");
        assertValid(sm, "archives.find({\"item.release_artifact.id\":\"2\"})");
        assertValid(sm, "modules.find({\"artifact.item.release_artifact.path\":\"2\"})");
        assertValid(sm, "modules.find({\"artifact.item.release_artifact.release.type\":\"TARGET\"})");
        assertValid(sm, "builds.find({\"module.dependency.item.release_artifact.item_id\":\"2\"})");
        assertValid(sm, "build.properties.find({\"key\":\"2\"},{\"build.module.dependency.item.release_artifact.release.signature\":\"2\"})");
        assertValid(sm, "builds.find({\"@blabla\":\"2\"},{\"module.dependency.item.release_artifact.release.version\":\"2\"})");
        assertValid(sm, "builds.find({\"@blabla\":\"2\"},{\"module.dependency.item.release_artifact.release.version\":null})");
        assertValid(sm, "builds.find({\"@blabla\":\"2\"},{\"module.dependency.item.release_artifact.release.id\":2})");
        assertValid(sm, "builds.find({\"@blabla\":\"2\"},{\"module.dependency.item.release_artifact.release.id\":2}).include(\"module.dependency.item.release_artifact\")");
        assertValid(sm, "builds.find({\"started\" : {\"$before\" : \"5D\"}}).limit(10)");
        assertValid(sm, "items.find({\"release_artifact.release.name\":\"2\"})");
        assertValid(sm, "items.find({\"release_artifact.release.type\":\"SOURCE\"})");
        assertValid(sm, "items.find({\"release_artifact.release.release_artifact.item.dependency.module.build.@bla\":\"2\"})");
        assertValid(sm, "items.find({\"release_artifact.release.release_artifact.item.dependency.module.build.@bla*\":\"2\"})");
        assertValid(sm, "items.find({\"release_artifact.release.release_artifact.item.dependency.module.build.@*\":\"2\"})");
        assertValid(sm, "items.find({\"release_artifact.release.release_artifact.item.dependency.module.build.@*\":\"*\"})");
        assertValid(sm, "items.find({\"release_artifact.release.release_artifact.item.dependency.module.name\":\"*\"})");
        assertValid(sm, "items.find({\"release_artifact.release.release_artifact.item.dependency.module.@bla\":\"2\"})");
        assertValid(sm, "items.find({\"release_artifact.release.release_artifact.item.@bla\":\"2\"})");
    }

    @Test
    public void findActionParseFail() throws Exception {
        AqlParser sm = new AqlParser();
        assertInvalid(sm, "artifacts({\"$names\" : [}).find({\"ver*\" : \"$eq\"})");
        assertInvalid(sm, "artifacts({\"$names\").find({\"ver*\" : \"$eq\"})");
        assertInvalid(sm, "artifacts().find({\"ver*\" : \"$eq\"})");
        assertInvalid(sm, "artifacts().find({\"blabla\" : \"jjhj\"})");
        assertInvalid(sm, "builds.find({\"module.dependency.nameg\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertInvalid(sm, "modules.find({\"repo\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertInvalid(sm, "dependencies.find({\"repo\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertInvalid(sm, "items.find({\"downloads\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertInvalid(sm, "items.find({\"repo\" : \"myrepo\", \"virtual_repos\" : \"v\"})");
        assertInvalid(sm, "artifacts.find({\"module.type\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertInvalid(sm, "artifacts.find({\"module\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertInvalid(sm, "artifacts.find({\"module.\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertInvalid(sm, "items.find({\"property.item.@*\" : {\"$eq\" : \"1.1*\"}}).limit(10)");
        assertInvalid(sm, "items.find().include(\"repo3\").limit(10)");
        assertInvalid(sm, "items.find().include(\"repo3\").offset(45)limit(10)");
        assertInvalid(sm, "items.find().include(\"repo3\").offser(45).limit(10)");
        assertInvalid(sm, "archive.entries.find({\"archive.item.module.build.created\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertInvalid(sm, "archive.entries.find({\"archive.item.artifact.module.build.created\" : {\"$before\" : \"10Minutes\"}}).limit(10).dryRun(\"true\")");
        assertInvalid(sm, "items.find({\"release.name\":\"2\"})");
        assertInvalid(sm, "releases.find({\"items.repo\":\"2\"})");
        assertInvalid(sm, "releases.find({\"release_artifact.@bla\":\"2\"})");
        assertInvalid(sm, "releases.find({\"id\":\"2\"}).include(\"repo_path\")");
        assertInvalid(sm, "release_artifacts.find({\"item.name\":\"2\"}).include(\"name\")");
        assertInvalid(sm, "release_artifacts.find({\"release.@bla\":\"2\"}).include(\"name\")");
        assertInvalid(sm, "builds.find({\"@blabla\":\"2\"},{\"module.dependency.item.release_artifact.release.id\":2}).include(\"module.dependency.item.release_artifactv\")");
        assertInvalid(sm, "builds.find({\"@blabla\":\"2\"},{\"module.item.release_artifact.release.id\":2}).include(\"module.dependency.item.release_artifactv\")");
        assertInvalid(sm, "builds.find({\"release.id\":2})");
        assertInvalid(sm, "builds.find({\"release_artifact.release.id\":2})");

    }

    @Test
    public void deleteActionParseSuccess() throws Exception {
        AqlParser sm = new AqlParser();
        assertValid(sm, "items.delete({\"@key\" : {\"$eq\" : \"bla\"}})");
        assertValid(sm, "items.delete({\"@key\" : {\"$eq\" : \"bla\"}}).dryRun(\"true\")");
        assertValid(sm, "items.delete({\"repo\":\"repo1\",\"stat.remote_origin\":\"other\"})");
        assertValid(sm, "items.delete({\"repo\":\"repo1\",\"stat.remote_origin\":\"other\"}).dryRun(\"false\")");
        assertValid(sm, "items.delete({\"@*\" : {\"$eq\" : \"1.1*\"}})");
        assertValid(sm, "items.delete({\"@*\" : {\"$eq\" : \"1.1*\"}}).dryRun(\"true\")");
        assertValid(sm, "items.delete({\"@*\" : {\"$eq\" : \"1.1*\"}}).dryRun(\"false\")");
        assertValid(sm, "items.delete({\"@*\" : {\"$eq\" : \"1.1*\"}}).include(\"repo\",\"path\",\"name\")");
        assertValid(sm, "items.delete({\"@*\" : {\"$eq\" : \"1.1*\"}}).include(\"repo\",\"path\",\"name\").dryRun(\"true\")");
        assertValid(sm, "items.delete({\"@*\" : {\"$eq\" : \"1.1*\"}}).include(\"repo\",\"path\",\"name\").dryRun(\"false\")");
        assertValid(sm, "items.delete({\"@key\" : {\"$eq\" : \"bla\"}}).include(\"repo\",\"path\",\"name\")");
        assertValid(sm, "items.delete({\"stat.downloads\" : {\"$eq\" : \"a.a\"}})");
        assertValid(sm, "items.delete({\"stat.downloads\" : {\"$eq\" : \"a.a\"}}).dryRun(\"true\")");
        assertValid(sm, "items.delete({\"stat.downloads\" : {\"$eq\" : \"a.a\"}}).dryRun(\"false\")");
        assertValid(sm, "items.delete({\"$msp\":[{\"repo\":\"jcentral\"},{\"type\":1}]})");
        assertValid(sm, "items.delete({\"repo\":\"repo1\",\"stat.remote_origin\":\"other\"}).include(\"repo\",\"path\",\"name\",\"stat.remote_downloads\",\"stat.downloads\")");
        assertValid(sm, "items.delete({\"repo\":\"repo1\",\"stat.remote_origin\":\"other\"}).include(\"repo\",\"path\",\"name\",\"stat.remote_downloads\",\"stat.downloads\").dryRun(\"true\")");
        assertValid(sm, "items.delete({\"repo\":\"repo1\",\"stat.remote_origin\":\"other\"}).include(\"repo\",\"path\",\"name\",\"stat.remote_downloads\",\"stat.downloads\").dryRun(\"false\")");
        assertValid(sm, "items.delete({\"@key\" : {\"$eq\" : \"bla\"}}).dryRun(\"false\")");
        assertValid(sm, "items.delete({\"@key\" : {\"$eq\" : \"bla\"}})");
        assertValid(sm, "items.delete({\"@key\" : {\"$eq\" : \"bla\"}}).dryRun(\"true\")");
        assertValid(sm, "items.delete({\"@key\" : {\"$eq\" : \"bla\"}})");
        assertValid(sm, "items.delete({\"@*\" : {\"$eq\" : \"1.1*\"}})");
    }

    @Test
    public void deleteActionParseFail() throws Exception {
        AqlParser sm = new AqlParser();
        assertInvalid(sm, "archive.entries.delete({\"archive.item.artifact.module.build.created\" : {\"$lastf\" : \"10df\"}}).limit(10)");
        assertInvalid(sm, "items.delete({\"@key\" : {\"$eq\" : \"bla\"}}).limit(10)");
        assertInvalid(sm, "items.delete({\"@key\" : {\"$eq\" : \"bla\"}}).offset(145)");
        assertInvalid(sm, "items.delete({\"@key\" : {\"$eq\" : \"bla\"}}).sort({\"$asc\" : [\"name\", \"repo\" ]})\"");
        assertInvalid(sm, "builds.delete({\"module.dependency.name\" : {\"$eq\" : \"a.a\"}})");
        assertInvalid(sm, "builds.delete({\"module.dependency.name\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertInvalid(sm, "modules.delete({\"name\" : {\"$eq\" : \"a.a\"}})");
        assertInvalid(sm, "modules.delete({\"name\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertInvalid(sm, "dependencies.delete({\"item.name\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertInvalid(sm, "build.properties.delete({\"key\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertInvalid(sm, "build.promotions.delete({\"repo\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertInvalid(sm, "archive.entries.delete({\"archive.item.artifact.module.build.created\" : {\"$eq\" : \"a.a\"}}).limit(10)");
        assertInvalid(sm, "stats.delete({\"remote_downloads\":{\"$gt\":\"7\"}}).limit(10)");
        assertInvalid(sm, "items.delete({\"@*\" : {\"$eq\" : \"1.1*\"}}).limit(10)");
        assertInvalid(sm, "archive.entries.delete({\"archive.item.artifact.module.build.created\" : {\"$before\" : \"10Minutes\"}})");
        assertInvalid(sm, "dependencies.delete({\"item.name\" : {\"$eq\" : \"a.a\"}})");
        assertInvalid(sm, "build.properties.delete({\"key\" : {\"$eq\" : \"a.a\"}})");
        assertInvalid(sm, "build.promotions.delete({\"repo\" : {\"$eq\" : \"a.a\"}})");
        assertInvalid(sm, "archive.entries.delete({\"archive.item.artifact.module.build.created\" : {\"$eq\" : \"a.a\"}})");
        assertInvalid(sm, "stats.delete({\"remote_downloads\":{\"$gt\":\"7\"}})");
        assertInvalid(sm, "items.delete({\"@*\" : {\"$eq\" : \"1.1*\"}})..dryRun(\"true\")");
    }

    @Test
    public void updatePropertiesActionParseSuccess() throws Exception {
        AqlParser sm = new AqlParser();
        //.dryRun("true")
        assertValid(sm, "properties.update({\"item.repo\" : {\"$eq\" : \"a.a\"}}).keys(\"bla\").newValue(\"val\")");
        assertValid(sm, "properties.update({\"item.repo\" : {\"$eq\" : \"a.a\"}}).keys(\"bla\", \"blabla\").newValue(\"val\").dryRun(\"false\")");
        assertValid(sm, "properties.update({\"item.repo\" : {\"$eq\" : \"a.a\"}}).keys(\"bla\", \"blabla\").newValue(\"val\").dryRun(\"true\")");
        assertValid(sm, "properties.update({\"@key\" : {\"$eq\" : \"bla\"}}).keys(\"bla\").newValue(\"val\")");
        assertValid(sm, "properties.update({\"@key\" : {\"$eq\" : \"bla\"}}).keys(\"bla\", \"blabla\").newValue(\"val\").dryRun(\"true\")");
        assertValid(sm, "properties.update({\"@key\" : {\"$eq\" : \"bla\"}}).keys(\"bla\").newValue(\"val\").dryRun(\"false\")");

        //assertValid(sm, "items.delete({\"repo\":\"repo1\",\"stat.remote_origin\":\"other\"}).dryRun(\"false\")");
    }

    @Test
    public void updatePropertiesActionParseFail() throws Exception {
        AqlParser sm = new AqlParser();
        assertInvalid(sm, "properties.update({\"item.repo\" : {\"$eq\" : \"a.a\"}}).newValue(\"val\")");
        assertInvalid(sm, "properties.update({\"@key\" : {\"$eq\" : \"bla\"}}).newValue(\"val\")");
        assertInvalid(sm, "items.update({\"@key\" : {\"$eq\" : \"bla\"}}).newValue(\"val\").dryRun(\"false\")");
        assertInvalid(sm, "properties.update({\"@key\" : {\"$eq\" : \"bla\"}})");
        assertInvalid(sm, "builds.update({\"module.dependency.name\" : {\"$eq\" : \"a.a\"}})");
        assertInvalid(sm, "modules.update({\"name\" : {\"$eq\" : \"a.a\"}})");
        assertInvalid(sm, "dependencies.update({\"item.name\" : {\"$eq\" : \"a.a\"}})");
        assertInvalid(sm, "properties.update({\"item.repo\" : {\"$eq\" : \"a.a\"}})");
        assertInvalid(sm, "properties.update({\"@key\" : {\"$eq\" : \"bla\"}})");
        assertInvalid(sm, "modules.update({\"name\" : {\"$eq\" : \"a.a\"}})");
        assertInvalid(sm, "dependencies.update({\"item.name\" : {\"$eq\" : \"a.a\"}})");
        assertInvalid(sm, "build.properties.update({\"key\" : {\"$eq\" : \"a.a\"}})");
        assertInvalid(sm, "archive.entries.update({\"archive.item.artifact.module.build.created\" : {\"$eq\" : \"a.a\"}})");
        assertInvalid(sm, "stats.update({\"remote_downloads\":{\"$gt\":\"7\"}})");
        assertInvalid(sm, "builds.update({\"module.dependency.name\" : {\"$eq\" : \"a.a\"}}).newValue(\"val\")");
        assertInvalid(sm, "modules.update({\"name\" : {\"$eq\" : \"a.a\"}}).newValue(\"val\")");
        assertInvalid(sm, "dependencies.update({\"item.name\" : {\"$eq\" : \"a.a\"}}).newValue(\"val\")");
        assertInvalid(sm, "build.properties.update({\"key\" : {\"$eq\" : \"a.a\"}}).newValue(\"val\")");
        assertInvalid(sm, "archive.entries.update({\"archive.item.artifact.module.build.created\" : {\"$eq\" : \"a.a\"}}).newValue(\"val\")");
        assertInvalid(sm, "stats.update({\"remote_downloads\":{\"$gt\":\"7\"}}).newValue(\"val\")");
    }

    private void assertValid(AqlParser sm, String script) throws Exception {
        sm.parse(script);
    }

    private void assertInvalid(AqlParser sm, String script) throws Exception {
        try {
            sm.parse(script);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }
}
