package com.spiga.core;

/**
 * NavireLogistique - Navire logistique
 * Herite de VehiculeSurface
 * 
 * Concept : Specialisation Metier
 * Ce navire ajoute une "capaciteCargaison" qui n'existe pas dans le vehicule de
 * base.
 */
public class NavireLogistique extends VehiculeSurface {

    private double capaciteCargaison;

    public NavireLogistique(String id, double x, double y) {
        super(id, x, y);
        this.vitesseMax = 30.0;
        this.autonomieMax = 120.0; // 5 jours
        this.autonomieActuelle = autonomieMax;
        this.capaciteCargaison = 1000.0; // 1000 tonnes
    }

    @Override
    public double getConsommation() {
        return 0.008; // 0.8% par heure
    }

    public double getCapaciteCargaison() {
        return capaciteCargaison;
    }
}
