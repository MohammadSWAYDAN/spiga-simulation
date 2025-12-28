package com.spiga.ui;

import com.spiga.core.*;
import com.spiga.environment.Obstacle;
import com.spiga.environment.RestrictedZone;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Vue Principale (La Carte 2D)
 * 
 * CONCEPTS CLES (GRAPHIQUE ET MATHS) :
 * 
 * 1. Canvas vs Scene :
 * - C'est quoi ? Une toile de peinture (Canvas) ou on dessine Pixel par Pixel.
 * - Pourquoi pas des Boutons ? Pour une simulation avec 100 objets qui bougent
 * 60 fois/sec, utiliser des objets JavaFX classiques (Nodes) serait trop lent.
 * Le Canvas est beaucoup plus performant (Dessin immediat).
 * 
 * 2. Systeme de Coordonnees (World vs Screen) :
 * - Concept Vital : Le "Monde" fait 1000km de large. L'Ecran fait 800 pixels.
 * - Il faut convertir : screenX = (worldX * scale) + offsetX.
 * Toutes les methodes "worldToScreen" servent a ca.
 */
public class MapCanvas extends Canvas {
    private List<ActifMobile> selectedAssets = new ArrayList<>();
    private Consumer<List<ActifMobile>> onSelectionChanged;
    private Consumer<double[]> onMapClicked;
    private boolean missionTargetMode = false;
    private List<ActifMobile> lastFleet;

    // --- Zoom & Pan Properties ---
    private double scale = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;
    private double lastMouseX, lastMouseY;

    // Live coordinate tracking
    private double mouseWorldX = 0;
    private double mouseWorldY = 0;
    private boolean showCoordinates = false;

    public MapCanvas(double width, double height) {
        super(width, height);
        setupMouseHandlers();
    }

    private void setupMouseHandlers() {
        // Zoom (Scroll)
        setOnScroll(event -> {
            double zoomFactor = 1.05;
            double deltaY = event.getDeltaY();
            if (deltaY < 0) {
                zoomFactor = 1 / zoomFactor;
            }

            // Zoom centered on mouse
            double mouseX = event.getX();
            double mouseY = event.getY();

            double newScale = scale * zoomFactor;
            // Limit zoom
            if (newScale < 0.1)
                newScale = 0.1;
            if (newScale > 10.0)
                newScale = 10.0;

            // Adjust offset to keep mouse pointed at same world coord
            // worldX = (screenX - offsetX) / scale
            // worldX must remain constant
            // (mouseX - oldOffsetX) / oldScale = (mouseX - newOffsetX) / newScale
            // newOffsetX = mouseX - (mouseX - oldOffsetX) * (newScale / oldScale)

            offsetX = mouseX - (mouseX - offsetX) * (newScale / scale);
            offsetY = mouseY - (mouseY - offsetY) * (newScale / scale);
            scale = newScale;

            draw(lastFleet != null ? lastFleet : new ArrayList<>(), null, null); // Redraw immediately
            event.consume();
        });

        // Pan (Press & Drag)
        setOnMousePressed(event -> {
            lastMouseX = event.getX();
            lastMouseY = event.getY();

            if (!event.isSecondaryButtonDown()) { // Left click logic (handled in released or click usually)
                // Consider moving selection logic here if needed, but click usually fine
            }
        });

        setOnMouseDragged(event -> {
            if (event.isSecondaryButtonDown() || event.isMiddleButtonDown()) { // Right/Middle drag to pan
                double dx = event.getX() - lastMouseX;
                double dy = event.getY() - lastMouseY;
                offsetX += dx;
                offsetY += dy;
                lastMouseX = event.getX();
                lastMouseY = event.getY();
                draw(lastFleet != null ? lastFleet : new ArrayList<>(), null, null);
            }
            // Update coords
            handleMouseMove(event);
        });

        setOnMouseClicked(this::handleMouseClick);
        setOnMouseMoved(this::handleMouseMove);
        setOnMouseExited(e -> showCoordinates = false);
    }

