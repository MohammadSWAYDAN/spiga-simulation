package com.spiga.core;

/**
 * VehiculeSousMarin - Véhicule sous-marin (AUV)
 * Conforme à SPIGA-SPEC.txt section 1.2
 * 
 * Opère en 3D sous l'eau
 * Z est critique (profondeur max/min)
 * Sensible aux courants marins
 */
public class VehiculeSousMarin extends ActifMarin {

    public VehiculeSousMarin(String id, double x, double y, double profondeur) {
        super(id, x, y, profondeur, 20.0, 72.0); // 20 km/h, 72h autonomie
        this.profondeurMax = 0;
        this.profondeurMin = -1000; // -1000m
    }

    @Override
    public double getConsommation() {
        return 0.014; // 1.4% par heure
    }
}
