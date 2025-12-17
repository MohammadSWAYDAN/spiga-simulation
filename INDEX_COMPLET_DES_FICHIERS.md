# 📑 INDEX COMPLET DES FICHIERS - SPIGA

## 📍 Documents de Compréhension (Lisez D'Abord!)

Ces fichiers vous aident à comprendre le projet:

| Fichier | Description | Niveau | Temps |
|---------|-------------|--------|-------|
| **GETTING_STARTED.md** | Guide de démarrage étape par étape | Débutant | 15 min |
| **ARCHITECTURE_OVERVIEW.md** | Explication générale du projet | Débutant | 20 min |
| **QUICK_REFERENCE.md** | Navigation rapide et TL;DR | Débutant | 10 min |
| **CODE_READING_GUIDE.md** | Comment lire le code avec exemples | Intermédiaire | 30 min |
| **RELATIONS_BETWEEN_FILES.md** | Relations entre tous les fichiers | Intermédiaire | 25 min |
| **UML_DIAGRAM.md** | Diagrammes visuels UML | Intermédiaire | 15 min |
| **INDEX_COMPLET_DES_FICHIERS.md** | Ce fichier | Référence | 10 min |

---

## 🚀 Entry Point

### Main.java
- **Chemin** : `src/main/java/com/spiga/Main.java`
- **Description** : Point d'entrée de l'application JavaFX
- **Rôle** : Lance l'interface graphique
- **Lignes** : ~20
- **Complexité** : ⭐ Très simple
- **À Lire** : 1er
- **Code** :
  ```java
  public class Main extends Application {
      @Override
      public void start(Stage primaryStage) throws Exception {
          // Charge MainView.fxml
          // Lance l'interface
      }
  }
  ```

---

## 🎮 Package CORE - Les Véhicules

### Classe Abstraite de Base

#### ActifMobile.java ⭐ TRÈS IMPORTANT
- **Chemin** : `src/main/java/com/spiga/core/ActifMobile.java`
- **Description** : Base abstraite de TOUS les véhicules
- **Rôle** : Logique commune (mouvement, batterie, état)
- **Lignes** : ~330
- **Complexité** : ⭐⭐⭐ Moyenne
- **À Lire** : 2e (après Main)
- **Implémente** : 5 interfaces (Deplacable, Rechargeable, Communicable, Pilotable, Alertable)
- **Méthodes Clés** :
  - `update(dt)` - Mise à jour physique
  - `moveTowards(x, y, z, dt)` - Déplacement vers cible
  - `updateBattery(dt)` - Consommation d'énergie
  - `checkBatteryState()` - Vérification batterie
- **État** : AssetState (IDLE, MOVING, RECHARGING, etc.)

### Classes Abstraites Spécialisées

#### ActifAerien.java
- **Chemin** : `src/main/java/com/spiga/core/ActifAerien.java`
- **Description** : Classe abstraite pour drones/hélicoptères
- **Rôle** : Ajoute limites d'altitude
- **Lignes** : ~40
- **Complexité** : ⭐ Très simple
- **Parents** : Extends ActifMobile
- **Enfants** : DroneLogistique, DroneAttaque, DroneReconnaissance, HelicoptereSauvetage, GliderOceanographique
- **Contrainte** : `altitudeMin ≤ z ≤ altitudeMax`

#### ActifMarin.java
- **Chemin** : `src/main/java/com/spiga/core/ActifMarin.java`
- **Description** : Classe abstraite pour navires/sous-marins
- **Rôle** : Ajoute limites de profondeur
- **Lignes** : ~40
- **Complexité** : ⭐ Très simple
- **Parents** : Extends ActifMobile
- **Enfants** : VehiculeSurface, VehiculeSousMarin
- **Contrainte** : `profondeurMin ≤ z ≤ profondeurMax`

