package com.spiga.core;

/**
 * HelicoptereSauvetage - Hélicoptère de sauvetage
 * Conforme à SPIGA-SPEC.txt section 1.2
 */
public class HelicoptereSauvetage extends ActifAerien {

    private int capacitePersonnes;
    private int personnesABord;

    public HelicoptereSauvetage(String id, double x, double y, double altitude) {
        super(id, x, y, altitude, 200.0, 6.0); // 200 km/h, 6h autonomie
        this.capacitePersonnes = 10;
        this.personnesABord = 0;
    }

    @Override
    public double getConsommation() {
        return 0.20; // 20% par heure
    }

    public void embarquerPersonnes(int nombre) {
        if (personnesABord + nombre <= capacitePersonnes) {
            personnesABord += nombre;
        }
    }

    public void debarquerPersonnes() {
        personnesABord = 0;
    }

    public int getCapacitePersonnes() {
        return capacitePersonnes;
    }

    public int getPersonnesABord() {
        return personnesABord;
    }
}
