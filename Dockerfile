FROM maven:3.8.1-openjdk-11-slim AS build
WORKDIR /usr/ir
COPY pom.xml /usr/ir
COPY src /usr/ir/src
RUN mvn clean package

FROM adoptopenjdk:11-jre-hotspot
COPY --from=build /usr/ir/target/*.jar ir.jar
COPY ./stopWords.txt stopWords.txt
COPY ./data/articles.json ./data/articles.json
COPY ./TREC/czechData.bin ./TREC/czechData.bin
EXPOSE 8080
ENTRYPOINT ["java","-jar","ir.jar"]
