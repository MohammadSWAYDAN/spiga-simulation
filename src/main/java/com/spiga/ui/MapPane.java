package com.spiga.ui;

import com.spiga.core.ActifMobile;
import com.spiga.core.SimConfig;
import com.spiga.environment.Obstacle;
import com.spiga.environment.RestrictedZone;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
// import javafx.scene.text.Text; // unused
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Composant de Carte Interactive (Vue de Dessus).
 * <p>
 * Remplace l'ancien Canvas monolithique par une approche basée sur des nœuds
 * (Scene Graph).
 * Gère plusieurs calques (Layers) : Grille, Zones/Obstacles, et Actifs Mobiles.
 * Gère également le zoom (désactivé par défaut) et la conversion coordonnées
 * Monde <-> Écran.
 * </p>
 *
 * @author Equipe SPIGA
 * @version 1.0
 */
public class MapPane extends Pane {

    /** Facteur d'échelle global (Pixels par Unité Monde). */
    private double scale = 1.0;
    private double userZoom = 1.0; // Facteur Zoom Utilisateur (1.0 = fit)

    /**
     * Map associant l'ID d'un actif à son nœud graphique pour des mises à jour
     * rapides.
     */
    private final Map<String, AssetNode> assetNodes = new HashMap<>();

    /** Calque d'arrière-plan (grille, zones statiques, obstacles). */
    private final Pane layersPane;

    /** Calque contenant les actifs mobiles (premier plan). */
    private final Pane assetsPane;

    /** Liste des actifs actuellement sélectionnés par l'utilisateur. */
    private List<ActifMobile> selectedAssets = new ArrayList<>();

    // Callbacks pour communiquer avec le Contrôleur
    private Consumer<List<ActifMobile>> onSelectionChanged;
    private Consumer<double[]> onMapClicked;
    private Consumer<double[]> onMapRightClicked;

    /** Mode Cible activé : le prochain clic définit une destination de mission. */
    private boolean missionTargetMode = false;

    /**
     * Constructeur. Initialise les calques et configure les interactions souris.
     */
    public MapPane() {
        this.setStyle("-fx-background-color: linear-gradient(to bottom, #4facfe, #00f2fe);");

        layersPane = new Pane();
        assetsPane = new Pane();
        this.getChildren().addAll(layersPane, assetsPane);

        setupInteractions();

        // Réajustement automatique de l'échelle quand la fenêtre change de taille
        this.widthProperty().addListener((obs, oldVal, newVal) -> updateScale());
        this.heightProperty().addListener((obs, oldVal, newVal) -> updateScale());
    }

    /**
     * Recalcule l'échelle d'affichage pour que tout le monde (WORLD_WIDTH x
     * WORLD_HEIGHT)
     * tienne dans la fenêtre visible, tout en conservant le ratio d'aspect.
     */
    private void updateScale() {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0)
            return;

        // Fit World to Screen (Keep Aspect Ratio)
        double scaleX = w / SimConfig.WORLD_WIDTH;
        double scaleY = h / SimConfig.WORLD_HEIGHT;
        double fitScale = Math.min(scaleX, scaleY);

        // Applique le zoom utilisateur par-dessus
        this.scale = fitScale * userZoom;

        // Force le rafraîchissement
        requestLayout();

        // Redessiner la grille immédiatemment (car statique)
        layersPane.getChildren().clear();
        drawGrid();

