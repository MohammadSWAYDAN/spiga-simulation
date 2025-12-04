package com.spiga.core;

/**
 * NavirePatrouille - Navire de patrouille
 * Hérite de VehiculeSurface
 */
public class NavirePatrouille extends VehiculeSurface {

    private double rayonRadar;

    public NavirePatrouille(String id, double x, double y) {
        super(id, x, y);
        this.vitesseMax = 50.0;
        this.autonomieMax = 96.0; // 4 jours
        this.autonomieActuelle = autonomieMax;
        this.rayonRadar = 50000.0; // 50km
    }

    @Override
    public double getConsommation() {
        return 0.01; // 1% par heure
    }

    public double getRayonRadar() {
        return rayonRadar;
    }
}
