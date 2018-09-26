#!/usr/bin/env bash

gnome-terminal -x sh -c "newman run ./annette-bpm.postman_collection.json --folder 'ResidentRegistrationProcess' -e ./ResidentRegistrationProcess.postman_environment.json; bash"