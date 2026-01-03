package com.spiga.core;

/**
 * Interface définissant le contrôle opérationnel de base (ON/OFF).
 * <p>
 * <strong>Concept :</strong> Contrat permettant à un contrôleur externe
 * de gérer le cycle de mise en marche et d'arrêt sans connaître les détails
 * internes (moteurs, électronique).
 * </p>
 */
public interface Pilotable {

    /**
     * Active les systèmes de l'actif.
     * <p>
     * Doit faire passer l'actif d'un état inactif (AU_SOL) à un état prêt
     * (EN_MISSION/IDLE).
     * Ne doit rien faire si l'actif est déjà démarré ou en panne critique.
     * </p>
     */
    void demarrer();

    /**
     * Désactive les systèmes de l'actif.
     * <p>
     * Arrête tout mouvement et passe l'actif à l'état inactif.
     * </p>
     */
    void eteindre();
}
