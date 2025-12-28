package com.spiga.core;

/**
 * Classe Concrete : Navire de Patrouille
 * 
 * CONCEPTS CLES (FEUILE DE L'ARBRE) :
 * 
 * 1. Classe Finale (au sens logique) :
 * - C'est une "Feuille" de l'arbre d'heritage. On ne va probablement pas
 * heriter de ca.
 * - Heritage : ActifMobile -> ActifMarin -> VehiculeSurface ->
 * NavirePatrouille.
 * 
 * 2. Specialisation Ultime :
 * - Elle a tout : La position (ActifMobile), la gestion de l'eau (ActifMarin),
 * la contrainte de surface (VehiculeSurface),
 * ET ses propres attributs (Radar).
 */
public class NavirePatrouille extends VehiculeSurface {

    private double porteeRadar;

    public NavirePatrouille(String id, double x, double y) {
        super(id, x, y);
        this.vitesseMax = 60.0; // Plus rapide qu'un navire standard
        this.autonomieMax = 96.0; // 4 jours
        this.autonomieActuelle = autonomieMax;
        this.porteeRadar = 50.0; // km
    }

    @Override
    public double getConsommation() {
        return 0.01; // 1% par heure
    }

    public double getRayonRadar() {
        return porteeRadar;
    }
}
