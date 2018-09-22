#!/usr/bin/env bash

cqlsh localhost -u valery -p abc
ALTER TABLE annette_aniklab.core_users ADD defaultlanguage text;
