package com.spiga.core;

/**
 * Classe Concrete : Drone de Reconnaissance
 * 
 * CONCEPTS CLES :
 * 
 * 1. Heritage (extends ActifAerien) :
 * - C'est quoi ? Recuperer tout le code d'un parent.
 * - Pourquoi ici ? Un Drone EST UN actif aerien. Il recupere automatiquement :
 * altitude, position x/y/z, methodes de deplacement.
 * On ne reinvente pas la roue !
 * 
 * 2. Constructeur ("super") :
 * - C'est quoi ? Appeler le constructeur du parent.
 * - Pourquoi ici ? L'objet parent (ActifAerien) a besoin d'initialiser ses
 * variables (vitesse, autonomie).
 * Le drone passe ses valeurs specifiques (120 km/h, 4h).
 * 
 * 3. Specialisation :
 * - C'est quoi ? Ajouter ce qui manque au parent.
 * - Ici : On ajoute rayonSurveillance qui n'existe pas dans un actif generique.
 */
public class DroneReconnaissance extends ActifAerien {

    // Attribut specifique a CE type de drone (Specialisation)
    private double rayonSurveillance;

    public DroneReconnaissance(String id, double x, double y, double altitude) {
        // APPEL AU PARENT :
        // On remplit le "contrat" de ActifAerien en lui donnant les infos de base
        // (ID, pos, vitesse=120, autonomie=4h)
        super(id, x, y, altitude, 120.0, 4.0);

        // Initialisation specifique locale
        this.rayonSurveillance = 1000.0; // 1km de rayon
        this.altitudeMax = 3000;
    }

    /**
     * POLYMORPHISME (@Override) :
     * 
     * Cette methode remplace celle du parent (ou implemente l'abstraite).
     * 
     * Quand on demande getConsommation() a ce drone, c'est CE code qui s'execute,
     * pas celui d'un navire ou d'un autre drone.
     */
    @Override
    public double getConsommation() {
        return 2.0; // 2.0/hr (Cap 4.0) -> 2h Autonomy. At 10x speed -> 12 min real time.
    }

    public double getRayonSurveillance() {
        return rayonSurveillance;
    }
}
