#!/usr/bin/env bash

java -jar /app/dda-serverspec-standalone.jar /app/certificate-file.edn
java -jar /app/dda-serverspec-standalone.jar /app/command.edn
java -jar /app/dda-serverspec-standalone.jar /app/file.edn
java -jar /app/dda-serverspec-standalone.jar /app/http-cert.edn
java -jar /app/dda-serverspec-standalone.jar /app/netcat.edn
java -jar /app/dda-serverspec-standalone.jar /app/netstat.edn
java -jar /app/dda-serverspec-standalone.jar /app/package.edn
java -jar /app/dda-serverspec-standalone.jar /app/iproute.edn
