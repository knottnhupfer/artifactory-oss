#!/bin/bash -e

if [ -z "$6" ]; then
    echo
    echo "Error: Usage is $0 productName mavenVersion rpmVersion releaseNumber outBuildDir [passphrase or NOPASS]"
    exit 1
fi

FILENAME_PREFIX="$1"
MAVEN_VERSION="$2"
RPM_VERSION="$3"
RELEASE_NUMBER="$4"
OUT_BUILD_DIR="$5"
PASSPHRASE="$6"

curDir="`dirname $0`"
curDir="`cd $curDir; pwd`"

RPM_SOURCES_DIR="$OUT_BUILD_DIR/SOURCES"

if [ -z "$OUT_BUILD_DIR" ] || [ ! -d "$OUT_BUILD_DIR" ]; then
    echo
    echo "Error: The output directory $OUT_BUILD_DIR does not exists!"
    exit 1
fi

function expect_script() {
    cat << End-of-text #No white space between << and End-of-text
spawn rpm --resign $RPMFILE
expect -exact "Enter pass phrase: "
send -- "${PASSPHRASE}\r"
expect eof
exit
End-of-text

}

function sign_rpm() {
    echo "Signing RPM..."
    expect_script | /usr/bin/expect -f -
}

echo "Building RPM for $FILENAME_PREFIX $MAVEN_VERSION using $RPM_VERSION"

cd $curDir && rpmbuild -bb \
--define="_tmppath $OUT_BUILD_DIR/tmp" \
--define="_topdir $PWD" \
--define="_rpmdir $OUT_BUILD_DIR" \
--define="buildroot $OUT_BUILD_DIR/BUILDROOT" \
--define="_sourcedir $RPM_SOURCES_DIR" \
--define="artifactory_version $RPM_VERSION" \
--define="artifactory_release $RELEASE_NUMBER" \
--define="filename_prefix $FILENAME_PREFIX" \
--define="full_version $MAVEN_VERSION" \
SPECS/artifactory-oss.spec

RET=$?
if [ $RET == 0 ]; then
    echo -e "   DONE"
else
    echo "ERROR building RPM  $FILENAME_PREFIX $MAVEN_VERSION using $RPM_VERSION"
    exit $RET
fi

if [ "$PASSPHRASE" != "NOPASS" ]; then
    echo "In Jenkins env, signing rpm"
    cd ../
    pwd
    RPMFILE="$FILENAME_PREFIX-$MAVEN_VERSION.rpm"
    if [ -f "$RPMFILE" ]; then
        sign_rpm
        RET=$?
        if [ $RET == 0 ]; then
            echo -e "   DONE"
        else
            echo "ERROR signing RPM  $FILENAME_PREFIX $MAVEN_VERSION using $RPM_VERSION"
            exit $RET
        fi
    else
        echo "ERROR trying to sign non existent RMP $RPMFILE for $RPM_VERSION"
        exit 5
    fi
fi