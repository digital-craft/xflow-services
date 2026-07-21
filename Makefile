# Makefile for xflow-services: setup, launch, monitor, inspect containers (dev/prod)

-include .env
-include services/xflow-auth-service/Makefile
-include services/xflow-api-gateway/Makefile

# Load ENV from .env (default to 'dev' if not set)
ifdef ENV
env := $(ENV)
else
env := prod
endif

# Color codes for terminal output
YELLOW=\033[1;33m
GREEN=\033[1;32m
BLUE=\033[0;34m
LIGHT_BLUE=\033[1;36m
NC=\033[0m # No Color

# Docker Compose command with env files
DOCKER_COMP = docker compose --env-file .env --env-file .env.$(ENV)
MVN = ./mvnw

# Generate RSA keys and update .env with Base64-encoded values
.PHONY: generate-keys
generate-keys:
	@if [ ! -f .env ]; then \
		echo "❌ Error: The file .env could not be found. Please create it before continuing."; \
		exit 1; \
	fi
	@mkdir -p infra/keys
	@if [ ! -f infra/keys/private.pem ]; then \
		openssl genrsa -out infra/keys/private.pem 2048; \
		openssl rsa -in infra/keys/private.pem -pubout -out infra/keys/public.pem; \
		echo "🔑 RSA keys generated in infra/keys."; \
	else \
		echo "⚠️  Keys already exist, proceeding with .env update."; \
	fi
	@PRIV_B64=$$(openssl pkcs8 -topk8 -inform PEM -outform DER -nocrypt -in infra/keys/private.pem | base64 | tr -d '\n'); \
	PUB_B64=$$(openssl rsa -pubin -inform PEM -outform DER -in infra/keys/public.pem | base64 | tr -d '\n'); \
	if ! grep -q "RSA_PRIVATE_KEY" .env; then \
		echo "RSA_PRIVATE_KEY=$$PRIV_B64" >> .env; \
		echo "✅ RSA_PRIVATE_KEY added to .env"; \
	else \
		echo "⚠️  RSA_PRIVATE_KEY already exists in .env, skipping."; \
	fi; \
	if ! grep -q "RSA_PUBLIC_KEY" .env; then \
		echo "RSA_PUBLIC_KEY=$$PUB_B64" >> .env; \
		echo "✅ RSA_PUBLIC_KEY added to .env"; \
	else \
		echo "⚠️  RSA_PUBLIC_KEY already exists in .env, skipping."; \
	fi
	@echo "✅ RSA variables added to .env (if they were not already present)."

# Show urls
.PHONY: show-urls
show-urls:
	@echo ""
	@printf "${BLUE}+-------------------------------------------------+\n"
	@printf "${BLUE}| XFLOW PLATFORM - Development Mode               |\n"
	@printf "${BLUE}+-------------------------------------------------+\n"
	@printf "${BLUE}| ${BLUE}%-19s ${BLUE}| ${LIGHT_BLUE}%-27s${BLUE} |\n" "🚀 API Gateway"     "http://localhost:$(API_GATEWAY_PORT)"
	@printf "${BLUE}| ${BLUE}%-19s ${BLUE}| ${LIGHT_BLUE}%-27s${BLUE} |\n" "🔐 Auth Service"     "http://localhost:$(AUTH_SERVICE_PORT)"
	@printf "${BLUE}| ${BLUE}%-19s ${BLUE}| ${LIGHT_BLUE}%-27s${BLUE} |\n" "📧 Email Service"     "http://localhost:$(MAILPIT_WEB_UI_PORT)"
	@printf "${BLUE}+-------------------------------------------------+\n"
	@printf "${GREEN}  ✔ All services are monitored with DevTools!${NC}\n"
	@echo ""

# Init (initialize environment)
.PHONY: init
init: generate-keys

# Setup (build images)
.PHONY: setup
build:
	$(DOCKER_COMP) build

# Watch realtime updates
.PHONY: watch all containers
watch-all: watch-auth watch-gateway

# Launch (up services)
.PHONY: dev
dev:
	$(DOCKER_COMP) up -d
	$(MAKE) watch-all
	$(MAKE) show-urls

# Monitor (logs)
.PHONY: logs-all
logs-all: logs-auth-service logs-gateway-service

# Inspect (status)
.PHONY: ps
ps:
	$(DOCKER_COMP) ps

# Stop and clean up
.PHONY: down
down:
	$(DOCKER_COMP) down

# restart services
.PHONY: restart
restart: down build dev

# clean mvn target directories (for Java services) for all containers
.PHONY: clean-all
clean-all: clean-auth-service

.PHONY: run-tests
run-tests: run-auth-tests