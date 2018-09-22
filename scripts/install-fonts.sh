#!/usr/bin/env bash

# Installing Times New Roman font
sudo apt-get install -y msttcorefonts
sudo apt-get install -y ttf-mscorefonts-installer

sudo fc-cache -fv

reboot