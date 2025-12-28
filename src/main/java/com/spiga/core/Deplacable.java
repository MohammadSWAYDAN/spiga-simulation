package com.spiga.core;

/**
 * Interface Deplacable.
 * 
 * Concepts (Cours 3) :
 * 
 * - Interface : Contrat que les classes doivent respecter (implementer).
 * Contrairement a une classe, elle ne contient que des signatures de methodes
 * (sans corps).
 * 
 * - Polymorphisme : Grace a cette interface, le systeme peut manipuler
 * n'importe quel objet Deplacable (Drone, Navire, etc.)
 * de maniere uniforme via ces methodes.
 * 
 * Conforme a SPIGA-SPEC.txt section 1.3
 */
public interface Deplacable {
    /**
     * Déplace l'actif vers une position cible.
     * C'est une méthode <b>abstraite</b> (par défaut dans une interface).
     * Les classes implémentant ({@code implements}) cette interface DOIVENT fournir
     * le code de cette méthode.
     * 
     * @param x coordonnée X cible
     * @param y coordonnée Y cible
     * @param z coordonnée Z cible (altitude/profondeur)
     */
    void deplacer(double x, double y, double z);

    /**
     * Calcule le trajet optimal vers une position.
     * 
     * @param x coordonnée X cible
     * @param y coordonnée Y cible
     * @param z coordonnée Z cible
     */
    void calculerTrajet(double x, double y, double z);
}
