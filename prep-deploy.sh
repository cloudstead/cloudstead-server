#!/bin/bash

DEPLOY=${1}
if [ -z ${DEPLOY} ] ; then
  echo "Usage $0 <deploy-dir>"
  exit 1
fi

BASE=$(cd $(dirname $0) && pwd)

mkdir -p ${DEPLOY}/cloudos-chef/cookbooks
CLOUDOS_CHEF=$(cd ${DEPLOY}/cloudos-chef && pwd)

rsync -ac ${BASE}/../cloudos-server/chef-repo/cookbooks/* ${CLOUDOS_CHEF}/cookbooks && \
rsync -ac ${BASE}/../cloudos-lib/chef-repo/cookbooks/* ${CLOUDOS_CHEF}/cookbooks/ && \
rsync -ac ${BASE}/../cloudos-apps/chef-repo/cookbooks/* ${CLOUDOS_CHEF}/cookbooks/ && \

for f in JSON.sh solo.rb install.sh deploy_lib.sh ; do
  cp ${BASE}/../cloudos-lib/chef-repo/${f} ${CLOUDOS_CHEF}/
done && \
cp ${BASE}/../cloudos-server/chef-repo/solo.json ${CLOUDOS_CHEF}/ && \
cp ${BASE}/../cloudos-server/chef-repo/deploy.sh ${CLOUDOS_CHEF}/
