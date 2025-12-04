package com.spiga.core;

/**
 * GliderOceanographique - Glider océanographique
 * Hérite de VehiculeSousMarin
 */
public class GliderOceanographique extends VehiculeSousMarin {

    private boolean collecteDonnees;

    public GliderOceanographique(String id, double x, double y, double profondeur) {
        super(id, x, y, profondeur);
        this.profondeurMin = -1000;
        this.vitesseMax = 5.0; // Très lent
        this.autonomieMax = 720.0; // 30 jours!
        this.autonomieActuelle = autonomieMax;
        this.collecteDonnees = false;
    }

    @Override
    public double getConsommation() {
        return 0.0014; // 0.14% par heure - très efficace
    }

    public void demarrerCollecte() {
        collecteDonnees = true;
    }

    public void arreterCollecte() {
        collecteDonnees = false;
    }

    public boolean isCollecteDonnees() {
        return collecteDonnees;
    }
}
