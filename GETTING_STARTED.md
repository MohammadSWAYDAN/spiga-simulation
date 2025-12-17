# 🚀 GUIDE DE DÉMARRAGE - ENTRER DANS LE PROJET

## 📖 Étape 1: Préparation Mentale (5 min)

Avant de lire du code, comprenez ceci :

**SPIGA** simule une flotte autonome.
- Des drones volent
- Des navires naviguent
- Des sous-marins plongent
- Ils exécutent des missions
- Une interface les affiche et les contrôle

C'est comme un jeu vidéo de stratégie, mais sans joueur - les véhicules décident seuls.

---

## 🎯 Étape 2: Architecture Générale (10 min)

### Les 3 Piliers

```
1. CORE (core/) - LES ACTIFS
   └─ Tous les véhicules et leur logique
   └─ Base: ActifMobile
   └─ Spécialisations: ActifAerien, ActifMarin
   └─ Implémentations: Drones, Navires, Sous-marins

2. MANAGEMENT (management/) - L'ORGANISATION  
   └─ GestionnaireEssaim = Conteneur de tous les actifs
   └─ Mission = Objectifs assignés aux actifs

3. UI (ui/) - L'AFFICHAGE
   └─ MainController = Orchestrateur
   └─ MapCanvas = Vue du dessus
   └─ SideViewCanvas = Profil vertical
```

### Le Flux

```
[Utilisateur] 
    ↓
[Clic sur Map ou Bouton]
    ↓
[MainController capture événement]
    ↓
[Appelle GestionnaireEssaim pour modifier l'état]
    ↓
[SimulationService (60 FPS) met à jour positions]
    ↓
[MapCanvas redessine]
    ↓
[Utilisateur voit le mouvement]
```

---

## 📝 Étape 3: Lire dans Cet Ordre

### JOUR 1 : Vue d'Ensemble (1-2 heures)

```
1. Ce fichier (vous êtes ici!)
2. ARCHITECTURE_OVERVIEW.md
   - Quoi, où, pourquoi
3. QUICK_REFERENCE.md
   - TL;DR et navigation

À ce stade: Vous comprenez le QUOI et le POURQUOI
```

### JOUR 2 : Structure du Code (2-3 heures)

```
1. CODE_READING_GUIDE.md
   - Comment lire le code
2. RELATIONS_BETWEEN_FILES.md
   - Relations entre fichiers
3. UML_DIAGRAM.md
   - Structure visuelle

À ce stade: Vous comprenez le COMMENT
```

### JOUR 3 : Lire du Vrai Code (2-3 heures)

```
1. Main.java
   - Entry point simple

2. ActifMobile.java (CORE)
   - Classe fondamentale
   - ~330 lignes
   - IMPORTANTE

3. SimulationService.java (CORE)
   - Boucle physique
   - ~246 lignes
   - IMPORTANTE

4. GestionnaireEssaim.java (MANAGEMENT)
   - Gestion flotte
   - ~70 lignes
   - Facile

5. MainController.java (UI)
   - Orchestrateur
   - ~487 lignes
   - Complexe mais crucial

À ce stade: Vous comprenez le CODE
```

---

## 💡 Étape 4: Concepts Clés à Comprendre

### Concept 1: Classe Abstraite vs Classe Concrète

```java
// ABSTRAITE (ne pas créer d'instance)
public abstract class ActifMobile {
    public abstract void execute(); // À implémenter
}

// CONCRÈTE (créer une instance)
public class DroneLogistique extends ActifMobile {
    @Override
    public void execute() {
        // Implémentation spécifique
    }
}

// Utilisation
// ActifMobile a = new ActifMobile(); ❌ ERREUR
DroneLogistique d = new DroneLogistique("D1", ...); ✅ OK
```

### Concept 2: Interface vs Classe Abstraite

```java
// INTERFACE = Contrat pur (QUE FAIRE?)
public interface Deplacable {
    void deplacer(double x, double y, double z);
}

// CLASSE ABSTRAITE = Contrat + code (COMMENT FAIRE?)
public abstract class ActifMobile implements Deplacable {
    public void deplacer(double x, double y, double z) {
        // Code réutilisable
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
    }
}
```

### Concept 3: Héritage en Chaîne

```java
// Hiérarchie
ActifMobile (base abstraite)
    ↓ extends
ActifAerien (spécialisation abstraite)
    ↓ extends
DroneLogistique (implémentation concrète)

// Propriétés héritées
DroneLogistique d = new DroneLogistique(...);
d.update(dt);        // De ActifMobile
d.deplacer(x, y, z); // De ActifMobile (et des interfaces)
d.getAltitudeMax();  // De ActifAerien
```

