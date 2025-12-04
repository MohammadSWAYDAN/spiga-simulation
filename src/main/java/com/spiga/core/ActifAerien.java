package com.spiga.core;

/**
 * Classe abstraite ActifAerien - Actifs aériens
 * Conforme à SPIGA-SPEC.txt section 1.1
 * 
 * Hérite de ActifMobile
 * Drones évoluant principalement en 2D (X, Y) avec composante Z (Altitude)
 * Sensibles aux facteurs atmosphériques
 */
public abstract class ActifAerien extends ActifMobile {

    protected double altitudeMax;
    protected double altitudeMin;

    public ActifAerien(String id, double x, double y, double altitude, double vitesseMax, double autonomieMax) {
        super(id, x, y, altitude, vitesseMax, autonomieMax);
        this.altitudeMax = 5000; // 5000m par défaut
        this.altitudeMin = 0;
    }

    @Override
    public void deplacer(double targetX, double targetY, double targetZ) {
        // Vérification altitude
        if (targetZ < altitudeMin)
            targetZ = altitudeMin;
        if (targetZ > altitudeMax)
            targetZ = altitudeMax;

        super.deplacer(targetX, targetY, targetZ);
    }

    public double getAltitudeMax() {
        return altitudeMax;
    }

    public double getAltitudeMin() {
        return altitudeMin;
    }
}
