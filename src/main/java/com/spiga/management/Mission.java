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
        PLANIFIEE, EN_COURS, PAUSED, TERMINEE, ECHOUEE, ANNULEE
    }

    /**
     * Enum : Strategie de completion.
     */
    public enum CompletionRule {
        ALL, ANY
    }

    // Attributs de la classe (État de l'objet)
    protected String id;
    protected String titre;
    protected MissionType type;
    protected StatutMission statut;

    // Timeline
    protected long plannedDurationSeconds;
    protected long actualStartTime;
    protected long actualEndTime;

    protected String objectives;
    protected String results;

    // Association : Une mission peut être liée à PLUSIEURS Actifs
    protected java.util.List<ActifMobile> assignedAssets;
    protected CompletionRule completionRule;

    // Coordonnées cibles (Encapsulation : Accès via setTarget/getTarget)
    protected double targetX;
    protected double targetY;
    protected double targetZ;

    public Mission(String titre, MissionType type) {
        this.id = "M-" + System.currentTimeMillis(); // Génération d'ID unique basée sur le temps
        this.titre = titre;
        this.type = type;
        this.statut = StatutMission.PLANIFIEE;
        this.objectives = "Objectifs par défaut";

        this.assignedAssets = new java.util.ArrayList<>();
        this.completionRule = CompletionRule.ANY; // Default to ANY (easier for now)
        this.plannedDurationSeconds = 180; // Default 3 mins

        // Valeurs par défaut
        this.targetX = 500;
        this.targetY = 500;
        this.targetZ = 0;
    }

    /**
     * Démarre la mission
     */
    /**
     * Démarre la mission
     */
    // --- HISTORIQUE D'EXECUTION ---
    public static class MissionExecution {
        public String runId;
        public long startTime;
        public long endTime;
        public StatutMission finalStatus;
        public String resultNote;
        public double targetX, targetY, targetZ;

        public MissionExecution(String runId, long startTime, double tx, double ty, double tz) {
            this.runId = runId;
            this.startTime = startTime;
            this.targetX = tx;
            this.targetY = ty;
            this.targetZ = tz;
            this.finalStatus = StatutMission.EN_COURS;
        }
    }

    protected java.util.List<MissionExecution> history = new java.util.ArrayList<>();
    protected MissionExecution currentRun = null;
    protected int runCounter = 0;

    public void start(long simulationTime) {
        // Allow Start if PLANIFIEE (created new) or if we are restarting
        if (this.statut == StatutMission.PLANIFIEE) {
            this.statut = StatutMission.EN_COURS;
            this.actualStartTime = simulationTime;

            // Create Execution Record
            this.runCounter++;
            String rId = this.id + "-RUN-" + runCounter;
            this.currentRun = new MissionExecution(rId, simulationTime, targetX, targetY, targetZ);

            System.out.println("▶️ Mission démarrée: " + titre + " (Run #" + runCounter + ")");

            // Wake up assets and Retarget
            for (ActifMobile asset : assignedAssets) {
                if (asset.getCurrentMission() == this) {
                    asset.setTarget(targetX, targetY, targetZ);
                    asset.setState(ActifMobile.AssetState.EXECUTING_MISSION);
                    // Force EN_MISSION status if IDLE
                    // asset.setEtat(EtatOperationnel.EN_MISSION); // Usually handled by setState
                    // logic or implicit
                    System.out.println("   -> Actif " + asset.getId() + " redirigé vers cible mission.");
                }
            }
        }
    }

    public void restart(long simulationTime) {
        if (this.statut == StatutMission.TERMINEE || this.statut == StatutMission.ECHOUEE
                || this.statut == StatutMission.ANNULEE) {
            System.out.println("🔄 Restarting Mission: " + titre);
            this.statut = StatutMission.PLANIFIEE; // Reset to planned
            start(simulationTime);
        }
    }

    public void pause() {
        if (statut == StatutMission.EN_COURS) {
            this.statut = StatutMission.PAUSED;
            System.out.println("Mission " + titre + " PAUSED");
            // Do NOT stop assets here, usually called before manual move.
            // If called standalone, maybe we should stop them?
            // For now, let manual move logic handle the asset state.
        }
    }

    public void resume(long simulationTime) {
        if (statut == StatutMission.PAUSED) {
            this.statut = StatutMission.EN_COURS;
            System.out.println("Mission " + titre + " RESUMED");

            // Retarget assets to Mission Target (in case they were moved manually)
            for (ActifMobile asset : assignedAssets) {
                if (asset.getCurrentMission() == this) {
                    asset.setTarget(targetX, targetY, targetZ);
                    asset.setState(ActifMobile.AssetState.EXECUTING_MISSION);
                    System.out.println("   -> Actif " + asset.getId() + " reprend la mission.");
                }
            }
        }
    }

    // Compatibility overload if anyone calls start() without args
    public void start() {
        start(System.currentTimeMillis() / 1000); // Fallback
    }

    /**
     * Assigne la mission à un ou plusieurs actifs
     */
    public void assignActifs(java.util.List<ActifMobile> assets) {
        this.assignedAssets.clear();
        this.assignedAssets.addAll(assets);
        // Only reset to PLANIFIEE if it was new, don't overwrite running status if
        // hot-swap
        if (this.statut == null || this.statut == StatutMission.PLANIFIEE) {
            this.statut = StatutMission.PLANIFIEE;
        }
        System.out.println("📋 Mission assignée à " + assets.size() + " actifs: " + titre);
    }

    public void addActif(ActifMobile asset) {
        if (!this.assignedAssets.contains(asset)) {
            this.assignedAssets.add(asset);
        }
    }

    /**
     * Tick method called by SimulationService every frame/second
     */
    public void tick(long currentSimTime) {
        if (this.statut != StatutMission.EN_COURS)
            return;

        // 1. Check Timeout
        long elapsed = currentSimTime - actualStartTime;
        if (elapsed > plannedDurationSeconds) {
            fail("TIMEOUT (Durée écoulée: " + elapsed + "s)");
            return;
        }

        // 2. Check Completion
        int arrivedCount = 0;
        int activeCount = 0;

        for (ActifMobile asset : assignedAssets) {
            // Basic state check - if destroyed/panne, maybe don't count?
            // For now assume all assigned are relevant.
            activeCount++;

            double dist = Math.sqrt(Math.pow(asset.getX() - targetX, 2) +
                    Math.pow(asset.getY() - targetY, 2) +
                    Math.pow(asset.getZ() - targetZ, 2));
            if (dist < 5.0) { // Tolerance 5m
                arrivedCount++;
            }
        }

        if (activeCount == 0)
            return; // Should not happen if assigned correctly

        boolean success = false;
        if (completionRule == CompletionRule.ANY && arrivedCount >= 1) {
            success = true;
        } else if (completionRule == CompletionRule.ALL && arrivedCount >= activeCount) {
            success = true;
        }

        if (success) {
            complete(currentSimTime);
        }
    }

    /**
     * Termine la mission avec succès
     */
    public void complete(long simulationTime) {
        this.statut = StatutMission.TERMINEE;
        this.actualEndTime = simulationTime;
        this.results = "Mission accomplie";

        // Update Run History
        if (currentRun != null) {
            currentRun.endTime = simulationTime;
            currentRun.finalStatus = StatutMission.TERMINEE;
            history.add(currentRun);
            currentRun = null;
        }

        long duration = (actualEndTime - actualStartTime);
        System.out.println(" Mission terminée: " + titre + " (durée: " + duration + "s)");
    }

    // Compatibility
    public void complete() {
        complete(System.currentTimeMillis() / 1000);
    }

    /**
     * Échoue la mission
     */
    public void fail(String reason) {
        this.statut = StatutMission.ECHOUEE;
        this.actualEndTime = System.currentTimeMillis() / 1000; // Approx
        this.results = "Échec: " + reason;

        if (currentRun != null) {
            currentRun.endTime = this.actualEndTime;
            currentRun.finalStatus = StatutMission.ECHOUEE;
            currentRun.resultNote = reason;
            history.add(currentRun);
            currentRun = null;
        }

        System.out.println(" Mission échouée: " + titre + " - " + reason);
    }

    /**
     * Annule la mission
     */
    /**
     * Annule la mission
     */
    public void cancel() {
        cancel("Annulation sans motif");
    }

    public void cancel(String reason) {
        this.statut = StatutMission.ANNULEE;
        this.results = "Annulée: " + reason; // Store reason
        this.actualEndTime = System.currentTimeMillis() / 1000;

        if (currentRun != null) {
            currentRun.endTime = this.actualEndTime;
            currentRun.finalStatus = StatutMission.ANNULEE;
            currentRun.resultNote = reason; // Store reason in history
            history.add(currentRun);
            currentRun = null;
        }

        System.out.println(" Mission annulée: " + titre + " (" + reason + ")");

        // Stop all assigned assets
        for (ActifMobile asset : assignedAssets) {
            // Only stop if they are currently working on THIS mission
            if (asset.getCurrentMission() == this) {
                asset.setState(ActifMobile.AssetState.IDLE); // Or STOPPED? IDLE allows new commands.
                // Also maybe stop movement?
                asset.setTarget(asset.getX(), asset.getY(), asset.getZ()); // Stop in place
                System.out.println("   -> Actif " + asset.getId() + " arrêté (Mission annulée).");
            }
        }
    }

    // Getters
    public MissionExecution getCurrentRun() {
        return currentRun;
    }

    public java.util.List<MissionExecution> getHistory() {
        return history;
    }

    public int getRunCount() {
        return runCounter;
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

    public boolean isTerminated() {
        return statut == StatutMission.TERMINEE || statut == StatutMission.ECHOUEE
                || statut == StatutMission.ANNULEE;
    }

    public String getObjectives() {
        return objectives;
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

    public long getPlannedDurationSeconds() {
        return plannedDurationSeconds;
    }

    public long getActualStartTime() {
        return actualStartTime;
    }

    public long getActualEndTime() {
        return actualEndTime;
    }

    public java.util.List<ActifMobile> getAssignedAssets() {
        return assignedAssets;
    }

    public long getElapsedSeconds(long currentSimTime) {
        if (statut == StatutMission.EN_COURS)
            return currentSimTime - actualStartTime;
        if (statut == StatutMission.TERMINEE || statut == StatutMission.ECHOUEE)
            return actualEndTime - actualStartTime;
        return 0;
    }

    // Setters
    public void setStatut(StatutMission statut) {
        this.statut = statut;
    }

    public void setObjectives(String objectives) {
        this.objectives = objectives;
    }

    public void setPlannedDurationSeconds(long duration) {
        this.plannedDurationSeconds = duration;
    }

    public void setCompletionRule(CompletionRule rule) {
        this.completionRule = rule;
    }

    /**
     * Crée une copie de la mission (pour réutilisation)
     */
    public abstract Mission copy();
}
