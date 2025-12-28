package com.spiga.core;

/**
 * SousMarinAttaque - Sous-marin d'attaque
 * Herite de VehiculeSousMarin
 * 
 * Concept : Actions Uniques
 * Seul ce type de sous-marin peut "lancerTorpille()".
 */
public class SousMarinAttaque extends VehiculeSousMarin {

    private int torpilles;

    public SousMarinAttaque(String id, double x, double y, double profondeur) {
        super(id, x, y, profondeur);
        this.profondeurMin = -500;
        this.vitesseMax = 35.0;
        this.autonomieMax = 168.0; // 7 jours
        this.autonomieActuelle = autonomieMax;
        this.torpilles = 20;
    }

    @Override
    public double getConsommation() {
        return 0.006; // 0.6% par heure
    }

    public void lancerTorpille() {
        if (torpilles > 0) {
            torpilles--;
            System.out.println(getId() + " a lancé une torpille. Restantes: " + torpilles);
        }
    }

    public int getTorpilles() {
        return torpilles;
    }
}
