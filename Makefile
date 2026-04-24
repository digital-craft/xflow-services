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
