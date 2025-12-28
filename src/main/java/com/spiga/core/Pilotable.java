package com.spiga.core;

/**
 * Interface : Pilotable
 * 
 * CONCEPT : LE CONTRAT
 * 
 * Une Interface est comme un contrat signe par une classe.
 * 
 * Si une classe (ex: Drone) dit "implements Pilotable", elle PROMET qu'elle
 * fournira le code pour :
 * - demarrer()
 * - eteindre()
 * 
 * Cela permet au controleur de piloter n'importe quoi (Navire, Drone, Robot)
 * sans savoir exactement ce que c'est,
 * tant que c'est "Pilotable".
 */
public interface Pilotable {

    /**
     * Démarre les moteurs/systèmes.
     * La classe qui implémente doit dire COMMENT elle démarre.
     */
    void demarrer();

    /**
     * Arrête les moteurs/systèmes.
     */
    void eteindre();
}