### Concept 4: Polymorphisme

```java
// Même code, différents résultats selon le type
List<ActifMobile> flotte = new ArrayList<>();
flotte.add(new DroneLogistique("D1", ...));
flotte.add(new NavirePatrouille("N1", ...));
flotte.add(new SousMarinAttaque("S1", ...));

// Boucle unique
for (ActifMobile actif : flotte) {
    actif.update(dt); // Chaque type a sa logique propre
    // Le drone monte en altitude si collision
    // Le navire contourne l'obstacle
    // Le sous-marin plonge sous l'obstacle
}
```

### Concept 5: État et Transition

```java
// Chaque actif a un état
enum AssetState {
    IDLE,                // Au repos
    MOVING_TO_TARGET,    // Se déplace
    RETURNING_TO_BASE,   // Batterie faible
    RECHARGING           // À la base
}

// L'état change selon les conditions
if (autonomieActuelle < autonomieMax * 0.1) {
    state = AssetState.RETURNING_TO_BASE; // Transition!
}
```

---

## 🎮 Étape 5: Essayer Vous-Même

### Exercice 1: Tracer une Exécution

Ouvrez `SimulationService.java` et tracez mentalement une frame:

```
SimulationService.handle() est appelée
    ↓
dt calculé depuis la dernière frame
    ↓
Pour chaque ActifMobile:
    └─ actif.update(dt)
        └─ if (state == MOVING_TO_TARGET)
            └─ moveTowards(targetX, targetY, targetZ, dt)
                ├─ distance = calcule distance à la cible
                ├─ if (distance < 1.0)
                │   └─ arrivé! state = IDLE
                └─ else
                    └─ x += velocityX * dt
                       y += velocityY * dt
                       z += velocityZ * dt
    └─ updateBattery(dt)
        └─ autonomieActuelle -= distanceParcourue * 0.1
    └─ checkBatteryState()
        └─ if (autonomieActuelle < 10%)
            └─ state = RETURNING_TO_BASE
    
MapCanvas redessine tous les actifs à leurs nouvelles positions

FRAME TERMINÉE - Prochaine frame dans 16.67 ms
```

### Exercice 2: Ajouter un Log

1. Ouvrir `ActifMobile.java`
2. Trouver méthode `checkBatteryState()`
3. Ajouter après ligne `if (autonomieActuelle < autonomieMax * 0.1)`:
```java
System.out.println("⚠️ " + id + " batterie faible: " + 
    autonomieActuelle + "/" + autonomieMax);
```
4. Compiler et exécuter
5. Créer un drone et attendre qu'il tourne la batterie faible
6. Voir le log dans la console

### Exercice 3: Modifier la Vitesse

1. Ouvrir `ActifMobile.java`
2. Trouver `moveTowards()` méthode
3. Trouver cette ligne:
```java
velocityX = dirX * vitesseMax;
```
4. Changer `vitesseMax` en `vitesseMax * 2` pour doubler la vitesse
5. Recompiler et tester

### Exercice 4: Ajouter un Nouveau Type de Drone

1. Copier `DroneLogistique.java`
2. Renommer en `DroneEspion.java`
3. Changer le nom de la classe
4. Changer les paramètres (vitesse plus élevée, batterie moins importante)
5. Sauvegarder
6. Ouvrir `MainController.java`
7. Chercher où on crée les drones
8. Ajouter option pour créer DroneEspion
9. Tester!

---

## 🔍 Étape 6: Techniques de Lecture

### Technique 1: Skim Reading (Lecture Rapide)

```
Ne pas lire chaque ligne!

1. Lire le nom de la classe
2. Lire les commentaires du haut (Javadoc)
3. Lire les noms des attributs (what it has)
4. Lire les noms des méthodes (what it does)
5. Ne lire le contenu que si vous avez une question
```

### Technique 2: Cherry Picking

```
Ne pas lire fichier complet!

1. Chercher la méthode qui vous intéresse (Ctrl+F)
2. Lire uniquement celle-là
3. Suivre ses appels si nécessaire
```

### Technique 3: Triangulation

```
Quand vous ne comprenez pas une méthode:

1. Cherchez où elle est DÉFINIE
2. Cherchez où elle est APPELÉE
3. Cherchez comment son RETOUR est UTILISÉ

Exemple: Cherchez "getFlotte()"
- DÉFINIE dans: GestionnaireEssaim.java
- APPELÉE dans: SimulationService, MainController
- RETOUR UTILISÉ COMME: List<ActifMobile> pour boucler
```

