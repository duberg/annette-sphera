#!/usr/bin/env bash

cqlsh localhost -u kantemirov -p abc
ALTER TABLE annette_aniklab.core_users ADD defaultlanguage text;
