#!/bin/bash

# Script for validating the server.xml template against the original artifactory-oss/distribution/standalone/src/main/install/misc/tomcat/server.xml
# Usage: ./test-server-xml-template.sh <path-to-artifactory-standalone-zip>

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Set test values
export SERVER_XML_ARTIFACTORY_PORT=8081
export SERVER_XML_ARTIFACTORY_MAX_THREADS=200
export SERVER_XML_ACCESS_MAX_THREADS=50
export SERVER_XML_ARTIFACTORY_EXTRA_CONFIG=
export SERVER_XML_ACCESS_EXTRA_CONFIG=
export SERVER_XML_EXTRA_CONNECTOR=

errorExit () {
    echo; echo "ERROR: $1"; echo
    exit 1
}

[ ! -z ${1} ] || errorExit "Must pass artifactory standalone zip as argument"

zip=${1}
[ -f "${zip}" ] || errorExit "File ${zip} does not exist"

server_xml_template=${SCRIPT_DIR}/server.xml.template
[ -f "${server_xml_template}" ] || errorExit "File ${server_xml_template} does not exist"

echo -e "\nTest validity of tomcat's server.xml compared to the template"

echo -e "\nUsing the following variables and values"
env | grep SERVER_XML_ | sort

tmp_dir=$(mktemp -d)

# Get server.xml from archive
echo -e "\nExtract server.xml from ${zip}"
server_xml=${tmp_dir}/server.xml
unzip -v -p ${zip} artifactory-*/tomcat/conf/server.xml > ${server_xml}

# Get server.xml.template
server_xml_template_processed=${tmp_dir}/server.xml.template
cp ${server_xml_template} ${server_xml_template_processed}

sed -i.org \
    -e "s,SERVER_XML_ARTIFACTORY_PORT,${SERVER_XML_ARTIFACTORY_PORT},g" \
    -e "s,SERVER_XML_ARTIFACTORY_MAX_THREADS,${SERVER_XML_ARTIFACTORY_MAX_THREADS},g" \
    -e "s,SERVER_XML_ACCESS_MAX_THREADS,${SERVER_XML_ACCESS_MAX_THREADS},g" \
    -e "s,SERVER_XML_ARTIFACTORY_EXTRA_CONFIG,${SERVER_XML_ARTIFACTORY_EXTRA_CONFIG},g" \
    -e "s,SERVER_XML_ACCESS_EXTRA_CONFIG,${SERVER_XML_ACCESS_EXTRA_CONFIG},g" \
    -e "s,SERVER_XML_EXTRA_CONNECTOR,${SERVER_XML_EXTRA_CONNECTOR},g" \
    -e "/^$/d" \
    ${server_xml_template_processed} || errorExit "Updating SERVER_XML_ACCESS_EXTRA_CONFIG in ${server_xml_template} failed"

echo -e "\nComparing ${server_xml} to ${server_xml_template_processed}"
diff -B -w ${server_xml} ${server_xml_template_processed} > ${tmp_dir}/diff
if [ $? -ne 0 ]; then
    echo "Differences found!"
    cat ${tmp_dir}/diff
    error=true
else
    echo "Files are identical"
fi

echo -e "\nDeleting temp files...\n"

if [ "${error}" == true ]; then
    echo "FAILED"
    exit 1
else
    echo "SUCCESS"
    exit 0
fi
