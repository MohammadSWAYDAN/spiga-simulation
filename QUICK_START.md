# ⚡ DÉMARRAGE RAPIDE (5 min)

## 🚀 Lancer l'Application

```powershell
# 1. Ouvrir terminal dans le dossier du projet
cd c:\Users\Mswaydan\Desktop\SPIGA-Java-Project

# 2. Compiler
mvnw clean compile

# 3. Exécuter
mvnw javafx:run
```

**Attendez 10-20 secondes...**

L'interface s'ouvre avec:
- Une grande zone map (vue du dessus)
- Une petite zone profil (altitude/profondeur)
- Une barre latérale
- Des boutons en haut

---

## 🎮 Ce Que Vous Pouvez Faire Maintenant

### 1. Créer un Drone
- Bouton: "Ajouter Drone Logistique" (ou similaire)
- Le drone apparaît sur la carte (petit carré bleu)

### 2. Déplacer le Drone
- Clic sur le drone → le sélectionner
- Clic sur la carte → le drone se déplace à ce point
- Attendre → le drone se déplace et... arrive

### 3. Voir la Batterie
- Barre latérale affiche batterie de chaque drone
- Elle diminue pendant le mouvement
- À 10% → drone retourne à la base (0, 0)
- À la base → recharge

### 4. Créer Plusieurs Véhicules
- Ajouter drone logistique
- Ajouter drone attaque
- Ajouter navire
- Ajouter sous-marin
- Les déplacer tous

### 5. Créer une Mission
- Bouton: "Créer Mission" (dans MissionPanel)
- Choisir type (Logistique, Surveillance, etc.)
- Choisir cible sur la carte
- Sélectionner drones
- Démarrer

---

## 📖 Comprendre la Structure (15 min)

Allez lire dans cet ordre:

1. **RESUME_UNE_PAGE.md** (5 min)
   - Comprendre l'essentiel

2. **QUICK_REFERENCE.md** (5 min)
   - Où trouver quoi

3. **FAQ.md** (5 min)
   - Répondre à vos questions

---

## 🔍 Explorer le Code (30 min)

### Fichier 1: Main.java
```
Chemin: src/main/java/com/spiga/Main.java
Lignes: ~15
Temps: 2 min

Qu'il fait: Lance l'app
À chercher: start() méthode
```

### Fichier 2: ActifMobile.java
```
Chemin: src/main/java/com/spiga/core/ActifMobile.java
Lignes: ~330
Temps: 15 min

Qu'il fait: Base de tous les véhicules
À chercher: 
  - update() → mise à jour physique
  - moveTowards() → déplacement
  - updateBattery() → consommation batterie
```

### Fichier 3: MainController.java
```
Chemin: src/main/java/com/spiga/ui/MainController.java
Lignes: ~487
Temps: 15 min

Qu'il fait: Orchestrateur de l'interface
À chercher:
  - initialize() → crée tout
  - handleMapClicked() → clic sur map
  - updateUI() → redessine
```

---

## 💡 Premier Exercice (15 min)

### Faire Marcher la Batterie Plus Rapidement

**Objectif**: Voir la batterie se décharger 2x plus vite

**Étapes**:
1. Ouvrir: `src/main/java/com/spiga/core/ActifMobile.java`
2. Chercher: `updateBattery()` méthode (Ctrl+F)
3. Trouver la ligne: `double consommation = distanceParcourue * 0.1;`
4. Changer `0.1` en `0.2`
5. Sauvegarder: Ctrl+S
6. Compiler: `mvnw clean compile`
7. Exécuter: `mvnw javafx:run`
8. Créer drone, le déplacer
9. La batterie diminue 2x plus vite! ✅

---

## 🎯 Comprendre le Flux (10 min)

### Flux: Clic sur la Carte

