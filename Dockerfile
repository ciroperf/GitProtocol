FROM alpine/git
WORKDIR /app
RUN git clone https://github.com/ciroperf/GitProtocol.git

FROM maven:3.5-jdk-8-alpine as base
WORKDIR /app
COPY --from=0 /app/GitProtocol /app

FROM base as test
CMD ["./mvnw", "test"]

FROM base as build
RUN mvn package

FROM openjdk:8-jre-alpine
WORKDIR /app
ENV MASTERIP=127.0.0.1
ENV ID=0
COPY --from=1 /app/target/gitprotocol-1.0-jar-with-dependencies.jar /app

CMD /usr/bin/java -jar gitprotocol-1.0-jar-with-dependencies.jar -m $MASTERIP -id $ID
