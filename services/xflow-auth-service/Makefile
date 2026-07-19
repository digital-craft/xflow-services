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

# Monitor auth service logs (logs)
.PHONY: auth-service-logs
logs-auth-service:
	$(DOCKER_COMP) logs xflow-auth-service -f

# clean mvn target directories (for Java services) for xflow-auth-service container
.PHONY: clean-auth-service
clean-auth-service:
	$(DOCKER_COMP) exec xflow-auth-service $(MVN) clean
	@echo "✅ Cleaned target directories in container xflow-auth-service."

.PHONY: run-auth-tests
run-auth-tests:
	$(DOCKER_COMP) exec xflow-auth-service $(MVN) clean test -e

.PHONY: watch-auth
watch-auth:
	@echo "Waiting for xflow-auth-service target directory (max 30s)..."
	@timeout=30; \
	while [ ! -d "services/xflow-auth-service/target" ]; do \
		if [ $$timeout -le 0 ]; then \
			echo "Timeout exceeded. The target directory for xflow-auth-service is not found."; \
			exit 1; \
		fi; \
		sleep 1; \
		timeout=$$((timeout - 1)); \
	done
	@echo "Target directory detected for xflow-auth-service. Applying permissions..."
	sudo chown -cR $(shell id -u):$(shell id -g) services/xflow-auth-service/target
	sudo chmod -cR u+rw services/xflow-auth-service/target
	@echo "Permissions applied successfully on xflow-auth-service !"
