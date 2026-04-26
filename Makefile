# Makefile for xflow-services: setup, launch, monitor, inspect containers (dev/prod)

-include .env
-include .env.local

# Load ENV from .env (default to 'dev' if not set)
ifdef ENV
env := $(ENV)
else
env := prod
endif

# Docker Compose command with env files
DOCKER_COMP=docker compose --env-file .env --env-file .env.$(ENV)

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

# Init (initialize environment)
.PHONY: init
init: generate-keys

# Setup (build images)
.PHONY: setup
build:
	$(DOCKER_COMP) build

# Launch (up services)
.PHONY: up
dev:
	$(DOCKER_COMP) up -d

# Monitor (logs)
.PHONY: logs logs-c
logs:
	$(DOCKER_COMP) logs -f

# Listen logs from a specific container: make logs-c c=<container_name>
logs-c:
	$(DOCKER_COMP) logs -f $(c)

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
restart: down dev
