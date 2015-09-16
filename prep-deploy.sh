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
mkdir -p ${DEPLOY}/cloudos-chef/data_files
CLOUDOS_CHEF=$(cd ${DEPLOY}/cloudos-chef && pwd)

for f in JSON.sh solo.rb install.sh uninstall.sh deploy_lib.sh ; do
  cp ${CLOUDOS_BASE}/cloudos-lib/chef-repo/${f} ${CLOUDOS_CHEF}/
done && \
cp ${CLOUDOS_BASE}/cloudos-server/chef-repo/deploy.sh ${CLOUDOS_CHEF}/

mkdir -p ${DEPLOY}/email && cp email/* ${DEPLOY}/email
