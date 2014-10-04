#!/bin/bash

BASE=$(cd $(dirname $0) && pwd)
cd ${BASE}

outfile=${BASE}/../cloudstead-apps/apps/cloudstead/files/cloudstead.sql

SILENT="${1}"
if [ ! -z "${SILENT}" ] ; then
  ${BASE}/../cloudos/cloudos-lib/gen-sql.sh cloudstead_test ${outfile} 1> /dev/null 2> /dev/null
else
  ${BASE}/../cloudos/cloudos-lib/gen-sql.sh cloudstead_test ${outfile}
fi
