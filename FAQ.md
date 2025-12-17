# ❓ FAQ - QUESTIONS FRÉQUEMMENT POSÉES

## 🎯 Questions Générales

### Q1: Par où je commence?
**R:** Lisez dans cet ordre:
1. Ce fichier (FAQ) pour comprendre les questions courantes
2. GETTING_STARTED.md pour le guide étape par étape
3. RESUME_UNE_PAGE.md pour l'essentiel
4. ARCHITECTURE_OVERVIEW.md pour la profondeur

### Q2: Combien de temps ça prend de comprendre le code?
**R:** 
- **Vue d'ensemble** : 1-2 heures
- **Structure complète** : 1-2 jours
- **Maîtrise du code** : 1 semaine
- **Développement confortable** : 2-3 semaines

### Q3: Je dois lire tous les documents?
**R:** Non. Dépend de vos objectifs:
- **Juste comprendre** : RESUME_UNE_PAGE + QUICK_REFERENCE
- **Faire des modifications** : Ajouter CODE_READING_GUIDE + INDEX
- **Contribuer au projet** : Lire tous les documents

### Q4: Quel IDE dois-je utiliser?
**R:** 
- **VS Code** : Léger, gratuit, bon support Java
- **IntelliJ** : Lourd mais excellent
- **Eclipse** : Gratuit, mais moins populaire

### Q5: Quel Java dois-je avoir?
**R:** Java 17+ (inclus dans le pom.xml). Vérifier:
```powershell
java -version
```

---

## 🏗️ Questions sur l'Architecture

### Q6: Quelle est la différence entre core, management et ui?
**R:**
```
core/      → Les véhicules et leur logique physique
management → Gestion flotte et missions
ui/        → Interface graphique et interaction
```

### Q7: Qu'est-ce qu'une classe abstraite vs une interface?
**R:**
```java
// INTERFACE = Contrat pur (QUOI FAIRE?)
public interface Deplacable {
    void deplacer(double x, double y, double z);
}

// CLASSE ABSTRAITE = Contrat + code (COMMENT?)
public abstract class ActifMobile implements Deplacable {
    public void deplacer(double x, double y, double z) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
    }
}
```

### Q8: Pourquoi ActifMobile ne s'instancie pas directement?
**R:** Car c'est une classe abstraite. Elle définit une interface commune mais chaque type de véhicule a sa logique propre:
```java
new ActifMobile("D1", 0, 0, 0, 100, 5000); // ❌ ERREUR
new DroneLogistique("D1", 0, 0, 0, 100, 5000); // ✅ OK
```

### Q9: Pourquoi y a-t-il plusieurs hiérarchies de drones?
**R:** Parce qu'ils ont des contraintes différentes:
```
ActifAerien   → Limites d'altitude
ActifMarin    → Limites de profondeur
  └─ VehiculeSurface → z = 0 (surface)
  └─ VehiculeSousMarin → z < 0 (profondeur)
```

### Q10: Comment marche l'héritage en chaîne?
**R:**
```java
class DroneLogistique extends ActifAerien
class ActifAerien extends ActifMobile

// Résultat: DroneLogistique a tout de ActifMobile + ActifAerien
DroneLogistique d = new DroneLogistique(...);
d.update(dt);           // De ActifMobile
d.deplacer(x, y, z);    // De ActifMobile
d.getAltitudeMax();     // De ActifAerien
```

---

## 🔄 Questions sur le Flux

### Q11: Qu'est-ce qui se passe quand j'appuie sur un bouton?
**R:**
```
1. Événement JavaFX généré
2. MainController reçoit l'événement
3. MainController appelle la bonne méthode
4. Modification de l'état des objets
5. MapCanvas se redessine
6. Vous voyez le changement
```

### Q12: Comment les drones se déplacent?
**R:** Voir le code dans ActifMobile.moveTowards():
```
1. Calculer distance jusqu'à la cible
2. Si distance < 1m → Arrivé!
3. Sinon:
   - Calculer direction vers cible
   - Appliquer vitesse
   - Déplacer x, y, z selon temps écoulé
   - Consommer batterie
```

### Q13: Pourquoi 60 FPS?
**R:** C'est la norme pour les jeux/simulations:
- 60 FPS = 16.67 ms par frame
- Assez rapide pour paraître fluide
- Pas trop lourd pour le CPU

