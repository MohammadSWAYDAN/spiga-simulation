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
        this.altitudeMax = 5000; // Legacy default, but we enforce 150 now
        this.altitudeMin = 0;
    }

    @Override
    protected double getWeatherImpact(com.spiga.environment.Weather w) {
        double impact = 1.0;
        // High Wind Sensitivity
        if (w.getWindSpeed() > 15) {
            impact += (w.getWindSpeed() - 15) * 0.015;
        }
        // Rain Sensitivity
        if (w.getRainIntensity() > 20) {
            impact += 0.15;
        }
        return impact;
    }

    @Override
    protected void clampPosition() {
        // Enforce [1, 150]
        if (z < 1)
            z = 1;
        if (z > 150)
            z = 150;
    }

    @Override
    public void deplacer(double targetX, double targetY, double targetZ) {
        // Enforce Aerial Constraints [1, 150] - Pre-check
        if (targetZ < 1) {
            System.out.println("⚠️ " + id + ": Rejet cible Z=" + targetZ + " (Sous l'eau/Sol). Force à 1m.");
            targetZ = 1;
        }
        if (targetZ > 150) {
            System.out.println("⚠️ " + id + ": Rejet cible Z=" + targetZ + " (Trop haut). Force à 150m.");
            targetZ = 150;
        }
        super.deplacer(targetX, targetY, targetZ);
    }

    public double getAltitudeMax() {
        return altitudeMax;
    }

    public double getAltitudeMin() {
        return altitudeMin;
    }
}
