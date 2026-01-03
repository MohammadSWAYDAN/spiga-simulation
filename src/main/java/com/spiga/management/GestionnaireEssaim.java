package com.spiga.management;

import com.spiga.core.ActifMobile;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.logging.Logger;

/**
 * Gestionnaire central de la flotte d'actifs (Design Pattern
 * Manager/Repository).
 * <p>
 * <strong>Rôle :</strong> Maintenir la liste officielle de tous les actifs de
 * la simulation
 * et fournir des outils de recherche, de filtrage (Streams) et d'assignation
 * globale.
 * </p>
 */
public class GestionnaireEssaim {

    // Encapsulation : Liste privée, non accessible directement de l'extérieur.
    private List<ActifMobile> flotte;
    private static final Logger logger = Logger.getLogger(GestionnaireEssaim.class.getName());

    /**
     * Crée un nouveau gestionnaire de flotte vide.
     */
    public GestionnaireEssaim() {
        this.flotte = new ArrayList<>();
    }

    /**
     * Ajoute un actif à la flotte gérée.
     * <p>
     * Permet l'ajout polymorphique (Drones, Navires, etc.).
     * </p>
     * 
     * @param actif L'entité à ajouter.
     */
    public void ajouterActif(ActifMobile actif) {
        flotte.add(actif);
        logger.info("✓ Actif ajouté: " + actif.getId());
    }

    /**
     * Supprime un actif du système.
     * 
     * @param id Identifiant de l'actif à retirer.
     */
    public void supprimerActif(String id) {
        flotte.removeIf(a -> a.getId().equals(id));
        logger.info("✗ Actif supprimé: " + id);
    }

    /**
     * Retourne la liste complète de la flotte.
     * 
     * @return Liste mutable des actifs.
     */
    public List<ActifMobile> getFlotte() {
        return flotte;
    }

    /**
     * Recherche les actifs prêts à partir en mission.
     * <p>
     * Critères :
     * <ul>
     * <li>État : AU_SOL</li>
     * <li>Batterie : > 20%</li>
     * </ul>
     * Utilise les <strong>Java Streams</strong> pour un filtrage déclaratif.
     * </p>
     * 
     * @return Liste des candidats valides.
     */
    public List<ActifMobile> getActifsDisponibles() {
        if (flotte == null) {
            return new ArrayList<>();
        }
        return flotte.stream()
                .filter(a -> a.getEtat() == ActifMobile.EtatOperationnel.AU_SOL)
                .filter(a -> a.getAutonomieActuelle() > a.getAutonomieMax() * 0.2)
                .collect(Collectors.toList());
    }

    /**
     * Lance une mission sur un groupe d'actifs donné.
     * 
     * @param mission La mission à démarrer.
     * @param essaim  La liste des actifs participants.
     */
    public void demarrerMission(Mission mission, List<ActifMobile> essaim) {
        logger.info("🚀 Démarrage mission: " + mission.getTitre());
        mission.assignActifs(essaim);
        for (ActifMobile actif : essaim) {
            actif.assignMission(mission);
        }
        // Start mission clock
        mission.start(System.currentTimeMillis() / 1000);
    }

    /**
     * Suggère l'actif le plus pertinent (ex: meilleure autonomie).
     * 
     * @return L'actif optimal ou null si aucun dispo.
     */
    public ActifMobile suggererActifOptimal() {
        return getActifsDisponibles().stream()
                .max((a1, a2) -> Double.compare(
                        a1.getAutonomieActuelle(),
                        a2.getAutonomieActuelle()))
                .orElse(null);
    }
}
