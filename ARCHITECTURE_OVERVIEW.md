# 📋 ARCHITECTURE GÉNÉRALE DU PROJET SPIGA - Explication Complète

## 🎯 Qu'est-ce que SPIGA ?

**SPIGA** = **Système de Pilotage Intelligent et Gestion d'Actifs**

C'est une application JavaFX qui simule une **flotte de véhicules autonomes** (drones, navires, sous-marins) dans un environnement 3D avec :
- Gestion de missions
- Système météorologique
- Évitement d'obstacles
- Interface graphique interactive

---

## 📁 Structure des Dossiers

```
src/main/java/com/spiga/
├── Main.java                          # Point d'entrée de l'application
├── core/                              # ⭐ CŒUR DU SYSTÈME
│   ├── ActifMobile.java              # Classe abstraite de base (tous les véhicules)
│   ├── ActifAerien.java              # Véhicules aériens (drones)
│   ├── ActifMarin.java               # Véhicules marins (navires, sous-marins)
│   ├── Interfaces/                   # Contrats comportementaux
│   │   ├── Deplacable.java           # Contrat pour le déplacement
│   │   ├── Rechargeable.java         # Contrat pour la batterie
│   │   ├── Communicable.java         # Contrat de communication
│   │   ├── Pilotable.java            # Contrat de pilotage
│   │   └── Alertable.java            # Contrat d'alertes
│   ├── Drones/                       # Implémentations des drones
│   │   ├── DroneAttaque.java
│   │   ├── DroneLogistique.java
│   │   └── DroneReconnaissance.java
│   ├── Navires/                      # Implémentations des navires
│   │   ├── NavirePatrouille.java
│   │   ├── NavireLogistique.java
│   │   └── NavireRecherche.java
│   ├── Sous-marins/                  # Implémentations des sous-marins
│   │   ├── SousMarinAttaque.java
│   │   └── SousMarinExploration.java
│   ├── VehiculeSurface.java          # Classe abstraite pour navires
│   ├── VehiculeSousMarin.java        # Classe abstraite pour sous-marins
│   ├── SimulationService.java        # 🎮 Boucle de simulation (60 FPS)
│   ├── Helicopter & Glider.java      # Autres actifs aériens
│   └── Environment classes           # Obstacles, météo, zones
│
├── management/                        # 📊 GESTION ET MISSIONS
│   ├── GestionnaireEssaim.java       # Gère la flotte (add/remove/list)
│   ├── Mission.java                  # Classe abstraite des missions
│   ├── MissionSurveillance.java      # Surveillance de zone
│   ├── MissionLogistique.java        # Transport de cargo
│   ├── MissionRechercheEtSauvetage.java # Rescue operations
│   └── MissionSurveillanceMaritime.java # Maritime patrol
│
├── environment/                       # 🌍 ENVIRONNEMENT PHYSIQUE
│   ├── ZoneOperation.java            # Zone de simulation (min/max X,Y)
│   ├── Weather.java                  # Système météo (vent, pluie)
│   └── Obstacle.java                 # Obstacles (îles, récifs)
│
└── ui/                                # 🖼️ INTERFACE UTILISATEUR (JavaFX)
    ├── Main.java → MainView.fxml     # Écran principal
    ├── MainController.java           # Logique de l'écran principal
    ├── MapCanvas.java                # Affichage 2D du monde (vue de dessus)
    ├── SideViewCanvas.java           # Affichage 2D du profil (altitude/profondeur)
    ├── SidebarController.java        # Contrôle de la barre latérale
    ├── MissionController.java        # Gestion des missions dans l'UI
    └── *.fxml files                  # Layouts XML (structure UI)
```

---

## 🔗 Relations Entre les Fichiers (Hiérarchie d'Héritage)

### **1. Hiérarchie des Classes - Les Véhicules (Core)**

```
                        ActifMobile (Classe Abstraite)
                      /            |            \
                     /             |             \
            ActifAerien      (future expansion)   ActifMarin
           /    |    \                           /        \
          /     |     \                         /          \
    DroneAttaque DroneLogistique           VehiculeSurface  VehiculeSousMarin
    DroneReconnaissance                    /       |    \       /    \
    Helicopter                      NavirePatrouille |    \  SousMarinAttaque
    Glider                          NavireLogistique NavireRecherche SousMarinExploration
```

### **2. Relations Entre les Interfaces**

Chaque `ActifMobile` **implémente** 5 interfaces :

```
ActifMobile implements:
├── Deplacable       → Méthodes: deplacer(), calculerTrajet()
├── Rechargeable     → Méthodes: recharger(), consommer batterie
├── Communicable     → Méthodes: envoyer message, recevoir ordre
├── Pilotable        → Méthodes: demarrer(), eteindre()
└── Alertable        → Méthodes: générer alertes
```

### **3. Relations Entre les Packages**

