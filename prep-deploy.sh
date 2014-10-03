#!/bin/bash

DEPLOY=${1}
if [ -z ${DEPLOY} ] ; then
  echo "Usage $0 <deploy-dir>"
  exit 1
fi

BASE=$(cd $(dirname $0) && pwd)
CLOUDOS_BASE="$(cd ${BASE}/../cloudos && pwd)"
mkdir -p ${DEPLOY}/cloudos-chef/cookbooks
CLOUDOS_CHEF=$(cd ${DEPLOY}/cloudos-chef && pwd)

rsync -ac ${CLOUDOS_BASE}/cloudos-server/chef-repo/cookbooks/* ${CLOUDOS_CHEF}/cookbooks/ && \
rsync -ac ${CLOUDOS_BASE}/cloudos-lib/chef-repo/cookbooks/* ${CLOUDOS_CHEF}/cookbooks/ && \
rsync -ac $(find ${CLOUDOS_BASE}/cloudos-apps/apps -type d -name cookbooks) ${CLOUDOS_CHEF}/ && \
rsync -ac $(find ${BASE}/../cloudstead-apps/apps -type d -name cookbooks) ${CLOUDOS_CHEF}/ \

for f in JSON.sh solo.rb install.sh deploy_lib.sh ; do
  cp ${CLOUDOS_BASE}/cloudos-lib/chef-repo/${f} ${CLOUDOS_CHEF}/
done && \
cp ${CLOUDOS_BASE}/cloudos-server/chef-repo/solo.json ${CLOUDOS_CHEF}/ && \
cp ${CLOUDOS_BASE}/cloudos-server/chef-repo/deploy.sh ${CLOUDOS_CHEF}/
