package com.spiga.core;

/**
 * Interface Rechargeable - Définit les comportements de recharge
 * Conforme à SPIGA-SPEC.txt section 1.3
 */
public interface Rechargeable {
    /**
     * Recharge l'actif (pour actifs électriques)
     */
    void recharger();

    /**
     * Ravitaille l'actif (pour actifs à carburant)
     */
    void ravitailler();
}
