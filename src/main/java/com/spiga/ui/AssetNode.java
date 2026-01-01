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
 * Représentation graphique d'un actif (Node) dans le MapPane.
 * Remplace le dessin Canvas par un objet interactif.
 */
public class AssetNode extends StackPane {

    private final ActifMobile asset;
    private final Shape mainShape;
    private final Label label;
    private final DropShadow glowEffect;
    private final ScaleTransition hoverAnimation;
    private final Circle avoidanceCircle; // New visual indicator

    public AssetNode(ActifMobile asset) {
        this.asset = asset;
        this.glowEffect = new DropShadow(10, Color.BLACK);

        // 1. Create Zone (Detection/Safety Range)
        Circle zoneCircle = new Circle(40); // Radius 40 (visual)
        zoneCircle.setFill(Color.rgb(255, 255, 255, 0.1)); // Transparent white
        zoneCircle.setStroke(Color.rgb(255, 255, 255, 0.3));
        zoneCircle.getStrokeDashArray().addAll(5.0, 5.0);
        // zoneCircle.setMouseTransparent(true); // User wants easier selection, so let
        // zone captures clicks

        // 2. Create Shape based on type
        this.mainShape = createShape();
        this.mainShape.setEffect(glowEffect);

        // 3. Label
        this.label = new Label(asset.getId());
        this.label.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        this.label.setTextFill(Color.WHITE);
        this.label.setTranslateY(15);

        // Label Background
        Rectangle labelBg = new Rectangle(label.getText().length() * 6 + 10, 14);
        labelBg.setArcWidth(5);
        labelBg.setArcHeight(5);
        labelBg.setFill(Color.rgb(0, 0, 0, 0.6));
        labelBg.setTranslateY(15);

        // 3b. Create Avoidance Indicator (Initially hidden)
        this.avoidanceCircle = new Circle(25);
        this.avoidanceCircle.setFill(Color.TRANSPARENT);
        this.avoidanceCircle.setStroke(Color.RED);
        this.avoidanceCircle.setStrokeWidth(3);
        this.avoidanceCircle.getStrokeDashArray().addAll(10d, 5d); // Dashed line
        this.avoidanceCircle.setVisible(false);

        // 4. Assemble
        // StackPane centers everything: Zone (Back), Shape (Mid), Labels (Front)
        // We want detection zone ONLY if it makes sense (e.g. Drone) or visually cool
        // for all.
        // User said "restore zone around drone".
        if (asset instanceof ActifAerien || asset instanceof DroneLogistique) {
            this.getChildren().addAll(zoneCircle, avoidanceCircle, mainShape, labelBg, label);
        } else {
            this.getChildren().addAll(avoidanceCircle, mainShape, labelBg, label);
        }

        // 4. Interactivity (Hover)
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

        // Initial Style
        updateStyle();
    }

    private Shape createShape() {
        Shape shape;
        double size = 10.0;

        if (asset instanceof ActifAerien) {
            // Triangle
            Polygon triangle = new Polygon();
            triangle.getPoints().addAll(
                    0.0, -size,
                    size, size,
                    -size, size);
            shape = triangle;
            shape.setFill(Color.RED);
        } else if (asset instanceof VehiculeSurface) {
            // Boat shape approximation
            Polygon boat = new Polygon();
            boat.getPoints().addAll(
                    -size, 0.0,
                    size, 0.0,
                    size / 1.5, size,
                    -size / 1.5, size);
            shape = boat;
            shape.setFill(Color.BLUE);
        } else if (asset instanceof VehiculeSousMarin) {
            // Submarine (Capsule/Oval)
            shape = new Rectangle(20, 10);
            ((Rectangle) shape).setArcWidth(10);
            ((Rectangle) shape).setArcHeight(10);
            shape.setFill(Color.GREEN);
        } else {
            // Default Circle
            shape = new Circle(size);
            shape.setFill(Color.GRAY);
        }

        if (asset instanceof DroneLogistique) {
            shape.setFill(Color.YELLOW);
        }

        shape.setStroke(Color.WHITE);
        shape.setStrokeWidth(2);
        return shape;
    }

    public void update(double scale) {
        // Position update
        this.setLayoutX(asset.getX() * scale - this.getWidth() / 2); // Center pivot roughly (layoutX is top-left)
        // Better: MapPane should handle precise positioning or we use setTranslate?
        // Let's use relate coordinates. Actually Pane uses layoutX/Y.

        // Note: For StackPane, layoutX points to top-left corner.
        // We want center at X,Y.
        // But getWidth() might be 0 until layout pass.
        // So we translate -10 (approx half size) or rely on MapPane alignment
        // management.
        // Simpler: Just position top-left relative to center offset.
        this.setLayoutX(asset.getX() * scale);
        this.setLayoutY(asset.getY() * scale);

        // Status updates (Color changes etc)
        updateStyle();
    }

    private void updateStyle() {
        if (asset.getState() == ActifMobile.AssetState.LOW_BATTERY) {
            mainShape.setStroke(Color.ORANGE);
        } else if (asset.getState() == ActifMobile.AssetState.STOPPED) {
            mainShape.setStroke(Color.RED);
        } else {
            mainShape.setStroke(Color.WHITE);
        }

        // Avoidance Visual Feedback
        if (asset.getNavigationMode() == ActifMobile.NavigationMode.AVOIDING ||
                (asset.getCollisionWarning() != null && !asset.getCollisionWarning().isEmpty())) {
            avoidanceCircle.setVisible(true);
            // Optional: Rotate animation could be nice here, but static red dash is clear
            // enough
        } else {
            avoidanceCircle.setVisible(false);
        }

        // Selection highlight handled by parent or effect?
        // We can expose a method setSelection(boolean)
    }

    public void setSelected(boolean selected) {
        if (selected) {
            glowEffect.setColor(Color.CYAN);
            glowEffect.setRadius(20);
        } else {
            glowEffect.setColor(Color.BLACK);
            glowEffect.setRadius(10);
        }
    }

    public ActifMobile getAsset() {
        return asset;
    }
}
