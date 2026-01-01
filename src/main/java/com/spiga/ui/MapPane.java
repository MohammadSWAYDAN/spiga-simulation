package com.spiga.ui;

import com.spiga.core.ActifMobile;
import com.spiga.core.SimConfig;
import com.spiga.environment.Obstacle;
import com.spiga.environment.RestrictedZone;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
// Unused imports removed
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Remplaçant du MapCanvas. Utilise un Pane et des Nodes (AssetNode).
 */
public class MapPane extends Pane {

    private double scale = 1.0;
    private double userZoom = 1.0; // User Zoom Factor (1.0 = fit)
    private final Map<String, AssetNode> assetNodes = new HashMap<>(); // ID -> Node
    private final Pane layersPane; // Container for zones, grid
    private final Pane assetsPane; // Container for assets

    private List<ActifMobile> selectedAssets = new ArrayList<>();

    // Callbacks
    private Consumer<List<ActifMobile>> onSelectionChanged;
    private Consumer<double[]> onMapClicked;
    private Consumer<double[]> onMapRightClicked; // Restored
    private boolean missionTargetMode = false;

    public MapPane() {
        this.setStyle("-fx-background-color: linear-gradient(to bottom, #4facfe, #00f2fe);");
        // Removed fixed PrefSize to allow resizing by parent
        // this.setPrefSize(SimConfig.WORLD_WIDTH, SimConfig.WORLD_HEIGHT);

        layersPane = new Pane();
        assetsPane = new Pane();
        this.getChildren().addAll(layersPane, assetsPane);

        setupInteractions();

        // Auto-Scale Logic (Base)
        this.widthProperty().addListener((obs, oldVal, newVal) -> updateScale());
        this.heightProperty().addListener((obs, oldVal, newVal) -> updateScale());
    }

    private void updateScale() {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0)
            return;

        // Fit World to Screen (Keep Aspect Ratio)
        double scaleX = w / SimConfig.WORLD_WIDTH;
        double scaleY = h / SimConfig.WORLD_HEIGHT;
        double fitScale = Math.min(scaleX, scaleY);

        // Apply User Zoom
        this.scale = fitScale * userZoom;

        // Center the content if aspect ratios differ?
        // For simplicity, we just align top-left logic or we could translate.
        // But AssetNode.update uses (x*scale, y*scale) so it draws from 0,0.

        // Refresh visuals immediately if possible
        requestLayout();

