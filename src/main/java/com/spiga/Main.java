package com.spiga;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Point d'Entrée de l'Application SPIGA (Main).
 * <p>
 * Cette classe hérite de {@link Application}, ce qui est obligatoire pour toute
 * application JavaFX.
 * Elle gère le cycle de vie de l'interface graphique, de l'initialisation au
 * chargement
 * de la vue principale définie en FXML.
 * </p>
 *
 * <h3>CONCEPTS CLÉS (JAVA FX) :</h3>
 * <ul>
 * <li><b>Héritage (extends Application) :</b> JavaFX impose d'hériter de la
 * classe {@code Application} pour bénéficier
 * de la gestion automatique de la fenêtre et des événements.</li>
 * <li><b>Cycle de Vie (start) :</b> La méthode {@link #start(Stage)} est le
 * point d'entrée graphique, appelée
 * automatiquement après l'initialisation du toolkit JavaFX.</li>
 * <li><b>Stage :</b> Représente la fenêtre principale de l'application (le
 * conteneur de haut niveau).</li>
 * </ul>
 *
 * @author Equipe SPIGA
 * @version 1.0
 */
public class Main extends Application {

    /**
     * Méthode principale standard de Java.
     * <p>
     * C'est le point d'entrée technique de la JVM. Pour une application JavaFX,
     * elle délègue immédiatement l'exécution à la méthode
     * {@link #launch(String...)}.
     * </p>
     *
     * @param args Les arguments de la ligne de commande (non utilisés ici).
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Point d'entrée de l'application JavaFX.
     * <p>
     * Cette méthode initialise la scène principale en chargeant le fichier FXML,
     * configure la fenêtre (titre, dimensions, plein écran) et l'affiche.
     * </p>
     *
     * @param primaryStage Le `Stage` principal (la fenêtre) fourni par la
     *                     plateforme JavaFX.
     * @throws Exception Si le fichier FXML ne peut pas être chargé ou si une erreur
     *                   survient lors de l'initialisation.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Chargement du fichier FXML (Séparation Vue/Contrôleur)
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("ui/MainView.fxml"));
        javafx.scene.Parent root = loader.load();

        // Création de la Scène avec la racine (root) chargée depuis le FXML
        // Dimensions par défaut : 1400x900 pixels
        javafx.scene.Scene scene = new javafx.scene.Scene(root, 1400, 900);

        // Configuration du Stage (Fenêtre)
        primaryStage.setTitle("SPIGA Simulation - Full Spec");
        primaryStage.setMaximized(true); // Démarrer en plein écran pour une meilleure immersion
        primaryStage.setScene(scene); // Placement de la scène dans la fenêtre
        primaryStage.show(); // Affichage de la fenêtre
    }
}
