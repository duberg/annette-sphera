#!/usr/bin/env bash

# Соединяешься по ssh к серверу
# password: Ddef34Rt*
# ssh sis@192.168.88.242
ssh ci@ci.aniklab.com

# Запускаешь скрипт
~/annette-imc/scripts/ci-server/update.sh

# http://ci.aniklab.com/