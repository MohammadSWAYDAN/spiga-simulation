package com.spiga.management;

/**
 * Mission - ENHANCED VERSION
 * Conforme à SPIGA-SPEC.txt section 3.2
 * 
 * NEW FEATURES:
 * - Target coordinates for navigation
 * - Mission type enum
 * - Complete lifecycle tracking
 * - Mission completion detection
 */
public abstract class Mission {

    /**
     * Type de mission
     */
    public enum MissionType {
        SURVEILLANCE, // Surveillance de zone
        LOGISTICS, // Transport logistique
        NAVIGATION, // Navigation point-à-point
        SEARCH_AND_RESCUE // Recherche et sauvetage
    }

    /**
     * Statut de la mission
     */
    /**
     * Statut de la mission (Conforme SPIGA-SPEC)
     */
    public enum StatutMission {
        PLANIFIEE, // Created/Assigned
        EN_COURS, // Executing
        TERMINEE, // Completed
        ECHOUEE, // Failed
        ANNULEE // Cancelled
    }

    protected String id;
    protected String titre;
    protected MissionType type;
    protected StatutMission statut;
    protected long startTime;
    protected long endTime;
    protected String objectives;
    protected String results;

    // NOUVEAUX ATTRIBUTS pour navigation
    protected double targetX;
    protected double targetY;
    protected double targetZ;

    public Mission(String titre, MissionType type) {
        this.id = "M-" + System.currentTimeMillis();
        this.titre = titre;
        this.type = type;
        this.statut = StatutMission.PLANIFIEE;
        this.objectives = "Objectifs par défaut";
        this.targetX = 500; // Centre par défaut
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
