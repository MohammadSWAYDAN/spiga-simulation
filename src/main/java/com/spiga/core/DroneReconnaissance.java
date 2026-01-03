package com.spiga.core;

/**
 * Classe concrète représentant un Drone de Reconnaissance rapide.
 * <p>
 * Ce drone léger est conçu pour l'observation rapide.
 * Il est très rapide (120 km/h) mais possède une autonomie limitée (4h).
 * Il dispose d'un capteur optique de surveillance (simulé par un rayon de
 * surveillance).
 * </p>
 */
public class DroneReconnaissance extends ActifAerien {

    /** Rayon de surveillance en mètres. */
    private double rayonSurveillance;

    /**
     * Constructeur standard.
     * Initialise le drone avec 120 km/h et 4h d'autonomie.
     *
     * @param id       Identifiant unique.
     * @param x        Position X.
     * @param y        Position Y.
     * @param altitude Altitude Z.
     */
    public DroneReconnaissance(String id, double x, double y, double altitude) {
        // APPEL AU PARENT :
        // On remplit le "contrat" de ActifAerien en lui donnant les infos de base
        // (ID, pos, vitesse=120, autonomie=4h)
        super(id, x, y, altitude, 120.0, 4.0);

        // Initialisation specifique locale
        this.rayonSurveillance = 1000.0; // 1km de rayon
        this.altitudeMax = 3000;
    }

    /**
     * Retourne la consommation spécifique de ce drone.
     * <p>
     * Consommation fixe de 2.0 unités/heure.
     * Avec une capacité de 4.0, cela donne bien 2h d'autonomie réelle à pleine
     * charge,
     * mais le constructeur déclare 4.0 d'autonomie max pour le calcul de jauge.
     * (Vérifiez la cohérence autonomie/conso dans SimConfig si nécessaire).
     * </p>
     *
     * @return 2.0
     */
    @Override
    public double getConsommation() {
        return 2.0;
    }

    /**
     * Retourne le rayon de surveillance du capteur.
     * 
     * @return Rayon en mètres.
     */
    public double getRayonSurveillance() {
        return rayonSurveillance;
    }
}
