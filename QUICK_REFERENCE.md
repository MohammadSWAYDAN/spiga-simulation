# 🗺️ CARTE DE NAVIGATION - QUICK REFERENCE

## ⚡ TL;DR (Version Ultra-Courte)

**SPIGA** = Simulation de flotte de véhicules autonomes (drones, navires, sous-marins)

**Dossiers clés** :
- `core/` = Les véhicules et leur logique
- `management/` = Gestion flotte et missions
- `environment/` = Monde physique (obstacles, météo)
- `ui/` = Interface graphique

**Flux principal** :
1. **Main.java** → Lance l'app
2. **MainController** → Crée tout et orchestre
3. **SimulationService** → Boucle 60 FPS
4. **ActifMobile** → Base de tous les véhicules
5. **MapCanvas** → Affichage et interaction

---

## 🎯 Où Aller Selon Ce que Vous Voulez Faire

### Je Veux COMPRENDRE le Projet

```
START HERE:
1. Lire ARCHITECTURE_OVERVIEW.md (ce repo)
   ↓
2. Lire CODE_READING_GUIDE.md (ce repo)
   ↓
3. Lire RELATIONS_BETWEEN_FILES.md (ce repo)
   ↓
4. Lire Main.java → MainController.java → SimulationService.java
```

### Je Veux AJOUTER un Nouveau Drone

```
1. Ouvrir: src/main/java/com/spiga/core/DroneLogistique.java
   (Exemple)
   ↓
2. Créer: src/main/java/com/spiga/core/MonNouveauDrone.java
   extends ActifAerien
   ↓
3. Modifier: MainController.java
   Ajouter bouton pour créer ce drone
   ↓
4. Modifier: SidebarController.java (optionnel)
   Afficher le nouveau drone dans la liste
```

### Je Veux AJOUTER une Nouvelle Mission

```
1. Ouvrir: src/main/java/com/spiga/management/MissionLogistique.java
   (Exemple)
   ↓
2. Créer: src/main/java/com/spiga/management/MissionNouvelle.java
   extends Mission
   ↓
3. Modifier: MissionController.java
   Ajouter option pour créer cette mission
   ↓
4. Tester avec MainController
```

### Je Veux MODIFIER la Physique

```
1. Ouvrir: src/main/java/com/spiga/core/ActifMobile.java
   ↓
2. Modifier:
   - update(dt) → Boucle d'update
   - moveTowards() → Déplacement
   - updateBattery() → Consommation batterie
   ↓
3. Rafraîchir l'application pour voir les changements
```

### Je Veux MODIFIER l'Interface

```
1. Modifier FXML:
   src/main/resources/com/spiga/ui/MainView.fxml
   ↓
2. Modifier Contrôleur:
   src/main/java/com/spiga/ui/MainController.java
   ↓
3. Recompiler et exécuter
```

### Je Veux AJOUTER un Obstacle

```
1. Ouvrir: src/main/java/com/spiga/core/SimulationService.java
   ↓
2. Trouver: initializeObstacles() méthode
   ↓
3. Ajouter:
   obstacles.add(new Obstacle(x, y, z, radius));
   ↓
4. Les obstacles s'afficheront automatiquement
```

### Je Veux DÉBOGUER

```
Options:

A. Ajouter des System.out.println()
   - Meilleur pour traces simples
   - Voir console

B. Activer DEBUG VIEW en FXML
   - Voir tous les attributs

C. Debugger VS Code
   - CTRL+SHIFT+D → Run Debug
   - F5 pour démarrer
   - F10/F11 pour pas à pas
```

---

## 📂 Arboresence Détaillée - Où Trouver Quoi

### **core/** (Le cœur du jeu)

