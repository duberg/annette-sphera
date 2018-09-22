#!/usr/bin/env bash

rm -rf ~/annette-imc/annette-imc-server/target

cd ~/annette-imc
git reset --hard
git checkout master
git fetch origin
git reset --hard origin/master
git pull

chmod +x ~/annette-imc/scripts/ci-server/update.sh

cd ~/annette-imc/annette-frontend-imc/ng/
#npm install -save
npm install
npm update
npm run build

cd ~/annette-imc
sbt clean
sbt compile
sbt annette-imc-server/universal:packageBin

cd ~/annette-imc/annette-imc-server/target/universal/
unzip -o annette-imc-server.zip


sudo killall -9 -u ci java
ps

# копируем конфигурацию
cp ~/conf/* ~/annette-imc/annette-imc-server/conf/

cd ~/annette-imc/annette-imc-server
./target/universal/annette-imc-server/bin/annette-imc-server &