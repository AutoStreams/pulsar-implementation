# Code adapted from:
# https://stackoverflow.com/questions/27767264/how-to-dockerize-maven-project-and-how-many-ways-to-accomplish-it

FROM maven:3.8.4-openjdk-17-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

FROM gcr.io/distroless/java17-debian11
COPY --from=build /home/app/target/pulsar-producer.jar /usr/local/lib/pulsar-producer.jar

ENV PULSAR_BROKER_URL='broker:6650'

ENV TOPIC_NAME='Testtopic'
ENV SEND_TIMEOUT_MS=30000
ENV BLOCK_IF_QUEUE_FULL=false
ENV MAX_PENDING_MESSAGES=1000
ENV MAX_PENDING_MESSAGES_ACROSS_PARTITIONS=50000
ENV MESSAGE_ROUTING_MODE=RoundRobinPartition
ENV HASHING_SCHEME=Murmur3_32Hash
ENV CRYPTO_FAILURE_ACTION=FAIL
ENV COMPRESSION_TYPE=NONE

ENTRYPOINT ["java", "-jar", "/usr/local/lib/pulsar-producer.jar"]