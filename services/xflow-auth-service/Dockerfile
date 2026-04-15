# ----------- DEV STAGE -----------
FROM eclipse-temurin:21-jdk AS dev
WORKDIR /app
COPY . /app
CMD ["./mvnw", "spring-boot:run"]

# ----------- BUILD STAGE -----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . /app
RUN ./mvnw clean package -DskipTests

# ----------- PROD STAGE -----------
FROM eclipse-temurin:21-jre AS prod
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
