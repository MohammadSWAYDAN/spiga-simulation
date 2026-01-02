package com.spiga.core;

/**
 * Classe Abstraite Intermediaire : Actif Aerien
 * 
 * CONCEPTS CLES (HERITAGE EN COUCHES) :
 * 
 * 1. Heritage Intermediaire :
 * - C'est quoi ? Une classe qui herite (de ActifMobile) mais qui est encore
 * trop generale pour etre utilisee seule (Abstract).
 * - Pourquoi ? Elle factorise ce qui est unique au vol : Altitude max/min,
 * impact du vent sur le vol.
 * Les bateaux n'ont pas ca, donc on ne met pas ca dans ActifMobile.
 * 
 * 2. Hierarchie :
 * ActifMobile (Grand-pere) -> ActifAerien (Pere) -> DroneReconnaissance (Fils).
 */
public abstract class ActifAerien extends ActifMobile {

    // Attributs propres aux objets volants
    protected double altitudeMax;
    protected double altitudeMin;

    public ActifAerien(String id, double x, double y, double altitude, double vitesseMax, double autonomieMax) {
        // Appelle le constructeur du Grand-Père (ActifMobile)
        super(id, x, y, altitude, vitesseMax, autonomieMax);
        this.altitudeMax = 5000;
        this.altitudeMin = 0;
    }

    // Réutilisation : Cette méthode sera utilisée par TOUS les drones.
    @Override
    protected double getSpeedMultiplier(com.spiga.environment.Weather w) {
        // speed = vmax * clamp(1 - 0.5*rain - 0.2*wind, 0.4, 1)
        double rain = w.getRainIntensity();
        double wind = w.getWindIntensity();
        double factor = 1.0 - (0.5 * rain) - (0.2 * wind);
        return Math.max(0.4, Math.min(1.0, factor));
    }

    @Override
    protected double getSpeedEfficiency(com.spiga.environment.Weather w) {
        // Base Wind Drag
        double efficiency = super.getSpeedEfficiency(w);

        // Rain Impact Logic:
        // Model: reduces speed by up to 50% at max rain (100)
        if (w.getRainIntensity() > 0) {
            double rainPenalty = (w.getRainIntensity() / 100.0) * 0.5; // Max 0.5
            efficiency -= rainPenalty;
        }

        // Safety floor
        if (efficiency < 0.1)
            efficiency = 0.1;

        return efficiency;
    }

    @Override
    protected double getBatteryMultiplier(com.spiga.environment.Weather w) {
        // batteryMult = 1 + 0.6*rain + 0.3*wind
        double rain = w.getRainIntensity();
        double wind = w.getWindIntensity();
        return 1.0 + (0.6 * rain) + (0.3 * wind);
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
    public void setTarget(double x, double y, double z) {
        double clampedZ = z;
        if (clampedZ < 1) {
            System.out.println("⚠️ " + id + ": Rejet cible Z=" + clampedZ + " (Sous l'eau/Sol). Force à 1m.");
            clampedZ = 1;
        }
        if (clampedZ > 150) {
            System.out.println("⚠️ " + id + ": Rejet cible Z=" + clampedZ + " (Trop haut). Force à 150m.");
            clampedZ = 150;
            // Visible Alert for Log Status
            setCollisionWarning("PLAFOND ATTEINT (150m)");
        }
        super.setTarget(x, y, clampedZ);
    }

    @Override
    public void deplacer(double targetX, double targetY, double targetZ) {
        // Enforce Aerial Constraints [1, 150] - Pre-check
        // Delegate to setTarget
        setTarget(targetX, targetY, targetZ);
    }

    public double getAltitudeMax() {
        return altitudeMax;
    }

    public double getAltitudeMin() {
        return altitudeMin;
    }
}
