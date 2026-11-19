#!/bin/bash
# Runs once, after the image has created the database (/opt/oracle/scripts/setup). Creates the application user {{user}} in
# {{service}} with the privileges the generated programs and samples use, and sets the vector pool for HNSW indexes and the
# In-Memory column store for load and release in the server parameter file (effective after the next restart; INMEMORY_SIZE
# is at least 100M and is taken from the SGA). The password comes from VECDSL_ORACLEDB_PASSWORD; it must not
# contain a double quotation mark.
set -euo pipefail
: "${VECDSL_ORACLEDB_PASSWORD:?VECDSL_ORACLEDB_PASSWORD is not set}"
sqlplus -s / as sysdba <<SQL
WHENEVER SQLERROR EXIT FAILURE
ALTER SYSTEM SET VECTOR_MEMORY_SIZE = ${VECDSL_VECTOR_MEMORY:-{{vector.memory}}} SCOPE = SPFILE;
ALTER SYSTEM SET INMEMORY_SIZE = ${VECDSL_INMEMORY_SIZE:-{{inmemory.size}}} SCOPE = SPFILE;
ALTER SESSION SET CONTAINER = {{service}};
CREATE USER {{user}} IDENTIFIED BY "${VECDSL_ORACLEDB_PASSWORD}";
GRANT CREATE SESSION, CREATE TABLE, CREATE VIEW, CREATE PROCEDURE, CREATE ROLE, UNLIMITED TABLESPACE TO {{user}};
GRANT CTXAPP TO {{user}};
GRANT EXECUTE ON CTXSYS.CTX_DDL TO {{user}};
EXIT
SQL
echo "VECDSL USER READY"