#### VehiculeSurface.java
- **Chemin** : `src/main/java/com/spiga/core/VehiculeSurface.java`
- **Description** : Classe abstraite pour navires de surface
- **Rôle** : Navires qui restent à la surface
- **Parents** : Extends ActifMarin
- **Enfants** : NavirePatrouille, NavireLogistique, NavireRecherche
- **Contrainte** : z = 0 (surface)

#### VehiculeSousMarin.java
- **Chemin** : `src/main/java/com/spiga/core/VehiculeSousMarin.java`
- **Description** : Classe abstraite pour sous-marins
- **Rôle** : Véhicules en profondeur
- **Parents** : Extends ActifMarin
- **Enfants** : SousMarinAttaque, SousMarinExploration
- **Contrainte** : z < 0 (profondeur)

### Implémentations Concrètes - DRONES AÉRIENS

#### DroneLogistique.java
- **Chemin** : `src/main/java/com/spiga/core/DroneLogistique.java`
- **Description** : Drone pour transport logistique
- **Rôle** : Drone standard
- **Parents** : Extends ActifAerien
- **Type Mission** : LOGISTICS
- **Paramètres** : Vitesse moyenne, bonne autonomie
- **À Copier** : Pour créer autres drones
- **Usage** : Transporter du cargo

#### DroneAttaque.java
- **Chemin** : `src/main/java/com/spiga/core/DroneAttaque.java`
- **Description** : Drone militaire d'attaque
- **Parents** : Extends ActifAerien
- **Type Mission** : Combat/Surveillance
- **Paramètres** : Vitesse très élevée, batterie limitée
- **Usage** : Missions militaires

#### DroneReconnaissance.java
- **Chemin** : `src/main/java/com/spiga/core/DroneReconnaissance.java`
- **Description** : Drone de reconnaissance
- **Parents** : Extends ActifAerien
- **Type Mission** : SURVEILLANCE
- **Paramètres** : Vitesse faible, très bonne autonomie
- **Usage** : Patrouille et surveillance

#### HelicoptereSauvetage.java
- **Chemin** : `src/main/java/com/spiga/core/HelicoptereSauvetage.java`
- **Description** : Hélicoptère de sauvetage
- **Parents** : Extends ActifAerien
- **Type Mission** : SEARCH_AND_RESCUE
- **Paramètres** : Vitesse lente, très bonne autonomie
- **Usage** : Opérations de sauvetage

#### GliderOceanographique.java
- **Chemin** : `src/main/java/com/spiga/core/GliderOceanographique.java`
- **Description** : Planeur pour études océaniques
- **Parents** : Extends ActifAerien
- **Type Mission** : SURVEILLANCE
- **Paramètres** : Vitesse très lente, autonomie quasi-infinie
- **Usage** : Collecte données océan

### Implémentations Concrètes - NAVIRES SURFACE

#### NavirePatrouille.java
- **Chemin** : `src/main/java/com/spiga/core/NavirePatrouille.java`
- **Description** : Navire de patrouille maritime
- **Parents** : Extends VehiculeSurface
- **Type Mission** : SURVEILLANCE
- **Paramètres** : Vitesse moyenne, très bonne autonomie
- **Usage** : Patrouille océan

#### NavireLogistique.java
- **Chemin** : `src/main/java/com/spiga/core/NavireLogistique.java`
- **Description** : Navire cargo logistique
- **Parents** : Extends VehiculeSurface
- **Type Mission** : LOGISTICS
- **Paramètres** : Vitesse lente, autonomie normale
- **Usage** : Transport de cargo

#### NavireRecherche.java
- **Chemin** : `src/main/java/com/spiga/core/NavireRecherche.java`
- **Description** : Navire de recherche et sauvetage
- **Parents** : Extends VehiculeSurface
- **Type Mission** : SEARCH_AND_RESCUE
- **Paramètres** : Vitesse moyenne, très bonne autonomie
- **Usage** : Opérations de sauvetage