        // Note: Zones et Obstacles seront redessinés à la prochaine boucle update()
    }

    /**
     * Configure la gestion des événements souris (Zoom, Clic, Clic Droit).
     */
    private void setupInteractions() {
        // Zoom (Ctrl + Molette)
        this.setOnScroll(e -> {
            if (e.isControlDown()) {
                e.consume();
                double delta = e.getDeltaY();
                double zoomFactor = 1.1; // 10%
                if (delta < 0) {
                    userZoom /= zoomFactor;
                } else {
                    userZoom *= zoomFactor;
                }
                // Clamp User Zoom (0.1x to 10x)
                userZoom = Math.max(0.1, Math.min(userZoom, 10.0));

                updateScale();
            }
        });

        this.setOnMouseClicked(e -> {
            boolean isBackground = !(e.getTarget() instanceof AssetNode);
            // Vérification simple : si la cible n'est pas un noeud d'actif, c'est le fond
            if (!isBackground && e.getTarget() instanceof javafx.scene.Node) {
                javafx.scene.Node n = (javafx.scene.Node) e.getTarget();
                while (n != null && n != this) {
                    if (n instanceof AssetNode) {
                        isBackground = false;
                        break;
                    }
                    n = n.getParent();
                }
            }

            if (!isBackground)
                return; // Laisser l'événement à AssetNode

            double wx = screenToWorldX(e.getX());
            double wy = screenToWorldY(e.getY());

            if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                // Clic Droit
                if (onMapRightClicked != null) {
                    onMapRightClicked.accept(new double[] { wx, wy });
                }
            } else {
                // Clic Gauche -> Sélection ou Mode Cible
                if (missionTargetMode && onMapClicked != null) {
                    onMapClicked.accept(new double[] { wx, wy });
                } else {
                    if (onMapClicked != null) {
                        onMapClicked.accept(new double[] { wx, wy });
                    }
                }
            }
        });
    }

    /**
     * Met à jour l'affichage de la carte, des actifs et des éléments
     * environnementaux.
     *
     * @param assets          Liste des actifs mobiles à afficher.
     * @param obstacles       Liste des obstacles.
     * @param restrictedZones Liste des zones interdites.
     */
    public void update(List<ActifMobile> assets, List<Obstacle> obstacles, List<RestrictedZone> restrictedZones) {
        // 1. Synchronisation des Actifs
        List<String> currentIds = new ArrayList<>();

        for (ActifMobile asset : assets) {
            currentIds.add(asset.getId());
            AssetNode node = assetNodes.get(asset.getId());

            if (node == null) {
                // Nouvel Actif détecté -> Création du noeud
                node = new AssetNode(asset);
                final AssetNode finalNode = node;
                // Gestionnaire de clic sur l'actif
                node.setOnMouseClicked(e -> {
                    e.consume();
                    handleAssetClick(finalNode.getAsset(), e.isControlDown());
                });
                assetsPane.getChildren().add(node);
                assetNodes.put(asset.getId(), node);
            }

            // Mise à jour Position & État
            node.update(scale);
            node.setSelected(selectedAssets.contains(asset));
        }

        // Suppression des actifs disparus (morts)
        List<String> toRemove = assetNodes.keySet().stream()
                .filter(id -> !currentIds.contains(id))
                .collect(Collectors.toList());
        toRemove.forEach(id -> {
            assetsPane.getChildren().remove(assetNodes.get(id));
            assetNodes.remove(id);
        });

        // 2. Redessin des éléments statiques (Optimisation possible : ne redessiner que
        // si chgt)
        layersPane.getChildren().clear();
        drawGrid();
        drawZones(restrictedZones);
        drawObstacles(obstacles);
    }

    private void drawGrid() {
        // Lignes verticales
        for (double x = 0; x <= SimConfig.WORLD_WIDTH; x += 100) {
            Line l = new Line(x * scale, 0, x * scale, SimConfig.WORLD_HEIGHT * scale);
            l.setStroke(Color.rgb(255, 255, 255, 0.2));
            layersPane.getChildren().add(l);
        }
        // Lignes horizontales
        for (double y = 0; y <= SimConfig.WORLD_HEIGHT; y += 100) {
            Line l = new Line(0, y * scale, SimConfig.WORLD_WIDTH * scale, y * scale);
            l.setStroke(Color.rgb(255, 255, 255, 0.2));
            layersPane.getChildren().add(l);
        }
    }

    private void drawZones(List<RestrictedZone> zones) {
        if (zones == null)
            return;
        for (RestrictedZone z : zones) {
            Circle c = new Circle(z.getX() * scale, z.getY() * scale, z.getRadius() * scale);
            c.setFill(Color.rgb(0, 0, 0, 0.3));
            c.setStroke(Color.BLACK);
            c.getStrokeDashArray().addAll(10d, 10d);
            layersPane.getChildren().add(c);

            Text t = new Text(z.getX() * scale - 40, z.getY() * scale, "ZONE INTERDITE");
            t.setFill(Color.BLACK);
            layersPane.getChildren().add(t);
        }
    }

    private void drawObstacles(List<Obstacle> obstacles) {
        if (obstacles == null)
            return;
        for (Obstacle obs : obstacles) {
            Circle c = new Circle(obs.getX() * scale, obs.getY() * scale, obs.getRadius() * scale);

            // Code Couleur selon Altitude/Profondeur
            if (obs.getZ() > 10) {
                // AERIEN (Montagne) - Marron Rouge
                c.setFill(Color.rgb(139, 69, 19, 0.6));
                c.setStroke(Color.DARKRED);
                Text t = new Text(obs.getX() * scale - 10, obs.getY() * scale, "H:" + (int) obs.getZ() + "m");
                t.setStyle("-fx-font-weight: bold; -fx-fill: white; -fx-stroke: black; -fx-stroke-width: 0.5px;");
                layersPane.getChildren().add(t);

            } else if (obs.getZ() < -10) {
                // SOUS-MARIN (Récif) - Vert Sombre
                c.setFill(Color.rgb(0, 100, 0, 0.5));
                c.setStroke(Color.DARKBLUE);
            } else {
                // SURFACE (Îlot) - Gris
                c.setFill(Color.rgb(100, 100, 100, 0.7));
                c.setStroke(Color.BLACK);
            }

            c.setStrokeWidth(2.0);
            layersPane.getChildren().add(c);
        }
    }

    /**
     * Gère la logique de sélection lors d'un clic sur un actif.
     * Supporte la sélection multiple via Ctrl.
     */
    private void handleAssetClick(ActifMobile asset, boolean multiSelect) {
        if (missionTargetMode && onMapClicked != null) {
            // En mode cible, on clique sur l'actif pour le viser comme destination
            onMapClicked.accept(new double[] { asset.getX(), asset.getY() });
            return;
        }

        if (multiSelect) {
            if (selectedAssets.contains(asset))
                selectedAssets.remove(asset);
            else
                selectedAssets.add(asset);
        } else {
            selectedAssets.clear();
            selectedAssets.add(asset);
        }
        if (onSelectionChanged != null)
            onSelectionChanged.accept(new ArrayList<>(selectedAssets));
    }

    // --- API PUBLIQUE pour le Contrôleur ---

    public void setScale(double s) {
        this.scale = s;
        this.setPrefSize(SimConfig.WORLD_WIDTH * scale, SimConfig.WORLD_HEIGHT * scale);
    }

    public void deselectAll() {
        selectedAssets.clear();
        if (onSelectionChanged != null)
            onSelectionChanged.accept(new ArrayList<>());
    }

    public void selectAll(List<ActifMobile> assets) {
        selectedAssets.clear();
        selectedAssets.addAll(assets);
        if (onSelectionChanged != null)
            onSelectionChanged.accept(new ArrayList<>(selectedAssets));
    }

    public void selectAsset(ActifMobile asset) {
        selectedAssets.clear();
        if (asset != null)
            selectedAssets.add(asset);
    }

    // --- Gestion Marqueur Temporaire (Cible Mission) ---
    private double[] temporaryTarget = null;
    private Circle targetMarkerNode = null;

    public void setTemporaryTarget(double x, double y) {
        this.temporaryTarget = new double[] { x, y };
        updateTargetMarker();
    }

    public void clearTemporaryTarget() {
        this.temporaryTarget = null;
        if (targetMarkerNode != null) {
            assetsPane.getChildren().remove(targetMarkerNode);
            targetMarkerNode = null;
        }
    }

    private void updateTargetMarker() {
        if (temporaryTarget == null)
            return;

        if (targetMarkerNode == null) {
            targetMarkerNode = new Circle(5, Color.RED);
            targetMarkerNode.setStroke(Color.WHITE);
            targetMarkerNode.setStrokeWidth(2);
            assetsPane.getChildren().add(targetMarkerNode);
        }

        targetMarkerNode.setTranslateX(temporaryTarget[0] * scale);
        targetMarkerNode.setTranslateY(temporaryTarget[1] * scale);
    }

    public List<ActifMobile> getSelectedAssets() {
        return new ArrayList<>(selectedAssets);
    }

    public void setOnSelectionChanged(Consumer<List<ActifMobile>> cb) {
        this.onSelectionChanged = cb;
    }

    public void setOnMapClicked(Consumer<double[]> handler) {
        this.onMapClicked = handler;
    }

    public void setOnMapRightClicked(Consumer<double[]> handler) {
        this.onMapRightClicked = handler;
    }

    public void setMissionTargetMode(boolean b) {
        this.missionTargetMode = b;
    }

    public boolean isMissionTargetMode() {
        return missionTargetMode;
    }

    private double screenToWorldX(double sx) {
        return sx / scale;
    }

    private double screenToWorldY(double sy) {
        return sy / scale;
    }
}