```
ActifMobile.java
├── Classe abstraite de base
├── Tous les véhicules héritent de ça
├── Logique commune: mouvement, batterie, missions
└── À lire en premier

ActifAerien.java
├── Classe abstraite pour drones
├── Ajoute limites d'altitude
└── Parent de: Drone*, Helicopter, Glider

ActifMarin.java
├── Classe abstraite pour navires
├── Ajoute limites de profondeur
└── Parent de: VehiculeSurface, VehiculeSousMarin

DroneLogistique.java (EXEMPLE DRONE)
├── Drone pour transport
├── À copier pour créer autre drone
└── Parent: ActifAerien

NavirePatrouille.java (EXEMPLE NAVIRE SURFACE)
├── Navire de patrouille
├── À la surface (z = 0)
└── Parent: VehiculeSurface

VehiculeSousMarin.java
├── Classe abstraite pour sous-marins
├── z < 0 (profondeur)
└── Parent de: SousMarinAttaque, SousMarinExploration

SousMarinAttaque.java (EXEMPLE SOUS-MARIN)
├── Sous-marin de combat
├── En profondeur (z < 0)
└── Parent: VehiculeSousMarin

Interfaces/:
├── Deplacable.java
├── Rechargeable.java
├── Communicable.java
├── Pilotable.java
└── Alertable.java
   (Tous implémentés par ActifMobile)

SimulationService.java
├── Le moteur 60 FPS
├── Met à jour tous les actifs chaque 16.67ms
├── Gère obstacles et météo
└── TRÈS IMPORTANT

(Autres fichiers d'environnement...)
├── Environment spécifiques
└── À explorer selon besoin
```

### **management/** (Organisation)

```
GestionnaireEssaim.java
├── Conteneur de tous les actifs
├── Gère add/remove/list
├── Principal point d'accès aux véhicules
└── À consulter souvent

Mission.java
├── Classe abstraite pour missions
├── Définit interface commune
└── À étendre pour nouvelles missions

MissionLogistique.java (EXEMPLE)
├── Mission de transport
├── À copier pour nouvelles missions
└── Parent: Mission

MissionSurveillance*.java
├── Missions de surveillance
├── Plusieurs types
└── Parent: Mission

MissionRechercheEtSauvetage.java
├── Mission de sauvetage
├── Parent: Mission
└── Type: SEARCH_AND_RESCUE
```

### **environment/** (Monde Physique)

```
ZoneOperation.java
├── Limites du monde (min/max X, Y)
├── Vérifier si point dans zone
└── Petit mais important

Obstacle.java
├── Obstacles statiques (îles, récifs)
├── Défini par: x, y, z, radius
└── Parcouru par SimulationService

Weather.java
├── Météo (vent, pluie)
├── Mise à jour cyclique
└── Affecte véhicules
```

### **ui/** (Interface)

```
MainView.fxml
├── Layout XML de l'interface
├── Définit structure visuelle
└── Modifié dans un éditeur XML

MainController.java
├── Orchestrateur principal
├── Crée tout au démarrage
├── Écoute tous les événements
├── Appelle tout le reste
└── POINT D'ACCÈS CENTRAL

MapCanvas.java
├── Affichage 2D (vue du dessus)
├── Dessine actifs et obstacles
├── Gère sélection et clics
└── Important pour interaction

SideViewCanvas.java
├── Affichage 2D (profil vertical)
├── Montre altitude/profondeur
└── Aide visualisation 3D

SidebarController.java
├── Barre latérale avec liste actifs
├── Affiche info sur chaque actif
└── Permet sélection

MissionController.java
├── Gestion missions dans l'UI
├── Crée missions
├── Les assigne aux actifs
└── Affiche statut
```

---

## 🔍 Comment Trouver une Méthode

### Syntaxe Java pour Chercher

```java
// Si vous cherchez "recharger":

1. Ctrl+Shift+F (Find in Files)
2. Tapez: "recharger"
3. Parcourez résultats

Résultat typique:
✓ ActifMobile.java - ligne 145
  public void recharger() { ... }
  
✓ SimulationService.java - ligne 89
  if (actif.getAutonomieActuelle() < 20) {
      actif.recharger();
  }
```

### Chercher par Type

