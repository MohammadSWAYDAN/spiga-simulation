# 🎯 RÉSUMÉ EN UNE PAGE - SPIGA

## Qu'est-ce que SPIGA?
Une **simulation de flotte autonome** - Des drones, navires et sous-marins que vous contrôlez via une interface graphique.

---

## Les 3 Piliers

```
┌─────────────┐    ┌──────────────┐    ┌────────────┐
│   CORE      │    │  MANAGEMENT  │    │     UI     │
├─────────────┤    ├──────────────┤    ├────────────┤
│ Les actifs  │→   │   Flotte +   │ →  │ Affichage  │
│(véhicules)  │    │   Missions   │    │ Contrôle   │
└─────────────┘    └──────────────┘    └────────────┘
```

---

## Architecture Simple

```
Main.java (lance tout)
    ↓
MainController (orchestrateur)
    ├─ Crée GestionnaireEssaim (liste actifs)
    ├─ Crée SimulationService (boucle 60 FPS)
    ├─ Crée MapCanvas (affichage)
    └─ Écoute événements utilisateur
        ↓
SimulationService (chaque 16.67ms)
    ├─ Pour chaque ActifMobile:
    │  └─ Appelle update(dt)
    │     ├─ Déplace vers cible
    │     ├─ Consomme batterie
    │     └─ Gère retour à base
    └─ Redessine via MapCanvas
```

---

## Hiérarchie des Véhicules

```
ActifMobile (base abstraite)
├── ActifAerien (drones)
│   ├── DroneLogistique
│   ├── DroneAttaque
│   ├── DroneReconnaissance
│   └── ...
└── ActifMarin (navires/sous-marins)
    ├── VehiculeSurface (à la surface)
    │   ├── NavirePatrouille
    │   ├── NavireLogistique
    │   └── ...
    └── VehiculeSousMarin (en profondeur)
        ├── SousMarinAttaque
        └── SousMarinExploration
```

---

## Fichiers CLÉS

| Fichier | Rôle | Lignes |
|---------|------|--------|
| **Main.java** | Lance l'app | 15 |
| **ActifMobile.java** | Base de tous les véhicules | 330 |
| **SimulationService.java** | Moteur 60 FPS | 246 |
| **GestionnaireEssaim.java** | Gestion flotte | 70 |
| **MainController.java** | Orchestrateur UI | 487 |
| **MapCanvas.java** | Affichage 2D | 300 |

---

## Concepts Clés

### Classe Abstraite
```java
abstract class ActifMobile {
    // Ne pas créer: new ActifMobile() ❌
    // Uniquement créer enfants: new DroneLogistique() ✅
}
```

### Héritage
```java
DroneLogistique extends ActifAerien // Récupère du code
  extends ActifMobile // Récupère du code
    implements Deplacable // Respecte contrat
```

### Polymorphisme
```java
for (ActifMobile actif : flotte) {
    actif.update(dt); // Chaque type a sa logique propre
}
```

### État
```java
enum AssetState {
    IDLE,                 // Au repos
    MOVING_TO_TARGET,     // Se déplace
    RETURNING_TO_BASE,    // Batterie faible
    RECHARGING           // À la base
}
```

---

## Flux d'une Action

### Utilisateur Clique sur la Carte

```
1. MapCanvas détecte clic
2. Appelle handleMapClicked()
3. MainController récupère actif sélectionné
4. Appelle actif.deplacer(x, y, z)
5. ActifMobile fixe targetX, targetY, targetZ
6. Change state → MOVING_TO_TARGET

Chaque 16.67ms (SimulationService):
7. Appelle actif.update(dt)
8. moveTowards() rapproche de la cible
9. Position changée (x, y, z)
10. Batterie consommée
11. MapCanvas redessine
12. Utilisateur voit le mouvement
```

---

## États et Transitions

```
IDLE ←─────────────────────────────┐
│                                   │
│ deplacer() appelé                 │
↓                                   │
MOVING_TO_TARGET                    │
│                                   │
│ Arrivé à destination             │
↓                                   │
IDLE (ou EXECUTING_MISSION)        │
│                                   │
│ Batterie < 10%                   │
↓                                   │
RETURNING_TO_BASE                  │
│                                   │
│ Arrivé à base                    │
↓                                   │
RECHARGING                          │
│                                   │
│ Batterie chargée                 │
└──────────────────────────────────┘
```

---

## Interfaces (Contrats)

