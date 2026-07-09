# ----------- DEV STAGE -----------
FROM eclipse-temurin:21-jdk AS dev
RUN apt-get update && apt-get install -y --no-install-recommends inotify-tools && rm -rf /var/lib/apt/lists/*
WORKDIR /xflow/auth/app
COPY . /xflow/auth/app
CMD ["./mvnw", "spring-boot:run"]

# ----------- BUILD STAGE -----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /xflow/auth/app
COPY . /xflow/auth/app
RUN ./mvnw clean package -DskipTests

# ----------- PROD STAGE -----------
FROM eclipse-temurin:21-jre AS prod
WORKDIR /xflow/auth/app
COPY --from=build /xflow/auth/app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
