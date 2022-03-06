# Code adapted from:
# https://stackoverflow.com/questions/27767264/how-to-dockerize-maven-project-and-how-many-ways-to-accomplish-it

FROM maven:3.8.4-openjdk-17-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

FROM gcr.io/distroless/java17-debian11
COPY --from=build /home/app/target/pulsar-producer-1.0-SNAPSHOT-jar-with-dependencies.jar /usr/local/lib/pulsar-producer.jar
ENV PULSAR_BROKER_URL='broker-1:6650,broker-2:6651'
EXPOSE 8992
ENTRYPOINT ["java", "-jar", "/usr/local/lib/pulsar-producer.jar"]