# ----------- DEV STAGE -----------
FROM eclipse-temurin:21-jdk AS dev
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
