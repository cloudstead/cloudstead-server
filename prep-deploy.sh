#!/bin/bash

DEPLOY=${1}
if [ -z ${DEPLOY} ] ; then
  echo "Usage $0 <deploy-dir>"
  exit 1
fi

BASE=$(cd $(dirname $0) && pwd)
CLOUDOS_BASE="$(cd ${BASE}/../cloudos && pwd)"

mkdir -p ${DEPLOY}/cloudos-chef/cookbooks
mkdir -p ${DEPLOY}/cloudos-chef/data_bags
CLOUDOS_CHEF=$(cd ${DEPLOY}/cloudos-chef && pwd)

rsync -ac ${CLOUDOS_BASE}/cloudos-server/chef-repo/cookbooks/* ${CLOUDOS_CHEF}/cookbooks/ > /dev/null && \
rsync -ac ${CLOUDOS_BASE}/cloudos-lib/chef-repo/cookbooks/* ${CLOUDOS_CHEF}/cookbooks/ > /dev/null && \
rsync -ac $(find ${CLOUDOS_BASE}/cloudos-apps/apps -type d -name cookbooks) ${CLOUDOS_CHEF}/ > /dev/null && \
rsync -ac $(find ${CLOUDOS_BASE}/cloudos-apps/apps -type d -name data_bags) ${CLOUDOS_CHEF}/ > /dev/null && \
for f in JSON.sh solo.rb install.sh deploy_lib.sh ; do
  cp ${CLOUDOS_BASE}/cloudos-lib/chef-repo/${f} ${CLOUDOS_CHEF}/
done && \
cp ${CLOUDOS_BASE}/cloudos-server/chef-repo/solo-base.json ${CLOUDOS_CHEF}/ && \
cp ${CLOUDOS_BASE}/cloudos-server/chef-repo/solo.json ${CLOUDOS_CHEF}/ && \
cp ${CLOUDOS_BASE}/cloudos-server/chef-repo/deploy.sh ${CLOUDOS_CHEF}/

mkdir -p ${DEPLOY}/email && cp email/* ${DEPLOY}/email
