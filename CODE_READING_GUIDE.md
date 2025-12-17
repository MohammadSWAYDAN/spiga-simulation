# 🔍 GUIDE DÉTAILLÉ DE LECTURE DU CODE SPIGA

## 📖 Où Commencer (Ordre de Lecture Recommandé)

### **Phase 1 : Comprendre la Structure (15 min)**

1. **Main.java** → Point d'entrée, lance JavaFX
2. **SPIGA-SPEC.txt** → Cahier des charges du projet
3. **README.md** → Features et fonctionnalités
4. **ARCHITECTURE_OVERVIEW.md** → (Le document qu'on vient de créer)

### **Phase 2 : Comprendre les Entités (30 min)**

1. **ActifMobile.java** → Classe de base abstraite
2. **ActifAerien.java** → Spécialisation pour drones
3. **ActifMarin.java** → Spécialisation pour navires
4. **DroneLogistique.java** → Implémentation concrète
5. **NavirePatrouille.java** → Implémentation concrète

### **Phase 3 : Comprendre le Moteur (30 min)**

1. **SimulationService.java** → Boucle principale 60 FPS
2. **GestionnaireEssaim.java** → Gestion de la flotte
3. **Mission.java** → Système de missions

### **Phase 4 : Comprendre l'UI (30 min)**

1. **MainController.java** → Contrôleur principal
2. **MapCanvas.java** → Affichage 2D
3. **SideViewCanvas.java** → Affichage 3D profil
4. **MainView.fxml** → Layout XML

---

## 🎯 Comprendre le Cycle de Vie d'un Actif

### **Étape 1 : Création**

```java
// Fichier: src/main/java/com/spiga/core/DroneLogistique.java
public class DroneLogistique extends ActifAerien {
    public DroneLogistique(String id, double x, double y, double z, double vitesseMax, double autonomieMax) {
        super(id, x, y, z, vitesseMax, autonomieMax);
        // Initialisation spécifique au drone logistique
    }
}

// Création dans MainController.java
DroneLogistique drone = new DroneLogistique(
    "DRONE-001",
    100,    // x
    100,    // y
    1000,   // altitude z
    200,    // vitesseMax (m/s)
    5000    // autonomieMax (secondes)
);
```

### **Étape 2 : Ajout à la Flotte**

```java
// Fichier: src/main/java/com/spiga/management/GestionnaireEssaim.java
public void ajouterActif(ActifMobile actif) {
    flotte.add(actif);  // Liste interne de tous les actifs
    System.out.println("✓ Actif ajouté: " + actif.getId());
}

// Utilisation dans MainController.initialize()
gestionnaire.ajouterActif(drone);
```

### **Étape 3 : Simulation (60 FPS)**

```java
// Fichier: src/main/java/com/spiga/core/SimulationService.java
@Override
public void handle(long now) {
    // ...calculs de temps...
    
    while (accumulator >= FRAME_TIME) {
        updateSimulation(FRAME_TIME);
        accumulator -= FRAME_TIME;
    }
}

private void updateSimulation(double dt) {
    // ===== PHASE 1 : UPDATE TOUS LES ACTIFS =====
    for (ActifMobile actif : gestionnaire.getFlotte()) {
        actif.update(dt);  // ← Appel du code d'ActifMobile
    }
    
    // ===== PHASE 2 : GESTION MISSIONS =====
    gestionnaire.updateMissions();
    
    // ===== PHASE 3 : COLLISION ET OBSTACLES =====
    handleObstacleAvoidance();
}
```

### **Étape 4 : Update Interne d'un Actif**

```java
// Fichier: src/main/java/com/spiga/core/ActifMobile.java
public void update(double dt) {
    // Vérifier l'état
    if (state == AssetState.MOVING_TO_TARGET || state == AssetState.EXECUTING_MISSION
            || state == AssetState.RETURNING_TO_BASE) {
        
        // 1. MOUVEMENT
        moveTowards(targetX, targetY, targetZ, dt);
        
        // 2. CONSOMMATION BATTERIE
        updateBattery(dt);
    }
    
    // 3. VÉRIFICATION BATTERIE
    checkBatteryState();
}

private void moveTowards(double targetX, double targetY, double targetZ, double dt) {
    // Calcule la distance jusqu'à la cible
    double dx = targetX - x;
    double dy = targetY - y;
    double dz = targetZ - z;
    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
    
    // Si proche de la destination (< 1m)
    if (distance < 1.0) {
        x = targetX;
        y = targetY;
        z = targetZ;
        velocityX = velocityY = velocityZ = 0;
        
        // Gestion de l'arrivée
        if (state == AssetState.RETURNING_TO_BASE) {
            state = AssetState.RECHARGING;
            recharger();
        } else if (state == AssetState.EXECUTING_MISSION && currentMission != null) {
            currentMission.complete();
            state = AssetState.IDLE;
        } else {
            state = AssetState.IDLE;
        }
    } else {
        // Sinon, se déplacer vers la cible
        double dirX = dx / distance;
        double dirY = dy / distance;
        double dirZ = dz / distance;
        
        velocityX = dirX * vitesseMax;
        velocityY = dirY * vitesseMax;
        velocityZ = dirZ * vitesseMax;
        
        // Appliquer le mouvement
        x += velocityX * dt;
        y += velocityY * dt;
        z += velocityZ * dt;
    }
}

private void updateBattery(double dt) {
    // Consommation basée sur : distance parcourue × consommation
    double distanceParcourue = Math.sqrt(velocityX*velocityX + velocityY*velocityY + velocityZ*velocityZ) * dt;
    double consommation = distanceParcourue * 0.1;  // 0.1 par mètre
    
    autonomieActuelle -= consommation;
    if (autonomieActuelle < 0) autonomieActuelle = 0;
}

private void checkBatteryState() {
    // Si batterie très faible, retour automatique à la base
    if (autonomieActuelle < autonomieMax * 0.1) {  // < 10%
        if (state != AssetState.RECHARGING && state != AssetState.RETURNING_TO_BASE) {
            state = AssetState.RETURNING_TO_BASE;
            // ← Définir la cible comme la base (0, 0, 0)
            targetX = 0;
            targetY = 0;
            targetZ = 0;
        }
    }
}
```

### **Étape 5 : Affichage (UI Update)**

```java
// Fichier: src/main/java/com/spiga/ui/MainController.java
private void updateUI() {
    // Récupère tous les actifs et les envoie au canvas
    mapCanvas.draw(
        gestionnaire.getFlotte(),      // Liste de tous les ActifMobile
        simulationService.getObstacles() // Obstacles
    );
    
    sideViewCanvas.draw(
        gestionnaire.getFlotte()
    );
}

// Fichier: src/main/java/com/spiga/ui/MapCanvas.java
public void draw(List<ActifMobile> actifs, List<Obstacle> obstacles) {
    GraphicsContext gc = canvas.getGraphicsContext2D();
    
    // Effacer l'écran
    gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    
    // Dessiner obstacles
    for (Obstacle obs : obstacles) {
        drawObstacle(gc, obs);
    }
    
    // Dessiner actifs
    for (ActifMobile actif : actifs) {
        drawActif(gc, actif);
    }
}

private void drawActif(GraphicsContext gc, ActifMobile actif) {
    // Convertir coords monde → coords écran
    double screenX = worldToScreenX(actif.getX());
    double screenY = worldToScreenY(actif.getY());
    
    // Couleur selon type
    if (actif instanceof DroneLogistique) {
        gc.setFill(Color.BLUE);
    } else if (actif instanceof NavirePatrouille) {
        gc.setFill(Color.GREEN);
    }
    
    // Dessiner le point
    gc.fillOval(screenX - 5, screenY - 5, 10, 10);
    
    // Afficher l'ID
    gc.fillText(actif.getId(), screenX + 10, screenY);
}
```

---

## 🎮 Interaction Utilisateur : Cliquer et Déplacer

### **Étape 1 : Utilisateur Clique sur la Carte**

```java
// Fichier: src/main/java/com/spiga/ui/MainController.java

// Dans initialize()
mapCanvas.setOnMapClicked(this::handleMapClicked);

// Quand clic détecté
private void handleMapClicked(double screenX, double screenY) {
    // 1. Convertir coords écran → coords monde
    double worldX = screenToWorldX(screenX);
    double worldY = screenToWorldY(screenY);
    
    // 2. Récupérer l'actif sélectionné
    ActifMobile selectedActif = mapCanvas.getSelectedActif();
    
    if (selectedActif == null) {
        System.out.println("Aucun actif sélectionné!");
        return;
    }
    
    // 3. Demander l'altitude/profondeur à l'utilisateur
    Optional<Double> result = showAltitudeDialog();
    if (result.isPresent()) {
        double z = result.get();
        
        // 4. Appeler la méthode de déplacement
        selectedActif.deplacer(worldX, worldY, z);
        
        // 5. Log
        System.out.println("Déplacement vers: " + worldX + ", " + worldY + ", " + z);
    }
}
```

### **Étape 2 : Déplacement (Interface Deplacable)**

```java
// Fichier: src/main/java/com/spiga/core/ActifAerien.java
@Override
public void deplacer(double targetX, double targetY, double targetZ) {
    // 1. Vérifier limite d'altitude
    if (targetZ < altitudeMin) targetZ = altitudeMin;
    if (targetZ > altitudeMax) targetZ = altitudeMax;
    
    // 2. Appeler la méthode parente
    super.deplacer(targetX, targetY, targetZ);
}

// Fichier: src/main/java/com/spiga/core/ActifMobile.java
@Override
public void deplacer(double targetX, double targetY, double targetZ) {
    this.targetX = targetX;
    this.targetY = targetY;
    this.targetZ = targetZ;
    
    // Changer l'état
    if (autonomieActuelle > autonomieMax * 0.2) {
        state = AssetState.MOVING_TO_TARGET;
    } else {
        System.out.println("⚠️ Batterie insuffisante! Retour à la base.");
        state = AssetState.RETURNING_TO_BASE;
        this.targetX = 0;
        this.targetY = 0;
        this.targetZ = 0;
    }
}
```

---

## 🎯 Systèmes Importants

### **Système de Batterie**

```java
// Fichier: src/main/java/com/spiga/core/ActifMobile.java

// Interface Rechargeable
public interface Rechargeable {
    void recharger();
    double getAutonomieActuelle();
    double getAutonomieMax();
}

// Implémentation dans ActifMobile
private void updateBattery(double dt) {
    // Consommation = distance parcourue × taux consommation
    double distanceParcourue = Math.sqrt(
        velocityX*velocityX + velocityY*velocityY + velocityZ*velocityZ
    ) * dt;
    
    double consommation = distanceParcourue * 0.1;
    autonomieActuelle -= consommation;
    
    if (autonomieActuelle < 0) {
        autonomieActuelle = 0;
        state = AssetState.STOPPED;  // Immobilisé !
    }
}

public void recharger() {
    autonomieActuelle += 10 * timeScale;  // +10 par frame
    if (autonomieActuelle > autonomieMax) {
        autonomieActuelle = autonomieMax;
        state = AssetState.IDLE;  // Recharge terminée
    }
}
```

### **Système de Missions**

```java
// Fichier: src/main/java/com/spiga/management/Mission.java

public abstract class Mission {
    public enum MissionType {
        SURVEILLANCE, LOGISTICS, NAVIGATION, SEARCH_AND_RESCUE
    }
    
    public enum StatutMission {
        PLANIFIEE, EN_COURS, TERMINEE, ECHOUEE, ANNULEE
    }
    
    protected String id;
    protected String titre;
    protected MissionType type;
    protected StatutMission statut;
    protected double targetX, targetY, targetZ;
    
    public abstract void execute();
}

// Sous-classe concrète
public class MissionLogistique extends Mission {
    public MissionLogistique(String titre, double targetX, double targetY) {
        super(titre, MissionType.LOGISTICS);
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = 0;  // Navires à la surface
    }
    
    @Override
    public void execute() {
        System.out.println("📦 Transport logistique vers " + targetX + ", " + targetY);
    }
}

// Utilisation
Mission m = new MissionLogistique("Livraison port", 800, 800);
List<ActifMobile> team = gestionnaire.getActifsDisponibles();
gestionnaire.demarrerMission(m, team);
```

### **Système de Sélection Multiple**

```java
// Fichier: src/main/java/com/spiga/ui/MapCanvas.java

private List<ActifMobile> selectedActifs = new ArrayList<>();

public void setOnSelectionChanged(Consumer<List<ActifMobile>> callback) {
    setOnMouseClicked(event -> {
        ActifMobile clicked = getActifAt(event.getX(), event.getY());
        
        if (clicked != null) {
            if (event.isControlDown()) {
                // Ctrl+Click = sélection multiple
                if (selectedActifs.contains(clicked)) {
                    selectedActifs.remove(clicked);
                } else {
                    selectedActifs.add(clicked);
                }
            } else {
                // Click simple = sélection unique
                selectedActifs.clear();
                selectedActifs.add(clicked);
            }
        }
        
        callback.accept(selectedActifs);
    });
}
```

---

## 📊 Diagramme de Flux : Une Boucle de Simulation

```
┌─────────────────────────────────────────────────────────────┐
│         BOUCLE PRINCIPALE (SimulationService)               │
│         Exécutée 60 fois par seconde (16.67 ms)             │
└─────────────────────────────────────────────────────────────┘
                           ↓
          ┌────────────────────────────────────┐
          │ 1. Pour chaque ActifMobile:        │
          │    actif.update(dt)                │
          └────────────────────────────────────┘
                           ↓
          ┌────────────────────────────────────┐
          │ 2. Dans ActifMobile.update():      │
          │    - moveTowards(targetX, Y, Z)    │
          │    - updateBattery(dt)             │
          │    - checkBatteryState()           │
          └────────────────────────────────────┘
                           ↓
          ┌────────────────────────────────────┐
          │ 3. Déplacement calculé:            │
          │    x += velocityX * dt             │
          │    y += velocityY * dt             │
          │    z += velocityZ * dt             │
          └────────────────────────────────────┘
                           ↓
          ┌────────────────────────────────────┐
          │ 4. Mise à jour UI:                 │
          │    MapCanvas.draw(actifs)          │
          │    SideViewCanvas.draw(actifs)     │
          └────────────────────────────────────┘
                           ↓
          ┌────────────────────────────────────┐
          │ Utilisateur voit la simulation!    │
          └────────────────────────────────────┘
```

---

## 💡 Tips pour Comprendre le Code

### **1. Trace un Actif**
Suivez mentalement un actif à travers les fichiers :
- DroneLogistique.java (implémentation concrète)
- ↓ ActifAerien.java (déplacement aérien)
- ↓ ActifMobile.java (logique commune)
- ↓ Interfaces (Deplacable, Rechargeable, etc.)

### **2. Cherche les Mots-Clés**
- `abstract` → Classe de base (ne pas instancier directement)
- `interface` → Contrat (garantit que les méthodes existent)
- `extends` → Héritage (réutilise le code du parent)
- `implements` → Utilisation d'interface
- `@Override` → Redéfinition d'une méthode

### **3. Comprends les États**
Chaque actif a un état (`AssetState`) :
- `IDLE` = au repos
- `MOVING_TO_TARGET` = se déplace
- `EXECUTING_MISSION` = en mission
- `RETURNING_TO_BASE` = batterie faible, retour
- `RECHARGING` = à la base, recharge

### **4. Cherche les Logs (println)**
Les messages `System.out.println()` aident à déboguer

---

## 🚀 Exercices de Compréhension

### **Exercice 1 : Créer un Nouveau Drone**
Tâche : Créez `DroneDelivery` qui hérite d'`ActifAerien`
Fichier à créer : `src/main/java/com/spiga/core/DroneDelivery.java`

### **Exercice 2 : Ajouter un Logs**
Tâche : Dans `MainController.handleMapClicked()`, ajoutez un log qui affiche les coordonnées du clic
Fichier à modifier : `MainController.java`

### **Exercice 3 : Modifier la Consommation**
Tâche : Changez le taux de consommation batterie
Fichier à modifier : `ActifMobile.java` → `updateBattery()`

---

**Vous pouvez maintenant lire le code avec une meilleure compréhension !**
