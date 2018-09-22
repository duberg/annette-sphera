#!/usr/bin/env bash

cqlsh -u 'cassandra' -p 'cassandra' -f ./cql/drop-keyspace.cql