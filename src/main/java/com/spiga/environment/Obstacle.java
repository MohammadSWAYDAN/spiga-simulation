package com.spiga.environment;

/**
 * Représente un obstacle physique statique (Rocher, île, montagne).
 * <p>
 * Un obstacle est défini par une position 3D (x,y,z) et un rayon.
 * Il est utilisé par le moteur physique pour repousser les actifs (Champs de
 * potentiel)
 * et détecter les collisions "dures".
 * </p>
 */
public class Obstacle {
    private double x, y, z;
    /** Rayon d'encombrement de l'obstacle. */
    private double radius;

    /**
     * Crée un nouvel obstacle.
     * 
     * @param x      Position X.
     * @param y      Position Y.
     * @param z      Position Z.
     * @param radius Rayon en mètres.
     */
    public Obstacle(double x, double y, double z, double radius) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
    }

    /**
     * Vérifie si un point se trouve à l'intérieur de l'obstacle.
     * 
     * @param ox Coordonnée X à tester.
     * @param oy Coordonnée Y à tester.
     * @param oz Coordonnée Z à tester.
     * @return true si collision (distance < rayon).
     */
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