    private void handleMouseMove(MouseEvent event) {
        mouseWorldX = screenToWorldX(event.getX());
        mouseWorldY = screenToWorldY(event.getY());
        showCoordinates = true; // Always show coords for debug/zoom check
        // Redraw if we want live cursor update (optional, heavy)
        // draw(lastFleet != null ? lastFleet : new ArrayList<>(), null);
    }

    private void handleMouseClick(MouseEvent event) {
        if (event.isSecondaryButtonDown())
            return; // Ignore right click (Pan)

        double worldX = screenToWorldX(event.getX());
        double worldY = screenToWorldY(event.getY());

        if (missionTargetMode) {
            if (onMapClicked != null) {
                onMapClicked.accept(new double[] { worldX, worldY });
            }
        } else {
            ActifMobile clicked = findAssetAt(worldX, worldY);
            if (clicked != null) {
                if (event.isControlDown()) {
                    toggleSelection(clicked);
                } else {
                    selectSingleAsset(clicked);
                }
            } else {
                if (onMapClicked != null) {
                    onMapClicked.accept(new double[] { worldX, worldY });
                }
            }
        }
    }

    // --- Metrics Transformations ---
    private double worldToScreenX(double wx) {
        // World 0..1000 -> Screen
        // World 0..2000 -> Screen
        double maxX = SimConfig.WORLD_WIDTH;
        // Base mapping: 0->0, maxX->Width. Then apply scale & offset
        double baseScreenX = (wx / maxX) * getWidth();
        // ACTUALLY: Let's assume World Coords ARE the pixels at scale 1.0 for
        // simplicity?
        // OR: Keep the projection logic.
        // Let's keep projection: World(0..1000) maps to Screen(0..Width) at Scale 1.
        return (baseScreenX * scale) + offsetX;
    }

    private double worldToScreenY(double wy) {
        double maxY = SimConfig.WORLD_HEIGHT;
        double baseScreenY = (wy / maxY) * getHeight();
        return (baseScreenY * scale) + offsetY;
    }

    private double screenToWorldX(double sx) {
        double maxX = SimConfig.WORLD_WIDTH;
        // sx = (base * scale) + offset => base = (sx - offset) / scale
        // base = (wx / maxX) * W => wx = base * maxX / W
        double baseScreenX = (sx - offsetX) / scale;
        return (baseScreenX / getWidth()) * maxX;
    }

    private double screenToWorldY(double sy) {
        double maxY = SimConfig.WORLD_HEIGHT;
        double baseScreenY = (sy - offsetY) / scale;
        return (baseScreenY / getHeight()) * maxY;
    }

    // Size scaler
    private double scaleSize(double worldSize) {
        // If worldSize is in world units (0..1000), we need to scale it to screen
        // pixels
        // 1 world unit = (Width / 1000) pixels
        // 1 world unit = (Width / WorldWidth) pixels
        double maxX = SimConfig.WORLD_WIDTH;
        double pixelSizeAtScale1 = (worldSize / maxX) * getWidth();
        return pixelSizeAtScale1 * scale;
    }

    public void deselectAll() {
        selectedAssets.clear();
        notifySelectionChanged();
    }

    public void selectAll(List<ActifMobile> assets) {
        selectedAssets.clear();
        selectedAssets.addAll(assets);
        notifySelectionChanged();
    }

    public void draw(List<ActifMobile> assets, List<Obstacle> obstacles, List<RestrictedZone> restrictedZones) {
        this.lastFleet = assets;
        GraphicsContext gc = getGraphicsContext2D();

        gc.clearRect(0, 0, getWidth(), getHeight());

        // DEBUG LOG
        // if (assets.size() > 0)
        // System.out.println("DEBUG: MapCanvas Drawing " + assets.size() + " assets");

        drawBackground(gc);
        drawGrid(gc);

        // Draw Restricted Zones (Underlay)
        drawRestrictedZones(gc, restrictedZones);

        // Safety Zones (Underlay)
        drawSafetyZones(gc, assets);

        drawObstacles(gc, obstacles);

        List<ActifMobile> underwater = assets.stream().filter(a -> a.getZ() < 0).collect(Collectors.toList());
        List<ActifMobile> surface = assets.stream().filter(a -> a.getZ() == 0).collect(Collectors.toList());
        List<ActifMobile> air = assets.stream().filter(a -> a.getZ() > 0).collect(Collectors.toList());

        drawLayer(gc, underwater, 0.7);
        drawLayer(gc, surface, 1.0);
        drawLayer(gc, air, 1.0);

        drawSelectionHighlights(gc);

        // Always draw coords if requested
        if (showCoordinates) {
            drawLiveCoordinates(gc);
        }
    }

