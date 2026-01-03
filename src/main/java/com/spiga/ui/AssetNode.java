package com.spiga.ui;

import com.spiga.core.ActifMobile;
import com.spiga.core.ActifAerien;
import com.spiga.core.VehiculeSurface;
import com.spiga.core.VehiculeSousMarin;
import com.spiga.core.DroneLogistique;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.effect.DropShadow;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

/**
 * Représentation Graphique d'un Actif (Node) dans l'interface JavaFX.
 * <p>
 * Cette classe étend {@link StackPane} pour encapsuler la forme géométrique,
 * les effets visuels (ombres, zones de détection) et les labels d'un actif.
 * Elle remplace l'ancien système de dessin sur Canvas pour offrir plus
 * d'interactivité (clic, hover).
 * </p>
 *
 * @author Equipe SPIGA
 * @version 1.0
 */
public class AssetNode extends StackPane {

    /** L'actif mobile associé (Modèle). */
    private final ActifMobile asset;

    /** La forme principale représentant l'actif (Triangle, Cercle, etc.). */
    private final Shape mainShape;

    /** Label affichant l'identifiant de l'actif. */
    private final Label label;

    /** Effet d'ombre pour le relief et la sélection. */
    private final DropShadow glowEffect;

    /** Animation de survol (agrandissement). */
    private final ScaleTransition hoverAnimation;

    /** Indicateur visuel d'évitement (cercle rouge pointillé). */
    private final Circle avoidanceCircle;

    /**
     * Construit un nouveau nœud graphique pour un actif donné.
     * Configure la forme, les couleurs et les interactions souris.
     *
     * @param asset L'actif mobile à représenter.
     */
    public AssetNode(ActifMobile asset) {
        this.asset = asset;
        this.glowEffect = new DropShadow(10, Color.BLACK);

        // 1. Zone de Détection / Sécurité (Visuel uniquement)
        // Représente le rayon de sécurité autour de l'actif
        Circle zoneCircle = new Circle(40); // Rayon visuel 40
        zoneCircle.setFill(Color.rgb(255, 255, 255, 0.1)); // Blanc transparent
        zoneCircle.setStroke(Color.rgb(255, 255, 255, 0.3));
        zoneCircle.getStrokeDashArray().addAll(5.0, 5.0);

        // 2. Création de la forme selon le type d'actif
        this.mainShape = createShape();
        this.mainShape.setEffect(glowEffect);

        // 3. Label d'identification
        this.label = new Label(asset.getId());
        this.label.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        this.label.setTextFill(Color.WHITE);
        this.label.setTranslateY(15);

        // Fond du Label (pour lisibilité)
        Rectangle labelBg = new Rectangle(label.getText().length() * 6 + 10, 14);
        labelBg.setArcWidth(5);
        labelBg.setArcHeight(5);
        labelBg.setFill(Color.rgb(0, 0, 0, 0.6));
        labelBg.setTranslateY(15);

        // 3b. Indicateur d'évitement (Caché par défaut)
        this.avoidanceCircle = new Circle(25);
        this.avoidanceCircle.setFill(Color.TRANSPARENT);
        this.avoidanceCircle.setStroke(Color.RED);
        this.avoidanceCircle.setStrokeWidth(3);
        this.avoidanceCircle.getStrokeDashArray().addAll(10d, 5d); // Pointillés
        this.avoidanceCircle.setVisible(false);

        // 4. Assemblage
        // StackPane centre tout : Zone (Fond), Forme (Milieu), Labels (Devant)
        // On affiche la zone de détection principalement pour les drones aériens
        if (asset instanceof ActifAerien || asset instanceof DroneLogistique) {
            this.getChildren().addAll(zoneCircle, avoidanceCircle, mainShape, labelBg, label);
        } else {
            this.getChildren().addAll(avoidanceCircle, mainShape, labelBg, label);
        }

        // 4. Interactivité (Animation au survol souris)
        this.hoverAnimation = new ScaleTransition(Duration.millis(200), this);
        this.hoverAnimation.setFromX(1.0);
        this.hoverAnimation.setFromY(1.0);
        this.hoverAnimation.setToX(1.15);
        this.hoverAnimation.setToY(1.15);

        this.setOnMouseEntered(e -> {
            this.setCursor(javafx.scene.Cursor.HAND);
            hoverAnimation.playFromStart();
        });
        this.setOnMouseExited(e -> {
            this.setCursor(javafx.scene.Cursor.DEFAULT);
            hoverAnimation.setRate(-1);
            hoverAnimation.play();
        });

        // Style initial
        updateStyle();
    }

