#!/usr/bin/env bash

yum -y update

yum -y install java

yum -y install git

vi /etc/yum.repos.d/cassandra.repo

[cassandra]
name=Apache Cassandra
baseurl=https://www.apache.org/dist/cassandra/redhat/311x/
gpgcheck=1
repo_gpgcheck=1
gpgkey=https://www.apache.org/dist/cassandra/KEYS

yum install cassandra

service cassandra start

cqlsh localhost -u cassandra -p cassandra

CREATE USER valery WITH PASSWORD 'abc' SUPERUSER;

CREATE USER annette WITH PASSWORD 'abc' SUPERUSER;

ALTER USER cassandra WITH PASSWORD 'Anik170929';



sbt annette-akka/universal:packageBin

scp annette-akka-3.0.0.zip root@92.242.40.154:annette-akka-3.0.0.zip
scp annette-akka-3.0.0.zip root@92.242.40.161:annette-akka-3.0.0.zip

yum -y install unzip

unzip annette-akka-3.0.0.zip

#disable SELINUX
vi /etc/sysconfig/selinux
SELINUX=disabled

#reboot
reboot

iptables -A INPUT -m state --state NEW -p tcp --dport 9000 -j ACCEPT
/etc/init.d/iptables restart

firewall-cmd --zone=public --permanent --add-port=9000/tcp
firewall-cmd --reload