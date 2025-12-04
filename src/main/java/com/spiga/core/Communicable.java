package com.spiga.core;

/**
 * Interface Communicable - Définit les comportements de communication
 * Conforme à SPIGA-SPEC.txt section 1.3
 */
public interface Communicable {
    /**
     * Transmet une alerte à un actif cible
     * 
     * @param message    le message d'alerte
     * @param actifCible l'actif destinataire
     */
    void transmettreAlerte(String message, ActifMobile actifCible);
}