### Technique 4: Backward Tracing

```
Quand vous voyez quelque chose:
1. D'où vient cette variable?
2. Qui la crée?
3. Qui l'appelle?

Exemple: Voir x += velocityX * dt
- velocityX vient de: calculateVelocity()
- calculateVelocity() est appelée de: moveTowards()
- moveTowards() est appelée de: update()
- update() est appelée de: SimulationService.handle()
```

---

## 📚 Étape 7: Ressources d'Apprentissage

### Concepts OOP à Connaître

```
✓ Classe et Objet
✓ Héritage (extends)
✓ Interface (implements)
✓ Classe Abstraite (abstract)
✓ Polymorphisme (@Override)
✓ Encapsulation (private, protected, public)
✓ Composition (HAS-A vs IS-A)
✓ Énumération (enum)
✓ Generics (List<Type>)
✓ Lambda (event -> handler())
```

### Outils JavaFX à Connaître

```
✓ Stage (window)
✓ Scene (content)
✓ Canvas (drawing surface)
✓ GraphicsContext (drawing API)
✓ FXML (XML layout)
✓ EventHandler (event listening)
✓ AnimationTimer (loop)
✓ Binding (property binding)
```

### Patterns à Reconnaître

```
✓ MVC (MainController = Controller)
✓ Strategy Pattern (différents types d'ActifMobile)
✓ Template Method (abstract update())
✓ Composite Pattern (List<ActifMobile>)
✓ Observer Pattern (event listeners)
```

---

## 🛠️ Étape 8: Environnement de Développement

### Setup Minimal

```
1. Java JDK 17+ installé
2. IDE: VS Code, IntelliJ, ou Eclipse
3. Maven (inclus via mvnw)
4. Git (optionnel mais recommandé)
```

### Commandes Essentielles

```powershell
# Compiler
mvnw clean compile

# Exécuter
mvnw javafx:run

# Tests
mvnw test

# Build JAR
mvnw package
```

### Shortcuts VS Code

```
Ctrl+P           - Quick file open
Ctrl+F           - Find in file
Ctrl+Shift+F     - Find in files
Ctrl+H           - Replace
F2               - Rename symbol
Ctrl+Shift+R     - Refactor
F5               - Start debugging
Ctrl+Shift+B     - Run build task
```

---

## ✅ Checklist: Vous Êtes Prêt Quand...

```
[ ] Vous pouvez expliquer le flux (Main → MainController → etc.)
[ ] Vous comprenez ActifMobile et ses enfants
[ ] Vous savez où trouver chaque concept
[ ] Vous pouvez lire une méthode sans paniquer
[ ] Vous pouvez compiler et exécuter
[ ] Vous avez réussi les 4 exercices
[ ] Vous connaissez l'interface que vous implémentez
```

---

## 🆘 Quand Vous Êtes Bloqué

### "Je ne comprends pas cette ligne"

```
1. Cherchez tous les mots-clés (abstract, interface, extends, etc.)
2. Cherchez les noms de variables et classes
3. Relisez la ligne en français
4. Cherchez comment cette ligne est APPELÉE
5. Cherchez ce que cette ligne RETOURNE
```

### "Je ne sais pas où chercher"

```
1. Allez sur QUICK_REFERENCE.md
2. Cherchez votre concept
3. Il y a une table "où aller"
```

### "Le code ne compile pas"

```
1. Lire le message d'erreur (ligne du problème)
2. Chercher dans ce fichier à cette ligne
3. Vérifier: import?, syntaxe?, type correct?
4. Relire le code autour
5. Demander à ChatGPT ou Google l'erreur exacte
```

---

## 🎓 Résumé: Votre Chemin d'Apprentissage

```
Day 1: Vue d'ensemble (2 heures)
  ├─ Lire ARCHITECTURE_OVERVIEW.md
  ├─ Lire QUICK_REFERENCE.md
  └─ Avoir une idée générale

Day 2: Structure (2-3 heures)
  ├─ Lire CODE_READING_GUIDE.md
  ├─ Lire RELATIONS_BETWEEN_FILES.md
  ├─ Lire UML_DIAGRAM.md
  └─ Comprendre les relations

Day 3: Code (2-3 heures)
  ├─ Lire Main.java
  ├─ Lire ActifMobile.java
  ├─ Lire SimulationService.java
  ├─ Lire MainController.java
  └─ Faire les exercices

Result: Vous comprenez le projet!
```

---

**Prêt à commencer ? Allez-y ! 🚀**
