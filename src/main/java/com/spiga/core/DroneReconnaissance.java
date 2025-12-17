package com.spiga.core;

/**
 * DroneReconnaissance - Drone de reconnaissance
 * Conforme à SPIGA-SPEC.txt section 1.2
 * 
 * Spécialisé en vitesse et capacité de surveillance (haute altitude)
 */
public class DroneReconnaissance extends ActifAerien {

    private double rayonSurveillance;

    public DroneReconnaissance(String id, double x, double y, double altitude) {
        super(id, x, y, altitude, 120.0, 4.0); // 120 km/h, 4h autonomie
        this.rayonSurveillance = 1000.0; // 1km de rayon
        this.altitudeMax = 3000;
    }

    @Override
    public double getConsommation() {
        return 12.0; // 1200% per hour -> ~5 mins autonomy
    }

    public double getRayonSurveillance() {
        return rayonSurveillance;
    }
}
