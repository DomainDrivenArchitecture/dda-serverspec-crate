FROM ubuntu
RUN apt-get update && apt-get upgrade -y && apt-get install openjdk-8-jre -y
COPY target/dda-serverspec-standalone.jar /app/dda-serverspec-standalone.jar
COPY integration/docker/image/resources/* /app/
COPY integration/resources/* /app/

RUN /app/test_it.sh
