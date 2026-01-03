package com.spiga.core;

/**
 * Classe concrète représentant un Véhicule Sous-Marin Autonome (AUV).
 * <p>
 * Ce véhicule évolue sous la surface (Z variant de -150 à 0).
 * Il est lent (20 km/h) mais possède une très grande autonomie (72h pour
 * mission longue durée).
 * </p>
 */
public class VehiculeSousMarin extends ActifMarin {

    private long lastDepthAlertTime = 0;
    private static final long ALERT_COOLDOWN = 5000; // 5 seconds

    /**
     * Constructeur standard.
     * Initialise l'AUV avec Vitesse=20km/h et Autonomie=72h.
     *
     * @param id         Identifiant unique.
     * @param x          Position X.
     * @param y          Position Y.
     * @param profondeur Profondeur initiale Z (doit être négative ou nulle).
     */
    public VehiculeSousMarin(String id, double x, double y, double profondeur) {
        super(id, x, y, profondeur, 20.0, 72.0); // Mis à jour pour refléter comm: 72h
        this.profondeurMax = 0;
        this.profondeurMin = -150; // Contrainte -150m
    }

    /**
     * Applique les contraintes de profondeur [-150, 0].
     */
    @Override
    protected void clampPosition() {
        // Enforce [-150, 0]
        if (z > 0)
            z = 0;
        if (z < -150)
            z = -150;
    }

    /**
     * Définit la cible avec validation de la profondeur.
     * <p>
     * Si la cible est hors des limites [-150, 0], elle est bornée et une alerte est
     * levée.
     * </p>
     *
     * @param x Cible X.
     * @param y Cible Y.
     * @param z Cible Z.
     */
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
