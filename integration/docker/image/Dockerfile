FROM ubuntu

# install ssh service to be tested by netstat
RUN apt-get update && apt-get upgrade -y && apt-get install openssh-server -y > /dev/null

# install java for testexecution
RUN apt-get install openjdk-11-jre -y > /dev/null

COPY target/uberjar/dda-serverspec-standalone.jar /app/dda-serverspec-standalone.jar
COPY integration/docker/image/resources/* /app/
COPY integration/resources/* /app/

RUN /app/test_it.sh
