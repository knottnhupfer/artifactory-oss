#!/bin/bash -x

errorExit () {
    echo; echo "ERROR: $1"; echo
    exit 1
}

if [ $# -ne 3 ]; then
    errorExit "Usage: $0 <image name> <image version> <Dockerfile name>"
fi

echo; echo "Building Docker image from $3"

cp src/main/docker/"$3" target/ || errorExit "Failed copying $3"
cp -r src/main/resources/* target || errorExit "Failed copying src/main/resources"

cd target || errorExit "Failed cd to target"

docker build --pull --build-arg ARTIFACTORY_VERSION=${2} -t "$1":"$2" -f "$3" . || errorExit "Failed building Docker image $3"
