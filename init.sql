-- Initialisation de la base de données PixelBook
-- Extensions PostgreSQL utiles
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Création des schémas si nécessaire
-- CREATE SCHEMA IF NOT EXISTS pixelbook_schema;

-- Vous pouvez ajouter ici des tables ou données initiales si nécessaire
-- Les tables seront créées automatiquement par Hibernate avec ddl-auto=update 