package com.spiga.core;

/**
 * Classe concrète représentant un Navire de Surface.
 * <p>
 * Ce véhicule est contraint d'évoluer strictement à la surface (Z=0).
 * Il possède un radar de portée moyenne et une autonomie standard (5h).
 * </p>
 */
public class VehiculeSurface extends ActifMarin {

    /** Portée du radar de surveillance en mètres. */
    private double porteeRadar = 50.0;

    /**
     * Constructeur standard.
     * Initialise le navire à Z=0 avec 40 km/h et 5h d'autonomie.
     *
     * @param id Identifiant unique.
     * @param x  Position initiale X.
     * @param y  Position initiale Y.
     */
    public VehiculeSurface(String id, double x, double y) {
        super(id, x, y, 0.0, 40.0, 5.0); // 40 km/h, 5h autonomie (Demo)
        this.profondeurMax = 0;
        this.profondeurMin = 0;
    }

    /**
     * Force la position Z à 0 instantanément si une dérive est détectée.
     * Implémentation de l'invariant de surface.
     */
    @Override
    protected void clampPosition() {
        if (Math.abs(z) > 0.001) {
            z = 0; // Force surface
        }
    }

    /**
     * Définit la cible en ignorant toute composante verticale (Z).
     * <p>
     * Si une cible Z != 0 est demandée, elle est rejetée (logs et alerte)
     * et remplacée par 0.
     * </p>
     *
     * @param x Cible X.
     * @param y Cible Y.
     * @param z Cible Z (sera forcée à 0).
     */
    @Override
    public void setTarget(double x, double y, double z) {
        if (Math.abs(z) > 0.001) {
            System.out.println("⚠️ " + id + ": Rejet cible Z=" + z + ". Force à 0m (Surface).");
            setCollisionWarning("INVALID Z (Surface Only)");
        }
        super.setTarget(x, y, 0.0);
    }

    @Override
    public void deplacer(double targetX, double targetY, double targetZ) {
        setTarget(targetX, targetY, targetZ);
    }

    @Override
    public double getConsommation() {
        return 1.0;
    }

    /**
     * Calcule l'impact météo pour un navire de surface.
     * <p>
     * Sensible aux vagues (-40% max) et au vent (-20% max).
     * La vitesse minimale est garantie à 50%.
     * </p>
     */
    @Override
    protected double getSpeedMultiplier(com.spiga.environment.Weather w) {
        // speed = vmax * clamp(1 - 0.4*waves - 0.2*wind, 0.5, 1)
        double waves = w.getWaveIntensity();
        double wind = w.getWindIntensity();
        double factor = 1.0 - (0.4 * waves) - (0.2 * wind);
        return Math.max(0.5, Math.min(1.0, factor));
    }

    /**
     * Calcule la surconsommation due à la météo.
     * <p>
     * Consomme plus avec des vagues (+50% max) et du vent (+20% max).
     * </p>
     */
    @Override
    protected double getBatteryMultiplier(com.spiga.environment.Weather w) {
        // batteryMult = 1 + 0.5*waves + 0.2*wind
        double waves = w.getWaveIntensity();
        double wind = w.getWindIntensity();
        return 1.0 + (0.5 * waves) + (0.2 * wind);
    }

    public double getRayonRadar() {
        return porteeRadar;
    }
}
