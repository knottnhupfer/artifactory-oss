package org.artifactory.addon.support;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.validation.constraints.Min;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtifactorySupportBundleParameters {

    // Note: do not use Lombok Builder and Builder.Default annotations
    // as they mess up default values during JSON deserialization
    private boolean configuration = true;
    private boolean system = true;
    private Logs logs = new Logs();
    @JsonProperty("thread_dump")
    @com.fasterxml.jackson.annotation.JsonProperty("thread_dump")
    private ThreadDump threadDump = new ThreadDump();

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(using = Logs.LogsItemSerializer.class)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class Logs {

        private boolean include = true;
        @JsonProperty("start_date")
        private Date startDate = DateUtils.addDays(DateUtils.truncate(new Date(), Calendar.DATE), -1);

        @JsonProperty("end_date")
        private Date endDate = DateUtils.truncate(new Date(), Calendar.DATE);

        @NoArgsConstructor
        public static class LogsItemSerializer extends JsonSerializer<Logs> {
            @Override
            public void serialize(Logs value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeStartObject();
                gen.writeBooleanField("include", value.isInclude());
                gen.writeStringField("start_date", formatDate(value.getStartDate()));
                gen.writeStringField("end_date", formatDate(value.getEndDate()));
                gen.writeEndObject();
            }

            String formatDate(Date date) {
                LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                return localDate.toString();
            }
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ThreadDump {
        @Min(0)
        private int count = 0;
        @Min(0)
        private long interval = 0;
    }
}
