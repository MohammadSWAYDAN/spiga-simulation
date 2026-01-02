package com.spiga.core;

/**
 * Classe Concrete/Intermediaire : Vehicule de Surface
 * 
 * CONCEPTS CLES :
 * 
 * 1. Heritage restrictif :
 * - C'est quoi ? Utiliser l'heritage pour IMPOSER une contrainte.
 * - Pourquoi ici ? Un navire de surface EST UN actif marin, MAIS sa profondeur
 * est forcee a 0.
 * Cette classe verrouille Z=0 pour tous ses enfants (NavirePatrouille, etc.).
 * 
 * 2. Override pour Controle :
 * - La methode deplacer est redefinie pour ignorer toute demande de plongee (Z
 * < 0) ou de vol (Z > 0).
 */
public class VehiculeSurface extends ActifMarin {

    private double porteeRadar = 50.0; // Default radar range

    public VehiculeSurface(String id, double x, double y) {
        super(id, x, y, 0.0, 40.0, 5.0); // 40 km/h, 5h autonomie (Demo)
        this.profondeurMax = 0;
        this.profondeurMin = 0;
    }

    @Override
    protected void clampPosition() {
        if (Math.abs(z) > 0.001) {
            z = 0; // Force surface
        }
    }

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

    @Override
    protected double getSpeedMultiplier(com.spiga.environment.Weather w) {
        // speed = vmax * clamp(1 - 0.4*waves - 0.2*wind, 0.5, 1)
        double waves = w.getWaveIntensity();
        double wind = w.getWindIntensity();
        double factor = 1.0 - (0.4 * waves) - (0.2 * wind);
        return Math.max(0.5, Math.min(1.0, factor));
    }

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
