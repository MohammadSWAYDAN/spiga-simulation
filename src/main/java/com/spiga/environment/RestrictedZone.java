package com.spiga.environment;

/**
 * Représente une zone géographique réglementée (No-Fly Zone, Zone militaire).
 * <p>
 * <strong>Abstraction :</strong> Modélise un cylindre vertical défini par :
 * <ul>
 * <li>Un cercle 2D (Centre X,Y + Rayon).</li>
 * <li>Une plage d'altitude/profondeur (Min Z, Max Z).</li>
 * </ul>
 * Les drones logistiques ne peuvent pas la traverser (Mur), tandis que certains
 * actifs
 * peuvent être autorisés (Reconnaissance).
 * </p>
 */
public class RestrictedZone {
    private String id;
    private double x, y;
    private double radius;
    private double minZ, maxZ;

    /**
     * Crée une nouvelle zone restreinte.
     * 
     * @param id     Identifiant nom.
     * @param x      Centre X.
     * @param y      Centre Y.
     * @param radius Rayon.
     * @param minZ   Plancher Z.
     * @param maxZ   Plafond Z.
     */
    public RestrictedZone(String id, double x, double y, double radius, double minZ, double maxZ) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.minZ = minZ;
        this.maxZ = maxZ;
    }

    /**
     * Vérifie si un point se trouve dans le volume interdit.
     * 
     * @param ax X à tester.
     * @param ay Y à tester.
     * @param az Z à tester.
     * @return true si dedans.
     */
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
