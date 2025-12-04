package com.spiga.environment;

public class Obstacle {
    private double x, y, z;
    private double radius; // Simplified as sphere/circle

    public Obstacle(double x, double y, double z, double radius) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
    }

    public boolean isCollision(double ox, double oy, double oz) {
        double dist = Math.sqrt(Math.pow(x - ox, 2) + Math.pow(y - oy, 2) + Math.pow(z - oz, 2));
        return dist < radius;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getRadius() {
        return radius;
    }
}
