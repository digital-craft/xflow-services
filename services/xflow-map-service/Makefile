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

.PHONY: watch-map
watch-map:
	@echo "Waiting for xflow-map-service target directory (max 30s)..."
	@timeout=30; \
	while [ ! -d "services/xflow-map-service/target" ]; do \
		if [ $$timeout -le 0 ]; then \
			echo "Timeout exceeded. The target directory for xflow-map-service is not found."; \
			exit 1; \
		fi; \
		sleep 1; \
		timeout=$$((timeout - 1)); \
	done
	@echo "Target directory detected for xflow-map-service. Applying permissions..."
	sudo chown -R $(shell id -u):$(shell id -g) services/xflow-map-service/target
	sudo chmod -R  u+rw services/xflow-map-service/target
	@echo "Permissions applied successfully on xflow-map-service !"