    /**
     * Crée la forme géométrique adaptée au type d'actif.
     *
     * @return Une forme JavaFX (Polygon, Rectangle, Circle).
     */
    private Shape createShape() {
        Shape shape;
        double size = 10.0;

        if (asset instanceof ActifAerien) {
            // Triangle pour les aéronefs
            Polygon triangle = new Polygon();
            triangle.getPoints().addAll(
                    0.0, -size,
                    size, size,
                    -size, size);
            shape = triangle;
            shape.setFill(Color.RED);
        } else if (asset instanceof VehiculeSurface) {
            // Forme de coque pour les bateaux
            Polygon boat = new Polygon();
            boat.getPoints().addAll(
                    -size, 0.0,
                    size, 0.0,
                    size / 1.5, size,
                    -size / 1.5, size);
            shape = boat;
            shape.setFill(Color.BLUE);
        } else if (asset instanceof VehiculeSousMarin) {
            // Rectangle arrondi pour les sous-marins
            shape = new Rectangle(20, 10);
            ((Rectangle) shape).setArcWidth(10);
            ((Rectangle) shape).setArcHeight(10);
            shape.setFill(Color.GREEN);
        } else {
            // Cercle par défaut
            shape = new Circle(size);
            shape.setFill(Color.GRAY);
        }

        // Couleur spécifique pour la logistique (Jaune)
        if (asset instanceof DroneLogistique) {
            shape.setFill(Color.YELLOW);
        }

        shape.setStroke(Color.WHITE);
        shape.setStrokeWidth(2);
        return shape;
    }

    /**
     * Met à jour la position et le style visuel de l'actif.
     * Appelé à chaque frame par {@link MapPane}.
     *
     * @param scale Le facteur d'échelle actuel de la carte pour le positionnement.
     */
    public void update(double scale) {
        // Mise à jour position écran (LayoutX/Y)
        this.setLayoutX(asset.getX() * scale);
        this.setLayoutY(asset.getY() * scale);

        // Mise à jour couleurs et indicateurs
        updateStyle();
    }

    /**
     * Met à jour l'apparence selon l'état de l'actif (Batterie faible, Arrêt,
     * Evitement).
     */
    private void updateStyle() {
        if (asset.getState() == ActifMobile.AssetState.LOW_BATTERY) {
            mainShape.setStroke(Color.ORANGE);
        } else if (asset.getState() == ActifMobile.AssetState.STOPPED) {
            mainShape.setStroke(Color.RED);
        } else {
            mainShape.setStroke(Color.WHITE);
        }

        // Feedback Visuel d'Evitement
        if (asset.getNavigationMode() == ActifMobile.NavigationMode.AVOIDING ||
                (asset.getCollisionWarning() != null && !asset.getCollisionWarning().isEmpty())) {
            avoidanceCircle.setVisible(true);
        } else {
            avoidanceCircle.setVisible(false);
        }
    }

    /**
     * Applique ou retire l'effet de surbrillance de sélection.
     *
     * @param selected Vrai si l'actif est sélectionné.
     */
    public void setSelected(boolean selected) {
        if (selected) {
            glowEffect.setColor(Color.CYAN);
            glowEffect.setRadius(20);
        } else {
            glowEffect.setColor(Color.BLACK);
            glowEffect.setRadius(10);
        }
    }

    /**
     * Récupère l'actif mobile associé à ce noeud graphique.
     * 
     * @return L'instance {@link ActifMobile}.
     */
    public ActifMobile getAsset() {
        return asset;
    }
}
