package com.spiga.core;

/**
 * SousMarinExploration - Sous-marin d'exploration
 * Herite de VehiculeSousMarin
 * 
 * Concept : Configuration Different
 * Meme fonctionnement que le parent, mais parametres (vitesse, profondeur)
 * differents.
 */
public class SousMarinExploration extends VehiculeSousMarin {

    public SousMarinExploration(String id, double x, double y, double profondeur) {
        super(id, x, y, profondeur);
        this.profondeurMin = -150; // -150m
        this.vitesseMax = 15.0;
        this.autonomieMax = 120.0; // 5 jours
        this.autonomieActuelle = autonomieMax;
    }

    @Override
    public double getConsommation() {
        return 0.008; // 0.8% par heure
    }
}
