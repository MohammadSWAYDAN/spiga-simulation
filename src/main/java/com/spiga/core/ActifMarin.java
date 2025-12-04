package com.spiga.core;

/**
 * Classe abstraite ActifMarin - Actifs marins
 * Conforme à SPIGA-SPEC.txt section 1.1
 * 
 * Hérite de ActifMobile
 * Véhicules de surface (ASV) et sous-marins
 * Sensibles aux courants marins et à la profondeur
 */
public abstract class ActifMarin extends ActifMobile {

    protected double profondeurMax;
    protected double profondeurMin;

    public ActifMarin(String id, double x, double y, double profondeur, double vitesseMax, double autonomieMax) {
        super(id, x, y, profondeur, vitesseMax, autonomieMax);
        this.profondeurMax = 0; // Surface par défaut
        this.profondeurMin = 0;
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
