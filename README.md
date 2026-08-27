# My Places — journal intime géographique

Application Android native permettant de cartographier ses souvenirs. Chaque lieu est
enregistré avec un titre, une description, un émoji de ressenti, une photo, et l'adresse
postale réelle résolue automatiquement depuis les coordonnées GPS.

**Auteurs :** Adrian, Léo, Théophile

## Fonctionnalités

- Carte plein écran centrée sur la position de l'utilisateur
- Ajout d'un lieu par appui long sur la carte
- Photo par appareil photo (CameraX) ou depuis la galerie
- Marqueurs affichant dynamiquement l'émoji choisi, fiche détaillée en bottom sheet
- Résolution automatique de l'adresse postale (géocodage inverse)
- Filtrage par émoji et vue liste récapitulative
- Export / import du journal au format JSON, avec fusion sans écrasement
- Verrouillage biométrique optionnel

## Stack technique

| Composant | Technologie | Pourquoi |
|---|---|---|
| Langage | Kotlin | Imposé, et standard Android |
| Interface | Jetpack Compose + Material 3 | Imposé ; UI déclarative |
| Navigation | Navigation Compose | Navigation entre les 4 écrans |
| Persistance | Room 2.8 (SQLite) | Imposé ; SQL vérifié à la compilation, observation via `Flow` |
| Préférences | DataStore Preferences | API `suspend` + `Flow`, gestion de la corruption |
| Réseau | Retrofit 3 + OkHttp + Gson | Imposé ; appel au service de géocodage |
| Photo | CameraX | Imposé ; capture intégrée à l'app |
| Affichage d'images | Coil | Chargement asynchrone depuis un chemin de fichier |
| Cartographie | osmdroid | Voir « Choix de la cartographie » |
| Sécurité | AndroidX Biometric | `BiometricPrompt` pour le verrouillage |
| Permissions | Accompanist Permissions | Gestion des permissions en Compose |
| Injection | Service locator manuel (`AppContainer`) | Suffisant à cette échelle, évite Hilt |

### Choix de la cartographie

Le sujet imposait le SDK Google Maps ; nous avons retenu **osmdroid** (OpenStreetMap) pour
deux raisons pratiques : aucune clé API à provisionner — le projet se clone et se lance sans
configuration préalable — et aucun compte de facturation Google Cloud.

Ce changement, effectué en cours de projet, n'a touché que `ui/map/MapScreen.kt` et le
manifeste : **aucun fichier de la couche `data/`**. C'est la meilleure vérification que nous
ayons du découpage en couches décrit ci-dessous.

## Architecture

Découpage en couches, dépendances dirigées vers le domaine :

```
ui/            Compose : carte, formulaire d'ajout, liste, réglages
  ▲            ViewModels injectés via AppViewModelProvider
data/
  repository/  PlaceRepository (contrat) + PlaceRepositoryImpl (orchestration)
  local/       Room : PlaceEntity, PlaceDao, MyPlacesDatabase, Mappers
  remote/      Retrofit : GeocodingApi, GeocodingDataSource, NetworkModule
  json/        DTO du fichier d'échange
  settings/    SettingsRepository (DataStore)
  ▲
domain/        Place — modèle métier, aucune dépendance Android
util/          PhotoStorage, BitmapUtils
di/            AppContainer — câblage des dépendances
```

L'interface ne connaît que `PlaceRepository`, une interface : elle ignore l'existence de Room
et de Retrofit. `PlaceRepositoryImpl` est le seul point qui connaît à la fois la base, le
réseau et le disque.

### Géocodage inverse en arrière-plan

À la validation du formulaire, le lieu est **immédiatement** inséré en base avec
`address = null`, puis un appel à `https://api-adresse.data.gouv.fr/reverse/` part dans une
coroutine portée par l'application. Quand l'adresse arrive, elle est écrite en base et l'UI
se met à jour via le `Flow` exposé par Room.

Conséquence voulue : **une panne réseau n'empêche jamais d'enregistrer un souvenir.**
L'adresse peut être redemandée plus tard via `PlaceRepository.resolveAddress(id)`.

### Gestion des photos

Le sujet interdit le stockage des images en Base64 dans la base — et c'est justifié :
l'encodage gonfle chaque image d'environ un tiers, et Android limite la taille d'une ligne
lue depuis SQLite (`CursorWindow`), ce qui peut faire échouer une simple lecture de liste.

- L'image est écrite dans `context.filesDir/photos/` (stockage interne **privé**)
- Seul le **chemin absolu** est persisté dans la colonne `photoPath`
- Une image choisie dans la galerie est **recopiée** : la permission sur une URI `content://`
  expire, la photo disparaîtrait au bout de quelques heures
