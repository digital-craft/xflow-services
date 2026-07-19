# ----------- DEV STAGE -----------
FROM eclipse-temurin:21-jdk AS dev
WORKDIR /xflow/auth/app
COPY . /xflow/auth/app


# COPY .mvn/ .mvn/
# COPY mvnw pom.xml ./

# RUN chmod +x mvnw
# RUN ./mvnw -q -DskipTests dependency:go-offline

# COPY src ./src

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
