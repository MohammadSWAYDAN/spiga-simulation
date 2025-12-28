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

    public VehiculeSousMarin(String id, double x, double y, double profondeur) {
        // Constructeur parent : Vitesse=20km/h, Autonomie=72h
        super(id, x, y, profondeur, 20.0, 72.0);
        this.profondeurMax = 0;
        this.profondeurMin = -1000; // Peut descendre à -1000m
    }

    @Override
    protected double getWeatherImpact(com.spiga.environment.Weather w) {
        double impact = 1.0;
        // Less sensitive to wind/rain
        // Sensitive to "Currents" (simulated via Waves for now as proxy)
        if (w.getSeaWaveHeight() > 2.0) {
            impact += 0.1; // Only rough seas affect subsurface noticeably
        }
        return impact;
    }

    @Override
    protected void clampPosition() {
        // Enforce [-150, -1]
        if (z > -1)
            z = -1;
        if (z < -150)
            z = -150;
    }

    @Override
    public void deplacer(double targetX, double targetY, double targetZ) {
        // Enforce Underwater Constraints [-500, -1]
        if (targetZ > -1) {
            System.out.println("⚠️ " + id + ": Rejet cible Z=" + targetZ + " (Surface/Air). Force à -1m.");
            targetZ = -1;
        }
        if (targetZ < -500) {
            System.out.println("⚠️ " + id + ": Rejet cible Z=" + targetZ + " (Trop profond). Force à -500m.");
            targetZ = -500;
        }
        super.deplacer(targetX, targetY, targetZ);
    }

    @Override
    public double getConsommation() {
        return 0.014; // 1.4% par heure
    }
}
