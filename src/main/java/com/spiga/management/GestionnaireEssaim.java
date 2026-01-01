package com.spiga.management;

import com.spiga.core.ActifMobile;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gestionnaire de Flotte (Logiciel de Gestion)
 * 
 * CONCEPTS CLES (JAVA BASICS & COLLECTIONS) :
 * 
 * 1. Collections et Genericite (List<ActifMobile>) :
 * - C'est quoi ? Une liste dynamique (taille variable) qui ne peut contenir QUE
 * des objets "ActifMobile" (et ses enfants).
 * - Pourquoi ? Plus sur qu'un tableau ActifMobile[] car on peut
 * ajouter/supprimer facilement.
 * 
 * 2. Programmation Fonctionnelle (Streams) :
 * - C'est quoi ? Traiter des listes comme des flux de donnees.
 * - Ou ? Dans getActifsDisponibles(), on utilise .stream().filter(...) pour
 * trier les drones sans faire de boucles for complexes.
 */
public class GestionnaireEssaim {

    // Encapsulation : Liste privée, non accessible directement de l'extérieur.
    private List<ActifMobile> flotte;

    public GestionnaireEssaim() {
        // Initialisation : On crée une liste vide prête à recevoir des actifs.
        this.flotte = new ArrayList<>();
    }

    /**
     * Ajoute un actif a la flotte.
     * 
     * Utilise le Polymorphisme : On peut passer un Drone, un Navire, un
     * Sous-marin...
     * Tout marche car ils SONT TOUS des ActifMobile.
     */
    public void ajouterActif(ActifMobile actif) {
        flotte.add(actif);
        System.out.println("✓ Actif ajouté: " + actif.getId());
    }

    /**
     * Retourne la flotte complète.
     */
    public List<ActifMobile> getFlotte() {
        return flotte;
    }

    /**
     * Retourne les actifs disponibles (AU_SOL avec autonomie > 20%).
     * Utilisation de <b>Streams</b> pour le filtrage (Programmation fonctionnelle).
     */
    public List<ActifMobile> getActifsDisponibles() {
        return flotte.stream()
                .filter(a -> a.getEtat() == ActifMobile.EtatOperationnel.AU_SOL) // Condition 1
                .filter(a -> a.getAutonomieActuelle() > a.getAutonomieMax() * 0.2) // Condition 2
                .collect(Collectors.toList()); // Rassemble les résultats dans une nouvelle liste
    }

    /**
     * Démarre une mission avec un essaim d'actifs
     */
    public void demarrerMission(Mission mission, List<ActifMobile> essaim) {
        System.out.println("Démarrage mission: " + mission.getTitre());
        mission.assign();
        for (ActifMobile actif : essaim) {
            actif.assignMission(mission);
        }
        mission.start();
    }

    /**
     * Suggère l'actif optimal pour une mission
     */
    public ActifMobile suggererActifOptimal() {
        return getActifsDisponibles().stream()
                .max((a1, a2) -> Double.compare(
                        a1.getAutonomieActuelle(),
                        a2.getAutonomieActuelle()))
                .orElse(null);
    }
}
