package com.spiga.core;

/**
 * Classe Concrete : Vehicule Sous-Marin (AUV)
 * 
 * CONCEPTS CLES :
 * 
 * 1. Heritage Concret :
 * - C'est quoi ? Une classe "finale" qu'on peut instancier (creer).
 * - Heritage : ActifMobile -> ActifMarin -> VehiculeSousMarin.
 * 
 * 2. Surcharge de Methode (@Override) :
 * - Exemple : getWeatherImpact. Un sous-marin se fiche de la pluie
 * (contrairement aux drones).
 * En revanche, il est sensible aux vagues/courants. On reecrit donc la logique
 * ici pour coller a la realite physique du sous-marin.
 */
public class VehiculeSousMarin extends ActifMarin {

    private long lastDepthAlertTime = 0;
    private static final long ALERT_COOLDOWN = 5000; // 5 seconds

    public VehiculeSousMarin(String id, double x, double y, double profondeur) {
        // Constructeur parent : Vitesse=20km/h, Autonomie=72h
        super(id, x, y, profondeur, 20.0, 6.0);
        this.profondeurMax = 0;
        this.profondeurMin = -150; // Contrainte -150m
    }

    @Override
    protected void clampPosition() {
        // Enforce [-150, 0]
        if (z > 0)
            z = 0;
        if (z < -150)
            z = -150;
    }

    @Override
    public void setTarget(double x, double y, double z) {
        double clampedZ = z;

        // Constraint 1: Max Height 0
        if (clampedZ > 0) {
            System.out.println(id + ": Rejet cible Z=" + clampedZ + " (Surface). Force à 0m.");
            clampedZ = 0;
        }

        // Constraint 2: Min Depth -150
        if (clampedZ < -150) {
            long now = System.currentTimeMillis();
            if (now - lastDepthAlertTime > ALERT_COOLDOWN) {
                setCollisionWarning("⚠️ PROFONDEUR LIMITE (-150m) ATTEINTE!");
                lastDepthAlertTime = now;
                System.out.println("⚠️ ALERTE: " + id + " bloque à -150m.");
            }
            clampedZ = -150;
        }

        super.setTarget(x, y, clampedZ);
    }

    @Override
    public void deplacer(double targetX, double targetY, double targetZ) {
        // Delegate to setTarget for consistency
        setTarget(targetX, targetY, targetZ);
    }

    @Override
    public double getConsommation() {
        return 1.0; // 1.0h consumed per hour
    }
}
