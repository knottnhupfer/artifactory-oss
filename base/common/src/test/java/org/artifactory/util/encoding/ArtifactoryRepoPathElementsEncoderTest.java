package org.artifactory.util.encoding;

import org.artifactory.util.PathValidator;
import org.testng.annotations.Test;

import static org.artifactory.util.encoding.ArtifactoryBuildRepoPathElementsEncoder.*;
import static org.testng.Assert.assertEquals;

/**
 * We test all strings as both build number and name since, as the creed states: "Nothing is true, everything is permitted"
 *
 * @author Dan Feldman
 */
@Test
public class ArtifactoryRepoPathElementsEncoderTest {

    public void encodedForwardSlash() {
        assertEncodingAsBuildName("/", ENCODED_FORWARD_SLASH);
        assertEncodingAsBuildNumber("/", ENCODED_FORWARD_SLASH);
    }

    public void encodedAmpersand() {
        assertEncodingAsBuildName("&", ENCODED_AMPERSAND);
        assertEncodingAsBuildNumber("&", "&");
    }

    public void encodedDot() {
        assertEncodingAsBuildName(".", ENCODED_DOT);
        assertEncodingAsBuildNumber(".", ".");
    }

    public void invalidBackslash() {
        assertEncodingAsBuildName("\\", ENCODED_BACKSLASH);
        assertEncodingAsBuildNumber("\\", ENCODED_BACKSLASH);
    }

    public void encodedPipe() {
        assertEncodingAsBuildName("|", ENCODED_PIPE);
        assertEncodingAsBuildNumber("|", ENCODED_PIPE);
    }

    public void encodedAsterisk() {
        assertEncodingAsBuildName("*", ENCODED_STAR);
        assertEncodingAsBuildNumber("*", ENCODED_STAR);
    }

    public void encodedQuote() {
        assertEncodingAsBuildName("\"", ENCODED_QUOTE);
        assertEncodingAsBuildNumber("\"", ENCODED_QUOTE);
    }

    public void encodedColon() {
        assertEncodingAsBuildName(":", ENCODED_COLON);
        assertEncodingAsBuildNumber(":", ENCODED_COLON);
    }

    public void encodedSemicolon() {
        assertEncodingAsBuildName(";", ENCODED_SEMICOLON);
        assertEncodingAsBuildNumber(";", ENCODED_SEMICOLON);
    }

    public void encodedSpace() {
        assertEncodingAsBuildName(" ", ENCODED_SPACE);
        assertEncodingAsBuildNumber(" ", ENCODED_SPACE);
    }

    public void encodedQuestionMark() {
        assertEncodingAsBuildName("?", ENCODED_QUESTION_MARK);
        assertEncodingAsBuildNumber("?", ENCODED_QUESTION_MARK);
    }

    public void encodedPercent() {
        assertEncodingAsBuildName("%", ENCODED_PERCENT);
        assertEncodingAsBuildNumber("%", ENCODED_PERCENT);
    }

    public void illegalAmpersandAfterSlash() {
        assertEncodingAsBuildName("bbb/&a/&", "bbb%2F&a%2F%26");
        assertEncodingAsBuildNumber("bbb/&a/&", "bbb%2F&a%2F&");
    }

    public void illegalDoubleDot() {
        assertEncodingAsBuildName("..", "." + ENCODED_DOT);
        assertEncodingAsBuildNumber("..", "..");
    }

    public void illegalDot() {
        assertEncodingAsBuildName("blabla/.", "blabla%2F%2E");
        assertEncodingAsBuildNumber("blabla/.", "blabla%2F.");
    }

    public void illegalDotDot() {
        assertEncodingAsBuildName("dot/../dot/file.jar", "dot%2F.%2E%2Fdot%2Ffile.jar");
        assertEncodingAsBuildNumber("dot/../dot/file.jar", "dot%2F.%2E%2Fdot%2Ffile.jar");
    }

    public void illegalAllSpaces() {
        assertEncodingAsBuildName("  ", ENCODED_SPACE + ENCODED_SPACE);
        assertEncodingAsBuildNumber("  ", ENCODED_SPACE + " ");
    }

    public void illegalBackslash() {
        assertEncodingAsBuildName("back\\slash.zors", "back%5Cslash.zors");
        assertEncodingAsBuildNumber("back\\slash.zors", "back%5Cslash.zors");
    }

    public void illegalQuestionMark() {
        assertEncodingAsBuildName("riddle/me_this?.tar", "riddle%2Fme_this%3F.tar");
        assertEncodingAsBuildNumber("riddle/me_this?.tar", "riddle%2Fme_this%3F.tar");
    }