```
Main.java (Point d'entrée)
    ↓
MainController (Coordonnateur UI)
    ↓
    ├─→ GestionnaireEssaim (Gère les actifs)
    │       ↓
    │   Liste[ActifMobile] - Tous les véhicules actifs
    │
    ├─→ SimulationService (Boucle physique 60 FPS)
    │       ↓
    │   ├─ Met à jour position de chaque ActifMobile
    │   ├─ Calcule collisions avec Obstacles
    │   ├─ Gère Weather cycles
    │   └─ Détecte missions terminées
    │
    ├─→ MapCanvas (Affichage 2D vue du dessus)
    │   ↓
    │   Dessine: tous les ActifMobile + Obstacles
    │
    └─→ SideViewCanvas (Affichage 2D profil vertical)
        ↓
        Dessine: altitude/profondeur des actifs
```

---

## 🔄 Flux de Données (Comment ça Marche)

### **Démarrage de l'Application**

```
1. Main.start() lance JavaFX
    ↓
2. Charge MainView.fxml (layout de l'interface)
    ↓
3. MainController.initialize() crée :
    - GestionnaireEssaim (gestionnaire = vide au démarrage)
    - SimulationService (timer 60 FPS)
    - MapCanvas (affichage)
    - ZoneOperation (limites du monde)
    ↓
4. Simulation démarre (boucle infinie à 60 FPS)
```

### **Ajouter un Actif (ex: un Drone)**

```
1. Utilisateur clique sur "Ajouter Drone" dans l'UI
    ↓
2. MainController reçoit l'événement
    ↓
3. Crée un objet: DroneLogistique drone = new DroneLogistique(...)
    ↓
4. Ajoute à la flotte: gestionnaire.ajouterActif(drone)
    ↓
5. Drone apparaît dans MapCanvas lors du prochain refresh
```

### **Déplacer un Actif**

```
1. Utilisateur clique sur la carte à position (X, Y)
    ↓
2. MapCanvas détecte le clic → appelle MainController.handleMapClicked(x, y)
    ↓
3. MainController récupère l'actif sélectionné
    ↓
4. Appelle: actif.deplacer(x, y, z)  ← Implémenté dans ActifMobile
    ↓
5. ActifMobile fixe targetX, targetY, targetZ et change state → MOVING_TO_TARGET
    ↓
6. SimulationService.handle() appelle chaque frame:
    - actif.update(dt)  ← Calcule nouveaux x, y, z vers la cible
    - Dessine nouvelle position
```

### **Boucle de Simulation (SimulationService - 60 FPS)**

```
Chaque 16.67ms (1/60 sec):

1. Pour chaque ActifMobile dans la flotte:
   ├─ Appelle actif.update(dt)
   │  ├─ Calcule mouvement vers la cible
   │  ├─ Consomme batterie selon distance et vitesse
   │  ├─ Vérifie si batterie critique (< 10%) → RETURNING_TO_BASE
   │  └─ Évite obstacles si en collision
   │
   ├─ Vérifie arrivée à destination
   │  ├─ Si c'est une mission → mission.complete()
   │  ├─ Si retour à la base → recharge batterie
   │  └─ Sinon → state = IDLE
   │
   └─ Met à jour affichage UI

2. Mise à jour météo (vent, pluie) tous les 30 secondes

3. Redessine les canvas (MapCanvas + SideViewCanvas)
```

---

## 📊 Classes Clés et Leur Rôle

### **ActifMobile (Classe Racine)**
- **Responsabilité** : Base commune de tous les véhicules
- **Attributs** : position (x,y,z), batterie, état, mission
- **Méthodes** : update(), moveTowards(), checkBatteryState()
- **Enfants** : ActifAerien, ActifMarin

### **ActifAerien**
- **Responsabilité** : Comportement spécifique aux drones/hélicoptères
- **Contraintes** : altitudeMin ≤ z ≤ altitudeMax
- **Enfants** : DroneAttaque, DroneLogistique, DroneReconnaissance, Helicopter, Glider

### **ActifMarin**
- **Responsabilité** : Comportement spécifique aux navires/sous-marins
- **Contraintes** : profondeurMin ≤ z ≤ profondeurMax
- **Enfants** : VehiculeSurface, VehiculeSousMarin

### **SimulationService**
- **Responsabilité** : Moteur physique et boucle principale
- **Fréquence** : 60 FPS
- **Tâches** : Update positions, gestion batterie, évitement obstacles, météo

### **GestionnaireEssaim**
- **Responsabilité** : Gestion de la flotte
- **Méthodes** : ajouterActif(), getFlotte(), getActifsDisponibles()

### **Mission (et ses sous-classes)**
- **Responsabilité** : Définir objectifs et suivi
- **Types** : Surveillance, Logistique, Rescue, Navigation
- **Lifecycle** : PLANIFIÉE → EN_COURS → TERMINÉE

