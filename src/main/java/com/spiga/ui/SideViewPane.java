package com.spiga.ui;

import com.spiga.core.ActifMobile;
import com.spiga.core.SimConfig;
import com.spiga.environment.Obstacle;
import com.spiga.environment.RestrictedZone;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Vue de Profil (Side View) basée sur des Nœuds JavaFX.
 * <p>
 * Offre une visualisation en coupe (Axe X et Axe Z) pour apprécier l'altitude
 * et la profondeur.
 * Idéal pour visualiser les sous-marins et les drones aériens simultanément.
 * Structure: {@link BorderPane}
 * <ul>
 * <li><b>Center:</b> Pane de dessin (Fond Ciel/Mer, Zones, Obstacles,
 * Actifs)</li>
 * <li><b>Bottom:</b> Label d'état (Coordonnées sélection)</li>
 * </ul>
 * </p>
 *
 * @author Equipe SPIGA
 * @version 1.0
 */
public class SideViewPane extends BorderPane {

    /** Conteneur principal des calques de dessin. */
    private final Pane drawingPane = new Pane();

    // Calques superposés
    private final Pane backgroundLayer = new Pane();
    private final Pane zonesLayer = new Pane();
    private final Pane obstaclesLayer = new Pane();
    private final Pane assetsLayer = new Pane();

    private final Label coordLabel = new Label("Séléction: Aucune");

    // Config Viewport (Fenêtre de vue)
    private double minZ = -200; // Profondeur max affichée
    private double maxZ = 200; // Altitude max affichée
    private double worldWidth = SimConfig.WORLD_WIDTH;

    private List<Obstacle> cachedObstacles = null; // Cache pour optimisation
    private Map<String, javafx.scene.Node> assetNodes = new HashMap<>();

    /**
     * Constructeur. Initialise les couches et l'arrière-plan statique.
     */
    public SideViewPane() {
        this.setStyle("-fx-background-color: white; -fx-border-color: #ccc;");

        // Assemblage des calques dans l'ordre de profondeur (Painters Algorithm)
        drawingPane.getChildren().addAll(backgroundLayer, zonesLayer, obstaclesLayer, assetsLayer);
        this.setCenter(drawingPane);

        // Barre d'état inférieure
        coordLabel.setStyle(
                "-fx-padding: 5px; -fx-background-color: #f4f4f4; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0; -fx-font-family: 'Consolas'; -fx-font-weight: bold;");
        coordLabel.setMaxWidth(Double.MAX_VALUE);
        this.setBottom(coordLabel);

        // Redessiner l'arrière-plan lors du redimensionnement
        drawingPane.widthProperty().addListener(e -> {
            drawBackground();
            renderObstacles();
        });
        drawingPane.heightProperty().addListener(e -> {
            drawBackground();
            renderObstacles();
        });
    }

    /**
     * Dessine l'environnement statique (Ciel, Mer, Lignes limites).
     */
    private void drawBackground() {
        backgroundLayer.getChildren().clear();
        double w = drawingPane.getWidth();
        double h = drawingPane.getHeight();
        if (w <= 0 || h <= 0)
            return;

        // Position Y du niveau de la mer (Z=0)
        double seaLevelY = zToScreenY(0);

        // 1. CIEL (Au-dessus de 0) - Dégradé Bleu Clair
        Rectangle sky = new Rectangle(0, 0, w, seaLevelY);
        sky.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#87CEEB")), new Stop(1, Color.web("#E0F7FA"))));
        backgroundLayer.getChildren().add(sky);

        // 2. MER (En-dessous de 0) - Dégradé Bleu Foncé
        Rectangle sea = new Rectangle(0, seaLevelY, w, h - seaLevelY);
        sea.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#006994")), new Stop(1, Color.web("#001e33"))));
        backgroundLayer.getChildren().add(sea);

        // 3. Ligne de Surface (Z=0)
        Line line = new Line(0, seaLevelY, w, seaLevelY);
        line.setStroke(Color.WHITE);
        line.getStrokeDashArray().addAll(5d, 5d);
        backgroundLayer.getChildren().add(line);

        // 4. Limite Sous-Marins (-150m)
        double subLimitY = zToScreenY(-150);
        Line limitSub = new Line(0, subLimitY, w, subLimitY);
        limitSub.setStroke(Color.RED);
        limitSub.getStrokeDashArray().addAll(10d, 5d);
        limitSub.setOpacity(0.5);
        backgroundLayer.getChildren().add(limitSub);

        Text txtLimit = new Text(10, subLimitY - 5, "LIMITE SOUS-MARINS (-150m)");
        txtLimit.setFill(Color.RED);
        txtLimit.setStyle("-fx-font-size: 10px; -fx-opacity: 0.7;");
        backgroundLayer.getChildren().add(txtLimit);

        // 5. Plafond Aérien (+150m)
        double airLimitY = zToScreenY(150);
        Line limitAir = new Line(0, airLimitY, w, airLimitY);
        limitAir.setStroke(Color.RED);
        limitAir.getStrokeDashArray().addAll(10d, 5d);
        limitAir.setOpacity(0.5);
        backgroundLayer.getChildren().add(limitAir);

        Text txtAirLimit = new Text(10, airLimitY - 5, "PLAFOND (150m)");
        txtAirLimit.setFill(Color.RED);
        txtAirLimit.setStyle("-fx-font-size: 10px; -fx-opacity: 0.7;");
        backgroundLayer.getChildren().add(txtAirLimit);

        // Labels Informatifs
        Text txtAir = new Text(10, 15, "AIR (+200m)");
        txtAir.setFill(Color.DARKBLUE);
        backgroundLayer.getChildren().add(txtAir);

        Text txtWater = new Text(10, h - 5, "EAU (-200m)");
        txtWater.setFill(Color.WHITE);
        backgroundLayer.getChildren().add(txtWater);
    }

