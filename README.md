# PixelBook - Application Spring Boot

## Déploiement avec GitLab CI/CD et Docker

### Configuration requise

1. **GitLab CI/CD Variables** (à configurer dans Settings > CI/CD > Variables) :
   - `VPS_HOST` : L'adresse IP ou le nom de domaine de votre VPS
   - `VPS_USERNAME` : Le nom d'utilisateur pour se connecter au VPS
   - `SSH_PRIVATE_KEY` : La clé SSH privée pour l'authentification
   - `SSH_KNOWN_HOSTS` : Le contenu du fichier known_hosts pour le VPS

2. **Sur le VPS** :
   - Docker et Docker Compose installés
   - Accès SSH configuré

### Architecture

L'application utilise :
- **Réseau Docker** : `pixel-net`
- **Base de données** : PostgreSQL 15
- **Application** : Spring Boot avec Java 17

### Pipeline GitLab CI/CD

Le pipeline comprend 3 étapes :

1. **Test** : Exécution des tests unitaires et d'intégration
2. **Build** : Construction de l'image Docker et push vers le registry GitLab
3. **Deploy** : Déploiement automatique sur le VPS

### Déploiement local

Pour tester localement :

```bash
# Cloner le projet
git clone <repository-url>
cd pixelbook

# Construire l'image Docker
docker build -t pixelbook:latest .

# Démarrer les services
docker-compose up -d

# Vérifier les logs
docker-compose logs -f
```

### Accès à l'application

- **Application** : http://localhost:8080
- **Base de données** : localhost:5432
- **Swagger UI** : http://localhost:8080/swagger-ui.html

### Variables d'environnement

Les variables d'environnement sont configurées dans le `docker-compose.yml` et peuvent être modifiées selon vos besoins.

### Base de données

La base de données PostgreSQL est automatiquement initialisée avec :
- Base de données : `pixelbook`
- Utilisateur : `Excalibruh`
- Mot de passe : `Saber1234`

Les tables sont créées automatiquement par Hibernate avec `ddl-auto=update`. 