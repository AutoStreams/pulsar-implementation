FROM maven:3.8.4-openjdk-17-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

ENV PULSAR_BROKER_URL='broker-1:6650,broker-2:6651'
ENV TOPIC_NAME = 'Testtopic'
ENV SUBSCRIPTION_NAME = 'subscription'

FROM gcr.io/distroless/java17-debian11
COPY --from=build /home/app/target/pulsar-consumer.jar /usr/local/lib/pulsar-consumer.jar
ENTRYPOINT ["java", "-jar", "/usr/local/lib/data-consumer.jar"]