### Implémentations Concrètes - SOUS-MARINS

#### SousMarinAttaque.java
- **Chemin** : `src/main/java/com/spiga/core/SousMarinAttaque.java`
- **Description** : Sous-marin militaire d'attaque
- **Parents** : Extends VehiculeSousMarin
- **Type Mission** : Combat/Surveillance
- **Paramètres** : Vitesse élevée, batterie limitée
- **Usage** : Missions militaires en profondeur

#### SousMarinExploration.java
- **Chemin** : `src/main/java/com/spiga/core/SousMarinExploration.java`
- **Description** : Sous-marin d'exploration
- **Parents** : Extends VehiculeSousMarin
- **Type Mission** : SURVEILLANCE
- **Paramètres** : Vitesse lente, très bonne autonomie
- **Usage** : Exploration océanique

### Interfaces - Les Contrats

#### Deplacable.java
- **Chemin** : `src/main/java/com/spiga/core/Deplacable.java`
- **Description** : Interface pour mouvement
- **Méthodes** :
  - `deplacer(x, y, z)` - Déplace vers position
  - `calculerTrajet(x, y, z)` - Calcule itinéraire
- **Implémentée par** : ActifMobile

#### Rechargeable.java
- **Chemin** : `src/main/java/com/spiga/core/Rechargeable.java`
- **Description** : Interface pour batterie
- **Méthodes** :
  - `recharger()` - Recharge batterie
  - `getAutonomieActuelle()` - Batterie actuelle
  - `getAutonomieMax()` - Batterie max
- **Implémentée par** : ActifMobile

#### Pilotable.java
- **Chemin** : `src/main/java/com/spiga/core/Pilotable.java`
- **Description** : Interface pour contrôle
- **Méthodes** :
  - `demarrer()` - Démarrer moteur
  - `eteindre()` - Arrêter moteur
- **Implémentée par** : ActifMobile

#### Communicable.java
- **Chemin** : `src/main/java/com/spiga/core/Communicable.java`
- **Description** : Interface pour communication
- **Méthodes** :
  - `envoyerMessage(msg)` - Envoyer message
  - `recevoirOrdre(ordre)` - Recevoir ordres
- **Implémentée par** : ActifMobile

#### Alertable.java
- **Chemin** : `src/main/java/com/spiga/core/Alertable.java`
- **Description** : Interface pour alertes
- **Méthodes** :
  - `genererAlerte(msg)` - Générer alerte
  - `recevoirAlerte(alert)` - Recevoir alerte
- **Implémentée par** : ActifMobile

### Service Principal

#### SimulationService.java ⭐ TRÈS IMPORTANT
- **Chemin** : `src/main/java/com/spiga/core/SimulationService.java`
- **Description** : Moteur de simulation 60 FPS
- **Rôle** : Boucle principale qui met à jour tout
- **Lignes** : ~246
- **Complexité** : ⭐⭐⭐ Moyenne
- **À Lire** : 3e
- **Fréquence** : 60 FPS (16.67 ms par frame)
- **Tâches** :
  - Met à jour position de chaque ActifMobile
  - Gère obstacles et collisions
  - Cycles météo
  - Détecte missions terminées
- **Méthodes Clés** :
  - `handle(long now)` - Appelée 60x par seconde
  - `updateSimulation(dt)` - Logique d'une frame
  - `startSimulation()` - Démarre la boucle
  - `stopSimulation()` - Arrête la boucle

---

## 📊 Package MANAGEMENT - Gestion

#### GestionnaireEssaim.java ⭐ IMPORTANT
- **Chemin** : `src/main/java/com/spiga/management/GestionnaireEssaim.java`
- **Description** : Gestionnaire de la flotte
- **Rôle** : Conteneur de tous les ActifMobile
- **Lignes** : ~70
- **Complexité** : ⭐ Très simple
- **À Lire** : 4e
- **Attributs** :
  - `flotte: List<ActifMobile>` - Tous les véhicules