### Q14: Qu'est-ce que la boucle SimulationService?
**R:** Une boucle infinie qui s'exécute 60 fois par seconde:
```
Chaque 16.67ms:
1. Pour chaque actif: update(dt)
2. Gère collisions et obstacles
3. Redessine l'affichage
4. Prochaine frame
```

### Q15: Comment la batterie marche?
**R:**
```
autonomieActuelle -= distance * 0.1

Exemple:
- Drone parcourt 100m → consomme 10 unités
- Drone au repos → ne consomme rien
- Drone recharge à la base → +10 par frame
```

---

## 🎮 Questions sur l'Utilisation

### Q16: Comment créer un nouveau drone?
**R:**
```java
// Copier DroneLogistique.java
public class MonDrone extends ActifAerien {
    public MonDrone(String id, double x, double y, double z, ...) {
        super(id, x, y, z, ...);
        // Paramètres spécifiques
    }
}

// L'ajouter dans MainController.initialize()
gestionnaire.ajouterActif(new MonDrone("D1", 100, 100, 1000, 150, 6000));
```

### Q17: Comment créer une nouvelle mission?
**R:**
```java
// Copier MissionLogistique.java
public class MaMission extends Mission {
    public MaMission(String titre, ...) {
        super(titre, MissionType.SURVEILLANCE);
        // Paramètres spécifiques
    }
    
    @Override
    public void execute() {
        System.out.println("Ma mission!");
    }
}

// L'assigner à des actifs
gestionnaire.demarrerMission(new MaMission(...), listActifs);
```

### Q18: Comment modifier la physique?
**R:** Éditer `ActifMobile.java`:
```java
// Changer la vitesse
velocityX = dirX * vitesseMax * 2;  // 2x plus rapide

// Changer la consommation
double consommation = distanceParcourue * 0.05;  // 0.05 au lieu de 0.1

// Changer la limite batterie basse
if (autonomieActuelle < autonomieMax * 0.05) {  // 5% au lieu de 10%
```

### Q19: Comment modifier l'interface?
**R:** Deux options:
1. **FXML** : Éditer `MainView.fxml` (layout)
2. **Contrôleur** : Éditer `MainController.java` (logique)

### Q20: Comment ajouter un obstacle?
**R:** Dans `SimulationService.initializeObstacles()`:
```java
obstacles.add(new Obstacle(
    x,      // position X
    y,      // position Y
    z,      // profondeur (-50 pour sous l'eau, 0 pour surface)
    radius  // taille (30-40 pour visible)
));
```

---

## 🐛 Questions sur le Debugging

### Q21: Comment déboguer si quelque chose ne marche pas?
**R:**
1. Ajouter des `System.out.println()` :
```java
System.out.println("Position: " + x + ", " + y);
```
2. Relancer l'app
3. Regarder la console pour les logs
4. Tracer l'exécution

### Q22: Où voir les logs?
**R:** Dans VS Code:
- Terminal → Affichage → Sortie de dépannage
- Ou: Fenêtre inférieure après `mvnw javafx:run`

### Q23: Pourquoi "Cannot find symbol"?
**R:** Erreur d'import probablement. Solution:
```java
// Ajouter au début du fichier
import com.spiga.core.ActifMobile;
```

### Q24: Pourquoi "NullPointerException"?
**R:** Une variable est `null` (non initialisée). Solution:
```java
// Ajouter une vérification
if (actif != null) {
    actif.update(dt);
}
```

### Q25: Pourquoi ça compile mais ne marche pas?
**R:** Erreur à l'exécution (runtime). Solutions:
1. Relancer l'app
2. Recompiler: `mvnw clean compile`
3. Regarder les logs
4. Tracer le code avec println()

---

## 📚 Questions sur la Lecture du Code

### Q26: Par quel fichier commencer?
**R:**
```
Main.java (15 lignes) ← FACILE
    ↓
ActifMobile.java (330 lignes) ← IMPORTANT
    ↓
SimulationService.java (246 lignes) ← IMPORTANT
    ↓
GestionnaireEssaim.java (70 lignes) ← FACILE
    ↓
MainController.java (487 lignes) ← COMPLEXE
```

### Q27: Comment lire un gros fichier?
**R:** Pas tout d'un coup!
1. Lire le commentaire du haut
2. Lire les noms des attributs (what it has)
3. Lire les noms des méthodes (what it does)
4. Ne lire le contenu que si besoin