    private void drawRestrictedZones(GraphicsContext gc, List<RestrictedZone> zones) {
        if (zones == null)
            return;

        for (RestrictedZone z : zones) {
            double cx = worldToScreenX(z.getX());
            double cy = worldToScreenY(z.getY());
            double r = scaleSize(z.getRadius());

            // Draw Black Circle (Semi-transparent)
            gc.setFill(Color.rgb(0, 0, 0, 0.4)); // Black with 40% opacity
            gc.fillOval(cx - r, cy - r, r * 2, r * 2);

            // Solid Black Outline/Hatch
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            gc.setLineDashes(10d, 10d); // Dashed
            gc.strokeOval(cx - r, cy - r, r * 2, r * 2);
            gc.setLineDashes(0);

            // Label
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            gc.fillText("ZONE INTERDITE", cx - 40, cy);
            gc.fillText("(H < " + (int) z.getMaxZ() + "m)", cx - 30, cy + 15);
        }
    }

    private void drawSafetyZones(GraphicsContext gc, List<ActifMobile> assets) {
        for (ActifMobile asset : assets) {
            double cx = worldToScreenX(asset.getX());
            double cy = worldToScreenY(asset.getY());

            // Safety Radius in World Units
            // SimConfig.SAFETY_RADIUS is in PIXELS (from Spec/Assumption) or World Units?
            // "50px" implies screen pixels usually, but in zoomable map, should be world
            // units.
            // Let's assume SimConfig values are WORLD UNITS (0..1000 range context).
            double r = scaleSize(SimConfig.SAFETY_RADIUS);

            // Check for violations to determine color
            boolean violation = !SwarmValidator.isPlacementValid(asset.getX(), asset.getY(), asset.getZ(),
                    assets.stream().filter(a -> a != asset).collect(Collectors.toList()));

            // Actually SwarmValidator uses MIN_DISTANCE (50).
            // Visualization should ideally match that.
            // Let's verify: safety zone = personal space.

            if (violation) {
                gc.setFill(Color.rgb(255, 0, 0, 0.3)); // Red Alert
                gc.setStroke(Color.RED);
            } else {
                gc.setFill(Color.rgb(0, 255, 0, 0.1)); // Safe Green
                gc.setStroke(Color.rgb(0, 255, 0, 0.3));
            }

            gc.fillOval(cx - r, cy - r, r * 2, r * 2);
            gc.strokeOval(cx - r, cy - r, r * 2, r * 2);
        }
    }

    private void drawLiveCoordinates(GraphicsContext gc) {
        String coords = String.format("X: %.0f  Y: %.0f | Zoom: %.1fx", mouseWorldX, mouseWorldY, scale);

        double boxW = 200;
        double boxH = 30;
        // Position at bottom right
        double boxX = getWidth() - boxW - 10;
        double boxY = getHeight() - boxH - 10;

        gc.setFill(Color.rgb(0, 0, 0, 0.8));
        gc.fillRoundRect(boxX, boxY, boxW, boxH, 5, 5);

        gc.setStroke(Color.CYAN);
        gc.setLineWidth(2);
        gc.strokeRoundRect(boxX, boxY, boxW, boxH, 5, 5);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        gc.fillText(coords, boxX + 10, boxY + 20);
    }

    private void drawBackground(GraphicsContext gc) {
        LinearGradient ocean = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#4facfe")),
                new Stop(1, Color.web("#00f2fe")));
        gc.setFill(ocean);
        gc.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(Color.rgb(255, 255, 255, 0.15));
        gc.setLineWidth(0.5);

