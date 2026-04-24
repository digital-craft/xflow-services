# Contributing to XFlow Platform

> Welcome to **XFlow** — thank you for contributing to this open-source project.

> This guide defines the working rules for the core team and external contributors.

> Read this entire document before opening a Pull Request.

---

## Table of Contents

1. [Prerequisites & Local Setup](#1-prerequisites--local-setup)
2. [Branching Strategy — Git Flow](#2-branching-strategy--git-flow)
3. [Commit Conventions — Conventional Commits](#3-commit-conventions--conventional-commits)
4. [Complete Workflow — From Feature to Merge](#4-complete-workflow--from-feature-to-merge)
5. [Pull Requests](#5-pull-requests)
6. [Code Review](#6-code-review)
7. [Java Code Conventions](#7-java-code-conventions)
8. [Versioning & Releases — SemVer](#8-versioning--releases--semver)
9. [Reporting bugs](#9-bug-reporting)
10. [Security Policy](#10-security-policy)

---

## 1. Prerequisites & Local Setup

### Required Tools

| Tool | Minimum Version | Verification |

|---|---|---|

| Java (JDK Temurin) | 21 LTS | `java -version` |

| Maven | 3.9+ (or `./mvnw`) | `./mvnw -v` |

| Docker Desktop / Engine | 24+ | `docker -v` |

| Docker Compose | V2 (plugin) | `docker compose version` |

| Make | all | `make --version` |

| OpenSSL | 3.x | `openssl version` |

| Git | 2.40+ | `git --version` |

### Getting Started in 5 Commands

```bash
# 1. Clone the repository
git clone https://github.com/xflow-platform/xflow-platform.git
cd xflow-platform

# 2. Generate RSA keys for JWT (first time only)
make keys
# → Copy JWT_PRIVATE_KEY and JWT_PUBLIC_KEY into .env

# 3. Configure the local environment
cp .env.example .env
# → Edit .env with the keys generated in step 2

# 4. Build Maven + Docker images

make build

# 5. Start the full stack
make dev
# → API available at http://localhost:8080
```

### Available Makefile Commands

```bash
make dev # Start the stack
make down # Stop the stack
make build # Build Maven + Docker
make restart # down + build + up
make logs # Logs of all services
make auth-logs # Logs of the auth-service only
make gateway-logs # Logs of the api-gateway only
make ps # Status of containers
make test # All Maven tests
make test-auth # Tests of the auth-service only
make test-gateway # Tests of the api-gateway only
make keys # Generate RSA JWT keys
make clean # Delete volumes + builds

```

### Monorepo Structure

```
xflow-platform/
├── services/
│ ├── xflow-api-gateway/
│ ├── xflow-auth-service/
│ ├── xflow-map-service/
│ ├── xflow-tracking-analytics-service/
│ ├── xflow-routing-service/
│ └── xflow-link-notif-service/
├── below/
│ ├── docker/
│ ├── k8s/
│ ├── helm/
│ └── terraform/
├── docs/
│ ├── adr/
│ └── api/
├── pom.xml ← Parent BOM Maven
├── docker-compose.yml
├── Makefile
└── .env.example
```

---

## 2. Branching Strategy — Git Flow

XFlow uses **Git Flow**. The diagram below summarizes permanent and ephemeral branches.

```
hand ─────────────────────────── ──────────────────────────► production 
│ ▲ 
└──► develop ──────────────────── ──────────────────►─┘ 
│ ▲ 
├──► feature/gw-jwt-filter ────►┘ 
├──► feature/auth-refresh-token ►┘ 
├──► fix/tracking-websocket ─────►┘
│
└──► release/1.2.0 ───────────────────────────► main + tag
│

└──► hotfix/1.2.1 ─────────────────► main + develop
```

### Permanent Branches

| Branch | Role | Protection |

|---|---|---|
| `main` | Production code — stable, tagged | ✅ Protected (see §2.3) |

| `develop` | Continuous integration — all features merged here | ✅ Protected |

### Ephemeral branches

| Type | Prefix | Example | Base | Merge to |

|---|---|---|---|---|
| New feature | `feature/` | `feature/auth-refresh-token` | `develop` | `develop` |

| Bug fixes | `fix/` | `fix/tracking-websocket-reconnect` | `develop` | `develop` |

| Production hotfix | `hotfix/` | `hotfix/1.2.1-jwt-expiry` | `main` | `main` + `develop` |
| Release | `release/` | `release/1.2.0` | `develop` | `main` + `develop` |
| Experimentation | `spike/` | `spike/pgvector-embeddings` | `develop` | ❌ Never submerged |
| Documentation | `docs/` | `docs/adr-003-kafka` | `develop` | `develop` |

### Branch Naming Rules

```
{type}/{optional-service}-{short-description-in-kebab-case}

# Valid Examples
feature/auth-refresh-token
feature/gateway-rate-limiting-by-role
fix/tracking-position-out-of-bounds
hotfix/1.2.1-jwt-null-claim
docs/adr-004-mtls-inter-services
spike/redis-cluster-sharding-benchmark

# Invalid Examples ❌
feature/AuthRefreshToken ← PascalCase not allowed
fix/bug ← too vague
my-branch ← no type
feature/XFLOW-42 ← ticket number only
```

> **Lifespan Rule**: A `feature/` or `fix/` branch must not remain open for more than **5 business days** without activity. After this time, it is considered abandoned and may be deleted.

### Branch Protection

#### `main`

- ✅ Merge only via Pull Request
- ✅ 1 approving reviewer required
- ✅ CI status checks required (build, tests, Trivy)
- ✅ Up-to-date branch with `main` required before merge
- ✅ Direct push prohibited (including for maintainers)
- ✅ Force push prohibited
- ✅ Deletion prohibited

#### `develop`

- ✅ Merge only via Pull Request
- ✅ 1 approving reviewer required
- ✅ CI status checks required
- ✅ Force push prohibited

---

## 3. Commit Conventions — Conventional Commits

XFlow follows the **[Conventional Commits v1.0](https://www.conventionalcommits.org)** specification.

### General Format

```
<type>(<scope>): <short description>

[optional body]

[optional footer — BREAKING CHANGE or refs]

```

### Allowed Types

| Type | Usage | Triggers a release |

|---|---|---|
| `feat` | New feature | ✅ Minor (`1.x.0`) |

| `fix` | Bug fix | ✅ Patch (`1.0.x`) |

| `perf` | Performance improvement without API change | ✅ Patch |

| `refactor` | Refactoring without behavior change | ❌ |

| `test` | Added or modified tests | ❌ |

| `docs` | Documentation only | ❌ |

| `chore` | Maintenance, dependencies, CI/CD | ❌ |

| `build` | Build system, Docker, Maven | ❌ |

| `ci` | CI/CD configuration (GitHub Actions) | ❌ |

| `style` | Formatting, spaces (no logic) | ❌ |

| `revert` | Undoing a previous commit | ✅ Patch |

### Recommended scopes (by service)

```
gateway → xflow-api-gateway
auth → xflow-auth-service
map → xflow-map-service
tracking → xflow-tracking-analytics-service
routing → xflow-routing-service
link-notif → xflow-link-notif-service
infra → Docker, Kubernetes, Terraform
ci → GitHub Actions
deps → Updating Maven dependencies
docs → General documentation
```

### Examples of valid commits

```bash
# Feature
feat(auth): add refresh token endpoint with Redis storage

#Fix
fix(gateway): handle null JWT claim in role propagation filter

# Performance
perf(tracking): add BRIN index on positions.recorded_at column

# Breaking changes
feat(auth)!: replace UUID v1 with UUID v4 for anonymous tokens

BREAKING CHANGE: existing anonymous tokens will be invalidated.
Clients must request a new token via POST /api/auth/anonymous.

# Chore
chore(deps): bump spring-boot from 3.4.0 to 3.4.1

# CI
ci: add Trivy CVE scan step to gateway workflow

#Docs
docs(adr): add ADR-004 — mTLS inter-service communication

#Revert
revert: feat(routing): dynamic weight recalculation on ZONE_SATURATED

Refs: #142
```

### Invalid examples ❌

```bash
fix: bug ← description too vague
Fix auth service ← no type, PascalCase
feat(AUTH): add login ← scope in uppercase
added refresh token ← no type at all
feat(auth): Add Refresh Token. ← uppercase + period
WIP: working on jwt filter ← WIP not allowed in final commit
```

### Style Guidelines

- The **short description** is in English, lowercase, without a period, max 72 characters
- The **body** is optional — use it to explain the *why*, not the *what*
- The **footer** `BREAKING CHANGE:` is mandatory if the `!` is used
- WIP commits (`WIP:`, `tmp:`, `savepoint:`) are tolerated during development **but must be squashed before the PR**

---

## 4. Complete Workflow — from feature to merge

### Step by Step

```bash
# 1. Switch to develop and fetch the latest changes
git checkout develop
git pull origin develop

# 2. Create the feature branch from develop
git checkout -b feature/auth-refresh-token

#3. Develop — commit regularly with Conventional Commits
git add .
git commit -m "feat(auth): add RefreshToken entity and repository"

git add .
git commit -m "feat(auth): implement refresh token generation in AuthService"

git add .
`git commit -m "test(auth): add integration tests for refresh token endpoint"`

# 4. Fetch updates from `develop` before the PR (avoid conflicts)
`git fetch origin`
`git rebase origin/develop`
# → Resolve any conflicts, then: `git rebase --continue`

# 5. Push the branch
`git push origin feature/auth-refresh-token`

# 6. Open the Pull Request on GitHub to `develop`
# → Use the PR template (see §5)
```

### Rebase before PR rule

> Before opening a PR, **always rebase your branch on `develop`** to ensure it is up-to-date and avoid conflicts during the merge.

> ```bash
> git fetch origin
> git rebase origin/develop
> ```
>
> Never use `git merge develop` in a feature branch — this creates unwanted merge commits in the history.

### Post-Merge Cleanup

```bash
# Delete the local branch after merging the PR
`git checkout develop`
`git pull origin develop`
`git branch -d feature/auth-refresh-token`

# Delete the remote branch (if not done automatically by GitHub)
`git push origin --delete feature/auth-refresh-token`
```

> **Configure GitHub** to automatically delete branches after merging:
> Settings → General → Pull Requests → ✅ Automatically delete head branches

---

## 5. Pull Requests

### General Rules

- All code changes must be submitted via a Pull Request — **no direct pushes to `develop` or `main`**
- A PR must be **focused**: one feature or fix at a time
- A PR must not exceed **400 lines of changes** (excluding generated files and SQL migrations)
- If a If a PR is larger, break it down into smaller PRs with explicit dependencies.

- A PR **doesn't necessarily have to be tied to an issue** but should be self-describing.

### PR Template

When you open a PR, the following template is automatically loaded from `.github/PULL_REQUEST_TEMPLATE.md`:

```markdown
## Description

<!-- Describe in 2-3 sentences what this PR does and why. -->

## Change Type

- [ ] `feat` — New feature
- [ ] `fix` — Bug fix
- [ ] `perf` — Performance improvement
- [ ] `refactor` — Refactoring with no behavior change
- [ ] `docs` — Documentation only
- [ ] `chore` — Maintenance / dependencies / CI

## Affected Service(s)

- [ ] xflow-api-gateway
- [ ] xflow-auth-service
- [ ] xflow-map-service
- [ ] xflow-tracking-analytics-service
- [ ] xflow-routing-service
- [ ] xflow-link-notif-service
- [ ] Infrastructure (Docker / Kubernetes / Terraform)

## Checklist

- [ ] Code compiles without errors (`./mvnw clean package`)
- [ ] Tests pass locally (`make test`)
- [ ] The new code paths are covered by tests
- [ ] Test coverage has not regressed (threshold: 70%)
- [ ] The code adheres to the project's Java naming conventions
- [ ] No sensitive data (key, password) is committed
- [ ] The Flyway migration is versioned correctly (if applicable)
- [ ] The added logs do not expose personal data (GDPR)
- [ ] The CHANGELOG.md is updated (if a feature or fix is ​​user-visible)
- [ ] The documentation is updated (if there is an API or architecture change)

## How to test

<!-- Describe the steps to manually verify this PR. -->
<!-- Ex: make dev → curl -X POST http://localhost:8080/api/auth/refresh ... -->

## Breaking changes

<!-- Describe any changes incompatible with previous versions. -->
<!-- Indicate NONE if none. -->

## References

<!-- Issues, ADR, associated tickets (optional). -->
<!-- Ex: Closes #42 · ADR-003 · GW-RL-02 -->
```

### Recommended PR Labels

| Label | Usage |

|---|---|
| `service: gateway` | Impact on the Gateway API |

| `service: auth` | Impact on the Auth Service |

| `breaking change` | Incompatibility with previous versions |

| `needs review` | PR ready for review |

| `work in progress` | Do not merge — in progress |
| `hotfix` | Urgent fix to main |

| `dependencies` | Dependency update |

---

## 6. Code Review

### For the reviewer

**Deadline**: Respond to a review request within **48 business hours**.

**What we evaluate (in order of priority)**

1. **Fixability** — Does the code do what it claims to do? Are edge cases handled?

2. **Security** — No sensitive data exposed, no obvious OWASP vulnerabilities, GDPR compliant
3. **Testing** — Is the coverage sufficient? Do the tests test the behavior, not the implementation?

4. **Architecture** — Does the code adhere to the layered architecture (API → application → domain → infrastructure)?

5. **Readability** — Is the code understandable without comments? Are the names descriptive?

6. **Performance** — Are there any N+1 queries or unnecessary loops on large volumes of code?

**Comment Conventions**

Use the following prefixes to clarify the purpose of the comment:

```
nit: → Minor, non-blocking detail (style, cosmetic naming)
question: → Request for clarification, not a change request
suggest: → Suggestion for improvement, non-blocking
blocking: → Must be fixed before merging
praise: → Positive feedback — encouraging good practices
```

**Examples**

```
nit: prefer `var` here for brevity

question: why is the TTL set to 5s and not 10s? Is this documented somewhere?

suggest: this logic could be extracted to a private method for readability

blocking: this query will trigger N+1 on the zones collection — use a JOIN instead

praise: great use of the domain method here, keeps the service layer clean
```

**Never do this in a review**
- Criticizing the person rather than the code
- Blocking a PR based on undocumented personal stylistic preferences
- Merging your own PR without external review (except for documented urgent hotfixes)
- Leaving a PR unanswered for more than 48 business hours

### For the PR author

- Respond to every comment — even to say "done" or "I prefer to keep X because…"
- Do not request a review again before addressing all the `blocking:` comments
- Use GitHub's direct suggestions (Apply suggestion) when offered
- Squash fixup commits before the final merge if requested

### Merge Strategy

| Situation | Strategy | GitHub Command |

|---|---|---|

| Standard Feature/Fix | **Squash and merge** — 1 clean commit in develop | "Squash and merge" |

| Release → main | **Merge commit** — preserve the release history | "Create a merge commit" |

| Hotfix → main + develop | **Merge commit** on both sides | "Create a merge commit" |

> **Why Squash for features?**
> Intermediate commits for a feature (WIP, typo fix, rebase) should not pollute the `develop` history. Squash produces a single, clean, traceable, and readable Conventional Commits commit in `git log`.

---

## 7. Java Code Conventions

### Naming

```java
// Packages: all lowercase, separated by periods
package io.xflow.auth.application;

// Classes: PascalCase
public class AuthService { }
public record LoginRequest(String email, String password) { }

// Methods and variables: camelCase
public AuthResponse login(LoginRequest request, String clientIp) { }
private final UserRepository userRepository;

// Constants: SCREAMING_SNAKE_CASE
private static final int MAX_FAILED_ATTEMPTS = 3;

// Enumerations: PascalCase (type) + SCREAMING_SNAKE_CASE (values)
public enum Role { ADMIN, OPERATOR, PARTICIPANT }
```

### Package Structure (per service)

```
io.xflow.{service}/
├── api/ ← Controllers + DTOs (@RestController, records)
│ └── dto/
├── application/ ← Services + use cases (@Service, @Transactional)
├── domain/ ← Entities + pure business logic (no Spring here)
└── infrastructure/ ← Repositories + Config + ExceptionHandler
```

### Style Rules

- **Lombok**: use `@Getter`, `@Builder`, `@RequiredArgsConstructor` — avoid `@Data` (generates an uncontrolled `equals` on JPA entities)
- **Java Records**: prefer `record` for immutable DTOs (requests/responses)
- **`@Transactional`**: always on service methods that write to the database, never in controllers
- **`open-in-view: false`**: configured in all services — do not access the database outside the transactional context
- **Dependency Injection**: always via constructor (`@RequiredArgsConstructor`) — never `@Autowired` on a field
- **Logs**: use `@Slf4j` from Lombok — logging in English — never log personal data (email, participant UUID)
- **Business Exceptions**: create specific exceptions in `application/` (e.g., `AuthException`) — do not allow generic `RuntimeException`s to be thrown by controllers

### Quality Checklist Before Commit

```
✅ No undocumented TODOs in the merged code
✅ No System.out.println() or e.printStackTrace()
✅ No hardcoded credentials or keys
✅ REST endpoints follow the convention: /api/{service}/{resource}
✅ Each public endpoint is documented with @Operation (Springdoc)
✅ Flyway migrations are named: V{N}__{description_en_snake_case}.sql
✅ Flyway migrations are idempotent
```

---

## 8. Versioning & Releases — SemVer

XFlow uses **Semantic Versioning 2.0** (`MAJOR.MINOR.PATCH`).

### Version Bump Rules

| Change | Version | Trigger |

|---|---|---|

| Breaking change (API incompatible) | `MAJOR` (`2.0.0`) | `feat!:` or `BREAKING CHANGE:` in the footer |

| New backward compatible feature | `MINOR` (`1.3.0`) | `feat:` |

| Backward compatible bug fix | `PATCH` (`1.2.1`) | `fix:`, `perf:`, `revert:` |

### Release Cycle

```bash
# 1. Create the release branch from develop
git checkout develop
git pull origin develop
git checkout -b release/1.2.0

# 2. Update the versions in pom.xml
./mvnw versions:set -DnewVersion=1.2.0
git commit -m "chore(release): bump version to 1.2.0"

# 3. Update CHANGELOG.md
# → Describe all the features and fixes of the release

# 4. Merge release → main
git checkout main
git merge --no-ff release/1.2.0
git tag -a v1.2.0 -m "Release v1.2.0"
git push origin main --tags

# 5. Merge release → develop (to integrate any bug fixes from the release)
git checkout develop
git merge --no-ff release/1.2.0
git push origin develop

#6. Delete the release branch
git branch -d release/1.2.0
git push origin --delete release/1.2.0
```

### Hotfix in production

```bash
#1. Create the hotfix from main
git checkout main
git pull origin main
git checkout -b hotfix/1.2.

# 2. Corriger + commiter
git commit -m "fix(gateway): handle null role claim in JWT filter"

# 3. Merger → main avec tag
git checkout main
git merge --no-ff hotfix/1.2.1-jwt-null-claim
git tag -a v1.2.1 -m "Hotfix v1.2.1 — null JWT role claim"
git push origin main --tags

# 4. Merger → develop pour ne pas perdre le fix
git checkout develop
git merge --no-ff hotfix/1.2.1-jwt-null-claim
git push origin develop

# 5. Supprimer la branche hotfix
git branch -d hotfix/1.2.1-jwt-null-claim
git push origin --delete hotfix/1.2.1-jwt-null-claim
```

### Format du CHANGELOG.md

```markdown
# Changelog

## [1.2.0] — 2026-05-01

### Added
- feat(auth): refresh token endpoint with Redis storage (#38)
- feat(gateway): rate limiting differentiated by user role (#41)

### Fixed
- fix(tracking): WebSocket reconnection backoff not resetting (#44)

### Performance
- perf(tracking): BRIN index on positions.recorded_at reduces Replay query time by 60%

## [1.1.0] — 2026-03-15
...
```

---

## 9. Signalement de bugs

### Bugs non critiques

Ouvrir une **GitHub Issue** avec le template `Bug Report` :

```markdown
**Description**
<!-- Une phrase claire décrivant le bug. -->

**Comportement attendu**
<!-- Ce qui devrait se passer. -->

**Comportement observé**
<!-- Ce qui se passe réellement. -->

**Étapes pour reproduire**
1. Démarrer la stack avec `make dev`
2. Envoyer la requête suivante : `curl ...`
3. Observer l'erreur

**Environnement**
- OS : macOS 14 / Ubuntu 22.04 / Windows WSL2
- Java : 21.x
- Docker : 24.x
- Branche : develop / commit SHA

**Logs pertinents**
```
[coller les logs ici]
```

**Workaround connu**
<!-- Si tu as trouvé un contournement temporaire. -->
```

### Priorité des bugs

| Label | Critère |
|---|---|
| `bug: critical` | Service en production indisponible ou perte de données |
| `bug: high` | Fonctionnalité majeure cassée, pas de workaround |
| `bug: medium` | Fonctionnalité dégradée, workaround disponible |
| `bug: low` | Comportement cosmétique ou edge case rare |

---

## 10. Politique de sécurité

> ⚠️ **Ne jamais ouvrir une issue publique GitHub pour signaler une vulnérabilité de sécurité.**

### Procédure de divulgation responsable

1. Envoyer un email à **security@xflow.io** avec :
- Description de la vulnérabilité
- Étapes pour la reproduire
- Impact estimé (CVSS si possible)
- Proof of concept (si disponible)

2. L'équipe accuse réception dans les **48 heures**

3. Un correctif est développé en branche privée et déployé avant toute divulgation publique

4. La vulnérabilité est documentée dans le `CHANGELOG.md` après correction, avec crédit au rapporteur si souhaité

### Ce qui est dans le scope de la politique

- Authentification et gestion des tokens JWT
- Injection SQL / NoSQL
- Exposition de données personnelles (RGPD)
- Élévation de privilèges (RBAC)
- Déni de service sur les endpoints publics

### Bonnes pratiques pour les contributeurs

- Ne jamais commiter de clés, tokens, mots de passe ou fichiers `.env` dans le dépôt
- Le fichier `.gitignore` exclut `.env`, `.keys/`, `*.pem` — vérifier avant chaque `git add`
- En cas de commit accidentel d'un secret : **contacter immédiatement l'équipe** — ne pas tenter de supprimer le commit seul (l'historique Git reste visible même après un force push)

---

## Questions ?

- **Slack / Discord** : canal `#xflow-dev`
- **Discussions GitHub** : [github.com/xflow-platform/xflow-platform/discussions](https://github.com/xflow-platform/xflow-platform/discussions)
- **Email** : contribute@xflow.io

---

*Ce document est maintenu par l'équipe core XFlow. Toute suggestion d'amélioration est la bienvenue via une PR sur ce fichier.*