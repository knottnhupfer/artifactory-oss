package org.artifactory.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;

/**
 * @author Dan Feldman
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StreamUtils {
    private static final Logger log = LoggerFactory.getLogger(StreamUtils.class);

    /**
     * A replacement for IOUtils.closeQuietly() that was deprecated and makes sonar upset.
     */
    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            log.debug("", e);
        }
    }
}
