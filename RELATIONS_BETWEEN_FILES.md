# 🔗 RELATIONS ENTRE FICHIERS - GUIDE VISUEL

## 📋 Index Complet des Fichiers et Leurs Relations

### **Groupe 1: POINT D'ENTRÉE**

```
Main.java
├── Lance l'application JavaFX
├── Charge: MainView.fxml
│   ├── Structure XML de l'interface
│   ├── Définit les panneaux (Map, SideView, Sidebar, etc.)
│   └── Charge les contrôleurs (MainController, SidebarController, MissionController)
└── Appelle: MainController.initialize()
```

---

### **Groupe 2: CONTRÔLEURS (UI LOGIC)**

```
MainController.java (Orchestrateur Principal)
├── Crée:
│   ├── GestionnaireEssaim() - Gestionnaire de flotte
│   ├── SimulationService() - Moteur physique
│   ├── ZoneOperation() - Limites du monde
│   ├── MapCanvas() - Affichage 2D vue du dessus
│   └── SideViewCanvas() - Affichage 2D profil
│
├── Appelle:
│   ├── SidebarController.setGestionnaire()
│   ├── MissionController.setGestionnaire()
│   └── simulationService.startSimulation()
│
├── Écoute:
│   ├── mapCanvas.setOnMapClicked() - Clic sur la carte
│   ├── mapCanvas.setOnSelectionChanged() - Changement sélection
│   └── sliderSpeed.valueProperty() - Vitesse simulation
│
└── Affiche:
    ├── actifs dans MapCanvas
    └── profils dans SideViewCanvas

    ↓

SidebarController.java
├── Gère: Affichage liste des actifs
├── Écoute: Clics sur les actifs
├── Appelle: GestionnaireEssaim pour get/remove actifs
└── Notifie: MainController

    ↓

MissionController.java
├── Gère: Affichage et création des missions
├── Crée: Missions (Surveillance, Logistique, Rescue)
├── Assigne: Missions à des actifs via GestionnaireEssaim
└── Affiche: Statut des missions
```

---

### **Groupe 3: CŒUR DE LA SIMULATION**

```
SimulationService.java (Moteur 60 FPS)
│
├── Contient:
│   ├── GestionnaireEssaim (référence)
│   ├── List<Obstacle> obstacles
│   ├── Weather weather
│   └── ZoneOperation zone
│
├── Boucle principale handle(long now):
│   └── Chaque 16.67 ms:
│       ├── Pour chaque ActifMobile:
│       │   ├── Appelle: actif.update(dt)
│       │   ├── Appelle: handleObstacleAvoidance()
│       │   └── Appelle: updateMissions()
│       │
│       ├── Mise à jour météo (tous les 30s)
│       │
│       └── Notifie: MainController pour redessiner
│
└── Appelle:
    └── ActifMobile.update(dt) ← Logique individuelle de chaque actif
```

---

### **Groupe 4: ENTITÉS (CLASSES ABSTRAITES ET CONCRÈTES)**

```
ActifMobile.java (Classe Abstraite - Base de Tous)
│   implements:
│   ├── Deplacable    (interface)
│   ├── Rechargeable  (interface)
│   ├── Communicable  (interface)
│   ├── Pilotable     (interface)
│   └── Alertable     (interface)
│
├── Contient:
│   ├── Attributs: x, y, z, vitesseMax, autonomieActuelle
│   ├── État: state (IDLE, MOVING, RECHARGING, etc.)
│   ├── Mission: currentMission
│   └── Vélocité: velocityX, velocityY, velocityZ
│
├── Méthodes clés:
│   ├── update(dt) - Mise à jour physique
│   ├── deplacer(x, y, z) - Fixe la cible
│   ├── moveTowards() - Déplacement vers cible
│   ├── updateBattery() - Consommation d'énergie
│   └── checkBatteryState() - Vérification batterie faible
│
├── HÉRITIERS DIRECTS:
│   ├── ActifAerien.java
│   │   ├── Contrainte: altitudeMin ≤ z ≤ altitudeMax
│   │   ├── HÉRITIERS:
│   │   │   ├── DroneAttaque.java
│   │   │   ├── DroneLogistique.java
│   │   │   ├── DroneReconnaissance.java
│   │   │   ├── HelicoptereSauvetage.java
│   │   │   └── GliderOceanographique.java
│   │   │
│   │   └── Chaque drone redéfinit:
│   │       ├── Paramètres vitesse/batterie
│   │       └── Type de mission spécifique
│   │
│   └── ActifMarin.java
│       ├── Contrainte: profondeurMin ≤ z ≤ profondeurMax
│       ├── HÉRITIERS DIRECTS:
│       │   ├── VehiculeSurface.java
│       │   │   ├── HÉRITIERS:
│       │   │   │   ├── NavirePatrouille.java
│       │   │   │   ├── NavireLogistique.java
│       │   │   │   └── NavireRecherche.java
│       │   │   └── Contrainte: z = 0 (surface)
│       │   │
│       │   └── VehiculeSousMarin.java
│       │       ├── HÉRITIERS:
│       │       │   ├── SousMarinAttaque.java
│       │       │   └── SousMarinExploration.java
│       │       └── Contrainte: z < 0 (profondeur)
│       │
│       └── Chaque navire/sous-marin redéfinit:
│           ├── Paramètres vitesse/batterie
│           └── Évitement obstacles spécifique
│
└── Relation avec:
    ├── Mission (assignée via assignMission())
    └── GestionnaireEssaim (stockée dans la liste)
```

