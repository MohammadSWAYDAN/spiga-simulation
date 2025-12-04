package com.spiga.core;

/**
 * Interface Pilotable - Définit les comportements de pilotage
 * Conforme à SPIGA-SPEC.txt section 1.3
 */
public interface Pilotable {
    /**
     * Démarre l'actif
     */
    void demarrer();

    /**
     * Éteint l'actif
     */
    void eteindre();
}
