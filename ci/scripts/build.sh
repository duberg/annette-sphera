#!/usr/bin/env bash

mkdir -p /root/.sbt/1.0/plugins
sbt clean
sbt compile
sbt compile