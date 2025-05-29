FROM openjdk:21
WORKDIR /app
COPY target/mapstruct.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]