    /**
     * Met à jour la vue avec les données dynamiques de la simulation.
     */
    public void update(List<ActifMobile> assets, List<RestrictedZone> zones, List<Obstacle> obstacles,
            ActifMobile selectedAsset) {
        if (drawingPane.getWidth() <= 0 || drawingPane.getHeight() <= 0)
            return;

        // 1. Mise à jour des Zones Interdites (Projection Latérale)
        zonesLayer.getChildren().clear();
        if (zones != null) {
            for (RestrictedZone z : zones) {
                // Bornes X
                double xMin = z.getX() - z.getRadius();
                double xMax = z.getX() + z.getRadius();

                // Bornes Z
                double zMin = z.getMinZ();
                double zMax = z.getMaxZ();

                // Conversion Écran
                double screenX = xToScreenX(Math.max(0, xMin));
                double screenW = xToScreenX(Math.min(worldWidth, xMax)) - screenX;

                double screenYTop = zToScreenY(zMax);
                double screenYBottom = zToScreenY(zMin);
                double screenH = screenYBottom - screenYTop;

                if (screenW > 0 && screenH > 0) {
                    Rectangle r = new Rectangle(screenX, screenYTop, screenW, screenH);
                    r.setFill(Color.rgb(255, 0, 0, 0.15));
                    r.setStroke(Color.RED);
                    r.getStrokeDashArray().addAll(2d, 2d);
                    zonesLayer.getChildren().add(r);

                    Text t = new Text(screenX + 5, screenYTop + 15, "ZONE INTERDITE");
                    t.setFill(Color.DARKRED);
                    t.setStyle("-fx-font-size: 9px; -fx-font-weight: bold;");
                    zonesLayer.getChildren().add(t);
                }
            }
        }

        // 2. Mise à jour Optimisée des Obstacles
        if (obstacles != cachedObstacles) {
            cachedObstacles = obstacles;
            renderObstacles();
        }

        // 3. Mise à jour des Actifs Mobiles
        List<String> currentIds = assets.stream().map(ActifMobile::getId).collect(Collectors.toList());

        for (ActifMobile asset : assets) {
            javafx.scene.Node node = assetNodes.get(asset.getId());
            if (node == null) {
                node = createAssetNode(asset);
                assetsLayer.getChildren().add(node);
                assetNodes.put(asset.getId(), node);
            }

            // Positionnement
            double sx = xToScreenX(asset.getX());
            double sy = zToScreenY(asset.getZ());

            node.setTranslateX(sx);
            node.setTranslateY(sy);

            // Mise en valeur Sélection
            Group g = (Group) node;
            if (asset == selectedAsset) {
                g.setOpacity(1.0);
                g.toFront();
                // Possibilité d'agrandir ou de changer la couleur ici
            } else {
                g.setOpacity(0.9);
            }
        }

        // Nettoyage des noeuds orphelins
        List<String> toRemove = assetNodes.keySet().stream()
                .filter(id -> !currentIds.contains(id))
                .collect(Collectors.toList());

        toRemove.forEach(id -> {
            assetsLayer.getChildren().remove(assetNodes.get(id));
            assetNodes.remove(id);
        });

        // 4. Mise à jour Label Statut
        if (selectedAsset != null) {
            coordLabel.setText(String.format("SÉLECTION: %s   |   X: %.1f   Y: %.1f   Z: %.1f",
                    selectedAsset.getId(), selectedAsset.getX(), selectedAsset.getY(), selectedAsset.getZ()));
            coordLabel.setStyle(
                    "-fx-padding: 5px; -fx-background-color: #e0f7fa; -fx-border-color: #0097a7; -fx-border-width: 1 0 0 0; -fx-font-family: 'Consolas'; -fx-font-weight: bold;");
        } else {
            coordLabel.setText("Aucune sélection");
            coordLabel.setStyle(
                    "-fx-padding: 5px; -fx-background-color: #f4f4f4; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0; -fx-font-family: 'Consolas';");
        }
    }

