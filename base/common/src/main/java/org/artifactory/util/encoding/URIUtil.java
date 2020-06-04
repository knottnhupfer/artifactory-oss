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
/*
 * Based on apache-httpclient 3.1 org.apache.commons.httpclient.util.URIUtil.java
 * Additional contributors:
 *    JFrog Ltd.
 */
package org.artifactory.util.encoding;

import com.google.common.base.Charsets;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.http.HttpException;
import org.apache.http.util.EncodingUtils;

import java.util.BitSet;

/**
 * The URI escape and character encoding and decoding utility.
 * It's compatible with {@link org.apache.commons.httpclient.HttpURL} rather
 * than {@link org.apache.commons.httpclient.URI}.
 *
 * @author <a href="mailto:jericho@apache.org">Sung-Gu</a>
 * @version $Revision: 507321 $ $Date: 2002/03/14 15:14:01
 */
public class URIUtil {

    protected static final BitSet empty = new BitSet(1);


    /**
     * Escape and encode a string regarded as the query component of an URI with
     * the default protocol charset.
     * When a query string is not misunderstood the reserved special characters
     * ("&amp;", "=", "+", ",", and "$") within a query component, this method
     * is recommended to use in encoding the whole query.
     *
     * @param unescaped an unescaped string
     * @return the escaped string
     * @throws HttpException if the default protocol charset is not supported
     * @see #encode
     */
    public static String encodeQuery(String unescaped) throws HttpException {
        return encodeQuery(unescaped, Charsets.UTF_8.name());
    }


    /**
     * Escape and encode a string regarded as the query component of an URI with
     * a given charset.
     * When a query string is not misunderstood the reserved special characters
     * ("&amp;", "=", "+", ",", and "$") within a query component, this method
     * is recommended to use in encoding the whole query.
     *
     * @param unescaped an unescaped string
     * @param charset   the charset
     * @return the escaped string
     * @throws HttpException if the charset is not supported
     * @see #encode
     */
    public static String encodeQuery(String unescaped, String charset)
            throws HttpException {

        return encode(unescaped, URI.allowed_query, charset);
    }


    /**
     * Escape and encode a given string with allowed characters not to be
     * escaped and the default protocol charset.
     *
     * @param unescaped a string
     * @param allowed   allowed characters not to be escaped
     * @return the escaped string
     * @throws HttpException if the default protocol charset is not supported
     */
    public static String encode(String unescaped, BitSet allowed)
            throws HttpException {

        return encode(unescaped, allowed, Charsets.UTF_8.name());
    }


    /**
     * Escape and encode a given string with allowed characters not to be
     * escaped and a given charset.
     *
     * @param unescaped a string
     * @param allowed   allowed characters not to be escaped
     * @param charset   the charset
     * @return the escaped string
     */
    public static String encode(String unescaped, BitSet allowed,
            String charset) throws HttpException {
        byte[] rawdata = URLCodec.encodeUrl(allowed,
                EncodingUtils.getBytes(unescaped, charset));
        return EncodingUtils.getAsciiString(rawdata);
    }


    /**
     * Unescape and decode a given string regarded as an escaped string with the
     * default protocol charset.
     *
     * @param escaped a string
     * @return the unescaped string
     * @throws HttpException if the string cannot be decoded (invalid)
     */
    public static String decode(String escaped) throws HttpException {
        try {
            byte[] rawdata = URLCodec.decodeUrl(EncodingUtils.getAsciiBytes(escaped));
            return EncodingUtils.getString(rawdata, Charsets.UTF_8.name());
        } catch (DecoderException e) {
            throw new HttpException(e.getMessage());
        }
    }

    /**
     * Unescape and decode a given string regarded as an escaped string.
     *
     * @param escaped a string
     * @param charset the charset
     * @return the unescaped string
     * @throws HttpException if the charset is not supported
     */
    public static String decode(String escaped, String charset)
            throws HttpException {

        return URI.decode(escaped.toCharArray(), charset);
    }

    /**
     * Escape and encode a string regarded as within the path component of an
     * URI with the default protocol charset.
     * The path may consist of a sequence of path segments separated by a
     * single slash "/" character.  Within a path segment, the characters
     * "/", ";", "=", and "?" are reserved.
     *
     * @param unescaped an unescaped string
     * @return the escaped string
     *
     * @see #encode
     */
    public static String encodeWithinPath(String unescaped)
            throws HttpException {

        return encodeWithinPath(unescaped, Charsets.UTF_8.name());
    }

    /**
     * Escape and encode a string regarded as within the path component of an
     * URI with a given charset.
     * The path may consist of a sequence of path segments separated by a
     * single slash "/" character.  Within a path segment, the characters
     * "/", ";", "=", and "?" are reserved.
     *
     * @param unescaped an unescaped string
     * @param charset   the charset
     * @return the escaped string
     *
     * @see #encode
     */
    public static String encodeWithinPath(String unescaped, String charset)
            throws HttpException {

        return encode(unescaped, URI.allowed_within_path, charset);
    }



}

