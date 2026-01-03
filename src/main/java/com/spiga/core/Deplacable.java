package com.spiga.core;

/**
 * Interface définissant la capacité d'un objet à se déplacer dans l'espace 3D.
 * <p>
 * <strong>Concepts POO :</strong>
 * <ul>
 * <li>Interface : Contrat de comportement. Tout objet implémentant cette
 * interface
 * garantit qu'il peut être déplacé.</li>
 * <li>Polymorphisme : Permet de manipuler uniformément des Drones, Navires,
 * etc.</li>
 * </ul>
 * </p>
 */
public interface Deplacable {

    /**
     * Déplace l'actif vers une position cible (x, y, z).
     * Les classes concrètes doivent implémenter la logique physique de ce
     * déplacement.
     *
     * @param x Coordonnée X cible.
     * @param y Coordonnée Y cible.
     * @param z Coordonnée Z cible (altitude ou profondeur).
     */
    void deplacer(double x, double y, double z);

    /**
     * Simule le calcul d'un trajet optimal vers la cible.
     * Cette méthode est généralement utilisée pour la planification ou le
     * debugging.
     *
     * @param x Coordonnée X cible.
     * @param y Coordonnée Y cible.
     * @param z Coordonnée Z cible.
     */
    void calculerTrajet(double x, double y, double z);
}