---

### **Groupe 5: GESTION DE FLOTTE**

```
GestionnaireEssaim.java (Conteneur de Tous les Actifs)
│
├── Contient:
│   └── List<ActifMobile> flotte
│       ├── Drones (DroneLogistique, DroneAttaque, etc.)
│       ├── Navires (NavirePatrouille, NavireLogistique, etc.)
│       └── Sous-marins (SousMarinAttaque, SousMarinExploration, etc.)
│
├── Méthodes principales:
│   ├── ajouterActif(ActifMobile) - Ajoute à la flotte
│   ├── getFlotte() - Retourne tous les actifs
│   ├── getActifsDisponibles() - Filtre: état AU_SOL + batterie > 20%
│   ├── demarrerMission(Mission, List<ActifMobile>) - Lance une mission
│   └── updateMissions() - Met à jour statut missions
│
├── Utilisé par:
│   ├── SimulationService - Pour accéder à la flotte
│   ├── MainController - Pour ajouter/supprimer actifs
│   ├── SidebarController - Pour afficher liste
│   └── MissionController - Pour assigner missions
│
└── Relation:
    └── Chaque ActifMobile a une référence pour:
        ├── currentMission
        └── etat (AU_SOL, EN_MISSION, EN_PANNE, etc.)
```

---

### **Groupe 6: SYSTÈME DE MISSIONS**

```
Mission.java (Classe Abstraite)
│   enum MissionType: SURVEILLANCE, LOGISTICS, NAVIGATION, SEARCH_AND_RESCUE
│   enum StatutMission: PLANIFIEE, EN_COURS, TERMINEE, ECHOUEE, ANNULEE
│
├── Attributs:
│   ├── id, titre, type, statut
│   ├── targetX, targetY, targetZ (position de la mission)
│   ├── startTime, endTime
│   └── objectives, results
│
├── Méthodes:
│   ├── execute() - Logique spécifique (abstract)
│   ├── complete() - Terminer la mission
│   ├── fail() - Échouer la mission
│   └── getStatut() - Récupérer statut
│
├── Sous-classes:
│   ├── MissionSurveillanceMaritime.java
│   │   ├── Type: SURVEILLANCE
│   │   ├── Cible: Zone maritime à surveiller
│   │   └── Actifs: NavirePatrouille, DroneReconnaissance
│   │
│   ├── MissionLogistique.java
│   │   ├── Type: LOGISTICS
│   │   ├── Cible: Point de livraison
│   │   └── Actifs: DroneLogistique, NavireLogistique
│   │
│   ├── MissionRechercheEtSauvetage.java
│   │   ├── Type: SEARCH_AND_RESCUE
│   │   ├── Cible: Coordonnées du sinistre
│   │   └── Actifs: HelicoptereSauvetage, NavireRecherche
│   │
│   └── [Futures missions...]
│
├── Cycle de vie:
│   1. Créée (MissionController.java)
│   2. PLANIFIEE
│   3. Assignée à des actifs (GestionnaireEssaim.demarrerMission())
│   4. EN_COURS
│   5. Actifs naviguent vers targetX, targetY, targetZ
│   6. Actifs atteignent la destination → Mission.complete()
│   7. TERMINEE
│
└── Référence:
    └── Chaque ActifMobile a: currentMission
```

