package com.spiga.core;

import java.util.List;

/**
 * Validateur de positionnement de l'essaim.
 * 
 * Concept (Cours 2 : Membres statiques) :
 * Cette classe agit comme un utilitaire. Elle n'a pas besoin d'etre instanciee
 * (pas de new SwarmValidator()).
 * Ses methodes sont static, ce qui signifie qu'elles appartiennent a la classe
 * elle-meme et non a une instance specifique.
 * On l'appelle directement par SwarmValidator.isPlacementValid(...).
 */
public class SwarmValidator {

    /**
     * Vérifie si un positionnement (x, y, z) est valide par rapport aux autres
     * drones.
     * static : Méthode de classe.
     */
    public static boolean isPlacementValid(double x, double y, double z, List<ActifMobile> existingDrones) {
        for (ActifMobile drone : existingDrones) {
            double dist = Math.sqrt(
                    Math.pow(drone.getX() - x, 2) + Math.pow(drone.getY() - y, 2) + Math.pow(drone.getZ() - z, 2));
            if (dist < SimConfig.MIN_DISTANCE) {
                return false;
            }
        }
        return true;
    }
}