        // Draw grid based on world coordinates (e.g. every 100 units)
        double step = 100; // world units
        double maxWorldX = 1000; // approx
        double maxWorldY = 1000;

        for (double wx = 0; wx <= maxWorldX; wx += step) {
            double sx = worldToScreenX(wx);
            gc.strokeLine(sx, 0, sx, getHeight()); // Vertical lines
        }
        for (double wy = 0; wy <= maxWorldY; wy += step) {
            double sy = worldToScreenY(wy);
            gc.strokeLine(0, sy, getWidth(), sy); // Horizontal lines
        }
    }

    private void drawObstacles(GraphicsContext gc, List<Obstacle> obstacles) {
        if (obstacles == null)
            return;

        for (Obstacle obs : obstacles) {
            double cx = worldToScreenX(obs.getX());
            double cy = worldToScreenY(obs.getY());
            double r = scaleSize(obs.getRadius());

            if (obs.getZ() < 0) {
                gc.setFill(Color.rgb(47, 79, 79, 0.8));
                gc.setStroke(Color.rgb(0, 0, 0, 0.5));
            } else {
                gc.setFill(Color.rgb(139, 69, 19, 0.9));
                gc.setStroke(Color.rgb(100, 50, 0));
            }
            gc.fillOval(cx - r, cy - r, r * 2, r * 2);
            gc.strokeOval(cx - r, cy - r, r * 2, r * 2);
        }
    }

    private void drawLayer(GraphicsContext gc, List<ActifMobile> assets, double opacity) {
        gc.setGlobalAlpha(opacity);
        for (ActifMobile asset : assets) {
            drawAssetIcon(gc, asset);
        }
        gc.setGlobalAlpha(1.0);
    }

    private void drawAssetIcon(GraphicsContext gc, ActifMobile asset) {
        double cx = worldToScreenX(asset.getX());
        double cy = worldToScreenY(asset.getY());

        // Fixed icon size or scalable?
        // Typically icons stay constant size while map zooms, OR they scale.
        // User wants "real distance", so scaling icons might be better for "Circle
        // drone".
        // Let's scale "Circle drone" but keep labels readable.

        double size = scaleSize(10); // 10 world units radius approx
        if (size < 5)
            size = 5; // Min size visibility

        Color color = Color.WHITE;
        String shape = "CIRCLE";

        if (asset instanceof ActifAerien) {
            color = Color.RED;
            shape = "TRIANGLE";
        } else if (asset instanceof VehiculeSurface) {
            color = Color.BLUE;
            shape = "SHIP";
        } else if (asset instanceof VehiculeSousMarin) {
            color = Color.GREEN;
            shape = "SUB";
        }
        if (asset instanceof DroneLogistique)
            color = Color.YELLOW;
        if (asset instanceof NavireLogistique)
            color = Color.NAVY;
        if (asset instanceof SousMarinAttaque)
            color = Color.DARKGREEN;

        gc.setFill(color);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.5);

        switch (shape) {
            case "TRIANGLE":
                gc.beginPath();
                gc.moveTo(cx, cy - size / 2);
                gc.lineTo(cx + size / 2, cy + size / 2);
                gc.lineTo(cx - size / 2, cy + size / 2);
                gc.closePath();
                gc.fill();
                gc.stroke();
                break;
            case "SHIP":
                gc.beginPath();
                gc.moveTo(cx - size / 2, cy);
                gc.lineTo(cx + size / 2, cy);
                gc.lineTo(cx + size / 3, cy + size / 2);
                gc.lineTo(cx - size / 3, cy + size / 2);
                gc.closePath();
                gc.fill();
                gc.stroke();
                break;
            case "SUB":
                gc.fillOval(cx - size / 2, cy - size / 4, size, size / 2);
                gc.strokeOval(cx - size / 2, cy - size / 4, size, size / 2);
                gc.strokeLine(cx, cy - size / 4, cx, cy - size / 2);
                break;
            default:
                gc.fillOval(cx - size / 2, cy - size / 2, size, size);
                break;
        }

        if (asset.getState() == ActifMobile.AssetState.LOW_BATTERY) {
            gc.setStroke(Color.ORANGE);
            gc.strokeOval(cx - size, cy - size, size * 2, size * 2);
        } else if (asset.getState() == ActifMobile.AssetState.STOPPED) {
            gc.setStroke(Color.RED);
            gc.strokeOval(cx - size, cy - size, size * 2, size * 2);
        }

        String label = asset.getId();
        gc.setFill(Color.WHITE);
        gc.fillText(label, cx + size + 2, cy);
    }

    private void drawSelectionHighlights(GraphicsContext gc) {
        // Draw World Border (Red Limit)
        gc.setStroke(Color.RED);
        gc.setLineWidth(5);
        // Valid area is 0,0 to WORLD_WIDTH, WORLD_HEIGHT
        gc.strokeRect(worldToScreenX(0), worldToScreenY(0), scaleSize(SimConfig.WORLD_WIDTH),
                scaleSize(SimConfig.WORLD_HEIGHT));

        // Draw Coordinates Label at corners
        gc.setFill(Color.RED);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("(0,0)", worldToScreenX(10), worldToScreenY(20));
        gc.fillText("(" + (int) SimConfig.WORLD_WIDTH + "," + (int) SimConfig.WORLD_HEIGHT + ")",
                worldToScreenX(SimConfig.WORLD_WIDTH - 120), worldToScreenY(SimConfig.WORLD_HEIGHT - 10));

        // Draw selected assets range rings
        for (ActifMobile asset : selectedAssets) {
            double cx = worldToScreenX(asset.getX());
            double cy = worldToScreenY(asset.getY());

            double r = scaleSize(15);

            gc.setStroke(Color.CYAN);
            gc.setLineWidth(2);
            gc.setLineDashes(5);
            gc.strokeOval(cx - r * 1.5, cy - r * 1.5, r * 3, r * 3);
            gc.setLineDashes(0);

            if (asset.getState() == ActifMobile.AssetState.MOVING_TO_TARGET ||
                    asset.getState() == ActifMobile.AssetState.EXECUTING_MISSION) {
                double tx = worldToScreenX(asset.getTargetX());
                double ty = worldToScreenY(asset.getTargetY());
                gc.setStroke(Color.CYAN);
                gc.setLineDashes(5);
                gc.strokeLine(cx, cy, tx, ty);
                gc.setLineDashes(0);
                gc.fillOval(tx - 3, ty - 3, 6, 6);
            }
        }
    }

    public ActifMobile findAssetAt(double worldX, double worldY) {
        if (lastFleet == null)
            return null;
        for (ActifMobile asset : lastFleet) {
            double dist = Math.sqrt(Math.pow(asset.getX() - worldX, 2) + Math.pow(asset.getY() - worldY, 2));
            // Hitbox in world units
            if (dist < 30) {
                return asset;
            }
        }
        return null;
    }

    private void toggleSelection(ActifMobile asset) {
        if (asset != null) {
            if (selectedAssets.contains(asset)) {
                selectedAssets.remove(asset);
            } else {
                selectedAssets.add(asset);
            }
            notifySelectionChanged();
        }
    }

    private void selectSingleAsset(ActifMobile asset) {
        selectedAssets.clear();
        if (asset != null) {
            selectedAssets.add(asset);
        }
        notifySelectionChanged();
    }

    private void notifySelectionChanged() {
        if (onSelectionChanged != null) {
            onSelectionChanged.accept(new ArrayList<>(selectedAssets));
        }
    }

    public void setOnSelectionChanged(Consumer<List<ActifMobile>> callback) {
        this.onSelectionChanged = callback;
    }

    public void setOnMapClicked(Consumer<double[]> callback) {
        this.onMapClicked = callback;
    }

    public void setMissionTargetMode(boolean enabled) {
        this.missionTargetMode = enabled;
    }

    public boolean isMissionTargetMode() {
        return missionTargetMode;
    }

    public List<ActifMobile> getSelectedAssets() {
        return new ArrayList<>(selectedAssets);
    }

    public void selectAsset(ActifMobile asset) {
        selectSingleAsset(asset);
    }
}