### Q28: Je ne comprends pas une ligne, que faire?
**R:**
1. Chercher tous les mots-clés (abstract, implements, etc.)
2. Chercher ce que la variable vaut
3. Relire en français
4. Relire le contexte (la fonction autour)
5. Chercher où cette ligne est appelée

### Q29: Où sont les tests?
**R:** `src/test/java/`:
```
ActifMobileTest.java
SimulationServiceTest.java
GestionnaireEssaimTest.java
```

Exécuter: `mvnw test`

### Q30: Comment écrire un test?
**R:**
```java
@Test
public void testActifCreation() {
    DroneLogistique drone = new DroneLogistique("D1", 0, 0, 0, 100, 5000);
    assertEquals("D1", drone.getId());
    assertEquals(0, drone.getX(), 0.01);
}
```

---

## 🎓 Questions Avancées

### Q31: Comment je contribue au projet?
**R:**
1. Fork sur GitHub
2. Clone localement
3. Créer une branche: `git checkout -b feature/ma-feature`
4. Modifier le code
5. Commit: `git commit -m "Ajout: ma feature"`
6. Push: `git push origin feature/ma-feature`
7. Pull Request

### Q32: Comment je documente mon code?
**R:** Avec Javadoc:
```java
/**
 * Déplace l'actif vers une position
 * 
 * @param x coordonnée X cible
 * @param y coordonnée Y cible
 * @param z coordonnée Z cible
 * @throws IllegalArgumentException si coordonnées invalides
 */
public void deplacer(double x, double y, double z) {
    // ...
}
```

### Q33: Comment générer la Javadoc?
**R:** `mvnw javadoc:javadoc`

Puis regarder dans `target/site/apidocs/index.html`

### Q34: Qu'est-ce que le pattern MVC?
**R:**
```
Model    = Données (ActifMobile, Mission, GestionnaireEssaim)
View     = Affichage (MapCanvas, SideViewCanvas)
Control  = Logique (MainController, SimulationService)
```

### Q35: Comment refactoriser?
**R:** Chercher les violations:
- Duplication de code → Extraire dans une méthode
- Méthode trop grosse → Splitter en plusieurs
- Classe trop grosse → Créer sous-classes
- Noms pas clairs → Renommer

---

## 💡 Questions Bonus

### Q36: Pourquoi JavaFX?
**R:** 
- Moderne et performant
- Support pour graphiques 2D/3D
- Multiplateformes
- Inclus dans Java

### Q37: Pourquoi Maven?
**R:**
- Gère les dépendances automatiquement
- Compilation reproductible
- Build standardisé

### Q38: Pourquoi Git?
**R:**
- Versioning du code
- Travail collaboratif
- Historique des modifications
- Possibilité de revenir en arrière

### Q39: Qu'est-ce que Spring Boot?
**R:** Un framework Java. SPIGA n'en utilise pas car c'est une simulation simple, pas une web app.

### Q40: Je veux ajouter une base de données?
**R:** Hors scope pour SPIGA. C'est une simulation temps réel. Les données sont volatiles (perdu au redémarrage).

---

## 📖 Ressources Supplémentaires

### Java et OOP
- Tutorials Point Java
- Oracle Java Documentation
- GeeksforGeeks Java

### JavaFX
- TutorialsPoint JavaFX
- JavaFX CSS Reference
- Official JavaFX Documentation

### Git
- GitHub Hello World
- Git Documentation
- Atlassian Git Tutorials

### Maven
- Maven Official Docs
- Apache Maven Beginner Guide

---

## ✅ Vérification: Êtes-Vous Prêt?

Si vous pouvez répondre OUI à ceci, vous comprenez le code:

```
[ ] Je peux expliquer le flux: Utilisateur → Code → Affichage
[ ] Je peux dire où est ActifMobile et ce qu'elle fait
[ ] Je peux dire où est SimulationService et ce qu'elle fait
[ ] Je peux lire une classe abstraite et comprendre l'héritage
[ ] Je peux tracer une exécution (clic → résultat)
[ ] Je peux compiler et exécuter
[ ] Je peux ajouter un drone (en copiant DroneLogistique)
[ ] Je peux modifier un paramètre et voir l'effet
[ ] Je peux ajouter un log et le voir dans la console
[ ] Je peux créer une nouvelle mission (en copiant une existante)
```

---

**N'hésitez pas à relire cette FAQ au cours de votre apprentissage ! 📚**
