package com.spiga.core;

/**
 * VehiculeSurface - Véhicule de surface
 * Conforme à SPIGA-SPEC.txt section 1.2
 * 
 * Opère à la surface (Z constant = 0)
 * Sensible au vent et à l'état de la mer
 */
public class VehiculeSurface extends ActifMarin {

    public VehiculeSurface(String id, double x, double y) {
        super(id, x, y, 0.0, 40.0, 48.0); // 40 km/h, 48h autonomie
        this.profondeurMax = 0;
        this.profondeurMin = 0;
    }

    @Override
    public double getConsommation() {
        return 0.02; // 2% par heure
    }
}
