# xflow-services
XFlow meets this need by digitizing the field and exposing a real-time demographic monitoring layer, accessible to administrators and operational teams

## Documentation

La documentation de la plateforme XFlow est gérée via **Spotify Backstage** et **MkDocs**.

- **Portail Développeur (Backstage)** : accessible sur `http://localhost:7007` (après `make dev`).
- **Documentation Technique (MkDocs)** : accessible sur `http://localhost:5000` (après `make dev`).

### Configuration Backstage

L'image Backstage (`docs/backstage/Dockerfile`) est **neutre** : aucune config n'y est embarquée. La configuration est fournie au runtime par couches :

| Fichier | Rôle |
|---|---|
| `docs/backstage/app-config.yaml` | Base, pilotée par variables d'environnement (`BACKSTAGE_BASE_URL`, `POSTGRES_*`, `GITHUB_TOKEN`) |
| `docs/backstage/app-config.dev.yaml` | Overlay dev : auth `guest`, CSP permissif, catalogue local (`/catalog`) — monté par `docker-compose.dev.yml` |
| `docs/backstage/app-config.production.yaml` | Overlay prod : URL publique, OAuth GitHub, catalogue via URLs GitHub |

En dev, la base + l'overlay dev sont montés par `docker-compose.dev.yml`. En prod, on monte la base + l'overlay production et on renseigne les variables d'environnement — la config dev ne quitte jamais le repo.

Pour savoir comment documenter un nouveau service, consultez la section dédiée dans le [Guide de Contribution](CONTRIBUTING.md#5-documentation-strategy--backstage--techdocs).
