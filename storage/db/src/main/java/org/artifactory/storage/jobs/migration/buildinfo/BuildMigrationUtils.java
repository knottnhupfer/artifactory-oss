package org.artifactory.storage.jobs.migration.buildinfo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.artifactory.storage.db.build.entity.BuildEntity;
import org.jfrog.common.JsonUtils;
import org.slf4j.Logger;

public abstract class BuildMigrationUtils {

    //Properties must be instance of ObjectNode
    static String fixBuildProperties(String build, BuildEntity buildEntity, Logger migrationLog) {
        JsonNode jsonNode = JsonUtils.getInstance().readTree(build);
        JsonNode properties = jsonNode.get("properties");
        if (properties != null) {
            properties.fieldNames().forEachRemaining(
                    property -> {
                        JsonNode node = properties.get(property);
                        if (node.isValueNode()) {
                            ((ObjectNode) properties).put(property, node.asText());
                        } else {
                            ((ObjectNode) properties).put(property, node.toString());
                            if (migrationLog != null) {
                                migrationLog
                                        .warn("Invalid property value for build id {} build name {} build number {}, property name : {}, modified value {}",
                                                buildEntity.getBuildId(), buildEntity.getBuildName(),
                                                buildEntity.getBuildNumber(), property, node);
                            }
                        }
                    });
        }
        return JsonUtils.getInstance().writeValueAsString(jsonNode);
    }

    public static String fixBuildProperties(String build) {
        return fixBuildProperties(build, null, null);
    }

}
