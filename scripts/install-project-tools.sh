#!/usr/bin/env bash

sudo apt-get install -y curl
# Install nodejs
curl -sL https://deb.nodesource.com/setup_8.x | sudo -E bash -
sudo apt-get install -y nodejs

# Install global dep
# Fix: bug with undefined loop
# https://github.com/angular/angular-cli/issues/6727
# Angular. Shame on you!
sudo npm install -g node-gyp
sudo npm install -g @angular/cli

# Prepare frontend
cd ~/Desktop/annette-aniklab/annette-frontend/
npm install
npm run build
cd ~/Desktop/annette-aniklab/annette-frontend-imc/
npm install
# npm run build
ng build --watch

# Install Cassandra
echo "deb http://www.apache.org/dist/cassandra/debian 311x main" | sudo tee -a /etc/apt/sources.list.d/cassandra.sources.list
curl https://www.apache.org/dist/cassandra/KEYS | sudo apt-key add -
sudo apt-get update
sudo apt-key adv --keyserver pool.sks-keyservers.net --recv-key A278B781FE4B2BDA
sudo apt-get install -y cassandra

# The default location of configuration files is /etc/cassandra.

# You need to enable PasswordAuthenticator in cassandra.yaml file.
# To enable PasswordAuthenticator you need to change authenticator property in cassandra.yaml
# Change: authenticator: AllowAllAuthenticator
# to: authenticator: PasswordAuthenticator
sudo gedit /etc/cassandra/cassandra.yaml
sudo service cassandra start
# If establishing connection fails try multiple times:
cqlsh localhost -u cassandra -p cassandra
CREATE USER annette WITH PASSWORD 'abc' SUPERUSER;
CREATE USER valery WITH PASSWORD 'abc' SUPERUSER;
ALTER USER cassandra WITH PASSWORD 'afsso67347mething54747long67a7ndincom4574prehensi562ble';

cd ~/Desktop/

# Install protobuf
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

# sbt annette-akka/run