- Supprimer un lieu supprime d'abord le fichier photo, puis la ligne en base
- Le Base64 n'apparaît que dans le fichier JSON d'échange, jamais en base

Le partage de fichiers passe par le `FileProvider` déclaré dans le manifeste
(`app/src/main/res/xml/file_paths.xml`).

### Distinction « mes lieux » / « lieux importés »

Contrainte de modélisation du sujet, traitée dans le schéma dès la conception :

| Colonne | Rôle |
|---|---|
| `uuid` | Identité globale du lieu (index **unique**) — survit à l'export, sert de clé de déduplication |
| `author` | `NULL` ⇒ lieu créé localement · non-`NULL` ⇒ pseudo de l'auteur qui l'a partagé |
| `importedAt` | Horodatage de l'import, `NULL` pour les lieux locaux |

L'`id` auto-incrémenté n'a de sens que sur l'appareil local : c'est l'`uuid` qui permet de
reconnaître un lieu après un aller-retour export/import.

L'import **n'écrase jamais** de données : il ajoute des lignes marquées d'un auteur, et les
`uuid` déjà connus sont ignorés. La déduplication a deux niveaux — filtrage préalable sur les
`uuid` en base (avant de matérialiser les photos, pour ne pas créer de fichiers orphelins),
puis `OnConflictStrategy.IGNORE` sur l'index unique comme filet de sécurité.

## Format du fichier d'échange

`places_export.json`, écrit dans `context.cacheDir/exports/` et partagé via le `FileProvider`.

```json
{
  "formatVersion": 1,
  "exportedAt": 1740000000000,
  "author": "adrian",
  "places": [
    {
      "uuid": "9f1c2b7e-5a3d-4c88-9d21-0b7f5a3e1c44",
      "title": "Pont Neuf",
      "description": "Premier café en terrasse de l'année",
      "emoji": "☕",
      "latitude": 48.8566,
      "longitude": 2.3522,
      "address": "1 Quai du Louvre 75001 Paris",
      "createdAt": 1739990000000,
      "photoBase64": "/9j/4AAQSkZJRgABAQ..."
    }
  ]
}
```

| Champ | Type | Note |
|---|---|---|
| `formatVersion` | `Int` | Un fichier d'une version **supérieure** est refusé (`InvalidExportFileException`) |
| `exportedAt` | `Long` | Millisecondes epoch |
| `author` | `String` | Pseudo de l'expéditeur ; devient la colonne `author` chez le destinataire |
| `places[].uuid` | `String` | Clé de déduplication ; un `uuid` déjà présent est ignoré |
| `places[].address` | `String?` | `null` si le géocodage n'a jamais abouti |
| `places[].photoBase64` | `String?` | Photo encodée `NO_WRAP` ; décodée vers le stockage interne à l'import |

Tous les champs des DTO portent une valeur par défaut. Gson étant une bibliothèque Java, il
ignore la nullabilité de Kotlin : sans constructeur sans argument, il instancie via `Unsafe` et
laisse des `null` dans des champs déclarés non-nullables. Avec des valeurs par défaut partout,
Kotlin génère ce constructeur et un champ absent retombe proprement sur son défaut.

## Sécurité

Le verrouillage biométrique est **optionnel**, activable depuis l'écran de réglages et
persisté dans DataStore (`SettingsRepository.biometricLockEnabled`). L'API `BiometricPrompt`
gère les différences entre appareils et retombe sur le code ou schéma de déverrouillage
lorsqu'aucun capteur biométrique n'est disponible.

Le verrouillage protège l'accès à l'application, pas le fichier de base de données lui-même.
Sur un appareil non rooté, le stockage interne privé est déjà isolé des autres applications.

## Lancer le projet

Aucune clé API n'est nécessaire.

```bash
git clone https://github.com/sharps4/MyPlaces.git
cd MyPlaces
./gradlew installDebug
```

Ou ouvrir le dossier dans Android Studio (Ladybug ou plus récent) et lancer sur un émulateur
API 24+. Une connexion réseau est nécessaire pour que le géocodage inverse renseigne les
adresses.

## Tests

```bash
./gradlew test
```

`ExportFormatTest` couvre le contrat du fichier d'échange : aller-retour de sérialisation,
tolérance aux champs absents, déduplication par `uuid`. Ce sont des tests JVM purs, sans
émulateur. Nous n'avons volontairement pas testé Room ni Retrofit : ce serait tester des
bibliothèques, alors qu'une évolution du format JSON casserait la compatibilité avec les
fichiers déjà partagés.

## Base de données

Le schéma Room exporté est versionné dans `app/schemas/`. Version courante : **1**.
Il servira de référence pour écrire une migration plutôt qu'un
`fallbackToDestructiveMigration`, qui effacerait les données de l'utilisateur.