### **MapCanvas**
- **Responsabilité** : Affichage 2D vue de dessus (X, Y)
- **Contient** : Positions de tous les actifs et obstacles
- **Interaction** : Détecte clics pour sélection/déplacement

### **SideViewCanvas**
- **Responsabilité** : Affichage 2D profil vertical
- **Affiche** : Altitude/profondeur (Z) vs position X

---

## 🔌 Points d'Extension

### Ajouter un Nouveau Type de Véhicule

```java
// 1. Créer une nouvelle classe
public class VehiculeDrone extends ActifAerien {
    public VehiculeDrone(String id, double x, double y, double z, ...) {
        super(id, x, y, z, ...);
        // Paramètres spécifiques
    }
}

// 2. Utiliser dans MainController
gestionnaire.ajouterActif(new VehiculeDrone("D1", 100, 100, 500, ...));
```

### Ajouter une Nouvelle Mission

```java
// 1. Créer une classe hériting Mission
public class MissionNouveauType extends Mission {
    public MissionNouveauType(String titre, ...) {
        super(titre, MissionType.NOUVEAU_TYPE);
    }
    
    @Override
    public void execute() {
        // Logique spécifique
    }
}

// 2. Assigner à des actifs
gestionnaire.demarrerMission(missionNouvelle, listActifs);
```

---

## 🎯 Règles Importantes

### Encapsulation (Private/Protected)
- **private** : Accès uniquement dans la classe
- **protected** : Accès dans les classes héritées
- **public** : Accès partout
- Getters/Setters avec validation

### Polymorphisme
- Les interfaces garantissent un contrat (ex: Deplacable)
- Les classes abstraites partagent du code (ex: ActifMobile)
- Les classes concrètes implémentent les détails

### Héritage
- **Vertical** : Classe → Sous-classe (code réutilisable)
- **Horizontal** : Interfaces (contrats)

---

## 📝 Exemple Complet : Créer et Déplacer un Drone

```java
// 1. CRÉATION
DroneLogistique drone1 = new DroneLogistique(
    "DRONE-001",      // ID unique
    100, 100, 1000,   // Position (x, y, altitude)
    200,              // Vitesse max (m/s)
    5000              // Autonomie max (secondes)
);

// 2. AJOUT À LA FLOTTE
gestionnaire.ajouterActif(drone1);  // ← Maintenant dans GestionnaireEssaim

// 3. DÉPLACEMENT (Utilisateur clique à (500, 500, 2000))
drone1.deplacer(500, 500, 2000);    // ← ActifMobile.deplacer()
// ↓ Interne
// - Fixe targetX=500, targetY=500, targetZ=2000
// - Change state = MOVING_TO_TARGET

// 4. SIMULATION (Boucle 60 FPS)
simulationService.handle();
// ↓ Pour chaque frame
// - drone1.update(dt)
//   ├─ moveTowards(500, 500, 2000, dt)
//   ├─ Déplace d'une fraction vers la cible
//   ├─ Consomme batterie
//   └─ Vérifie collisions
// - MapCanvas redessine drone1 à nouvelle position

// 5. ARRIVÉE (Après ~10 secondes)
// - ActifMobile détecte distance < 1m
// - state = IDLE
// - Si c'était une mission → mission.complete()
```

---

## 💡 Points Clés à Retenir

| Concept | Explication |
|---------|-------------|
| **ActifMobile** | Base commune de tous les véhicules |
| **Héritage** | ActifAerien/ActifMarin héritent d'ActifMobile |
| **Interfaces** | Contrats : Deplacable, Rechargeable, etc. |
| **SimulationService** | Moteur 60 FPS qui met à jour tout |
| **GestionnaireEssaim** | Conteneur de tous les actifs |
| **Missions** | Objectifs assignés aux actifs |
| **UI** | MapCanvas (vue du dessus) + SideViewCanvas (profil) |
| **État** | Chaque actif a un état (IDLE, MOVING, CHARGING...) |
| **Batterie** | Tous les actifs consomment et doivent recharger |

---

## 🚀 Flux Global Simplifié

```
UTILISATEUR
    ↓
UI (MainController, MapCanvas)
    ↓
crée/sélectionne ActifMobile
    ↓
GestionnaireEssaim stocke les actifs
    ↓
SimulationService (60 FPS) met à jour positions
    ↓
MapCanvas redessine
    ↓
UTILISATEUR voit le mouvement
```

---

## 📚 Fichiers à Consulter d'Abord

1. **Main.java** - Point d'entrée
2. **ActifMobile.java** - Classe fondamentale
3. **SimulationService.java** - Cœur physique
4. **GestionnaireEssaim.java** - Gestion flotte
5. **MainController.java** - Coordination UI
6. **MapCanvas.java** - Affichage

---

**Cette explication couvre la structure générale et les relations entre les fichiers. Avez-vous des questions sur une partie spécifique ?**
