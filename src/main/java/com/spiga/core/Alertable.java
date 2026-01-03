package com.spiga.core;

/**
 * Interface définissant la capacité à émettre des notifications d'état
 * critique.
 * <p>
 * Permet au système de surveillance (Monitoring) de recevoir des alertes
 * standardisées
 * de la part des actifs, quel que soit leur type.
 * </p>
 */
public interface Alertable {

    /**
     * Notifie le système d'un état critique nécessitant une attention immédiate.
     *
     * @param typeAlerte Description courte de l'alerte (ex: "BATTERY_LOW",
     *                   "ERROR_SENSOR").
     */
    void notifierEtatCritique(String typeAlerte);
}
