# ----------- DEV STAGE -----------
FROM eclipse-temurin:21.0.8_9-jdk-ubi10-minimal AS dev
WORKDIR /xflow/auth/app
RUN microdnf update -y && \
    microdnf install -y curl vim less procps && \
    microdnf clean all && \
    useradd --uid 10001 --gid 0 --shell /bin/bash --create-home appuser && \
    chown -R 10001:0 /xflow/auth/app
USER 10001
COPY . /xflow/auth/app


# COPY .mvn/ .mvn/
# COPY mvnw pom.xml ./

# RUN chmod +x mvnw
# RUN ./mvnw -q -DskipTests dependency:go-offline

# COPY src ./src

CMD ["./mvnw", "spring-boot:run"]

# ----------- BUILD STAGE -----------
FROM eclipse-temurin:21.0.8_9-jdk-ubi10-minimal AS build
WORKDIR /xflow/auth/app
COPY . /xflow/auth/app
RUN ./mvnw clean package -DskipTests -B && \
    ls -la target/*.jar && \
    rm -rf ~/.m2/repository && \
    rm -rf target/maven-status target/maven-archiver

# ----------- PROD STAGE -----------
FROM eclipse-temurin:21.0.8_9-jre-ubi10-minimal AS prod
WORKDIR /xflow/auth/app
COPY --from=build /xflow/auth/app/target/*.jar app.jar
RUN microdnf update -y && \
    microdnf install -y curl vim less procps && \
    microdnf clean all && \
    useradd --uid 10001 --gid 0 --shell /bin/bash --create-home appuser && \
    chown -R 10001:0 /xflow/auth/app
USER 10001
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health/liveness || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]