    public void illegalQuotationMark() {
        assertEncodingAsBuildName("make\"believe.hello", "make%22believe.hello");
        assertEncodingAsBuildNumber("make\"believe.hello", "make%22believe.hello");
    }

    public void illegalPipe() {
        assertEncodingAsBuildName("sup|r|mario", "sup%7Cr%7Cmario");
        assertEncodingAsBuildNumber("sup|r|mario", "sup%7Cr%7Cmario");
    }

    public void illegalStar() {
        assertEncodingAsBuildName("lots/of/*", "lots%2Fof%2F%2A");
        assertEncodingAsBuildNumber("lots/of/*", "lots%2Fof%2F%2A");
    }

    public void illegalSpaceBeforeSlash() {
        assertEncodingAsBuildName(" shsh/gaga /hdt", "%20shsh%2Fgaga%20%2Fhdt");
        assertEncodingAsBuildNumber(" shsh/gaga /hdt", "%20shsh%2Fgaga%20%2Fhdt");
    }

    public void illegalSpaceAfterSlash() {
        assertEncodingAsBuildName("hello/  ", "hello%2F%20%20");
        assertEncodingAsBuildNumber("hello/  ", "hello%2F%20 ");
    }

    public void illegalColon() {
        assertEncodingAsBuildName("hello/:a:a", "hello%2F%3Aa%3Aa");
        assertEncodingAsBuildNumber("hello/:a:a", "hello%2F%3Aa%3Aa");
    }

    public void illegalSemicolon() {
        assertEncodingAsBuildName("hello/;x;z", "hello%2F%3Bx%3Bz");
        assertEncodingAsBuildNumber("hello/;x;z", "hello%2F%3Bx%3Bz");
    }

    public void illegalDotAfterSlash() {
        assertEncodingAsBuildName("hello/.x/.", "hello%2F.x%2F%2E");
        //this can actually be hello%2F.x%2F. by making a specific build-name assumption (see the tested class for details)
        assertEncodingAsBuildNumber("hello/.x/.", "hello%2F.x%2F.");
    }

    public void illegalDotAfterSpaces() {
        assertEncodingAsBuildName("hello/ x/ ", "hello%2F%20x%2F%20");
        assertEncodingAsBuildNumber("hello/ x/ ", "hello%2F%20x%2F%20");
    }

    public void percentIsDoubleEncoded() {
        assertEncodingAsBuildName("this/ is/ too.much::who;does|this\"in\\their/&right &x/?minds*//%2F%26%5C",
                "this%2F%20is%2F%20too.much%3A%3Awho%3Bdoes%7Cthis%22in%5Ctheir%2F&right &x%2F%3Fminds%2A%2F%2F%252F%2526%255C");
        assertEncodingAsBuildNumber("this/ is/ too.much::who;does|this\"in\\their/&right &x/?minds*//%2F%26%5C",
                "this%2F%20is%2F%20too.much%3A%3Awho%3Bdoes%7Cthis%22in%5Ctheir%2F&right &x%2F%3Fminds%2A%2F%2F%252F%2526%255C");
    }

