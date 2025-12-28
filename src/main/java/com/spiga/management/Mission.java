package com.spiga.management;

import com.spiga.core.ActifMobile;

/**
 * Classe abstraite representant une Mission.
 * 
 * Concepts POO (Cours 1 & 2) :
 * 
 * - Classe : Modele definissant les attributs (etat) et methodes (comportement)
 * communs aux missions.
 * - Abstraite : Ne peut etre instanciee directement (on ne fait pas new
 * Mission()), mais sert de base pour MissionLogistique, etc. (Voir Cours 3 :
 * Generalisation).
 * - Encapsulation : Les attributs sont protected (accessibles aux sous-classes)
 * et exposes via des methodes publiques (Getters/Setters).
 * 
 * Conforme a SPIGA-SPEC.txt section 3.2
 */
public abstract class Mission {

    /**
     * Énumération (Enum) : Type spécial de classe représentant un groupe de
     * Enum : Définit strictement les TYPES de missions possibles.
     */
    public enum MissionType {
        SURVEILLANCE, LOGISTICS, NAVIGATION, SEARCH_AND_RESCUE
    }

    /**
     * Enum : Définit strictement le CYCLE DE VIE d'une mission.
     */
    public enum StatutMission {
        PLANIFIEE, EN_COURS, TERMINEE, ECHOUEE, ANNULEE
    }

    // Attributs de la classe (État de l'objet)
    protected String id;
    protected String titre;
    protected MissionType type;
    protected StatutMission statut;
    protected long startTime;
    protected long endTime;
    protected String objectives;
    protected String results;

    // Association : Une mission peut être liée à un Actif
    protected ActifMobile assignedAsset;

    // Coordonnées cibles (Encapsulation : Accès via setTarget/getTarget)
    protected double targetX;
    protected double targetY;
    protected double targetZ;

    /**
     * Constructeur : Méthode spéciale appelée lors de la création d'une instance
     * (objet).
     * Elle initialise l'état initial de l'objet.
     * 
     * @param titre Le nom de la mission
     * @param type  Le type de mission (Enum)
     */
    public Mission(String titre, MissionType type) {
        this.id = "M-" + System.currentTimeMillis(); // Génération d'ID unique basée sur le temps
        this.titre = titre;
        this.type = type;
        this.statut = StatutMission.PLANIFIEE;
        this.objectives = "Objectifs par défaut";
        // Valeurs par défaut
        this.targetX = 500;
        this.targetY = 500;
        this.targetZ = 0;
    }

    /**
     * Démarre la mission
     */
    public void start() {
        this.statut = StatutMission.EN_COURS;
        this.startTime = System.currentTimeMillis();
        System.out.println("▶️ Mission démarrée: " + titre);
    }

    /**
     * Assigne la mission à un actif
     */
    public void assign() {
        this.statut = StatutMission.PLANIFIEE;
        System.out.println("📋 Mission assignée: " + titre);
    }

    /**
     * Termine la mission avec succès
     */
    public void complete() {
        this.statut = StatutMission.TERMINEE;
        this.endTime = System.currentTimeMillis();
        this.results = "Mission accomplie";
        long duration = (endTime - startTime) / 1000;
        System.out.println("✅ Mission terminée: " + titre + " (durée: " + duration + "s)");
    }

    /**
     * Échoue la mission
     */
    public void fail(String reason) {
        this.statut = StatutMission.ECHOUEE;
        this.endTime = System.currentTimeMillis();
        this.results = "Échec: " + reason;
        System.out.println("❌ Mission échouée: " + titre + " - " + reason);
    }

    /**
     * Annule la mission
     */
    public void cancel() {
        this.statut = StatutMission.ANNULEE;
        this.endTime = System.currentTimeMillis();
        System.out.println("🚫 Mission annulée: " + titre);
    }

    /**
     * Définit la cible de la mission
     */
    public void setTarget(double x, double y, double z) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public MissionType getType() {
        return type;
    }

    public StatutMission getStatut() {
        return statut;
    }

    public String getObjectives() {
        return objectives;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public String getResults() {
        return results;
    }

    public double getTargetX() {
        return targetX;
    }

    public double getTargetY() {
        return targetY;
    }

    public double getTargetZ() {
        return targetZ;
    }

    // Setters
    public void setStatut(StatutMission statut) {
        this.statut = statut;
    }

    public void setObjectives(String objectives) {
        this.objectives = objectives;
    }

    /**
     * Crée une copie de la mission (pour réutilisation)
     */
    public abstract Mission copy();
}
