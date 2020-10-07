FROM ubuntu:18.04

# install ssh service to be tested by netstat
RUN apt-get update && apt-get upgrade -y && apt-get install openssh-server -y > /dev/null

# install java for testexecution
RUN apt-get install openjdk-11-jre -y > /dev/null

COPY dda-serverspec-standalone.jar /app/dda-serverspec-standalone.jar
COPY resources/* /app/

RUN /app/test_it.sh