- **Méthodes Clés** :
  - `ajouterActif(ActifMobile)` - Ajoute à la flotte
  - `getFlotte()` - Retourne tous les actifs
  - `getActifsDisponibles()` - Filtre disponibles
  - `demarrerMission(Mission, List)` - Lance mission

#### Mission.java (Abstract)
- **Chemin** : `src/main/java/com/spiga/management/Mission.java`
- **Description** : Classe abstraite pour missions
- **Rôle** : Définit interface commune des missions
- **Lignes** : ~179
- **Complexité** : ⭐⭐ Facile
- **Enums** :
  - `MissionType` : SURVEILLANCE, LOGISTICS, NAVIGATION, SEARCH_AND_RESCUE
  - `StatutMission` : PLANIFIEE, EN_COURS, TERMINEE, ECHOUEE, ANNULEE
- **Attributs** :
  - `targetX, targetY, targetZ` - Position cible
  - `statut` - État actuel
- **Méthodes** :
  - `execute()` - abstract
  - `complete()` - Terminer mission
  - `fail()` - Échouer mission

#### MissionLogistique.java
- **Chemin** : `src/main/java/com/spiga/management/MissionLogistique.java`
- **Description** : Mission de transport de cargo
- **Parents** : Extends Mission
- **Type** : LOGISTICS
- **À Copier** : Pour nouvelles missions
- **Usage** : Transporter du cargo d'un point A à B

#### MissionSurveillanceMaritime.java
- **Chemin** : `src/main/java/com/spiga/management/MissionSurveillanceMaritime.java`
- **Description** : Mission de surveillance maritime
- **Parents** : Extends Mission
- **Type** : SURVEILLANCE
- **Usage** : Patrouiller une zone

#### MissionRechercheEtSauvetage.java
- **Chemin** : `src/main/java/com/spiga/management/MissionRechercheEtSauvetage.java`
- **Description** : Mission de recherche et sauvetage
- **Parents** : Extends Mission
- **Type** : SEARCH_AND_RESCUE
- **Usage** : Opérations d'urgence

---

## 🌍 Package ENVIRONMENT - Environnement

#### ZoneOperation.java
- **Chemin** : `src/main/java/com/spiga/environment/ZoneOperation.java`
- **Description** : Zone opérationnelle (limites du monde)
- **Rôle** : Définit boundaries (min/max X, Y)
- **Lignes** : ~30
- **Complexité** : ⭐ Trivial
- **Attributs** :
  - `minX, maxX, minY, maxY` - Limites
- **Méthode** :
  - `isInside(x, y)` - Vérifie si point dans zone

#### Obstacle.java
- **Chemin** : `src/main/java/com/spiga/environment/Obstacle.java`
- **Description** : Obstacle statique (île, récif)
- **Rôle** : Obstruction à éviter
- **Lignes** : ~30
- **Complexité** : ⭐ Trivial
- **Attributs** :
  - `x, y, z` - Position
  - `radius` - Taille
- **Usage** : Initialiser dans SimulationService

#### Weather.java
- **Chemin** : `src/main/java/com/spiga/environment/Weather.java`
- **Description** : Système météorologique
- **Rôle** : Vent, pluie, conditions
- **Lignes** : ~40
- **Complexité** : ⭐ Simple
- **Attributs** :
  - `windSpeed` - Vitesse vent
  - `rainIntensity` - Intensité pluie
- **Effets** : Ralentit drones, affecte autonomie

---

## 🖼️ Package UI - Interface Graphique

### Fichiers FXML (Layout XML)

#### MainView.fxml
- **Chemin** : `src/main/resources/com/spiga/ui/MainView.fxml`
- **Description** : Layout principal de l'interface
- **Rôle** : Structure XML de l'UI
- **Type** : Fichier XML JavaFX
- **Contient** :
  - `mapContainer` - Zone pour MapCanvas
  - `sideViewContainer` - Zone pour SideViewCanvas
  - `sidebarContainer` - Zone pour SidebarController
  - `missionPanel` - Zone pour MissionController
  - Contrôles (boutons, sliders, labels)

