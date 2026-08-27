# Plan d'implémentation - Ajout des Paramètres (Biométrie et Export)

Ce plan décrit les étapes pour ajouter un bouton "Paramètres" dans l'application, permettant d'activer/désactiver le verrouillage biométrique et d'exporter les données au format JSON.

## User Review Required

> [!IMPORTANT]
> L'exportation des données générera un fichier JSON et ouvrira le menu de partage Android pour permettre à l'utilisateur de l'enregistrer ou de l'envoyer.
> Le verrouillage biométrique sera géré via une option dans les paramètres, mais son application au démarrage de l'application (si activé) n'est pas explicitement demandée. Je vais me concentrer sur l'interface de réglage.

## Proposed Changes

### [Component] Data & ViewModel
#### [MODIFY] [AppViewModelProvider.kt](file:///C:/Users/leohr/StudioProjects/MyPlaces1/app/src/main/java/com/example/myplaces/ui/AppViewModelProvider.kt)
- Ajouter l'initialisation de `SettingsViewModel`.

#### [NEW] [SettingsViewModel.kt](file:///C:/Users/leohr/StudioProjects/MyPlaces1/app/src/main/java/com/example/myplaces/ui/settings/SettingsViewModel.kt)
- Gérer l'état de `biometricLockEnabled`.
- Gérer l'état de `authorName`.
- Fonction pour déclencher l'exportation via `PlaceRepository`.

### [Component] UI Screens
#### [NEW] [SettingsScreen.kt](file:///C:/Users/leohr/StudioProjects/MyPlaces1/app/src/main/java/com/example/myplaces/ui/settings/SettingsScreen.kt)
- Écran avec un interrupteur pour la biométrie.
- Champ de texte pour le nom de l'auteur.
- Bouton "Exporter les données".
- Intégration du partage de fichier via `Intent`.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/leohr/StudioProjects/MyPlaces1/app/src/main/java/com/example/myplaces/MainActivity.kt)
- Ajouter la route `"settings"` au `NavHost`.

#### [MODIFY] [MapScreen.kt](file:///C:/Users/leohr/StudioProjects/MyPlaces1/app/src/main/java/com/example/myplaces/ui/map/MapScreen.kt)
- Ajouter un bouton (IconButton) pour accéder aux paramètres.

#### [MODIFY] [MainScreen.kt](file:///C:/Users/leohr/StudioProjects/MyPlaces1/app/src/main/java/com/example/myplaces/ui/MainScreen.kt)
- Passer le callback de navigation vers les paramètres.

## Verification Plan

### Automated Tests
- Vérifier que `SettingsViewModel` met bien à jour le `SettingsRepository`.
- Vérifier que `exportToJson` crée bien un fichier.

### Manual Verification
- Naviguer vers l'écran des paramètres via le nouveau bouton.
- Activer/désactiver la biométrie et vérifier que l'état est sauvegardé (en revenant sur l'écran).
- Modifier le nom de l'auteur.
- Cliquer sur "Exporter" et vérifier que le sélecteur de partage Android s'ouvre avec le fichier JSON.
