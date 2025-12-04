package com.spiga.ui;

import com.spiga.core.ActifMobile;
import com.spiga.environment.Obstacle;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;

/**
 * SideViewCanvas - IMPROVED PROFILE VIEW
 * Shows assets projected onto X-Z plane with clearer visualization.
 */
public class SideViewCanvas extends Canvas {

    private static final double MAX_ALTITUDE = 200;
    private static final double MAX_DEPTH = -200;
    private static final Font AXIS_FONT = Font.font("Consolas", 11);
    private static final Font LABEL_FONT = Font.font("Arial", FontWeight.BOLD, 14);
    private static final double AXIS_WIDTH = 50;

    public SideViewCanvas(double width, double height) {
        super(width, height);
    }

    public void draw(List<ActifMobile> assets, List<Obstacle> obstacles) {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.clearRect(0, 0, w, h);

        double range = MAX_ALTITUDE - MAX_DEPTH;
        double zeroY = (MAX_ALTITUDE / range) * h;

        // 1. Draw Zones
        LinearGradient sky = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#87CEEB")), new Stop(1, Color.web("#E0F7FA")));
        gc.setFill(sky);
        gc.fillRect(AXIS_WIDTH, 0, w - AXIS_WIDTH, zeroY);

        LinearGradient water = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#006994")), new Stop(1, Color.web("#001e36")));
        gc.setFill(water);
        gc.fillRect(AXIS_WIDTH, zeroY, w - AXIS_WIDTH, h - zeroY);

        // Surface Line
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeLine(AXIS_WIDTH, zeroY, w, zeroY);

        // 2. Draw Axis
        drawAxis(gc, h, zeroY);

        // Zone Labels
        gc.setFont(LABEL_FONT);
        gc.setFill(Color.rgb(50, 50, 50, 0.8));
        gc.fillText("AIR ZONE", AXIS_WIDTH + 10, 25);
        gc.setFill(Color.WHITE);
        gc.fillText("UNDERWATER", AXIS_WIDTH + 10, h - 15);

        // 3. Draw Obstacles (only if in reasonable Y range)
        if (obstacles != null) {
            for (Obstacle obs : obstacles) {
                // Only show obstacles in middle Y range (400-600) to reduce clutter
                if (obs.getY() < 300 || obs.getY() > 700)
                    continue;

                double cx = (obs.getX() / 1000.0) * (w - AXIS_WIDTH) + AXIS_WIDTH;
                double cy = mapZtoY(obs.getZ(), zeroY, h);

                // Scale radius down significantly
                double rX = (obs.getRadius() / 1000.0) * (w - AXIS_WIDTH) * 0.5;
                double rY = (obs.getRadius() / range) * h * 0.5;

                gc.setFill(Color.rgb(139, 69, 19, 0.3));
                gc.setStroke(Color.rgb(139, 69, 19, 0.6));
                gc.setLineWidth(1);
                gc.fillOval(cx - rX, cy - rY, rX * 2, rY * 2);
                gc.strokeOval(cx - rX, cy - rY, rX * 2, rY * 2);
            }
        }

        // 4. Draw Assets
        for (ActifMobile asset : assets) {
            double cx = (asset.getX() / 1000.0) * (w - AXIS_WIDTH) + AXIS_WIDTH;
            double cy = mapZtoY(asset.getZ(), zeroY, h);

            // Icon with glow
            gc.setFill(Color.WHITE);
            if (asset.getZ() > 0)
                gc.setFill(Color.RED);
            else if (asset.getZ() == 0)
                gc.setFill(Color.BLUE);
            else
                gc.setFill(Color.LIME);

            gc.fillOval(cx - 6, cy - 6, 12, 12);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeOval(cx - 6, cy - 6, 12, 12);

            // ID Label with background
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            String label = asset.getId();

            gc.setFill(Color.rgb(0, 0, 0, 0.7));
            gc.fillRoundRect(cx + 8, cy - 8, label.length() * 6 + 4, 14, 3, 3);

            gc.setFill(Color.WHITE);
            gc.fillText(label, cx + 10, cy + 2);
        }
    }

    private void drawAxis(GraphicsContext gc, double h, double zeroY) {
        gc.setFill(Color.rgb(40, 40, 40));
        gc.fillRect(0, 0, AXIS_WIDTH, h);

        gc.setStroke(Color.rgb(100, 100, 100));
        gc.setLineWidth(1);
        gc.strokeLine(AXIS_WIDTH, 0, AXIS_WIDTH, h);

        gc.setFill(Color.WHITE);
        gc.setFont(AXIS_FONT);

        // Ticks every 50m
        for (double z = MAX_DEPTH; z <= MAX_ALTITUDE; z += 50) {
            double y = mapZtoY(z, zeroY, h);
            gc.strokeLine(AXIS_WIDTH - 5, y, AXIS_WIDTH, y);

            String label = String.format("%.0f", z);
            gc.fillText(label, 8, y + 4);
        }

        // Bold 0m line
        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        gc.fillText("0m", 12, zeroY + 4);
    }

    private double mapZtoY(double z, double zeroY, double h) {
        double range = MAX_ALTITUDE - MAX_DEPTH;
        return (1 - (z - MAX_DEPTH) / range) * h;
    }
}
