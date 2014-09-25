#!/bin/bash

BASE=$(cd $(dirname $0) && pwd)
cd ${BASE}

outfile=${BASE}/../cloudos-apps/apps/cloudstead/files/cloudstead.sql

${BASE}/../cloudos-lib/gen-sql.sh cloudstead_test ${outfile}
