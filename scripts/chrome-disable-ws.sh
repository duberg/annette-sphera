#!/usr/bin/env bash

killall -9 -u sis chrome
ps
google-chrome http://localhost:4200/ --new-window --test-type --auto-open-devtools-for-tabs --disable-web-security --user-data-dir="/home/$(whoami)/.config/google-chrome/"