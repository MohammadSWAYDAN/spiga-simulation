package com.spiga.environment;

/**
 * Zone Interdite (Zone No-Fly / No-Sail).
 * 
 * Abstraction : Modelise une zone cylindrique (Cercle + Hauteur min/max) ou les
 * actifs ne doivent pas entrer.
 * Utilise par SimulationService pour valider les deplacements.
 */
public class RestrictedZone {
    private String id;
    private double x, y;
    private double radius;
    private double minZ, maxZ; // Height limits (e.g., 0 to 100m)

    public RestrictedZone(String id, double x, double y, double radius, double minZ, double maxZ) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.minZ = minZ;
        this.maxZ = maxZ;
    }

    public boolean isInside(double ax, double ay, double az) {
        // Check Z first (optimisation)
        if (az < minZ || az > maxZ)
            return false;

        // Check 2D distance
        double dx = ax - x;
        double dy = ay - y;
        return (dx * dx + dy * dy) <= (radius * radius);
    }

    // Getters
    public String getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRadius() {
        return radius;
    }

    public double getMinZ() {
        return minZ;
    }

    public double getMaxZ() {
        return maxZ;
    }
}
