#!/bin/bash
#
# Usage: ./deploy.sh [host]
#
# Required environment variables:
#
# INIT_FILES -- a directory containing files that are unique to each chef-run. See README.md
#
# Relies on a deploy_lib.sh being either in the same directory as this script,
# or in ../../cloudos-lib/chef-repo/ (the location if being run from a local git repo)
#

function die {
  echo 1>&2 "${1}"
  exit 1
}

BASE=$(cd $(dirname $0) && pwd)
cd ${BASE}
INC_BASE=$(cd ${BASE}/../.. && pwd)
CLOUDOS_BASE=$(cd ${INC_BASE}/cloudos && pwd)

DEPLOYER=${BASE}/deploy_lib.sh
if [ ! -x ${DEPLOYER} ] ; then
  DEPLOYER=${CLOUDOS_BASE}/cloudos-lib/chef-repo/deploy_lib.sh
  if [ ! -x ${DEPLOYER} ] ; then
    die "ERROR: deployer not found or not executable: ${DEPLOYER}"
  fi
fi

host="${1:?no user@host specified}"

if [ -z ${INIT_FILES} ] ; then
  die "INIT_FILES is not defined in the environment."
fi
INIT_FILES=$(cd ${INIT_FILES} && pwd) # make into absolute path

REQUIRED=" \
data_bags/cloudstead/base.json \
data_bags/cloudstead/init.json \
data_bags/cloudstead/ports.json \
data_bags/cloudos-dns/init.json \
data_bags/cloudos-dns/ports.json \
data_bags/cloudos-appstore/init.json \
data_bags/cloudos-appstore/ports.json \
certs/cloudstead/ssl-https.key \
certs/cloudstead/ssl-https.pem \
certs/cloudstead/ssl-https-wildcard.key \
certs/cloudstead/ssl-https-wildcard.pem \
"

COOKBOOK_SOURCES=" \
$(find ${INC_BASE}/cloudstead-apps/apps -type d -name cookbooks) \
$(find ${CLOUDOS_BASE}/cloudos-apps/apps -type d -name cookbooks) \
${CLOUDOS_BASE}/cloudos-lib/chef-repo/cookbooks \
"

${DEPLOYER} ${host} ${INIT_FILES} "${REQUIRED}" "${COOKBOOK_SOURCES}"
