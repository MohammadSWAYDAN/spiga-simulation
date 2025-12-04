package com.spiga.core;

/**
 * Interface Alertable - Définit les comportements d'alerte
 * Conforme à SPIGA-SPEC.txt section 1.3
 */
public interface Alertable {
    /**
     * Notifie un état critique
     * 
     * @param typeAlerte le type d'alerte (panne, batterie faible, etc.)
     */
    void notifierEtatCritique(String typeAlerte);
}
