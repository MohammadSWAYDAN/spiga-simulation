package com.spiga.core;

/**
 * Interface définissant les capacités de ravitaillement énergétique.
 * <p>
 * S'applique à tout actif consommant de l'énergie (batterie ou carburant).
 * </p>
 */
public interface Rechargeable {

    /**
     * Remplit les réserves d'énergie au maximum (ex: Batterie 100%).
     * <p>
     * Cette méthode est généralement appelée lors du retour à la base
     * ou sur une station de charge.
     * </p>
     */
    void recharger();

    /**
     * Alias sémantique pour {@link #recharger()}, utilisé pour les véhicules à
     * carburant.
     */
    void ravitailler();
}
