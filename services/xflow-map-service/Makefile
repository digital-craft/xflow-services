# Makefile for xflow-services: setup, launch, monitor, inspect containers (dev/prod)
ROOT_DIR := ../..

-include $(ROOT_DIR)/.env

# Load ENV from .env (default to 'dev' if not set)
ifdef ENV
env := $(ENV)
else
env := prod
endif

# Docker Compose command with env files
DOCKER_COMP = docker compose --project-directory ${ROOT_DIR} -f ${ROOT_DIR}/docker-compose.yml --env-file $(ROOT_DIR)/.env --env-file $(ROOT_DIR)/.env.$(env)
MVN = ./mvnw

# Monitor map service logs (logs)
.PHONY: map-service-logs
logs-map-service:
	$(DOCKER_COMP) logs xflow-map-service -f

# clean mvn target directories (for Java services) for xflow-map-service container
.PHONY: clean-map-service
clean-map-service:
	$(DOCKER_COMP) exec xflow-map-service $(MVN) clean
	@echo "✅ Cleaned target directories in container xflow-map-service."

.PHONY: run-map-tests
run-map-tests:
	$(DOCKER_COMP) exec xflow-map-service $(MVN) clean test -e