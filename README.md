# TP 11 — Authentification JDBC / JPA avec Spring Security

Portail web sécurisé construit avec **Spring Boot 3.5**, **Spring Security 6**, **Spring Data JPA** et **Thymeleaf**.
Les comptes ne sont pas déclarés en mémoire : ils sont lus dans **MySQL**, les mots de passe sont stockés sous forme
d'empreintes **BCrypt** et les autorités proviennent d'une vraie relation plusieurs-à-plusieurs entre comptes et rôles.

---

## Ce que fait l'application

| Fonction | Détail |
|---|---|
| Authentification | Formulaire personnalisé sur `/connexion`, identifiant vérifié en base via `UserDetailsService` |
| Stockage des mots de passe | Hachage BCrypt à l'inscription, comparaison par `DaoAuthenticationProvider` |
| Autorisation | `/espace/**` réservé aux rôles `MEMBRE` et `ADMIN`, `/administration/**` réservé à `ADMIN` |
| Traçabilité | La date de dernière connexion est enregistrée à chaque login réussi |
| Session | Déconnexion en `POST` avec invalidation de session et suppression du cookie `JSESSIONID` |
| Erreurs | Page d'échec de connexion dédiée et page 403 personnalisée |

## Données de démonstration

Les rôles et les comptes sont créés automatiquement au premier démarrage par un `CommandLineRunner`.

| Identifiant | Mot de passe | Rôles | Accès |
|---|---|---|---|
| `a.benidar` | `Portail@2026` | `ROLE_ADMIN`, `ROLE_MEMBRE` | Espace membre **et** administration |
| `s.elidrissi` | `Membre@2026` | `ROLE_MEMBRE` | Espace membre uniquement |
| `y.ouazzani` | `Membre@2026` | `ROLE_MEMBRE`, `ROLE_OBSERVATEUR` | Espace membre |
| `h.alaoui` | `Lecteur@2026` | `ROLE_OBSERVATEUR` | Aucun espace protégé → page 403 |

## Prérequis

- JDK 17 ou supérieur
- Maven 3.9+
- MySQL / MariaDB accessible sur `localhost:3306` avec l'utilisateur `root`
- La base `portail_usmba_db` est créée automatiquement (`createDatabaseIfNotExist=true`)

## Lancer le projet

```bash
mvn spring-boot:run
```

L'application écoute sur **http://localhost:8098** et redirige vers la page de connexion.

---

## Résultats obtenus

### 1. Page de connexion personnalisée

![Page de connexion](docs/screenshots/01-page-connexion.png)

Le formulaire remplace l'écran par défaut de Spring Security : il pointe sur `/connexion` en `POST` et utilise les
paramètres `identifiant` et `motDePasse` déclarés dans `SecuriteConfig`. Le jeton CSRF est injecté automatiquement par
Thymeleaf, ce qui permet de garder la protection contre les requêtes forgées.

### 2. Refus d'un mot de passe incorrect

