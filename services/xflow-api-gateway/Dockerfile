# ----------- DEV STAGE -----------
FROM eclipse-temurin:21.0.8_9-jdk-ubi10-minimal AS dev
WORKDIR /xflow/gateway/app
# RUN microdnf update -y && \
#     microdnf install -y curl vim less procps && \
#     microdnf clean all && \
#     useradd --uid 10001 --gid 0 --shell /bin/bash --create-home appuser && \
#     chown -R 10001:0 /xflow/gateway/app
# USER 10001
COPY . /xflow/gateway/app
CMD ["./mvnw", "spring-boot:run"]

# ----------- BUILD STAGE -----------
FROM eclipse-temurin:21.0.8_9-jdk-ubi10-minimal AS build
WORKDIR /xflow/gateway/app
COPY . /xflow/gateway/app
RUN ./mvnw clean package -DskipTests -B && \
    ls -la target/*.jar && \
    rm -rf ~/.m2/repository && \
    rm -rf target/maven-status target/maven-archiver

# ----------- PROD STAGE -----------
FROM eclipse-temurin:21.0.8_9-jre-ubi10-minimal AS prod
WORKDIR /xflow/gateway/app
COPY --from=build /xflow/gateway/app/target/*.jar app.jar
# RUN microdnf update -y && \
#     microdnf install -y curl vim less procps && \
#     microdnf clean all && \
#     useradd --uid 10001 --gid 0 --shell /bin/bash --create-home appuser && \
#     chown -R 10001:0 /xflow/gateway/app
# USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
