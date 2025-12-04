package com.spiga.core;

/**
 * NavireRecherche - Navire de recherche
 * Hérite de VehiculeSurface
 */
public class NavireRecherche extends VehiculeSurface {

    private boolean laboratoireActif;

    public NavireRecherche(String id, double x, double y) {
        super(id, x, y);
        this.vitesseMax = 25.0;
        this.autonomieMax = 240.0; // 10 jours
        this.autonomieActuelle = autonomieMax;
        this.laboratoireActif = false;
    }

    @Override
    public double getConsommation() {
        return laboratoireActif ? 0.012 : 0.008; // +50% si labo actif
    }

    public void activerLaboratoire() {
        laboratoireActif = true;
    }

    public void desactiverLaboratoire() {
        laboratoireActif = false;
    }

    public boolean isLaboratoireActif() {
        return laboratoireActif;
    }
}