```
"public abstract" → Classes abstraites
"interface" → Interfaces
"extends" → Classes qui héritent
"implements" → Classes qui implémentent interface
"@Override" → Méthodes redéfinies
```

---

## 📋 Checklist: Avant de Modifier du Code

```
[ ] J'ai lu le fichier où je vais modifier
[ ] J'ai compris la classe et ses parents
[ ] J'ai cherché d'autres utilisations de cette méthode
[ ] J'ai compris les dépendances
[ ] J'ai fait un backup (git commit)
[ ] Je vais faire une modification à la fois
[ ] Je vais tester après chaque modification
[ ] Je vais relire mon code avant de commiter
```

---

## 🚀 Compilation et Exécution

### En Ligne de Commande

```powershell
# Compiler
mvnw clean compile

# Exécuter
mvnw javafx:run

# Tests
mvnw test
```

### Depuis VS Code

```
Ctrl+Shift+B → Compile
F5 → Debug
Ctrl+Shift+D → Ouvrir Debug
```

---

## 🔗 Dépendances Principales

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <!-- Interface graphique -->
</dependency>

<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <!-- Tests unitaires -->
</dependency>
```

---

## 💾 Fichiers de Données

```
target/
├── classes/
│   └── com/spiga/
│       ├── ui/*.fxml (layouts compilés)
│       ├── core/*.class (bytecode)
│       ├── management/*.class
│       ├── environment/*.class
│       └── ui/*.class
│
└── test-classes/
    └── Tests compilés
```

---

## 🐛 Erreurs Courantes et Solutions

| Erreur | Cause | Solution |
|--------|-------|----------|
| "Cannot find symbol" | Import manquant | Ajouter: `import ...;` |
| "Variable is private" | Encapsulation | Utiliser getter ou modifier access |
| "Method not found" | Typage incorrect | Vérifier classe/interface |
| "NPE" (NullPointerException) | Objet null | Ajouter null check |
| "Compilation failed" | Syntaxe | Relire la ligne d'erreur |

---

## 📞 Aide Rapide

### Je comprends pas une classe

```
1. Lire le commentaire en haut (Javadoc)
2. Lire les attributs (What it stores)
3. Lire le constructeur (How it's created)
4. Lire les méthodes (What it does)
5. Lire les @Override (How it differs from parent)
```

### Je comprends pas une méthode

```
1. Lire le nom (indicateur de fonction)
2. Lire les paramètres (What goes in)
3. Lire le return type (What comes out)
4. Lire le contenu (Step by step)
5. Lire les appels (Where it's used)
```

### Je peux pas trouver où quelque chose arrive

```
1. Chercher le nom de la méthode (Ctrl+F)
2. Chercher dans tous les fichiers (Ctrl+Shift+F)
3. Chercher la classe (Ctrl+Shift+T)
4. Vérifier les imports
5. Vérifier l'héritage (extends, implements)
```

---

## 📚 Ressources Externes

- **JavaFX Docs** : https://openjfx.io/
- **Java Collections** : `List<>`, `ArrayList<>`, `Stream`
- **OOP Concepts** : `abstract`, `interface`, `extends`, `implements`
- **Maven** : `mvnw clean compile`

---

## 🎓 Résumé des Concepts OOP Utilisés

| Concept | Exemple |
|---------|---------|
| **Héritage** | `DroneLogistique extends ActifAerien` |
| **Interface** | `ActifMobile implements Deplacable` |
| **Polymorphisme** | Tous les drones ont `update()` |
| **Encapsulation** | `private autonomieActuelle` |
| **Classe Abstraite** | `abstract class ActifMobile` |
| **Énumération** | `enum AssetState { IDLE, MOVING, ... }` |
| **Generics** | `List<ActifMobile>` |
| **Lambda** | `event -> handleMapClicked(...)` |

---

**Besoin d'aide ? Révisez ARCHITECTURE_OVERVIEW.md ou CODE_READING_GUIDE.md !**
