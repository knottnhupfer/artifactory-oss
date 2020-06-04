package org.artifactory.support.core.collectors.logs;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jfrog.support.common.LogsFilesMatcher;
import org.jfrog.support.common.config.SystemLogsConfiguration;
import org.jfrog.support.common.core.exceptions.IllegalConditionException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;

/**
 * @author Tamir Hadad
 */
public class ArtifactorylogsMatcher implements LogsFilesMatcher {
    private static final Logger log = LoggerFactory.getLogger(ArtifactorylogsMatcher.class);

    @Override
    public boolean isMatch(Path filePath, SystemLogsConfiguration configuration) {
        String fileName = filePath.getFileName().toString();
        Matcher matcher1 = DatePatternsHolder.getMatcher1(fileName);
        Matcher matcher2 = DatePatternsHolder.getMatcher2(fileName);
        Matcher matcher3 = DatePatternsHolder.getMatcher3(fileName);
        Matcher matcher4 = DatePatternsHolder.getMatcher4(fileName);
        Matcher matcherDateZip = DatePatternsHolder.getDateZipPattern(fileName);

        if (matcher1.find()) {
            log.debug("File '{}' matches pattern 1", filePath.getFileName());
            String date = matcher1.group(0);
            return isDateInRequestedRange(date, configuration,
                    DatePatternsHolder.DatePattern.PATTERN_1);
        } else if (matcher2.find()) {
            log.debug("File '{}' matches pattern 2", filePath.getFileName());
            String date = matcher2.group(0);
            return isDateInRequestedRange(date, configuration,
                    DatePatternsHolder.DatePattern.PATTERN_2);
        } else if (matcher3.find()) {
            log.debug("File '{}' matches pattern 3", filePath.getFileName());
            String date = matcher3.group(0);
            return isDateInRequestedRange(date, configuration,
                    DatePatternsHolder.DatePattern.PATTERN_3);
        } else if (matcher4.find()) {
            log.debug("File '{}' matches pattern 4", filePath.getFileName());
            String date = matcher4.group(0);
            return isDateInRequestedRange(date, configuration,
                    DatePatternsHolder.DatePattern.PATTERN_4);
        } else if (matcherDateZip.find()) {
            log.debug("File '{}' matches pattern log.zip", filePath.getFileName());
            return isDateInRequestedZipRange(filePath, configuration);
        }

        log.debug("File '{}' doesn't match any known date pattern, " +
                "using default fallback (true)", fileName);
        return true;
    }

    /**
     * Checks zipped artifactory.log based on 'created' attribute
     * to avoid attaching heavy legacy zipped logs
     *
     * @return should be collected or not
     */
    private boolean isDateInRequestedZipRange(Path filePath, SystemLogsConfiguration configuration) {
        Date start;
        Date end;
        if (configuration.getDaysCount() != null) {
            int dayCount = configuration.getDaysCount();
            end = new Date();
            start = new DateTime(end).minusDays(dayCount).toDate();
        } else {
            start = configuration.getStartDate();
            end = configuration.getEndDate();
        }

        try {
            BasicFileAttributes attr = Files.readAttributes(filePath, BasicFileAttributes.class);
            if (attr != null) {
                FileTime time = attr.creationTime();
                if (time != null) {
                    return time.compareTo(FileTime.fromMillis(start.getTime())) >= 0 &&
                            time.compareTo(FileTime.fromMillis(end.getTime())) <= 0;
                } else {
                    log.debug("Created attribute for '{}' was not located", filePath);
                }
            } else {
                log.debug("No attributes for '{}' were located", filePath);
            }
        } catch (IOException e) {
            log.warn("Reading '{}' attributes has failed: {}", filePath, e.getMessage());
            log.debug("Cause: {}", e);
        }
        return true;
    }


    /**
     * Checks whether log file date falls into {@link SystemLogsConfiguration}
     *
     * @param date          log file date
     * @param configuration {@link SystemLogsConfiguration}
     * @param datePattern   {@link DatePatternsHolder.DatePattern} to work with
     * @return boolean
     */
    private boolean isDateInRequestedRange(String date,
            SystemLogsConfiguration configuration, DatePatternsHolder.DatePattern datePattern) {
        log.debug("Initiating range check for date '{}'", date);

        Date logFileDate = null;

        for (String patternTemplate : datePattern.getPatternTemplates()) {
            DateFormat df = new SimpleDateFormat(patternTemplate);
            try {
                logFileDate = df.parse(date);
                log.debug("Date '{}' matches template '{}'", date, patternTemplate);
                break;
            } catch (ParseException e) {
                log.debug("Cannot parse date '{}' with template '{}'", date, patternTemplate);
            }
        }

        if (logFileDate == null) {
            log.debug(
                    "Could not parse date '{}' using any known template for pattern '{}', " +
                            "assuming positive answer",
                    date, datePattern.getPattern()
            );
            return true;
        }

        if (configuration.getDaysCount() != null) { // days offset
            DateTime dateTime = new DateTime(logFileDate);
            return DateTime.now().minusDays(configuration.getDaysCount()).compareTo(dateTime) <= 0;
        } else if (configuration.getStartDate() != null) { // date range
            Date endDate = configuration.getEndDate() != null ?
                    configuration.getEndDate() : new Date();
            return configuration.getStartDate().compareTo(logFileDate) <= 0 &&
                    endDate.compareTo(logFileDate) >= 0;
        } else {
            throw new IllegalConditionException(
                    "Either days offset or StartDate must be specified"
            );
        }
    }
}
