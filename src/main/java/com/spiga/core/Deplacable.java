package com.spiga.core;

/**
 * Interface Deplacable - Définit les comportements de déplacement
 * Conforme à SPIGA-SPEC.txt section 1.3
 */
public interface Deplacable {
    /**
     * Déplace l'actif vers une position cible
     * 
     * @param x coordonnée X cible
     * @param y coordonnée Y cible
     * @param z coordonnée Z cible (altitude/profondeur)
     */
    void deplacer(double x, double y, double z);

    /**
     * Calcule le trajet optimal vers une position
     * 
     * @param x coordonnée X cible
     * @param y coordonnée Y cible
     * @param z coordonnée Z cible
     */
    void calculerTrajet(double x, double y, double z);
}