```
1. Vous cliquez sur la carte
   └─ Coordonnées: (500, 300)

2. MapCanvas détecte le clic
   └─ handleMapClicked(500, 300) appelée

3. MainController reçoit l'événement
   └─ Récupère le drone sélectionné

4. Appelle drone.deplacer(500, 300, 0)
   └─ Dans ActifMobile.java

5. ActifMobile fixe:
   ├─ targetX = 500
   ├─ targetY = 300
   ├─ state = MOVING_TO_TARGET
   └─ (retour de la fonction)

6. SimulationService boucle 60x/sec:
   ├─ Appelle drone.update(dt) toutes les 16.67ms
   ├─ moveTowards() rapproche le drone
   ├─ updateBattery() consomme batterie
   └─ MapCanvas redessine

7. Drone se déplace
   └─ Vous voyez le mouvement!

8. Drone arrive
   └─ state = IDLE
```

---

## 📊 Architecture en 30 Secondes

```
Utilisateur
    ↓
[Interface - JavaFX]
    ├─ MainController (orchestrateur)
    ├─ MapCanvas (affichage)
    └─ Boutons et panneaux
    ↓
[Logique - Core]
    ├─ ActifMobile (base des véhicules)
    ├─ Drones, Navires, Sous-marins
    └─ SimulationService (moteur 60 FPS)
    ↓
[Gestion - Management]
    ├─ GestionnaireEssaim (flotte)
    └─ Mission (objectifs)
    ↓
[Monde - Environment]
    ├─ ZoneOperation (limites)
    ├─ Obstacle (îles, récifs)
    └─ Weather (météo)
    ↓
[Affichage - UI]
    ├─ MapCanvas redessine
    └─ Vous voyez le résultat
```

---

## ✅ Checklist: Prêt?

```
[ ] Application lancée et fonctionne
[ ] Vous pouvez créer un drone
[ ] Vous pouvez le déplacer
[ ] La batterie diminue
[ ] Vous avez lu RESUME_UNE_PAGE.md
[ ] Vous comprenez le flux général
[ ] Vous avez ouvert ActifMobile.java et vu la structure
[ ] Vous avez modifié et recompilé (exercice batterie)
[ ] Vous pouvez répondre: "Qu'est-ce que ActifMobile?"
[ ] Vous savez où est SimulationService
```

---

## 🆘 Si Ça Ne Marche Pas

### Erreur: "Cannot find symbol"
```
Solution: 
1. Relancer: mvnw clean compile
2. Vérifier les imports en haut du fichier
3. Chercher la classe dans le projet (Ctrl+Shift+F)
```

### Erreur: "Compilation failed"
```
Solution:
1. Lire le message d'erreur
2. Aller à la ligne indiquée
3. Chercher la typo ou l'import manquant
4. Recompiler
```

### L'application ne se lance pas
```
Solution:
1. Java installé? → java -version
2. Maven OK? → mvnw -v
3. Port 8080 bloqué? → Redémarrer PC
4. Relancer: mvnw clean compile; mvnw javafx:run
```

---

## 🎓 Prochaine Étape

Une fois que tout marche:

1. **Lire ARCHITECTURE_OVERVIEW.md** (20 min)
2. **Lire CODE_READING_GUIDE.md** (30 min)
3. **Commencer à modifier le code** (1-2 heures)
4. **Créer un nouveau drone** (30 min)
5. **Créer une nouvelle mission** (30 min)

---

## 📚 Documents Importants

| Fichier | Quand Lire | Temps |
|---------|-----------|-------|
| **RESUME_UNE_PAGE.md** | Dès maintenant | 5 min |
| **QUICK_REFERENCE.md** | Dès maintenant | 5 min |
| **FAQ.md** | Si question | 10 min |
| **GETTING_STARTED.md** | Après lancement | 15 min |
| **ARCHITECTURE_OVERVIEW.md** | Après démarrage | 20 min |
| **CODE_READING_GUIDE.md** | Avant de coder | 30 min |

---

## 🚀 Commandes Utiles

```powershell
# Compiler
mvnw clean compile

# Exécuter
mvnw javafx:run

# Tests
mvnw test

# Build JAR
mvnw package

# Nettoyer
mvnw clean

# Tout depuis zéro
mvnw clean compile javafx:run
```

---

**Vous êtes prêt? Lancez l'app maintenant! 🚀**

```powershell
mvnw javafx:run
```
