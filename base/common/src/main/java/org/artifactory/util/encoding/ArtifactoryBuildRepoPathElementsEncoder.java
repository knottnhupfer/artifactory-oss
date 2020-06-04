package org.artifactory.util.encoding;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.artifactory.util.PathValidator;

/**
 * Built around the logic of {@link PathValidator} this utility makes any given path legal as long as you pass it
 * path elements that are positioned *BETWEEN* path separators "/", since it always encodes the forward slash character.
 *
 * The reason for this is that this utility was constructed for the build-info repo deployment paths logic which
 * uses this to encode the build name and build number separately (as any build json will eventually be positioned as
 * build_mame/build_number-timestamp.json) so that we don't break the *actual* path the repo's layout defines.
 *
 * Note that its possible to encode even less characters then this class currently does by making certain assumptions.
 * For instance when we backwards-encode any character then the STATE should no longer hold that character.
 * for instance if we backwards-encode a slash then the STATE should be STATE_OK and not STATE_SLASH (since we can now
 * have any character after the encoded string again as it is a legal path).
 *
 * @author Dan Feldman
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ArtifactoryBuildRepoPathElementsEncoder {

    // The state when starting to parse the entire path and when we are valid so far
    private static final int STATE_OK = 0;
    // The state when we found a slash ('/')
    private static final int STATE_SLASH = 1;
    // The state when we found a dot ('.')
    private static final int STATE_DOT = 2;
    // The state when we found a space character (' ' even from tab etc)
    private static final int STATE_SPACE = 3;
    // The state when an ampersand is found ('&') only at the start of a token (start of the entire path or after a '/')
    private static final int STATE_AMPERSAND = 4;

    private static final char EOF = (char) -1;
    private static final char FORWARD_SLASH = '/';
    private static final char AMPERSAND = '&';
    public static final char DOT = '.';
    private static final char BACKSLASH = '\\';
    public static final char PIPE = '|';
    public static final char STAR = '*';
    public static final char QUESTION_MARK = '?';
    public static final char QUOTE = '"';
    public static final char COLON = ':';
    public static final char SEMICOLON = ';';
    public static final char SPACE = ' ';
    private static final char PERCENT = '%';

    static final String ENCODED_FORWARD_SLASH = "%2F";
    static final String ENCODED_AMPERSAND = "%26";
    static final String ENCODED_DOT = "%2E";
    static final String ENCODED_BACKSLASH = "%5C";
    static final String ENCODED_PIPE = "%7C";
    static final String ENCODED_STAR = "%2A";
    static final String ENCODED_QUESTION_MARK = "%3F";
    static final String ENCODED_QUOTE = "%22";
    static final String ENCODED_COLON = "%3A";
    static final String ENCODED_SEMICOLON = "%3B";
    static final String ENCODED_SPACE = "%20";
    static final String ENCODED_PERCENT = "%25";

    /**
     * Makes the given path a legal artifactory path, taking into consideration that its of the form /path/
     *
     * @param path                 The path to validate, it is expected to be in unix separators
     * @param isBuildNumberSegment Allows us to make
     */
    public static String encode(String path, boolean isBuildNumberSegment) {

        if (StringUtils.isEmpty(path)) {
            return path;
        }
        char[] chars = (FORWARD_SLASH + path + (isBuildNumberSegment ? FORWARD_SLASH : "")).toCharArray();

        int state = STATE_OK;
        int len = chars.length;
        int pos = 0;

        StringBuilder result = new StringBuilder();
        while (pos <= len) {
            char c = pos == len ? EOF : chars[pos];

            // special check for whitespace
            if (c != ' ' && Character.isWhitespace(c)) {
                c = ' ';
            }
            switch (c) {
                case EOF:
                    //There's no real EOF in build path naming since the end is always .json
                    break;
                case FORWARD_SLASH:
                    handleEndOrSlash(state, result);
                    result.append(ENCODED_FORWARD_SLASH);
                    state = STATE_SLASH;
                    break;
                case AMPERSAND:
                    if (pos == 0 || state == STATE_SLASH) {
                        // We do not allow only & at a path token but it is allowed in general ('test&', '&test')
                        state = STATE_AMPERSAND;
                    }
                    //Ampersand is illegal only next to path separator "/" and EOF so it gets backwards-encoded in that section
                    result.append(AMPERSAND);
                    break;
                case DOT:
                    if (pos == 0 || state == STATE_SLASH) {
                        state = STATE_DOT;
                    }
                    result.append(DOT);
                    break;
                case SPACE:
                    if (state == STATE_SLASH) {
                        result.append(ENCODED_SPACE);
                    } else {
                        result.append(SPACE);
                    }
                    state = STATE_SPACE;
                    break;
                case BACKSLASH:
                    result.append(ENCODED_BACKSLASH);
                    state = STATE_OK;
                    break;
                case PIPE:
                    result.append(ENCODED_PIPE);
                    state = STATE_OK;
                    break;
                case STAR:
                    result.append(ENCODED_STAR);
                    state = STATE_OK;
                    break;
                case QUESTION_MARK:
                    result.append(ENCODED_QUESTION_MARK);
                    state = STATE_OK;
                    break;
                case QUOTE:
                    result.append(ENCODED_QUOTE);
                    state = STATE_OK;
                    break;
                case COLON:
                    result.append(ENCODED_COLON);
                    state = STATE_OK;
                    break;
                case SEMICOLON:
                    result.append(ENCODED_SEMICOLON);
                    state = STATE_OK;
                    break;
                case PERCENT:
                    result.append(ENCODED_PERCENT);
                    state = STATE_OK;
                    break;
                default:
                    state = STATE_OK;
                    result.append(c);
                    break;
            }
            pos++;
        }

        if (isBuildNumberSegment) {
            return result.substring(3, result.length() - 3);
        } else {
            return result.substring(3);
        }
    }

    private static void handleEndOrSlash(int state, StringBuilder result) {
        if (state == STATE_SPACE) {
            encodeLastCharIfNeeded(result, SPACE, ENCODED_SPACE);
        } else if (state == STATE_DOT) {
            encodeLastCharIfNeeded(result, DOT, ENCODED_DOT);
        } else if (state == STATE_AMPERSAND) {
            encodeLastCharIfNeeded(result, AMPERSAND, ENCODED_AMPERSAND);
        }
    }

    /**
     * If the previous character is not encoded, need to encode it now to make the result a legal path
     */
    private static void encodeLastCharIfNeeded(StringBuilder result, char notEncoded, String encodeTo) {
        //Special case for input of length 1
        int currentResultPos = result.length() - 1;
        if (notEncoded == result.charAt(currentResultPos)) {
            result.deleteCharAt(currentResultPos);
            result.append(encodeTo);
        }
    }
}
