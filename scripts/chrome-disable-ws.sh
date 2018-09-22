#!/usr/bin/env bash

google-chrome http://localhost:4200/ --new-window --test-type --auto-open-devtools-for-tabs --disable-web-security --user-data-dir="/home/$(whoami)/.config/google-chrome/"