package com.spiga.core;

/**
 * Interface Alertable.
 * <p>
 * <b>Concepts :</b>
 * <ul>
 * <li><b>Interface Segregation</b> : Cette interface est spécifique aux
 * alertes.
 * Un objet peut être {@code Deplacable} sans être forcément {@code Alertable}
 * (bien que dans ce projet ActifMobile implémente tout).</li>
 * </ul>
 * 
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
