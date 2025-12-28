package com.spiga.core;

/**
 * Classe Abstraite Intermediaire : Actif Marin
 * 
 * CONCEPTS CLES (SPECIALISATION) :
 * 
 * 1. Factorisation du Code :
 * - C'est quoi ? Mettre le code commun au meme endroit.
 * - Pourquoi ici ? Sous-marins et Navires de surface partagent des contraintes
 * d'eau (Profondeur).
 * Au lieu de reecrire la gestion de la profondeur dans les deux, on la met ici.
 * 
 * 2. Distinction Surface/Sous-marin :
 * - Cette classe sert de socle pour VehiculeSurface (Z=0 fixe) et
 * VehiculeSousMarin (Z negatif).
 */
public abstract class ActifMarin extends ActifMobile {

    protected double profondeurMax;
    protected double profondeurMin;

    public ActifMarin(String id, double x, double y, double profondeur, double vitesseMax, double autonomieMax) {
        super(id, x, y, profondeur, vitesseMax, autonomieMax);
        this.profondeurMax = 0; // Surface par défaut
        this.profondeurMin = 0;
    }

    public void setProfondeurMax(double profondeurMax) {
        this.profondeurMax = profondeurMax;
    }

    @Override
    protected double getSpeedEfficiency(com.spiga.environment.Weather w) {
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
