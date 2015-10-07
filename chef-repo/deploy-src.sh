#!/bin/bash

CHEF_DIR=$(cd $(dirname $0) && pwd)
BASE=$(cd ${CHEF_DIR}/../.. && pwd)
CLOUDSTEAD=${BASE}/cloudstead-server
APPSTORE=${BASE}/appstore-server

function die {
  echo 1>&2 "${1}"
  exit 1
}

if [ -z "${INIT_FILES}" ] ; then
  die "no INIT_FILES env var defined"
fi

# make INIT_FILES an absolute path
INIT_FILES=$(cd ${CHEF_DIR} && cd ${INIT_FILES} && pwd)

CLOUDSTEAD_DATA="${INIT_FILES}/data_files/cloudstead"
mkdir -p ${CLOUDSTEAD_DATA}
cp ${CLOUDSTEAD}/target/cloudstead-server.tar.gz ${CLOUDSTEAD_DATA}

APPSTORE_DATA="${INIT_FILES}/data_files/cloudos-appstore"
mkdir -p ${APPSTORE_DATA}
cp ${APPSTORE}/target/cloudos-appstore-server.tar.gz ${APPSTORE_DATA}

${CHEF_DIR}/deploy.sh ${@}