Chaque ActifMobile implémente:
```
Deplacable       → deplacer()
Rechargeable     → recharger(), getAutonomie()
Pilotable        → demarrer(), eteindre()
Communicable     → envoyerMessage(), recevoirOrdre()
Alertable        → genererAlerte()
```

---

## Packages

```
com.spiga/
├─ core/
│  ├─ ActifMobile (base)
│  ├─ Drones + Navires + Sous-marins (implémentations)
│  ├─ Interfaces (contrats)
│  └─ SimulationService (moteur)
│
├─ management/
│  ├─ GestionnaireEssaim (flotte)
│  └─ Mission + ses variantes
│
├─ environment/
│  ├─ ZoneOperation (limites monde)
│  ├─ Obstacle (îles, récifs)
│  └─ Weather (météo)
│
└─ ui/
   ├─ MainController (orchestration)
   ├─ MapCanvas (affichage)
   ├─ SidebarController (liste actifs)
   ├─ MissionController (gestion missions)
   └─ *.fxml (layouts)
```

---

## Comment Lire le Code

1. **Skim Reading** : Lire titres et commentaires
2. **Cherry Picking** : Chercher la méthode qui intéresse
3. **Triangulation** : Chercher où elle est DÉFINIE, APPELÉE, UTILISÉE
4. **Backward Tracing** : D'où vient cette variable? Qui la crée?

---

## Exercices pour Comprendre

```
1. Tracer une exécution mentalement:
   Utilisateur clique → Code exécuté → Résultat

2. Modifier la vitesse:
   Fichier: ActifMobile.java
   Ligne: moveTowards() méthode
   Changer: vitesseMax * 2

3. Ajouter un log:
   Fichier: ActifMobile.java
   Méthode: checkBatteryState()
   Ajouter: System.out.println(...)

4. Créer un nouveau drone:
   Copier: DroneLogistique.java
   Renommer: MonDrone.java
   Modifier: paramètres

5. Créer une nouvelle mission:
   Copier: MissionLogistique.java
   Renommer: MaMission.java
   Modifier: logique
```

---

## Documents à Lire

| # | Document | Temps | Niveau |
|---|----------|-------|--------|
| 1 | GETTING_STARTED.md | 15m | Débutant |
| 2 | ARCHITECTURE_OVERVIEW.md | 20m | Débutant |
| 3 | QUICK_REFERENCE.md | 10m | Débutant |
| 4 | CODE_READING_GUIDE.md | 30m | Intermédiaire |
| 5 | RELATIONS_BETWEEN_FILES.md | 25m | Intermédiaire |
| 6 | UML_DIAGRAM.md | 15m | Intermédiaire |
| 7 | PLAN_DE_LECTURE.md | 10m | Navigation |

---

## Commandes Essentielles

```powershell
# Compiler
mvnw clean compile

# Exécuter
mvnw javafx:run

# Tests
mvnw test

# Build
mvnw package
```

---

## Checklist: Comprenez-Vous?

```
[ ] Le flux: Utilisateur → UI → Core → Affichage
[ ] ActifMobile est la base de tous les véhicules
[ ] SimulationService s'exécute 60x par seconde
[ ] GestionnaireEssaim contient la liste des actifs
[ ] MainController coordonne tout
[ ] MapCanvas affiche la position des véhicules
[ ] Chaque véhicule a un état (IDLE, MOVING, etc.)
[ ] La batterie se consomme lors du mouvement
[ ] Les drones retournent à la base si batterie faible
[ ] Les missions sont assignées à des actifs
```

---

## Prochaines Étapes

1. **Lire les documents** (1-2 heures)
2. **Lire le code principal** (2-3 heures)
3. **Tracer une exécution** (30 min)
4. **Faire les exercices** (1 heure)
5. **Développer une nouvelle feature** (2-3 heures)

---

## Points Clés à Retenir

🎯 **Architecture** = 3 packages (core, management, ui)
🎯 **Hiérarchie** = ActifMobile → Aérien/Marin → Implémentations
🎯 **Flux** = Utilisateur → MainController → Actifs → Affichage
🎯 **Moteur** = SimulationService met à jour 60 fois par seconde
🎯 **État** = Chaque actif a un state (IDLE, MOVING, RECHARGING, etc.)
🎯 **Batterie** = Se consomme durant mouvement, recharge à la base
🎯 **Missions** = Assignées à des actifs, avec statut et target
🎯 **Interface** = MapCanvas + SideViewCanvas pour visualisation

---

**Le reste, c'est du détail ! 🚀**
