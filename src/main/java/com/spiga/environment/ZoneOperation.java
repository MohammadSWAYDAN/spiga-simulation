package com.spiga.environment;

public class ZoneOperation {
    private double minX, maxX;
    private double minY, maxY;
    // Z is handled by specific assets (altitude/depth)

    public ZoneOperation(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    public boolean isInside(double x, double y) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxY() {
        return maxY;
    }

    // Getters for environment factors (Wind, Current, etc.) will be added here
}
