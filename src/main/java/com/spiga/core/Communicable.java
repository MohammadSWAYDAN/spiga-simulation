package com.spiga.core;

/**
 * Interface définissant les capacités de communication entre actifs.
 * <p>
 * Permet l'échange d'informations (ordres, alertes, statuts) au sein de la
 * flotte.
 * </p>
 */
public interface Communicable {

    /**
     * Transmet un message d'alerte spécifique à un autre actif.
     *
     * @param message    Le contenu du message (ex: "Collision imminente").
     * @param actifCible L'actif destinataire du message.
     */
    void transmettreAlerte(String message, ActifMobile actifCible);
}