        // We need to trigger a redraw of assets and grid with new scale
        // But draw() is called by AnimationTimer in Controller.
        // Ideally we just update scale and next update() call fetches it.
        // However, static elements (grid) need explicit redraw.
        layersPane.getChildren().clear();
        drawGrid();
        // drawZones... we need the list of zones to redraw.
        // We can't easily redraw zones here without the list.
        // They will be redrawn on next Controller update loop anyway!
    }

    private void setupInteractions() {
        // Zoom Handling (Ctrl + Scroll)
        this.setOnScroll(e -> {
            if (e.isControlDown()) {
                e.consume();
                double delta = e.getDeltaY();
                double zoomFactor = 1.1; // 10% zoom
                if (delta < 0) {
                    userZoom /= zoomFactor;
                } else {
                    userZoom *= zoomFactor;
                }
                // Clamp User Zoom (0.1x to 10x relative to fit)
                userZoom = Math.max(0.1, Math.min(userZoom, 10.0));

                updateScale(); // Recalculate true scale
            }
        });
        // Zoom Removed as per user request

        this.setOnMouseClicked(e -> {
            boolean isBackground = !(e.getTarget() instanceof AssetNode); // Simple check
            // Better check: target is MapPane or LayersPane (not AssetNode)
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
                return; // Let AssetNode/Bubble handle it

            double wx = screenToWorldX(e.getX());
            double wy = screenToWorldY(e.getY());

            if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                // Right Click -> Move Command
                if (onMapRightClicked != null) {
                    onMapRightClicked.accept(new double[] { wx, wy });
                }
            } else {
                // Left Click -> Select / Target Mode
                if (missionTargetMode && onMapClicked != null) {
                    onMapClicked.accept(new double[] { wx, wy });
                } else {
                    // Do not deselectAll() here. Let controller decide.
                    if (onMapClicked != null) {
                        onMapClicked.accept(new double[] { wx, wy });
                    }
                }
            }
        });
    }

    public void update(List<ActifMobile> assets, List<Obstacle> obstacles, List<RestrictedZone> restrictedZones) {
        // 1. Sync Assets
        List<String> currentIds = new ArrayList<>();

        for (ActifMobile asset : assets) {
            currentIds.add(asset.getId());
            AssetNode node = assetNodes.get(asset.getId());

            if (node == null) {
                // New Asset
                node = new AssetNode(asset);
                final AssetNode finalNode = node;
                node.setOnMouseClicked(e -> {
                    e.consume();
                    handleAssetClick(finalNode.getAsset(), e.isControlDown());
                });
                assetsPane.getChildren().add(node);
                assetNodes.put(asset.getId(), node);
            }

            // Update Position & State
            node.update(scale);
            node.setSelected(selectedAssets.contains(asset));
        }

        // Remove dead assets
        List<String> toRemove = assetNodes.keySet().stream()
                .filter(id -> !currentIds.contains(id))
                .collect(Collectors.toList());
        toRemove.forEach(id -> {
            assetsPane.getChildren().remove(assetNodes.get(id));
            assetNodes.remove(id);
        });

        // 2. Draw Static Elements (Zones, Grid) only if needed or cleared
        // For simplicity, we can redraw zones if they change, but here we might assume
        // static?
        // Let's do a simple clear/redraw for zones to be safe, or optimize later.
        layersPane.getChildren().clear();
        drawGrid();
        drawZones(restrictedZones);
<<<<<<< HEAD
        drawObstacles(obstacles);
=======
>>>>>>> 2e1c7d997378ffc2a62a0fdc8796641db0ce29fa
    }

    private void drawGrid() {
        // Simple grid lines
        for (double x = 0; x <= SimConfig.WORLD_WIDTH; x += 100) {
            Line l = new Line(x * scale, 0, x * scale, SimConfig.WORLD_HEIGHT * scale);
            l.setStroke(Color.rgb(255, 255, 255, 0.2));
            layersPane.getChildren().add(l);
        }
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

<<<<<<< HEAD
    private void drawObstacles(List<Obstacle> obstacles) {
        if (obstacles == null)
            return;
        for (Obstacle obs : obstacles) {
            Circle c = new Circle(obs.getX() * scale, obs.getY() * scale, obs.getRadius() * scale);

            // Color Coding based on Z height
            if (obs.getZ() > 10) {
                // AERIAL (Mountain/Structure) - Red/Brown
                c.setFill(Color.rgb(139, 69, 19, 0.6)); // SaddleBrown
                c.setStroke(Color.DARKRED);

                // Add specific label for aerial
                Text t = new Text(obs.getX() * scale - 10, obs.getY() * scale, "H:" + (int) obs.getZ() + "m");
                t.setStyle("-fx-font-weight: bold; -fx-fill: white; -fx-stroke: black; -fx-stroke-width: 0.5px;");
                layersPane.getChildren().add(t);

            } else if (obs.getZ() < -10) {
                // UNDERWATER (Reef) - Dark Blue/Green
                c.setFill(Color.rgb(0, 100, 0, 0.5));
                c.setStroke(Color.DARKBLUE);
            } else {
                // SURFACE (Island) - Gray/Sand
                c.setFill(Color.rgb(100, 100, 100, 0.7));
                c.setStroke(Color.BLACK);
            }

            c.setStrokeWidth(2.0);
            layersPane.getChildren().add(c);
        }
    }

=======
>>>>>>> 2e1c7d997378ffc2a62a0fdc8796641db0ce29fa
    private void handleAssetClick(ActifMobile asset, boolean multiSelect) {
        if (missionTargetMode && onMapClicked != null) {
            // If in mission target mode, maybe we want to target the asset location?
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

    // --- API for Controller ---

    public void setScale(double s) {
        this.scale = s;
        // Resize pane
        this.setPrefSize(SimConfig.WORLD_WIDTH * scale, SimConfig.WORLD_HEIGHT * scale);
        // Force update positions? We wait for next loop usually, but can force layout.
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
        // Note: Do not trigger callback here to avoid loops if called from Controller
    }

    // --- Temporary Marker Logic ---
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

        // Reposition
        targetMarkerNode.setTranslateX(temporaryTarget[0] * scale);
        targetMarkerNode.setTranslateY(temporaryTarget[1] * scale);
    }

    public List<ActifMobile> getSelectedAssets() {
        return new ArrayList<>(selectedAssets);
    }

    // Callbacks are defined at the top of the file
    // private Consumer<double[]> onMapClicked; // Removed duplicate
    // private Consumer<double[]> onMapRightClicked; // Removed duplicate
    // private Consumer<List<ActifMobile>> onSelectionChanged; // Removed duplicate

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
