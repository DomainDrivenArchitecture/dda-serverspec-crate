#!/usr/bin/env bash

java -jar /app/dda-serverspec-standalone.jar /app/certificate-file.edn
echo "----------"
java -jar /app/dda-serverspec-standalone.jar /app/command.edn
echo "----------"
java -jar /app/dda-serverspec-standalone.jar /app/file.edn
echo "----------"
java -jar /app/dda-serverspec-standalone.jar /app/package.edn
echo "----------"
java -jar /app/dda-serverspec-standalone.jar --install-dependencies /app/http-cert.edn
java -jar /app/dda-serverspec-standalone.jar /app/http-cert.edn
echo "----------"
apt-get install openssh-server -y
service ssh start
java -jar /app/dda-serverspec-standalone.jar --install-dependencies /app/netstat.edn
java -jar /app/dda-serverspec-standalone.jar /app/netstat.edn
echo "----------"
#java -jar /app/dda-serverspec-standalone.jar /app/netcat.edn
#echo "\\n\\n"
#java -jar /app/dda-serverspec-standalone.jar /app/iproute.edn