    /**
     * Crée le nœud graphique représentant un actif dans la vue de profil.
     */
    private javafx.scene.Node createAssetNode(ActifMobile asset) {
        Group group = new Group();
        javafx.scene.shape.Shape shape;

        if (asset instanceof com.spiga.core.ActifAerien) {
            // Drone: Triangle pointant vers la droite
            shape = new Polygon(-6, 6, 6, 6, 0, -6);
            shape.setFill(Color.RED);
            shape.setStroke(Color.BLACK);
        } else if (asset instanceof com.spiga.core.VehiculeSousMarin) {
            // Sous-Marin: Rectangle Arrondi Jaune
            Rectangle r = new Rectangle(-8, -4, 16, 8);
            r.setArcWidth(6);
            r.setArcHeight(6);
            r.setFill(Color.YELLOW);
            r.setStroke(Color.BLACK);
            // Centrage
            r.setX(-8);
            r.setY(-4);
            shape = r;
        } else {
            // Bateau: Rectangle Orange
            Rectangle r = new Rectangle(-6, -3, 12, 6);
            r.setX(-6);
            r.setY(-3);
            r.setFill(Color.ORANGE);
            r.setStroke(Color.BLACK);
            shape = r;
        }

        group.getChildren().add(shape);

        // Label Nom (Petit, sur fond semi-transparent)
        Label nameLabel = new Label(asset.getId());
        nameLabel.setStyle(
                "-fx-background-color: rgba(0,0,0,0.5); -fx-text-fill: white; -fx-font-size: 9px; -fx-padding: 1px 3px; -fx-background-radius: 3;");
        nameLabel.setTranslateX(8); // Décalage Droite
        nameLabel.setTranslateY(-8); // Décalage Haut
        nameLabel.setMouseTransparent(true);

        group.getChildren().add(nameLabel);

        return group;
    }

    // --- Transformations de Coordonnées ---

    private double xToScreenX(double worldX) {
        return (worldX / worldWidth) * drawingPane.getWidth();
    }

    private double zToScreenY(double worldZ) {
        // Z positif (Haut) -> Y petit (Haut écran). Z négatif (Bas) -> Y grand (Bas
        // écran)
        // Mapping: [maxZ, minZ] -> [0, height]
        double range = maxZ - minZ;
        double pct = (maxZ - worldZ) / range;
        return pct * drawingPane.getHeight();
    }

    private void renderObstacles() {
        if (drawingPane.getWidth() <= 0 || drawingPane.getHeight() <= 0)
            return;

        obstaclesLayer.getChildren().clear();
        if (cachedObstacles == null)
            return;

        for (Obstacle obs : cachedObstacles) {
            // Projection du volume sphérique sur le plan X-Z -> Ellipse
            double r = obs.getRadius();
            double xMin = obs.getX() - r;
            double xMax = obs.getX() + r;
            double zMin = obs.getZ() - r;
            double zMax = obs.getZ() + r;

            double screenX = xToScreenX(Math.max(0, xMin));
            double screenW = xToScreenX(Math.min(worldWidth, xMax)) - screenX;

            double screenYTop = zToScreenY(zMax);
            double screenYBottom = zToScreenY(zMin);
            double screenH = screenYBottom - screenYTop;

            if (screenW > 0 && screenH > 0) {
                javafx.scene.shape.Ellipse oval = new javafx.scene.shape.Ellipse();
                oval.setCenterX(screenX + screenW / 2);
                oval.setCenterY(screenYTop + screenH / 2);
                oval.setRadiusX(screenW / 2);
                oval.setRadiusY(screenH / 2);

                if (obs.getZ() > 10) {
                    // Montagne / Aérien
                    oval.setFill(Color.rgb(139, 69, 19, 0.4));
                    oval.setStroke(Color.DARKRED);
                } else if (obs.getZ() < -10) {
                    // Récif / Sous-marin
                    oval.setFill(Color.rgb(0, 100, 0, 0.4));
                    oval.setStroke(Color.DARKBLUE);
                } else {
                    // Surface (Îlot)
                    oval.setFill(Color.rgb(100, 100, 100, 0.5));
                    oval.setStroke(Color.BLACK);
                }
                obstaclesLayer.getChildren().add(oval);
            }
        }
    }
}