#### MissionPanel.fxml
- **Chemin** : `src/main/resources/com/spiga/ui/MissionPanel.fxml`
- **Description** : Layout pour gestion des missions
- **Rôle** : Interface missions
- **Type** : Fichier XML JavaFX

#### Sidebar.fxml
- **Chemin** : `src/main/resources/com/spiga/ui/Sidebar.fxml`
- **Description** : Layout de la barre latérale
- **Rôle** : Affichage liste des actifs
- **Type** : Fichier XML JavaFX

### Contrôleurs JavaFX

#### MainController.java ⭐ TRÈS IMPORTANT
- **Chemin** : `src/main/java/com/spiga/ui/MainController.java`
- **Description** : Contrôleur principal de l'UI
- **Rôle** : Orchestrateur central
- **Lignes** : ~487
- **Complexité** : ⭐⭐⭐⭐ Complexe
- **À Lire** : 5e
- **Responsabilités** :
  - Créer GestionnaireEssaim
  - Créer SimulationService
  - Créer MapCanvas et SideViewCanvas
  - Écouter les événements utilisateur
  - Coordonner tous les contrôleurs
- **Événements** :
  - `handleMapClicked(x, y)` - Clic sur map
  - `handleSelectionChanged()` - Sélection changée
  - `updateUI()` - Redessine l'interface

#### MapCanvas.java ⭐ IMPORTANT
- **Chemin** : `src/main/java/com/spiga/ui/MapCanvas.java`
- **Description** : Affichage 2D (vue du dessus)
- **Rôle** : Dessine les actifs et obstacles
- **Lignes** : ~300
- **Complexité** : ⭐⭐⭐ Moyenne
- **À Lire** : Après MainController
- **Contient** :
  - Canvas JavaFX pour drawing
  - GraphicsContext pour primitive drawing
- **Méthodes** :
  - `draw(actifs, obstacles)` - Redessine tout
  - `getActifAt(x, y)` - Détecte clic sur actif
  - `worldToScreenX/Y()` - Convertit coords

#### SideViewCanvas.java
- **Chemin** : `src/main/java/com/spiga/ui/SideViewCanvas.java`
- **Description** : Affichage 2D profil vertical
- **Rôle** : Montre altitude/profondeur
- **Lignes** : ~150
- **Complexité** : ⭐⭐ Facile
- **Méthode** :
  - `draw(actifs)` - Dessine profil

#### SidebarController.java
- **Chemin** : `src/main/java/com/spiga/ui/SidebarController.java`
- **Description** : Contrôleur de la barre latérale
- **Rôle** : Affiche liste des actifs
- **Lignes** : ~200
- **Complexité** : ⭐⭐ Facile
- **Affiche** :
  - Liste tous les actifs
  - Batterie, état, position
  - Boutons d'action

#### MissionController.java
- **Chemin** : `src/main/java/com/spiga/ui/MissionController.java`
- **Description** : Contrôleur pour gestion missions
- **Rôle** : Crée et assigne missions
- **Lignes** : ~250
- **Complexité** : ⭐⭐ Facile
- **Responsabilités** :
  - Afficher missions disponibles
  - Créer nouvelles missions
  - Assigner missions à actifs
  - Afficher statut

---

## 🧪 Package TEST

#### ActifMobileTest.java
- **Chemin** : `src/test/java/com/spiga/core/ActifMobileTest.java`
- **Description** : Tests unitaires pour ActifMobile
- **Type** : JUnit 5

#### SimulationServiceTest.java
- **Chemin** : `src/test/java/com/spiga/core/SimulationServiceTest.java`
- **Description** : Tests unitaires pour SimulationService
- **Type** : JUnit 5