---

### **Groupe 7: ENVIRONNEMENT PHYSIQUE**

```
ZoneOperation.java (Limites du Monde)
├── Attributs:
│   ├── minX, maxX (limites horizontales)
│   └── minY, maxY (limites verticales)
├── Méthode: isInside(x, y) - Vérifier si point dans zone
└── Utilisé par:
    ├── SimulationService - Pour créer obstacles
    └── MainController - Pour initialiser la simulation

Obstacle.java (Îles, Récifs, Objets Fixes)
├── Attributs:
│   ├── x, y (position)
│   ├── z (profondeur/altitude)
│   └── radius (taille)
├── Vérification collision: distance(actif) < radius + seuil
└── Évité par:
    ├── Drones: Passent AU-DESSUS (z augmente)
    ├── Navires: Contournent AUTOUR (x, y changent)
    └── Sous-marins: Passent AU-DESSOUS (z diminue)

Weather.java (Conditions Météorologiques)
├── Attributs:
│   ├── windSpeed (vitesse vent)
│   ├── windDirection (direction)
│   └── rainIntensity (intensité pluie)
├── Mise à jour: Cycles aléatoires (tous les 30s)
└── Effet:
    ├── Vent: Ralentit les drones
    └── Pluie: Réduit visibilité et autonomie
```

---

### **Groupe 8: INTERFACES (CONTRATS COMPORTEMENTAUX)**

```
Deplacable.java (Interface)
├── Méthodes:
│   ├── deplacer(x, y, z)
│   └── calculerTrajet(x, y, z)
└── Implémentée par:
    └── ActifMobile

Rechargeable.java (Interface)
├── Méthodes:
│   ├── recharger()
│   ├── getAutonomieActuelle()
│   └── getAutonomieMax()
└── Implémentée par:
    └── ActifMobile

Communicable.java (Interface)
├── Méthodes:
│   ├── envoyerMessage(String)
│   └── recevoirOrdre(String)
└── Implémentée par:
    └── ActifMobile

Pilotable.java (Interface)
├── Méthodes:
│   ├── demarrer()
│   └── eteindre()
└── Implémentée par:
    └── ActifMobile

Alertable.java (Interface)
├── Méthodes:
│   ├── genererAlerte(String)
│   └── recevoirAlerte(String)
└── Implémentée par:
    └── ActifMobile
```

---

### **Groupe 9: INTERFACE UTILISATEUR (UI)**

```
MapCanvas.java (Affichage 2D - Vue du Dessus)
├── Contient:
│   ├── List<ActifMobile> selectedActifs
│   ├── ZoneOperation zone
│   └── Canvas canvas (JavaFX)
│
├── Méthodes clés:
│   ├── draw(List<ActifMobile>, List<Obstacle>)
│   │   └── Redessine tous les actifs et obstacles
│   ├── getActifAt(screenX, screenY)
│   │   └── Retourne actif au pixel (pour sélection)
│   ├── worldToScreenX/Y()
│   │   └── Convertit coords monde → écran
│   └── setOnMapClicked(callback)
│       └── Écoute clics pour déplacement
│
├── Affiche:
│   ├── Positions X, Y de tous les actifs
│   ├── Obstacles statiques
│   ├── Zone opérationnelle
│   └── Trajectoire cible (ligne pointillée)
│
└── Interactions:
    ├── Clic simple: Sélectionner 1 actif
    ├── Ctrl+Click: Sélection multiple
    └── Click sur actif: Affiche info

SideViewCanvas.java (Affichage 2D - Profil Vertical)
├── Affiche:
│   ├── Altitude/Profondeur (Z) en Y
│   ├── Position horizontale (X) en X
│   ├── Limite altitude/profondeur
│   └── Tous les actifs en profil
│
└── Aide à:
    ├── Visualiser les sous-marins en profondeur
    ├── Visualiser les drones en altitude
    └── Coordonner les mouvements 3D

SidebarController.java (Barre Latérale)
├── Affiche:
│   ├── Liste actifs (tous)
│   ├── État de chaque actif (batterie, position, état)
│   └── Boutons actions (delete, select, etc.)
│
├── Écoute:
│   ├── Clics sur les actifs
│   └── Boutons de contrôle
│
└── Appelle:
    ├── GestionnaireEssaim.remove()
    └── MainController.updateUI()

MissionController.java (Gestion Missions)
├── Affiche:
│   ├── Liste missions disponibles
│   ├── Statut missions en cours
│   └── Création nouvelle mission
│
├── Permet:
│   ├── Créer MissionLogistique
│   ├── Créer MissionSurveillance
│   ├── Créer MissionRescue
│   └── Assigner missions à des actifs
│
└── Appelle:
    ├── GestionnaireEssaim.demarrerMission()
    └── MainController.updateUI()
```

