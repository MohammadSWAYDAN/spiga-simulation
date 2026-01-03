package com.spiga.core;

import com.spiga.environment.Weather;

/**
 * Classe abstraite intermédiaire représentant un actif marin (Navire ou
 * Sous-marin).
 * <p>
 * Cette classe factorise la logique commune aux véhicules évoluant en milieu
 * aquatique,
 * notamment la gestion de la profondeur (Z <= 0) et l'impact des vagues sur la
 * navigation.
 * </p>
 *
 * @see VehiculeSurface
 * @see VehiculeSousMarin
 */
public abstract class ActifMarin extends ActifMobile {

    /** Profondeur maximale (la plus proche de la surface, souvent 0). */
    protected double profondeurMax;
    /** Profondeur minimale (la plus profonde, ex: -500). */
    protected double profondeurMin;

    /**
     * Constructeur pour un actif marin.
     *
     * @param id           Identifiant unique.
     * @param x            Position X.
     * @param y            Position Y.
     * @param profondeur   Profondeur initiale Z.
     * @param vitesseMax   Vitesse max.
     * @param autonomieMax Autonomie max.
     */
    public ActifMarin(String id, double x, double y, double profondeur, double vitesseMax, double autonomieMax) {
        super(id, x, y, profondeur, vitesseMax, autonomieMax);
        this.profondeurMax = 0; // Surface par défaut
        this.profondeurMin = 0;
    }

    public void setProfondeurMax(double profondeurMax) {
        this.profondeurMax = profondeurMax;
    }

    /**
     * Calcule l'efficacité de déplacement en fonction de la météo marine.
     * <p>
     * Modèle :
     * <ul>
     * <li>Les vagues réduisent la vitesse (-5% par mètre de vague).</li>
     * <li>Si immergé (Z < -5m), l'effet du vent est atténué.</li>
     * </ul>
     * </p>
     *
     * @param w Conditions météo.
     * @return Facteur d'efficacité (0.1 à 1.0).
     */
    @Override
    protected double getSpeedEfficiency(Weather w) {
        // Base Wind Drag (Less relevant for underwater, but keeps inheritance chain)
        double efficiency = super.getSpeedEfficiency(w);

        // Recover some wind penalty if underwater (simplified model)
        if (z < -5) {
            efficiency += 0.05; // Less wind drag underwater
        }

        // Wave Impact (Specific to Marine)
        // 5% speed loss per meter of wave height
        if (w.getSeaWaveHeight() > 0) {
            double wavePenalty = w.getSeaWaveHeight() * 0.05;
            efficiency -= wavePenalty;
        }

        if (efficiency > 1.0)
            efficiency = 1.0;
        if (efficiency < 0.1)
            efficiency = 0.1;

        return efficiency;
    }

    @Override
    public void deplacer(double targetX, double targetY, double targetZ) {
        // Vérification profondeur
        if (targetZ > profondeurMax)
            targetZ = profondeurMax;
        if (targetZ < profondeurMin)
            targetZ = profondeurMin;

        super.deplacer(targetX, targetY, targetZ);
    }

    public double getProfondeurMax() {
        return profondeurMax;
    }

    public double getProfondeurMin() {
        return profondeurMin;
    }
}