#### GestionnaireEssaimTest.java
- **Chemin** : `src/test/java/com/spiga/management/GestionnaireEssaimTest.java`
- **Description** : Tests unitaires pour GestionnaireEssaim
- **Type** : JUnit 5

---

## ⚙️ Fichiers de Configuration

#### pom.xml
- **Chemin** : `pom.xml` (racine)
- **Description** : Configuration Maven du projet
- **Contient** :
  - Dépendances (JavaFX, JUnit)
  - Version Java 17
  - Plugins de build
  - Propriétés du projet

#### README.md
- **Chemin** : `README.md` (racine)
- **Description** : Documentation projet
- **Contient** :
  - Description générale
  - Features principales
  - Instructions de démarrage
  - Requirements

#### SPIGA-SPEC.txt
- **Chemin** : `SPIGA-SPEC.txt` (racine)
- **Description** : Cahier des charges du projet
- **Contient** :
  - Spécifications complètes
  - Recommandations
  - Checklist d'implémentation
  - Consignes de présentation

#### TEAM_SETUP_GUIDE.md
- **Chemin** : `TEAM_SETUP_GUIDE.md` (racine)
- **Description** : Guide pour l'équipe
- **Contient** :
  - Configuration du projet
  - Organisation du travail
  - Commits et git

---

## 🎯 Fichiers à Lire en Priorité

### Première Semaine
```
1. GETTING_STARTED.md
2. ARCHITECTURE_OVERVIEW.md
3. Main.java
4. ActifMobile.java
5. SimulationService.java
```

### Deuxième Semaine
```
6. GestionnaireEssaim.java
7. MainController.java
8. MapCanvas.java
9. Mission.java
10. DroneLogistique.java (exemple concret)
```

### Troisième Semaine
```
11. Autres implémentations (NavirePatrouille, etc.)
12. Interface classes (Deplacable, Rechargeable, etc.)
13. Environment classes
14. Tests unitaires
15. Code refactoring
```

---

## 📊 Statistiques du Projet

| Métrique | Valeur |
|----------|--------|
| Nombre de fichiers | ~40 |
| Lignes de code | ~3000 |
| Nombre de classes | ~25 |
| Nombre d'interfaces | 5 |
| Nombre de tests | 3 fichiers |
| Dépendances principales | JavaFX, JUnit |

---

## 🔍 Comment Utiliser Cet Index

### Chercher par Concept
Utilisez **Ctrl+F** pour chercher:
- "drone" → tous les fichiers drone
- "mission" → tous les fichiers mission
- "UI" → fichiers interface
- "test" → fichiers test

### Chercher par Niveau
- ⭐ = Simple
- ⭐⭐ = Facile
- ⭐⭐⭐ = Moyen
- ⭐⭐⭐⭐ = Complexe

### Chercher par Chemin
Ouvrir le fichier dans VS Code:
1. **Ctrl+P** (Quick Open)
2. Tapez le chemin (ex: "ActifMobile.java")
3. Entrée pour ouvrir

---

## ✅ Checklist: Avez-vous Lu...

```
Core Package:
[ ] Main.java
[ ] ActifMobile.java
[ ] ActifAerien.java
[ ] ActifMarin.java
[ ] SimulationService.java
[ ] Au moins 1 implémentation (DroneLogistique ou autre)

Management Package:
[ ] GestionnaireEssaim.java
[ ] Mission.java
[ ] Au moins 1 mission concrète

UI Package:
[ ] MainController.java
[ ] MapCanvas.java
[ ] Fichiers FXML principaux

Documentation:
[ ] GETTING_STARTED.md
[ ] ARCHITECTURE_OVERVIEW.md
[ ] CODE_READING_GUIDE.md
```

---

**Cet index contient TOUS les fichiers du projet. Utilisez-le comme référence ! 📑**
