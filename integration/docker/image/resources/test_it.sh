#!/usr/bin/env bash

# start ssh to be tested "running" by netstat
service ssh start

# run the tests - every test should result in "passed"
cd /app && \
java -jar /app/dda-serverspec-standalone.jar /app/certificate-file.edn && \
echo "----------" && \
java -jar /app/dda-serverspec-standalone.jar /app/command.edn && \
echo "----------" && \
java -jar /app/dda-serverspec-standalone.jar /app/file.edn && \
echo "----------" && \
java -jar /app/dda-serverspec-standalone.jar /app/package.edn && \
echo "----------" && \
java -jar /app/dda-serverspec-standalone.jar --install-dependencies /app/http-cert.edn && \
java -jar /app/dda-serverspec-standalone.jar /app/http-cert.edn && \
echo "----------" && \
java -jar /app/dda-serverspec-standalone.jar --install-dependencies /app/netstat.edn && \
java -jar /app/dda-serverspec-standalone.jar /app/netstat.edn && \
echo "----------" && \
java -jar /app/dda-serverspec-standalone.jar --install-dependencies /app/netcat.edn && \
java -jar /app/dda-serverspec-standalone.jar /app/netcat.edn && \
echo "----------" && \
java -jar /app/dda-serverspec-standalone.jar --install-dependencies /app/iproute.edn && \
java -jar /app/dda-serverspec-standalone.jar /app/iproute.edn -v