---

## 🔄 Flux d'Exécution Complet

```
APPLICATION DÉMARRE
    ↓
Main.start()
    ↓
Charge MainView.fxml
    ↓
Crée MainController (et ses sous-contrôleurs)
    ↓
MainController.initialize():
    ├─ Crée GestionnaireEssaim() [flotte vide]
    ├─ Crée SimulationService() [timer 60 FPS]
    ├─ Crée ZoneOperation() [limites monde]
    ├─ Crée MapCanvas() et SideViewCanvas()
    ├─ Crée SidebarController et MissionController
    └─ Appelle simulationService.startSimulation()
        ↓
    ┌─ SimulationService.handle() s'exécute 60x/sec (16.67 ms)
    │
    │  Pour chaque ActifMobile dans GestionnaireEssaim.flotte:
    │    ├─ actif.update(dt)
    │    │   ├─ moveTowards(targetX, targetY, targetZ, dt)
    │    │   │   ├─ Calcule direction vers cible
    │    │   │   ├─ Applique vitesse
    │    │   │   └─ Détecte arrivée
    │    │   ├─ updateBattery(dt)
    │    │   │   └─ Consomme batterie selon distance
    │    │   └─ checkBatteryState()
    │    │       └─ Si < 10% → Retour à la base
    │    └─ handleObstacleAvoidance()
    │        └─ Évite obstacles selon type d'actif
    │
    │  Mise à jour météo (tous les 30s)
    │    └─ weather.update()
    │
    │  MainController.updateUI():
    │    ├─ mapCanvas.draw(actifs, obstacles)
    │    └─ sideViewCanvas.draw(actifs)
    │
    └─ Redessine l'interface
        ↓
    UTILISATEUR VOIT L'ANIMATION
```

---

## 📊 Table de Correspondance: Fichier → Responsabilité

| Fichier | Package | Responsabilité |
|---------|---------|-----------------|
| Main.java | com.spiga | Lancement JavaFX |
| ActifMobile.java | core | Base tous les véhicules |
| ActifAerien.java | core | Drones/Hélicoptères |
| ActifMarin.java | core | Navires/Sous-marins |
| DroneLogistique.java | core | Drone pour livraison |
| NavirePatrouille.java | core | Navire surveillance |
| SousMarinAttaque.java | core | Sous-marin combat |
| SimulationService.java | core | Moteur 60 FPS |
| GestionnaireEssaim.java | management | Gestion flotte |
| Mission.java | management | Base missions |
| MissionLogistique.java | management | Mission transport |
| MissionSurveillance.java | management | Mission patrouille |
| ZoneOperation.java | environment | Limites monde |
| Obstacle.java | environment | Obstacles fixes |
| Weather.java | environment | Météo |
| MainController.java | ui | Orchestrateur UI |
| MapCanvas.java | ui | Affichage 2D |
| SideViewCanvas.java | ui | Affichage profil |
| SidebarController.java | ui | Barre latérale |
| MissionController.java | ui | Gestion missions UI |

---

## 🎯 Pour Trouver un Concept, Cherchez...

| Si vous cherchez... | Regardez le fichier... | Méthode... |
|-------------------|------------------------|-----------|
| Comment on crée un drone | DroneLogistique.java | Constructeur |
| Comment on ajoute à la flotte | GestionnaireEssaim.java | ajouterActif() |
| Comment on déplace un actif | ActifMobile.java | deplacer() |
| Comment on gère la batterie | ActifMobile.java | updateBattery() |
| Comment on crée une mission | Mission.java | Constructeur sous-classe |
| Comment on assigne une mission | GestionnaireEssaim.java | demarrerMission() |
| Comment on affiche la map | MapCanvas.java | draw() |
| Comment on détecte clic | MainController.java | handleMapClicked() |
| Comment on gère obstacles | SimulationService.java | handleObstacleAvoidance() |
| Comment on met à jour météo | Weather.java | update() |

---

**Ce guide devrait vous permettre de naviguer facilement dans le projet !**
