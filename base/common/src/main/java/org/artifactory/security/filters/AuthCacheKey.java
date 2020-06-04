package org.artifactory.security.filters;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author Yoav Landman
 */
public class AuthCacheKey {
    private static final String EMPTY_HEADER = DigestUtils.sha1Hex("");

    private final String hashedHeader;
    private final String ip;

    public AuthCacheKey(String header, String ip) {
        if (header == null) {
            this.hashedHeader = EMPTY_HEADER;
        } else {
            this.hashedHeader = DigestUtils.sha1Hex(header);
        }
        this.ip = ip;
    }

    public boolean hasEmptyHeader() {
        return this.hashedHeader.equals(EMPTY_HEADER);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthCacheKey key = (AuthCacheKey) o;
        return hashedHeader.equals(key.hashedHeader) && ip.equals(key.ip);
    }

    @Override
    public int hashCode() {
        int result = hashedHeader.hashCode();
        result = 31 * result + ip.hashCode();
        return result;
    }
}
