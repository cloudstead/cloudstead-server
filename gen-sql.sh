#!/bin/bash

BASE=$(cd $(dirname $0) && pwd)
cd ${BASE}

outfile=${BASE}/../cloudstead-apps/apps/cloudstead/files/cloudstead.sql

${BASE}/../cloudos/cloudos-lib/gen-sql.sh cloudstead_test ${outfile}
