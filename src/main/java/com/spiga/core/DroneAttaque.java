package com.spiga.core;

/**
 * DroneAttaque - Drone d'attaque
 * Classe supplémentaire pour atteindre 10+ classes
 */
public class DroneAttaque extends ActifAerien {

    private int munitions;

    public DroneAttaque(String id, double x, double y, double altitude) {
        super(id, x, y, altitude, 150.0, 3.0); // 150 km/h, 3h autonomie
        this.munitions = 4;
    }

    @Override
    public double getConsommation() {
        return 0.33; // 33% par heure
    }

    public void tirer() {
        if (munitions > 0) {
            munitions--;
            System.out.println(getId() + " a tiré. Munitions restantes: " + munitions);
        }
    }

    public int getMunitions() {
        return munitions;
    }
}