![Échec d'authentification](docs/screenshots/02-echec-authentification.png)

Un couple identifiant / mot de passe invalide renvoie sur `/connexion?erreur=true`. Le contrôleur positionne alors le
drapeau `erreurIdentifiants` et le message d'erreur s'affiche au-dessus du formulaire, sans révéler si c'est
l'identifiant ou le mot de passe qui est faux.

### 3. Tableau de bord d'administration

![Tableau de bord administrateur](docs/screenshots/03-tableau-de-bord-admin.png)

Connecté avec `a.benidar`, l'utilisateur est redirigé par le `AuthenticationSuccessHandler` vers
`/administration/tableau-de-bord`. La page liste les quatre comptes avec leurs rôles issus de la table `roles` et leur
date de dernière connexion. Le lien « Administration » n'apparaît dans la barre de navigation que pour un administrateur,
grâce à l'expression `${#authorization.expression('hasRole(''ADMIN'')')}` de Thymeleaf.

### 4. Espace membre

![Espace membre](docs/screenshots/04-espace-membre.png)

La même session accède aussi à `/espace/accueil`, protégé par `hasAnyRole("MEMBRE","ADMIN")`. L'objet `ComptePrincipal`
expose le nom complet, l'e-mail et la liste des autorités réellement chargées depuis la jointure `comptes_roles`.

### 5. Profil détaillé du compte

![Profil du compte](docs/screenshots/05-profil.png)

La page `/espace/profil` affiche l'identifiant technique, l'état du compte et la date de dernière connexion. Cette
valeur a été mise à jour au moment du login : c'est la preuve que le handler de succès écrit bien en base après
l'authentification.

### 6. Déconnexion effective

![Confirmation de déconnexion](docs/screenshots/06-deconnexion.png)

La déconnexion se fait en `POST /deconnexion`, invalide la session HTTP et supprime le cookie `JSESSIONID`. L'utilisateur
revient sur la page de connexion avec un message de confirmation.

### 7. Contrôle d'accès refusé

![Accès refusé](docs/screenshots/07-acces-refuse.png)

Connecté avec `s.elidrissi` (rôle `ROLE_MEMBRE` seulement), une tentative d'accès à `/administration/tableau-de-bord`
est interceptée par Spring Security. Le serveur répond **403** et redirige vers la page personnalisée
`/acces-refuse`, qui rappelle les rôles du compte connecté.

---

## Choix d'implémentation

- **`ComptePrincipal`** implémente `UserDetails` et adapte l'entité `Compte` au contrat attendu par Spring Security.
  Cette classe évite d'exposer l'entité JPA directement dans la couche de sécurité.
- **`CompteUserDetailsService`** implémente `UserDetailsService` et charge le compte par identifiant avec un
  `left join fetch` sur les rôles pour éviter le problème *N+1* et la `LazyInitializationException`.
- **`DaoAuthenticationProvider`** est déclaré explicitement avec l'encodeur BCrypt et
  `setHideUserNotFoundExceptions(false)`, ce qui permet de distinguer un identifiant inconnu dans les journaux.
- **`EncodageConfig`** isole le bean `PasswordEncoder` dans une configuration séparée. Sans cela, `SecuriteConfig`
  dépendait du handler de succès, qui dépendait du service, qui dépendait de l'encodeur déclaré dans `SecuriteConfig` :
  une dépendance circulaire empêchait le démarrage.
- **`SuccesConnexionHandler`** étend `SimpleUrlAuthenticationSuccessHandler` pour enregistrer la date de connexion puis
  rediriger selon le rôle (`ADMIN` vers l'administration, sinon vers l'espace membre).
- **Double barrière d'autorisation** : les règles `hasRole` / `hasAnyRole` de la chaîne de filtres protègent les URL,
  et l'expression `#authorization` masque les liens inutiles dans les vues. La sécurité reste côté serveur.
- **Aucune donnée sensible en clair** : les mots de passe des comptes de démonstration sont hachés au démarrage, jamais
  stockés en clair, et la colonne `mot_de_passe` est limitée à 100 caractères pour accueillir un hash BCrypt.

## Personnalisation par rapport à l'énoncé

| Élément | Énoncé | Ce projet |
|---|---|---|
| Package | `ma.fstg.security` | `ma.usmba.portail` |
| Artefact | `spring-security-jpa` | `portail-securise` |
| Base de données | `security_db` | `portail_usmba_db` |
| Port | `8080` | `8098` |
| Entité utilisateur | `User` | `Compte` |
| Table des utilisateurs | `user` | `comptes` |
| Table de jointure | `user_roles` | `comptes_roles` |
| Dépôts | `UserRepository`, `RoleRepository` | `CompteDepot`, `RoleDepot` |
| Service | `CustomUserDetailsService` | `CompteUserDetailsService` + `ComptePrincipal` |
| Configuration | `SecurityConfig` | `SecuriteConfig` + `EncodageConfig` |
| Initialisation | `DatabaseInitializer` | `InitialisationDonnees` |
| Rôles | `ROLE_ADMIN`, `ROLE_USER` | `ROLE_ADMIN`, `ROLE_MEMBRE`, `ROLE_OBSERVATEUR` |
| Comptes | `admin/1234`, `user/1111` | 4 comptes nominatifs |
| Pages | `/login`, `/home` | `/connexion`, `/espace/accueil` |
| Zone admin | `/admin/**` | `/administration/**` |
| Zone membre | `/user/**` | `/espace/**` |

## Structure du projet

```
src/main/java/ma/usmba/portail/
├── PortailSecuriseApplication.java
├── config/
│   ├── EncodageConfig.java            # bean BCryptPasswordEncoder
│   ├── InitialisationDonnees.java     # rôles + comptes de démonstration
│   └── SecuriteConfig.java            # chaîne de filtres, formLogin, logout
├── controleur/
│   └── PortailControleur.java         # routes MVC
├── depot/
│   ├── CompteDepot.java
│   └── RoleDepot.java
├── entite/
│   ├── Compte.java
│   └── Role.java
├── securite/
│   ├── ComptePrincipal.java           # adaptateur UserDetails
│   ├── CompteUserDetailsService.java  # chargement depuis MySQL
│   └── SuccesConnexionHandler.java    # traçabilité + redirection par rôle
└── service/
    └── CompteService.java

src/main/resources/
├── application.properties
├── static/css/portail.css
└── templates/
    ├── acces-refuse.html
    ├── administration-tableau-de-bord.html
    ├── connexion.html
    ├── espace-accueil.html
    └── profil.html
```
