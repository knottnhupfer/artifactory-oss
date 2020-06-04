#!/bin/bash

i=1
runBuild() {
    local logFile="/tmp/configBuild-$i.log"
    echo "Log in $logFile"
    mvn -Prelease clean install $1 > ${logFile} 2>&1
    res=$?
    if [ ${res} -ne $2 ]; then
        tail ${logFile}
        echo "ERROR: Maven build return was $res not the expected $2"
        exit 1
    fi
}

echo "****************************************************"
echo "****** Testing Local environment. Type 1 ***********"
echo ""
unset JENKINS_HOME
# Using values that actually are identical to pom values to verify the test check the -D params
version="5.x-SNAPSHOT"
revision="dev"
timestamp="1503848548000"

echo "Base working build"
runBuild "" 0
((i++))

echo "Fail local build by injecting version"
runBuild "-Dartifactory.version.prop=$version" 1
((i++))

echo "Fail local build by injecting revision"
runBuild "-Dartifactory.revision.prop=$revision" 1
((i++))

echo "Fail local build by injecting timestamp"
runBuild "-Dartifactory.timestamp.prop=$timestamp" 1
((i++))

echo "****************************************************"
echo "****** Testing Jenkins Dev environment. Type 2 *****"
echo ""
mkdir /tmp/jenkins
export JENKINS_HOME="/tmp/jenkins"
# Using a fred branch dev build
version="5.x.fred"
revision=334
timestamp="1503848548000"

echo "Base working build injecting all vars"
runBuild "-Dartifactory.version.prop=$version -Dartifactory.revision.prop=$revision -Dartifactory.timestamp.prop=$timestamp" 0
((i++))

echo "Fail local build by NOT injecting version"
runBuild "-Dartifactory.revision.prop=$revision -Dartifactory.timestamp.prop=$timestamp" 1
((i++))

echo "Fail local build by NOT injecting revision"
runBuild "-Dartifactory.version.prop=$version -Dartifactory.timestamp.prop=$timestamp" 1
((i++))

echo "Fail local build by NOT injecting timestamp"
runBuild "-Dartifactory.version.prop=$version -Dartifactory.revision.prop=$revision" 1
((i++))


echo "****************************************************"
echo "****** Testing Jenkins Release environment. Type 3 *"
echo ""
mkdir /tmp/jenkins
export JENKINS_HOME="/tmp/jenkins"
# Using a fred branch dev build
version="5.5.1_p002"
revision=50501902
timestamp="1503848548000"

sed -i ".test" '/<\/project>/d' pom.xml

echo "    <properties>
        <artifactory.version.prop>$version</artifactory.version.prop>
        <artifactory.revision.prop>$revision</artifactory.revision.prop>
    </properties>
</project>
" >> pom.xml

sed -i ".test" 's#^    next#//    next#g;
s#^    //v551#    v551#g;' src/main/java/org/artifactory/version/ArtifactoryVersion.java

echo "Base working release build injecting only timestamp"
runBuild "-Dartifactory.timestamp.prop=$timestamp" 0
((i++))

echo "Fail release build by injecting version"
runBuild "-Dartifactory.version.prop=$version" 1
((i++))

echo "Fail release build by injecting revision"
runBuild "-Dartifactory.revision.prop=$revision" 1
((i++))


