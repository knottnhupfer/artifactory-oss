package org.artifactory.addon.support;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.*;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.jfrog.support.rest.model.BundleCreationStatus;
import org.jfrog.support.rest.model.SupportBundleConfig;
import org.jfrog.support.rest.model.SupportBundleParameters;
import org.jfrog.support.rest.model.SupportBundleParameters.Logs;
import org.jfrog.support.rest.model.SupportBundleParameters.ThreadDump;
import org.jfrog.support.rest.model.manifest.NodeManifestBundleInfo;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * This class exists solely because of Artifactory's Jersey+Jackson combination (that is mandated by the Jersey version)
 * Once upgraded, we can drop this class and switch to the models given in {@link org.jfrog.support.rest.model.SupportBundleConfig}
 * which is exactly the same (but uses Jersey2 notation)
 *
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString(exclude = {"parameters"})
public class ArtifactorySupportBundleConfig {

    private String id;
    private String name;
    private String description;
    private ArtifactorySupportBundleParameters parameters;
    private String status = BundleCreationStatus.IN_PROGRESS.toString();
    @JsonSerialize(using = CustomDateSerializer.class)
    private Date created = new Date();

    public static NodeManifestBundleInfo fromSupportBundleConfig(SupportBundleConfig config) {
        String status = config.getStatus() == null ? null : config.getStatus().toString();
        return NodeManifestBundleInfo.builder()
                .id(config.getId())
                .name(config.getName())
                .description(config.getDescription())
                .created(config.getCreated())
                .status(status)
                .build();
    }

    SupportBundleConfig toCommonSupportBundleModel() {
        SupportBundleConfig.SupportBundleConfigBuilder builder = SupportBundleConfig.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .parameters(paramsToCommonParams())
                .status(BundleCreationStatus.fromJson(this.status));
        if (created != null) {
            builder.created(ZonedDateTime.ofInstant(created.toInstant(),
                    ZoneId.systemDefault()));
        } else {
            builder.created(ZonedDateTime.now());
        }
        return builder.build();
    }

    private SupportBundleParameters paramsToCommonParams() {
        SupportBundleParameters params = null;
        if (this.parameters != null) {
            params = new SupportBundleParameters();
            params.setConfiguration(this.parameters.isConfiguration());
            params.setSystem(this.parameters.isSystem());

            Logs logs = new Logs();
            logs.setInclude(this.parameters.getLogs().isInclude());
            logs.setStartDate(this.parameters.getLogs().getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            logs.setEndDate(this.parameters.getLogs().getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            params.setLogs(logs);

            ThreadDump threadDump = new ThreadDump();
            threadDump.setCount(this.parameters.getThreadDump().getCount());
            threadDump.setInterval(this.parameters.getThreadDump().getInterval());
            params.setThreadDump(threadDump);
        }
        return params;
    }

    public static class CustomDateSerializer extends StdSerializer<Date> {

        public CustomDateSerializer() {
            this(null);
        }

        public CustomDateSerializer(Class<Date> t) {
            super(t);
        }

        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (value == null) {
                gen.writeString("");
                return;
            }
            gen.writeObject(ZonedDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault()).withNano(0));
        }
    }

}
