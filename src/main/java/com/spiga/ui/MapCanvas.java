package com.spiga.ui;

import com.spiga.core.*;
import com.spiga.environment.Obstacle;
import com.spiga.environment.ZoneOperation;
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
 * MapCanvas - WITH LIVE COORDINATE TRACKING
 */
public class MapCanvas extends Canvas {
    private ZoneOperation zone;
    private List<ActifMobile> selectedAssets = new ArrayList<>();
    private Consumer<List<ActifMobile>> onSelectionChanged;
    private Consumer<double[]> onMapClicked;
    private boolean missionTargetMode = false;
    private List<ActifMobile> lastFleet;

    // Live coordinate tracking
    private double mouseWorldX = 0;
    private double mouseWorldY = 0;
    private boolean showCoordinates = false;

    private static final Font LABEL_FONT = Font.font("Arial", FontWeight.BOLD, 10);

    public MapCanvas(double width, double height, ZoneOperation zone) {
        super(width, height);
        this.zone = zone;
        setupMouseHandlers();
    }

    private void setupMouseHandlers() {
        setOnMouseClicked(this::handleMouseClick);
        setOnMouseMoved(this::handleMouseMove);
        setOnMouseExited(e -> showCoordinates = false);
    }

    private void handleMouseMove(MouseEvent event) {
        double maxX = (zone != null) ? zone.getMaxX() : 1000;
        double maxY = (zone != null) ? zone.getMaxY() : 1000;

        mouseWorldX = (event.getX() / getWidth()) * maxX;
        mouseWorldY = (event.getY() / getHeight()) * maxY;
        showCoordinates = missionTargetMode;
    }

    private void handleMouseClick(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        double maxX = (zone != null) ? zone.getMaxX() : 1000;
        double maxY = (zone != null) ? zone.getMaxY() : 1000;

        double worldX = (x / getWidth()) * maxX;
        double worldY = (y / getHeight()) * maxY;

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
                // Clicked on empty space - Notify listener for potential move command
                if (onMapClicked != null) {
                    onMapClicked.accept(new double[] { worldX, worldY });
                }
                // Do NOT deselect automatically to allow click-to-move
            }
        }
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

    public void draw(List<ActifMobile> assets, List<Obstacle> obstacles) {
        this.lastFleet = assets;
        GraphicsContext gc = getGraphicsContext2D();

        gc.clearRect(0, 0, getWidth(), getHeight());

        drawBackground(gc);
        drawGrid(gc);
        drawObstacles(gc, obstacles);

        List<ActifMobile> underwater = assets.stream().filter(a -> a.getZ() < 0).collect(Collectors.toList());
        List<ActifMobile> surface = assets.stream().filter(a -> a.getZ() == 0).collect(Collectors.toList());
        List<ActifMobile> air = assets.stream().filter(a -> a.getZ() > 0).collect(Collectors.toList());

        drawLayer(gc, underwater, 0.7);
        drawLayer(gc, surface, 1.0);
        drawLayer(gc, air, 1.0);

        drawSelectionHighlights(gc);

        if (showCoordinates && missionTargetMode) {
            drawLiveCoordinates(gc);
        }
    }

    private void drawLiveCoordinates(GraphicsContext gc) {
        String coords = String.format("X: %.0f  Y: %.0f", mouseWorldX, mouseWorldY);

        double boxW = 120;
        double boxH = 30;
        double boxX = Math.min(getWidth() - boxW - 10, Math.max(10, (mouseWorldX / 1000.0) * getWidth()));
        double boxY = Math.max(10, (mouseWorldY / 1000.0) * getHeight() - 40);

        gc.setFill(Color.rgb(0, 0, 0, 0.8));
        gc.fillRoundRect(boxX, boxY, boxW, boxH, 5, 5);

        gc.setStroke(Color.CYAN);
        gc.setLineWidth(2);
        gc.strokeRoundRect(boxX, boxY, boxW, boxH, 5, 5);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        gc.fillText(coords, boxX + 8, boxY + 20);
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
        for (double i = 0; i < getWidth(); i += 50)
            gc.strokeLine(i, 0, i, getHeight());
        for (double i = 0; i < getHeight(); i += 50)
            gc.strokeLine(0, i, getWidth(), i);
    }

    private void drawObstacles(GraphicsContext gc, List<Obstacle> obstacles) {
        if (obstacles == null)
            return;
        double maxX = (zone != null) ? zone.getMaxX() : 1000;
        double maxY = (zone != null) ? zone.getMaxY() : 1000;

        for (Obstacle obs : obstacles) {
            double cx = (obs.getX() / maxX) * getWidth();
            double cy = (obs.getY() / maxY) * getHeight();
            double r = (obs.getRadius() / maxX) * getWidth();

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
        double maxX = (zone != null) ? zone.getMaxX() : 1000;
        double maxY = (zone != null) ? zone.getMaxY() : 1000;

        double cx = (asset.getX() / maxX) * getWidth();
        double cy = (asset.getY() / maxY) * getHeight();
        double size = 15;

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
        gc.setFont(LABEL_FONT);
        double textWidth = label.length() * 6;

        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRoundRect(cx + size, cy - 10, textWidth + 4, 14, 5, 5);

        gc.setFill(Color.WHITE);
        gc.fillText(label, cx + size + 2, cy);
    }

    private void drawSelectionHighlights(GraphicsContext gc) {
        double maxX = (zone != null) ? zone.getMaxX() : 1000;
        double maxY = (zone != null) ? zone.getMaxY() : 1000;

        for (ActifMobile asset : selectedAssets) {
            double cx = (asset.getX() / maxX) * getWidth();
            double cy = (asset.getY() / maxY) * getHeight();

            gc.setStroke(Color.CYAN);
            gc.setLineWidth(2);
            gc.setLineDashes(5);
            gc.strokeOval(cx - 20, cy - 20, 40, 40);
            gc.setLineDashes(0);

            if (asset.getState() == ActifMobile.AssetState.MOVING_TO_TARGET ||
                    asset.getState() == ActifMobile.AssetState.EXECUTING_MISSION) {
                double tx = (asset.getTargetX() / maxX) * getWidth();
                double ty = (asset.getTargetY() / maxY) * getHeight();
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