    //Some samples taken from repo21, rjo, ojo and repo.spring.io
    public void productionExamples() {
        assertEncodingAsBuildName("wharf-1.1", "wharf-1.1");
        assertEncodingAsBuildName("2.x-release", "2.x-release");
        assertEncodingAsBuildName("binary-store-2.x", "binary-store-2.x");
        assertEncodingAsBuildName("Build-Info%20Trunk", "Build-Info%2520Trunk");
        assertEncodingAsBuildName("MATSim_contrib_M2", "MATSim_contrib_M2");
        assertEncodingAsBuildName("jfrog-crypto-1.1.x", "jfrog-crypto-1.1.x");
        assertEncodingAsBuildName("maven3-extractor-trunk", "maven3-extractor-trunk");
        assertEncodingAsBuildName("artifactory-closed-5.x", "artifactory-closed-5.x");
        assertEncodingAsBuildName("Artifactory-2.1.x-for-AOL", "Artifactory-2.1.x-for-AOL");
        assertEncodingAsBuildName("com.laynemobile.sharewear", "com.laynemobile.sharewear");
        assertEncodingAsBuildName("02_artifactoryReleaseBuild", "02_artifactoryReleaseBuild");
        assertEncodingAsBuildName("Artifactory :: Test-service-pipe", "Artifactory %3A%3A Test-service-pipe");
        assertEncodingAsBuildName("EcoSystem%20::%20xray-client-java", "EcoSystem%2520%3A%3A%2520xray-client-java");
        assertEncodingAsBuildName("Artifactory%20::%20binary-store-5.x", "Artifactory%2520%3A%3A%2520binary-store-5.x");
        assertEncodingAsBuildName("jfrog-crypto-5.x-feature-RTFACT-13205", "jfrog-crypto-5.x-feature-RTFACT-13205");
        assertEncodingAsBuildName("Spring Security - 3.2.x - Default Job", "Spring Security - 3.2.x - Default Job");
        assertEncodingAsBuildName("metacubed-projects/jsonapi-lite/master", "metacubed-projects%2Fjsonapi-lite%2Fmaster");
        assertEncodingAsBuildName("artifactory-pro-5.x-pipe-release-5.3.0-rc", "artifactory-pro-5.x-pipe-release-5.3.0-rc");
        assertEncodingAsBuildName("EcoSystem :: artifactory-jenkins-plugin", "EcoSystem %3A%3A artifactory-jenkins-plugin");
        assertEncodingAsBuildName("artifactory-oss-5.x-pipe-bugfix-RTFACT-10301", "artifactory-oss-5.x-pipe-bugfix-RTFACT-10301");
        assertEncodingAsBuildName("EcoSystem%20::%20build-info-gradle-extractor", "EcoSystem%2520%3A%3A%2520build-info-gradle-extractor");
        assertEncodingAsBuildName("EcoSystem :: artifactory-client-java-matankatz", "EcoSystem %3A%3A artifactory-client-java-matankatz");
        assertEncodingAsBuildName("Groovy :: Bintray integration :: Upload snapshots", "Groovy %3A%3A Bintray integration %3A%3A Upload snapshots");

        assertEncodingAsBuildName("Spring Integration - Master (5.1.x) - Default Job",
                "Spring Integration - Master (5.1.x) - Default Job");
        assertEncodingAsBuildName("Common :: package-indexer-multibranch :: feature :: chef",
                "Common %3A%3A package-indexer-multibranch %3A%3A feature %3A%3A chef");
        assertEncodingAsBuildName("artifactory-pro-5.x-pipe-bugfix-RTFACT-12890-Bower_Watchers",
                "artifactory-pro-5.x-pipe-bugfix-RTFACT-12890-Bower_Watchers");
        assertEncodingAsBuildName("Artifactory :: package-indexer-multibranch :: feature :: helm-dev",
                "Artifactory %3A%3A package-indexer-multibranch %3A%3A feature %3A%3A helm-dev");
        assertEncodingAsBuildName("Spring Cloud Data Flow - UI (Linux, Publish, 1.5.x) - Default Job",
                "Spring Cloud Data Flow - UI (Linux, Publish, 1.5.x) - Default Job");
        assertEncodingAsBuildName("Spring Data Couchbase - Spring Data Couchbase - 3.1.x - Default Job",
                "Spring Data Couchbase - Spring Data Couchbase - 3.1.x - Default Job");
        assertEncodingAsBuildName("Common%20::%20package-indexer-multibranch%20::%20feature%20::%20chef",
                "Common%2520%3A%3A%2520package-indexer-multibranch%2520%3A%3A%2520feature%2520%3A%3A%2520chef");
        assertEncodingAsBuildName("Artifactory%20::%20package-indexer-multibranch%20::%20feature%20::%20helm-dev",
                "Artifactory%2520%3A%3A%2520package-indexer-multibranch%2520%3A%3A%2520feature%2520%3A%3A%2520helm-dev");
        assertEncodingAsBuildName("Spring Cloud Data Flow - Server - Kubernetes 1.10 - Master - Build Spring Cloud Dataflow Kubernetes Server",
                "Spring Cloud Data Flow - Server - Kubernetes 1.10 - Master - Build Spring Cloud Dataflow Kubernetes Server");
    }

    private void assertEncodingAsBuildName(String needsEncoding, String expected) {
        assertEncoding(needsEncoding, expected, true);
    }

    private void assertEncodingAsBuildNumber(String needsEncoding, String expected) {
        assertEncoding(needsEncoding, expected, false);
    }

    private void assertEncoding(String needsEncoding, String expected, boolean isBuildNumberSegment) {
        String legalPath = encode(needsEncoding, isBuildNumberSegment);
        assertEquals(legalPath, expected);
        //Also assert its a legal path, that's what we're here for after all.
        //Adding the path separators since in the worst case its gonna be <repo_key>/<build_name>/ that goes to path validation
        if (isBuildNumberSegment) {
            PathValidator.validate("/" + legalPath + "/");
        } else {
            PathValidator.validate("/" + legalPath + "-123.json");
        }
    }
}