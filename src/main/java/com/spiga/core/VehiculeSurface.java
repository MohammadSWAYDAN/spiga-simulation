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

    public VehiculeSurface(String id, double x, double y) {
        super(id, x, y, 0.0, 40.0, 48.0); // 40 km/h, 48h autonomie
        this.profondeurMax = 0;
        this.profondeurMin = 0;
    }

    @Override
    protected double getWeatherImpact(com.spiga.environment.Weather w) {
        double impact = 1.0;
        // Sea State + Wind Sensitivity
        // Wind affects surface vessels
        if (w.getWindSpeed() > 30) {
            impact += (w.getWindSpeed() - 30) * 0.005;
        }
        // Waves
        if (w.getSeaWaveHeight() > 1.0) {
            impact += (w.getSeaWaveHeight() - 1.0) * 0.2; // Significant impact
        }
        return impact;
    }

    @Override
    protected void clampPosition() {
        if (Math.abs(z) > 0.001) {
            z = 0; // Force surface
        }
    }

    @Override
    public void deplacer(double targetX, double targetY, double targetZ) {
        if (Math.abs(targetZ) > 0.1) {
            System.out.println("⚠️ " + id + ": Rejet cible Z=" + targetZ + ". Force à 0m (Surface).");
        }
        super.deplacer(targetX, targetY, 0.0);
    }

    @Override
    public double getConsommation() {
        return 0.02; // 2% par heure
    }
}
