#!/usr/bin/env bash

#
# Install protobuf
#
#
# Make sure you grab the latest version
curl -OL https://github.com/google/protobuf/releases/download/v3.2.0/protoc-3.2.0-linux-x86_64.zip

# Unzip
unzip protoc-3.2.0-linux-x86_64.zip -d protoc3

# Move protoc to /usr/local/bin/
sudo mv protoc3/bin/* /usr/local/bin/

# Move protoc3/include to /usr/local/include/
sudo mv protoc3/include/* /usr/local/include/

# Optional: change owner
sudo chown sis /usr/local/bin/protoc
sudo chown -R sis /usr/local/include/google

# SBT install
echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
sudo apt-get update
sudo apt-get install -y sbt

# Install postgresql
sudo apt-get install postgresql-10 postgresql-contrib
sudo -u postgres psql postgres
create database annette_sphera;
#\password postgres
#abc
\q

# Generate default application data. In conf/application.conf set this:
# core.initDb.createSchema = true

# Install nodejs
sudo apt-get install -y curl
curl -sL https://deb.nodesource.com/setup_8.x | sudo -E bash -
sudo apt-get install -y nodejs

# Install angular/cli
sudo npm install -g @angular/cli

# Build frontend
cd annette-frontend-sphera/ng/
npm install
npm run build

# Start sbt task:
cd ../../
sbt ~annette-sphera-server/reStart


