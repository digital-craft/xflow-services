# ----------- DEV STAGE -----------
FROM eclipse-temurin:21-jdk AS dev
RUN apt-get update && apt-get install -y --no-install-recommends inotify-tools && rm -rf /var/lib/apt/lists/*
WORKDIR /xflow/gateway/app
COPY . /xflow/gateway/app
CMD ["./mvnw", "spring-boot:run"]

# ----------- BUILD STAGE -----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /xflow/gateway/app
COPY . /xflow/gateway/app
RUN ./mvnw clean package -DskipTests

# ----------- PROD STAGE -----------
FROM eclipse-temurin:21-jre AS prod
WORKDIR /xflow/gateway/app
# Copy only the built JAR from the build stage
COPY --from=build /xflow/gateway/app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
