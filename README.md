# 🍽️ My Popote

**Application web full-stack de planification des repas, gestion des recettes et génération de listes de courses.**

My Popote est un projet personnel né d'un besoin concret : disposer d'une alternative aux services de box repas pour organiser ses menus, maîtriser son budget alimentaire et conserver le contrôle de ses données.

L'application est **fonctionnelle, conteneurisée et déployée sur un VPS personnel**. Elle couvre l'ensemble du cycle de vie d'un produit logiciel : conception fonctionnelle, développement, tests, sécurité, déploiement et exploitation.

---

## 🎯 Fonctionnalités principales

- Création et gestion des recettes et ingrédients
- Gestion des portions, tags, saisons et coûts estimés
- Génération de plannings de repas hebdomadaires
- Prise en compte des préférences et contraintes budgétaires
- Remplacement individuel d'un repas
- Création de recettes directement depuis le planning
- Génération et regroupement automatique des ingrédients dans la liste de courses
- Ajout manuel de produits et gestion des articles disponibles
- Suivi du budget hebdomadaire
- Historique des semaines
- Authentification et gestion des comptes
- Interface responsive adaptée aux smartphones

---

## 🏗️ Architecture

L'application repose sur une architecture full-stack à trois niveaux :

```text
Utilisateur
    |
    v
Angular 19 / TypeScript
    |
    | REST / JSON
    v
Spring Boot / Java 21
    |
    | Spring Data JPA
    v
MariaDB
```

Le backend est organisé en couches :

```text
Controller
    |
    v
Service / Business Logic
    |
    v
Repository
    |
    v
JPA / MariaDB
```

Cette organisation favorise la séparation des responsabilités, la testabilité et la maintenabilité.

Le **Repository Pattern** permet notamment d'isoler la persistance de la logique métier.

---

## 🛠️ Stack technique

| Domaine | Technologies |
|---|---|
| Frontend | Angular 19, TypeScript, RxJS |
| Backend | Java 21, Spring Boot, Spring Web |
| Données | MariaDB, JPA / Hibernate |
| Sécurité | Spring Security, JWT RS256 |
| Migrations | Flyway |
| Tests backend | JUnit, Spring Boot Test, Testcontainers |
| Tests frontend | Jasmine, Karma |
| Tests E2E | Playwright |
| Infrastructure | Docker, Docker Compose, VPS Linux |
| Exploitation | Caddy, HTTPS, Actuator, health checks |
| Versionnement | Git, GitHub |

---

## 🧪 Qualité logicielle et stratégie de tests

Le développement s'appuie sur une démarche inspirée du **Test-Driven Development (TDD)** :

```text
Besoin fonctionnel
       |
       v
Cas de test
       |
       v
Implémentation
       |
       v
Exécution et validation
       |
       v
Refactoring
```

Les contrôles couvrent plusieurs niveaux :

- Tests unitaires de la logique métier
- Tests d'intégration des services et endpoints REST
- Validation des données
- Tests d'authentification et d'autorisation
- Tests de persistance
- Tests d'intégration MariaDB avec Testcontainers
- Tests end-to-end avec Playwright

**Objectif :** prévenir les régressions, vérifier les comportements attendus et fiabiliser les évolutions fonctionnelles.

---

## 🔐 Sécurité et authentification

La sécurité est intégrée aux choix d'architecture :

- Authentification avec Spring Security et JWT
- Signature asymétrique **RS256**
- Contrôle d'accès aux ressources utilisateur
- Validation des entrées
- Configuration CORS contrôlée
- Secrets et identifiants externalisés
- Configuration par variables d'environnement
- Migrations de schéma avec Flyway
- Récupération de compte par e-mail
- Exposition publique via HTTPS et reverse proxy

Les secrets applicatifs et identifiants d'infrastructure ne sont pas versionnés dans le dépôt.

### Intégration avec ProductPulse

My Popote constitue le point d'entrée d'un écosystème applicatif comprenant **ProductPulse**, une application complémentaire dédiée à la collecte et à la gestion des retours utilisateurs.

L'authentification entre les deux applications repose sur des **JWT signés en RS256** :

- My Popote émet les jetons d'authentification.
- ProductPulse vérifie leur signature à l'aide d'une clé publique.
- Les applications utilisent une identité utilisateur commune, sans dupliquer les comptes.
- La clé privée de signature reste détenue par le service émetteur.

Cette architecture illustre une approche de sécurité adaptée à la communication entre applications.

---

## 🐳 Docker et déploiement

My Popote est déployé sur un **VPS Linux personnel** avec Docker Compose.

```text
Internet
    |
  HTTPS
    |
    v
Caddy (reverse proxy)
    |
    +----> Frontend Angular
    |
    +----> API Spring Boot
                 |
                 v
              MariaDB
                 |
                 v
          Volume persistant
```

L'environnement comprend :

- Des conteneurs séparés pour le frontend, l'API et la base de données
- Une configuration externalisée
- Des volumes persistants
- Des health checks
- Une exposition publique sécurisée via HTTPS
- Des sauvegardes de base de données
- Des outils de supervision et de diagnostic

L'infrastructure est administrée personnellement, du déploiement initial à la maintenance.

---

## 🔄 Méthode de développement

Le projet évolue de manière incrémentale :

```text
Besoin ou anomalie
       |
       v
Analyse et cadrage
       |
       v
Branche dédiée
       |
       v
Développement et tests
       |
       v
Revue et validation
       |
       v
Intégration dans main
       |
       v
Déploiement
```

Git et GitHub permettent de conserver des changements isolés, testables et traçables.

---

## 🤖 Utilisation de l'intelligence artificielle

L'intelligence artificielle est utilisée comme **assistant technique**, et non comme agent autonome chargé de développer le projet de bout en bout.

Elle intervient notamment pour :

- Explorer et comparer des solutions techniques
- Analyser des erreurs
- Proposer des implémentations
- Identifier des cas limites
- Participer aux revues de code et aux refactorings

Les décisions d'architecture, de conception fonctionnelle, de sécurité, de stratégie de tests et de déploiement restent sous contrôle humain.

Les propositions de l'IA sont analysées, testées et validées avant intégration.

> **AI-assisted, developer-driven.**

---

## 📈 Démarche produit

My Popote n'est pas uniquement un exercice technique : c'est une application conçue pour répondre à un usage réel.

Son évolution suit un cycle d'amélioration continue :

1. Identifier un besoin utilisateur
2. Traduire le besoin en fonctionnalités
3. Concevoir et développer la solution
4. Vérifier les comportements fonctionnels et techniques
5. Déployer en conditions réelles
6. Recueillir les retours et améliorer le produit

Cette démarche associe **conception produit, développement et qualité logicielle**, avec une attention particulière à l'expérience utilisateur.

---

## 🚀 État du projet

**Application fonctionnelle et déployée.**

Le projet est utilisé dans des conditions réelles, ce qui permet d'identifier et de traiter des améliorations fonctionnelles, UX et techniques.

### Axes d'amélioration

- Optimisation de l'algorithme de génération des repas
- Renforcement de la couverture de tests
- Automatisation progressive des contrôles qualité et du déploiement
- Amélioration continue de l'UX mobile et de l'observabilité
- Exploitation des retours utilisateurs via ProductPulse

---

## 👤 Auteur

**Jérémy Sabadie**

**Product Builder | Conception produit & qualité logicielle**

Développement Java / Angular · Analyse fonctionnelle · QA

Projet personnel illustrant une démarche complète : **du besoin utilisateur jusqu'à l'exploitation d'une application web en production**.