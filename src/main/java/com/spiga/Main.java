package com.spiga;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Point d'Entree de l'Application (Main)
 * 
 * CONCEPTS CLES (JAVA FX) :
 * 
 * 1. Heritage (extends Application) :
 * - C'est quoi ? Pour faire une application graphique, JavaFX NOUS OBLIGE a
 * heriter de la classe Application.
 * - Pourquoi ? Elle contient toute la "magie" pour lancer la fenetre (Window),
 * gerer la souris, etc.
 * 
 * 2. Cycle de Vie (start) :
 * - C'est quoi ? Le point de depart.
 * - Ou ? La methode start(Stage primaryStage) est appelee automatiquement par
 * JavaFX quand l'app se lance.
 * Le Stage correspond a la FENETRE principale.
 * 
 * 3. Mode CLI/GUI :
 * - Argument --cli : Lance l'interface console (MainTestCLI)
 * - Argument --gui ou aucun : Lance l'interface graphique (defaut)
 */
public class Main extends Application {

    /**
     * Méthode principale standard de Java.
     * Detecte l'argument --cli pour lancer le mode console,
     * sinon lance l'interface graphique par defaut.
     * 
     * @param args Arguments de ligne de commande (--cli ou --gui)
     */
    public static void main(String[] args) {
        // Check for CLI mode
        for (String arg : args) {
            if ("--cli".equalsIgnoreCase(arg) || "-c".equalsIgnoreCase(arg)) {
                System.out.println("=== Mode Console (CLI) ===");
                MainTestCLI.main(args);
                return;
            }
        }

        // Default: GUI mode
        System.out.println("=== Mode Graphique (GUI) ===");
        System.out.println("Tip: Utilisez --cli pour le mode console");
        launch(args);
    }

    /**
     * Point de demarrage de l'interface graphique JavaFX.
     * Charge le fichier FXML et affiche la fenetre principale.
     *
     * @param primaryStage Stage principal fourni par JavaFX
     * @throws Exception si le chargement du FXML echoue
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Chargement du fichier FXML (Séparation Vue/Contrôleur)
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("ui/MainView.fxml"));
        javafx.scene.Parent root = loader.load();

        // Création de la Scène avec la racine (root) chargée depuis le FXML
        // Dimensions : 1400x900 pixels
        javafx.scene.Scene scene = new javafx.scene.Scene(root, 1400, 900);

        // Configuration du Stage (Fenêtre)
        primaryStage.setTitle("SPIGA Simulation - Full Spec");
        primaryStage.setMaximized(true); // Démarrer en plein écran
        primaryStage.setScene(scene); // Placement de la scène dans la fenêtre
        primaryStage.show(); // Affichage de la fenêtre
    }
}