package com.spiga.management;

import com.spiga.core.ActifMobile;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GestionnaireEssaim - Gestionnaire de flotte
 * Conforme à SPIGA-SPEC.txt section 3.1
 * 
 * Gère la disponibilité des actifs et la composition des groupes
 * Coordination et maintenance de la flotte
 */
public class GestionnaireEssaim {
    private List<ActifMobile> flotte;

    public GestionnaireEssaim() {
        this.flotte = new ArrayList<>();
    }

    /**
     * Ajoute un actif à la flotte
     */
    public void ajouterActif(ActifMobile actif) {
        flotte.add(actif);
        System.out.println("✓ Actif ajouté: " + actif.getId());
    }

    /**
     * Retourne la flotte complète
     */
    public List<ActifMobile> getFlotte() {
        return flotte;
    }

    /**
     * Retourne les actifs disponibles (AU_SOL avec autonomie > 20%)
     */
    public List<ActifMobile> getActifsDisponibles() {
        return flotte.stream()
                .filter(a -> a.getEtat() == ActifMobile.EtatOperationnel.AU_SOL)
                .filter(a -> a.getAutonomieActuelle() > a.getAutonomieMax() * 0.2)
                .collect(Collectors.toList());
    }

    /**
     * Démarre une mission avec un essaim d'actifs
     */
    public void demarrerMission(Mission mission, List<ActifMobile> essaim) {
        System.out.println("🚀 Démarrage mission: " + mission.getTitre());
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
