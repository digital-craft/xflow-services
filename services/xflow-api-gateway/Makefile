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

# Monitor gateway service logs (logs)
.PHONY: gateway-service-logs
logs-gateway-service:
	$(DOCKER_COMP) logs xflow-api-gateway -f

# clean mvn target directories (for Java services) for xflow-api-gateway container
.PHONY: clean-gateway-service
clean-gateway-service:
	$(DOCKER_COMP) exec xflow-api-gateway $(MVN) clean
	@echo "✅ Cleaned target directories in container xflow-api-gateway."

.PHONY: run-gateway-tests
run-gateway-tests:
	$(DOCKER_COMP) exec xflow-api-gateway $(MVN) clean test -e

.PHONY: watch-gateway
watch-gateway:
	@echo "Waiting for xflow-api-gateway target directory (max 30s)..."
	@timeout=30; \
	while [ ! -d "services/xflow-api-gateway/target" ]; do \
		if [ $$timeout -le 0 ]; then \
			echo "Timeout exceeded. The target directory for xflow-api-gateway is not found."; \
			exit 1; \
		fi; \
		sleep 1; \
		timeout=$$((timeout - 1)); \
	done
	@echo "Target directory detected for xflow-api-gateway. Applying permissions..."
	sudo chown -cR $(shell id -u):$(shell id -g) services/xflow-api-gateway/target
	sudo chmod -cR u+rw services/xflow-api-gateway/target
	@echo "Permissions applied successfully on xflow-api-gateway !"
