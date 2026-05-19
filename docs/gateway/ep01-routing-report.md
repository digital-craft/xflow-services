# EP-01 : Routage par préfixe de chemin — Rapport Technique

**Date de rédaction** : 19 mai 2026  
**Version** : 1.0  
**Branche** : `feature/gateway-path-prefix-routing`  
**Story ID** : GW-RTE-01  
**Jalon** : M1  
**Priorité** : Must Have

---

## Sommaire

1. [Vue d'ensemble](#vue-densemble)
2. [Objectif et contexte](#objectif-et-contexte)
3. [Architecture et design](#architecture-et-design)
4. [Configuration déclarative](#configuration-déclarative)
5. [Variables d'environnement](#variables-denvironnement)
6. [Exemple d'utilisation](#exemple-dutilisation)
7. [Guides de test](#guides-de-test)
8. [Résultats de validation](#résultats-de-validation)
9. [Limitations et future work](#limitations-et-future-work)
10. [Checklist pour reviewer](#checklist-pour-reviewer)

---

## Vue d'ensemble

La **feature EP-01** ajoute un système de routage déclaratif à la XFlow API Gateway. Chaque requête avec un préfixe `/api/{service}/**` est automatiquement routée vers le service backend correspondant. Le préfixe `/api/{service}` est ensuite supprimé avant la transmission de la requête au backend.

### Périmètre de cette implémentation

- ✅ Routage déclaratif via `spring.cloud.gateway` dans `application.yaml`
- ✅ 5 routes backend configurées : `auth`, `map`, `track`, `route`, `link-notif`
- ✅ URIs des services configurables via variables d'environnement
- ✅ Filtre `StripPrefix=2` pour supprimer `/api/{service}` avant de proxifier
- ⏭ Tests d'intégration (GW-RTE-02, future stories ajouteront validation plus poussée)
- ⏭ Fallback HTTP 503 (GW-RTE-05)
- ⏭ Endpoint `/actuator/gateway/routes` (GW-RTE-06)

---

## Objectif et contexte

### Problème posé

L'API Gateway XFlow agit comme point d'entrée unique pour les 6 services backend. Sans routage centralisé :
- Les clients devaient connaître l'adresse directe de chaque service
- Aucun contrôle centralisé des requêtes

### Solution

Utiliser **Spring Cloud Gateway** pour :
1. Accepter toutes les requêtes sur `/api/{service}/**`
2. Les router automatiquement vers le backend correspondant
3. Adapter le chemin transmis au backend (supprimer le préfixe)

### Bénéfices

- **Abstraction** : clients ignorent les adresses des backends
- **Flexibilité** : redirection sans modification client
- **Configuration déclarative** : aucun code Java, tout dans `application.yaml`
- **Scalabilité** : prépare le terrain pour load-balancing, circuit breaker (stories futures)

---

## Architecture et design

### Pile technologique

| Composant | Version | Rôle |
|-----------|---------|------|
| Spring Boot | 3.4.0+ | Framework application |
| Spring Cloud Gateway | 4.1.0+ | Routeur HTTP / proxy |
| Spring Cloud Commons | 4.1.0+ | Service discovery (future) |

### Flux de requête

```
Client HTTP
    │
    ├─ GET /api/auth/login
    │
    ▼
XFlow API Gateway:8080
    │
    ├─ Prédicate: Path=/api/auth/**
    │  ✓ Match !
    │
    ├─ Filtre: StripPrefix=2
    │  Supprime /api/auth
    │  Chemin maintenant: /login
    │
    ▼
xflow-auth-service:8080
    │
    └─ GET /login
```

### Pattern utilisé : Spring Cloud Gateway Declarative Routes

Spring Cloud Gateway supporte deux approches :
1. **Déclarative** (utilisée ici) : routes définies dans `application.yaml` + profils env
2. **Programmée** : routes construites dynamiquement en Java

Nous avons choisi l'approche **déclarative** car :
- Configuration externalizable
- Lisibilité et maintenabilité
- Pas de recompilation pour changer les routes

---

## Configuration déclarative

### Fichier `application.yaml` modifié

```yaml
spring:
  application:
    name: xflow-api-gateway
  cloud:
    gateway:
      routes:
        # Route vers xflow-auth-service
        # Chemins: /api/auth/**
        # Variables d'environnement: XFLOW_AUTH_SERVICE_URL (défaut: http://xflow-auth-service:8080)
        - id: auth-service
          uri: ${XFLOW_AUTH_SERVICE_URL:http://xflow-auth-service:8080}
          predicates:
            - Path=/api/auth/**
          filters:
            - StripPrefix=2

        # Route vers xflow-map-service
        # Chemins: /api/map/**
        # Variables d'environnement: XFLOW_MAP_SERVICE_URL (défaut: http://xflow-map-service:8080)
        - id: map-service
          uri: ${XFLOW_MAP_SERVICE_URL:http://xflow-map-service:8080}
          predicates:
            - Path=/api/map/**
          filters:
            - StripPrefix=2

        # Route vers xflow-tracking-analytics-service
        # Chemins: /api/track/**
        # Variables d'environnement: XFLOW_TRACKING_SERVICE_URL (défaut: http://xflow-tracking-analytics-service:8080)
        - id: tracking-service
          uri: ${XFLOW_TRACKING_SERVICE_URL:http://xflow-tracking-analytics-service:8080}
          predicates:
            - Path=/api/track/**
          filters:
            - StripPrefix=2

        # Route vers xflow-routing-service
        # Chemins: /api/route/**
        # Variables d'environnement: XFLOW_ROUTING_SERVICE_URL (défaut: http://xflow-routing-service:8080)
        - id: routing-service
          uri: ${XFLOW_ROUTING_SERVICE_URL:http://xflow-routing-service:8080}
          predicates:
            - Path=/api/route/**
          filters:
            - StripPrefix=2

        # Route vers xflow-link-notif-service
        # Chemins: /api/link/**
        # Variables d'environnement: XFLOW_LINK_NOTIF_SERVICE_URL (défaut: http://xflow-link-notif-service:8080)
        - id: link-notif-service
          uri: ${XFLOW_LINK_NOTIF_SERVICE_URL:http://xflow-link-notif-service:8080}
          predicates:
            - Path=/api/link/**
          filters:
            - StripPrefix=2
```

### Explication des champs

| Champ | Valeur | Rôle |
|-------|--------|------|
| `id` | `auth-service`, `map-service`, etc. | Identifiant unique de la route |
| `uri` | `${VAR:default}` | URL cible du backend (property placeholder avec fallback) |
| `predicates` | `Path=/api/{service}/**` | Règle de matching des requêtes entrantes |
| `filters` | `StripPrefix=2` | Supprime 2 segments du chemin : `/api/{service}` |

---

## Variables d'environnement

### Configuration par environnement

| Variable | Défaut | Environnement | Exemple |
|----------|--------|---------------|---------|
| `XFLOW_AUTH_SERVICE_URL` | `http://xflow-auth-service:8080` | dev, staging, prod | `http://auth-svc.internal:8080` |
| `XFLOW_MAP_SERVICE_URL` | `http://xflow-map-service:8080` | dev, staging, prod | `http://map-svc.internal:9000` |
| `XFLOW_TRACKING_SERVICE_URL` | `http://xflow-tracking-analytics-service:8080` | dev, staging, prod | `http://tracking-svc.internal:8081` |
| `XFLOW_ROUTING_SERVICE_URL` | `http://xflow-routing-service:8080` | dev, staging, prod | `http://routing-svc.internal:8080` |
| `XFLOW_LINK_NOTIF_SERVICE_URL` | `http://xflow-link-notif-service:8080` | dev, staging, prod | `http://link-notif-svc.internal:8080` |

### Hiérarchie de configuration

Spring Boot applique cette hiérarchie (ordre de précédence croissant) :

1. **Défauts dans `application.yaml`**
2. **Fichier `application-{profile}.yaml`** (ex: `application-prod.yaml`)
3. **Variables d'environnement** (ex: `$XFLOW_AUTH_SERVICE_URL`)
4. **Fichier `.env`** (via Spring profiles)

### Docker Compose (exemple pour dev)

```yaml
services:
  xflow-api-gateway:
    image: xflow-api-gateway:latest
    ports:
      - "8080:8080"
    environment:
      XFLOW_AUTH_SERVICE_URL: http://xflow-auth-service:8080
      XFLOW_MAP_SERVICE_URL: http://xflow-map-service:8080
      XFLOW_TRACKING_SERVICE_URL: http://xflow-tracking-analytics-service:8080
      XFLOW_ROUTING_SERVICE_URL: http://xflow-routing-service:8080
      XFLOW_LINK_NOTIF_SERVICE_URL: http://xflow-link-notif-service:8080
    depends_on:
      - xflow-auth-service
      - xflow-map-service
      # ... autres services
```

### Kubernetes (futur)

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: gateway-config
data:
  XFLOW_AUTH_SERVICE_URL: "http://xflow-auth-service:8080"
  XFLOW_MAP_SERVICE_URL: "http://xflow-map-service:8080"
  # ...

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: xflow-api-gateway
spec:
  template:
    spec:
      containers:
      - name: gateway
        envFrom:
        - configMapRef:
            name: gateway-config
```

---

## Exemple d'utilisation

### Client localhost / dev

Supposons que la Gateway écoute sur `http://localhost:8080` et l'auth-service sur `http://localhost:8081` (en interne).

#### Avant (sans Gateway)

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"secret"}'
```

#### Après (avec Gateway)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"secret"}'
```

Le chemin `/api/auth/login` atteint la Gateway :
- Prédicate `Path=/api/auth/**` → ✓ match
- Filtre `StripPrefix=2` → transforme en `/login`
- Route `uri: http://xflow-auth-service:8080` → envoie à `http://xflow-auth-service:8080/login`

### Autres exemples

| Requête entrante | Prédicate | Routes vers | Chemin transmis |
|------------------|-----------|-------------|-----------------|
| `GET /api/auth/health` | `/api/auth/**` | auth-service:8080 | `/health` |
| `POST /api/map/publish` | `/api/map/**` | map-service:8080 | `/publish` |
| `GET /api/track/events` | `/api/track/**` | tracking-service:8080 | `/events` |
| `POST /api/route/optimize` | `/api/route/**` | routing-service:8080 | `/optimize` |
| `PUT /api/link/notify` | `/api/link/**` | link-notif-service:8080 | `/notify` |

---

## Guides de test

### Prérequis

- Docker Desktop ou Docker Engine 24.x
- `docker-compose` V2 (plugin)
- Clés RSA générées (`make init`)
- Stack démarrée (`make dev`)

### Test local avec curl

Après `make dev` (tous les services en conteneurs) :

```bash
# 1. Test de la route auth-service
curl -i http://localhost:8080/api/auth/health

# 2. Test de la route map-service
curl -i http://localhost:8080/api/map/health

# 3. Test de la route tracking-service
curl -i http://localhost:8080/api/track/health

# 4. Test de la route routing-service
curl -i http://localhost:8080/api/route/health

# 5. Test de la route link-notif-service
curl -i http://localhost:8080/api/link/health
```

### Résultats attendus

Si tous les services sont actifs (conteneurs running) :

```http
HTTP/1.1 200 OK
Content-Type: application/json

{ "status": "UP", "service": "auth-service" }
```

Si un service est indisponible (conteneur arrêté) :

```http
HTTP/1.1 502 Bad Gateway
Content-Type: application/json

{ "error": "Service unavailable" }
```

### Test avec proxy (curl verbose)

Pour vérifier les headers de proxy :

```bash
curl -v http://localhost:8080/api/auth/health 2>&1 | grep -E "^> |^< "
```

Résultat attendu :

```
> GET /api/auth/health HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.85.0
> Accept: */*
>
< HTTP/1.1 200 OK
< Content-Type: application/json
< X-Forwarded-For: 127.0.0.1
< X-Forwarded-Proto: http
< X-Forwarded-Host: localhost
```

### Test de configuration personnalisée

Pour tester avec des URLs custom :

```bash
# Définir les variables d'environnement
export XFLOW_AUTH_SERVICE_URL=http://custom-auth-server:9000
export XFLOW_MAP_SERVICE_URL=http://custom-map-server:9001

# Redémarrer la gateway
docker-compose restart xflow-api-gateway

# Vérifier (la requête sera routée vers custom-auth-server:9000)
curl -i http://localhost:8080/api/auth/health
```

---

## Résultats de validation

### Compilation et build

✅ **Status** : OK

```bash
./mvnw -pl services/xflow-api-gateway clean package
```

Résultat :
- [x] Compile sans erreurs
- [x] Tests exécutés (note: aucun test EP-01 pour l'instant)
- [x] JAR produit : `target/xflow-api-gateway-0.0.1-SNAPSHOT.jar`

### Exécution locale

✅ **Status** : Branche compilée et pushée

```bash
cd services/xflow-api-gateway
./mvnw spring-boot:run
```

La gateway démarre avec 5 routes configurées, prête à proxifier vers les backends.

### Validation des chemins

| Chemin testable | État | Notes |
|-----------------|------|-------|
| `/api/auth/**` | ✅ Route déclarée | StripPrefix=2 |
| `/api/map/**` | ✅ Route déclarée | StripPrefix=2 |
| `/api/track/**` | ✅ Route déclarée | StripPrefix=2 |
| `/api/route/**` | ✅ Route déclarée | StripPrefix=2 |
| `/api/link/**` | ✅ Route déclarée | StripPrefix=2 |

---

## Limitations et future work

### Limitations de cette implémentation

1. **Pas de gestion d'erreur centralisée** : les erreurs 502/503 ne sont pas formatées JSON. Story GW-RTE-05 ajoutera un fallback structuré.

2. **Pas d'endpoint `/actuator/gateway/routes`** : les routes ne sont pas exposées pour consultation publique. Story GW-RTE-06 l'ajoutera.

3. **Pas de Load Balancing automatique** : si un service a plusieurs replicas en K8s, Spring Cloud Gateway ne les reconnaît pas encore. Story GW-RTE-04 intègrera le service discovery.

4. **Pas de authentification JWT** : les requêtes ne sont pas filtrées/validées. Story GW-SEC-01 ajoutera la validation JWT.

5. **Pas de rate limiting** : aucune limite de débit. Story GW-RL-01 ajoutera un rate limiter.

6. **Pas de tests d'intégration** : aucun test spécifique à EP-01 (trop tôt pour une config test centralisée). Sera ajouté avec une future story "tests d'intégration gateway".

### Future stories dépendant d'EP-01

- **GW-RTE-02** : Réécriture des chemins (StripPrefix) — déjà implémenté ici !
- **GW-RTE-03** : Routage vers Martin (tuiles publiques)
- **GW-RTE-04** : Load balancing entre replicas
- **GW-RTE-05** : Route fallback — service indisponible
- **GW-RTE-06** : Endpoint de santé des routes
- **GW-SEC-01** : Validation JWT RS256 — filtre global
- **GW-RL-01** : Rate limiting global par IP

---

## Checklist pour reviewer

Avant d'approuver cette PR, vérifier :

### Code & Configuration

- [ ] `application.yaml` contient 5 routes avec les 5 services backend (auth, map, track, route, link-notif)
- [ ] Chaque route a un `id` unique, `uri` configurable, et filtre `StripPrefix=2`
- [ ] Les variables d'environnement sont documentées dans ce rapport
- [ ] Le fichier compile sans erreur (`./mvnw -pl services/xflow-api-gateway clean package`)

### Documentation

- [ ] Ce rapport détaille l'architecture, config, et usage
- [ ] Le rapport inclut des exemples `curl` testables
- [ ] Les limites et futures stories références sont clairement documentées

### Tests (en attente)

- [ ] ⏸ Tests d'intégration non implémentés (config test à venir)
- [ ] ⏸ Validation manuelle possible avec `make dev` + `curl`

### PR & Commits

- [ ] Le commit suit le format Conventional Commits : `feat(gateway): add path-prefix routing for 5 backend services`
- [ ] Un seul commit squashé (via "Squash and merge")
- [ ] Pas de secrets commités (`.env`, clés, mots de passe)

### Avant merge

- [ ] Valider localement : `make dev && curl http://localhost:8080/api/auth/health`
- [ ] Approuver si tous les points ci-dessus sont OK

---

## Notes pour l'implémentation future

### Amélioration : Service Discovery (Kubernetes)

Dans un environnement Kubernetes, remplacer les URIs statiques par des noms de service :

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://xflow-auth-service  # Load balancer Kubernetes
          predicates:
            - Path=/api/auth/**
          filters:
            - StripPrefix=2
```

Nécessite :
- Dépendance : `spring-cloud-kubernetes-discovery`
- Activation du discovery client

### Amélioration : Circuit Breaker

Ajouter un circuit breaker à chaque route pour la résilience :

```yaml
filters:
  - StripPrefix=2
  - name: CircuitBreaker
    args:
      name: authCircuitBreaker
      fallbackUri: forward:/fallback/auth
```

Story : GW-RES-01

### Amélioration : Rate Limiting

Ajouter un rate limiter global :

```yaml
filters:
  - StripPrefix=2
  - name: RequestRateLimiter
    args:
      redis-rate-limiter.replenish-rate: 100
      redis-rate-limiter.burst-capacity: 200
```

Story : GW-RL-01

---

## Conclusion

La feature **EP-01** établit les fondations du routage dans la XFlow API Gateway. Configuration déclarative, URIs configurables, et filtrage du chemin sont maintenant en place. Les stories futures (GW-RTE-02 à GW-RTE-06, GW-SEC-01, GW-RL-01, etc.) s'appuieront sur cette base pour ajouter résilience, sécurité, et performance.

**Statut** : ✅ Prêt pour review et merge vers `develop`

---

**Rédaction** : GitHub Copilot  
**Révision** : À valider par l'équipe core XFlow

