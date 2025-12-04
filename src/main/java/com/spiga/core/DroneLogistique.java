package com.spiga.core;

/**
 * DroneLogistique - Drone logistique
 * Conforme à SPIGA-SPEC.txt section 1.2
 * 
 * Spécialisé en capacité de charge utile et autonomie
 * Vitesse réduite, consommation optimisée
 */
public class DroneLogistique extends ActifAerien {

    private double chargeUtileMax;
    private double chargeActuelle;

    public DroneLogistique(String id, double x, double y, double altitude) {
        super(id, x, y, altitude, 60.0, 8.0); // 60 km/h, 8h autonomie
        this.chargeUtileMax = 50.0; // 50kg
        this.chargeActuelle = 0.0;
    }

    @Override
    public double getConsommation() {
        // Consommation augmente avec la charge
        double facteurCharge = 1.0 + (chargeActuelle / chargeUtileMax) * 0.5;
        return 0.125 * facteurCharge; // 12.5% base par heure
    }

    public void charger(double poids) {
        if (chargeActuelle + poids <= chargeUtileMax) {
            chargeActuelle += poids;
        }
    }

    public void decharger() {
        chargeActuelle = 0;
    }

    public double getChargeUtileMax() {
        return chargeUtileMax;
    }

    public double getChargeActuelle() {
        return chargeActuelle;
    }
}
