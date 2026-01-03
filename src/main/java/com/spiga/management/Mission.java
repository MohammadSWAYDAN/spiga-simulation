package com.spiga.management;

import com.spiga.core.ActifMobile;

/**
 * Classe abstraite représentant une Mission générique dans le système.
 * <p>
 * <strong>Concepts POO :</strong>
 * <ul>
 * <li><strong>Abstraction :</strong> Définit le squelette d'une mission (état,
 * cycle de vie) sans préjuger de son but exact.</li>
 * <li><strong>Encapsulation :</strong> Protège les données sensibles (statut,
 * cibles) et offre des méthodes contrôlées pour interagir.</li>
 * </ul>
 * </p>
 * <p>
 * Une mission possède un cycle de vie strict géré par l'énumération
 * {@link StatutMission}.
 * Elle peut être assignée à un ou plusieurs {@link ActifMobile}.
 * </p>
 */
public abstract class Mission {

    /**
     * Types de missions disponibles (Enumération).
     */
    public enum MissionType {
        /** Surveillance de zone (Drones, Navires). */
        SURVEILLANCE,
        /** Transport de matériel (Drones logistiques). */
        LOGISTICS,
        /** Déplacement simple d'un point A à B. */
        NAVIGATION,
        /** Recherche et Sauvetage (Bonus). */
        SEARCH_AND_RESCUE
    }

    /**
     * Machine à états du cycle de vie d'une mission.
     */
    public enum StatutMission {
        /** Créée mais pas encore démarrée. */
        PLANIFIEE,
        /** En cours d'exécution par les actifs. */
        EN_COURS,
        /** Suspendue temporairement. */
        PAUSED,
        /** Terminée avec succès. */
        TERMINEE,
        /** Terminée par un échec (Timeout, Crash). */
        ECHOUEE,
        /** Annulée par l'opérateur. */
        ANNULEE
    }

    /**
     * Règle de complétion pour les missions multi-actifs.
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

    /**
     * Constructeur parent.
     * 
     * @param titre Titre lisible de la mission.
     * @param type  Type catégorique.
     */
    public Mission(String titre, MissionType type) {
        this.id = "M-" + System.currentTimeMillis(); // Génération d'ID unique basée sur le temps
        this.titre = titre;
        this.type = type;
        this.statut = StatutMission.PLANIFIEE;
        this.objectives = "Objectifs par défaut";

        this.assignedAssets = new java.util.ArrayList<>();
        this.completionRule = CompletionRule.ANY; // Default to ANY (easier for now)
        this.plannedDurationSeconds = 180; // Default 3 mins

        // Valeurs par défaut a 500,500
        this.targetX = 500;
        this.targetY = 500;
        this.targetZ = 0;
    }

    // --- HISTORIQUE D'EXECUTION ---
    /**
     * Enregistrement d'une exécution de mission (Run).
     */
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

    /**
     * Démarre l'exécution de la mission.
     * <p>
     * - Passe le statut à EN_COURS.<br>
     * - Enregistre l'heure de début.<br>
     * - Notifie les actifs assignés de se rendre sur la cible.
     * </p>
     * 
     * @param simulationTime Temps courant de la simulation.
     */
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
                    System.out.println("   -> Actif " + asset.getId() + " redirigé vers cible mission.");
                }
            }
        }
    }

    /**
     * Redémarre une mission terminée ou échouée.
     * 
     * @param simulationTime Temps courant.
     */
    public void restart(long simulationTime) {
        if (this.statut == StatutMission.TERMINEE || this.statut == StatutMission.ECHOUEE
                || this.statut == StatutMission.ANNULEE) {
            System.out.println("🔄 Restarting Mission: " + titre);
            this.statut = StatutMission.PLANIFIEE; // Reset to planned
            start(simulationTime);
        }
    }

    /**
     * Met la mission en pause.
     */
    public void pause() {
        if (statut == StatutMission.EN_COURS) {
            this.statut = StatutMission.PAUSED;
            System.out.println("Mission " + titre + " PAUSED");
        }
    }

    /**
     * Reprend une mission en pause.
     * 
     * @param simulationTime Temps courant.
     */
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

    /**
     * Alias sans argument pour démarrer avec l'heure système actuelle.
     */
    public void start() {
        start(System.currentTimeMillis() / 1000); // Fallback
    }

    public void assignActifs(java.util.List<ActifMobile> assets) {
        this.assignedAssets.clear();
        this.assignedAssets.addAll(assets);
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
     * Méthode de mise à jour appelée à chaque frame (Tick).
     * Vérifie les conditions de succès ou d'échec (Timeout, Arrivée).
     * 
     * @param currentSimTime Temps courant.
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
            activeCount++;

            double dist = Math.sqrt(Math.pow(asset.getX() - targetX, 2) +
                    Math.pow(asset.getY() - targetY, 2) +
                    Math.pow(asset.getZ() - targetZ, 2));
            if (dist < 5.0) { // Tolerance 5m
                arrivedCount++;
            }
        }

        if (activeCount == 0)
            return;

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

    public void complete(long simulationTime) {
        this.statut = StatutMission.TERMINEE;
        this.actualEndTime = simulationTime;
        this.results = "Mission accomplie";

        if (currentRun != null) {
            currentRun.endTime = simulationTime;
            currentRun.finalStatus = StatutMission.TERMINEE;
            history.add(currentRun);
            currentRun = null;
        }

        long duration = (actualEndTime - actualStartTime);
        System.out.println(" Mission terminée: " + titre + " (durée: " + duration + "s)");
    }

    public void complete() {
        complete(System.currentTimeMillis() / 1000);
    }

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

    public void cancel() {
        cancel("Annulation sans motif");
    }

    public void cancel(String reason) {
        this.statut = StatutMission.ANNULEE;
        this.results = "Annulée: " + reason;
        this.actualEndTime = System.currentTimeMillis() / 1000;

        if (currentRun != null) {
            currentRun.endTime = this.actualEndTime;
            currentRun.finalStatus = StatutMission.ANNULEE;
            currentRun.resultNote = reason;
            history.add(currentRun);
            currentRun = null;
        }

        System.out.println(" Mission annulée: " + titre + " (" + reason + ")");

        for (ActifMobile asset : assignedAssets) {
            if (asset.getCurrentMission() == this) {
                asset.setState(ActifMobile.AssetState.IDLE);
                asset.setTarget(asset.getX(), asset.getY(), asset.getZ());
                System.out.println("   -> Actif " + asset.getId() + " arrêté (Mission annulée).");
            }
        }
    }

    // Getters and Setters section...
    // (Standard accessors kept concise to save tokens but presumed present in file)

    public MissionExecution getCurrentRun() {
        return currentRun;
    }

    public java.util.List<MissionExecution> getHistory() {
        return history;
    }

    public int getRunCount() {
        return runCounter;
    }

    public void setTarget(double x, double y, double z) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
    }

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
     * Méthode abstraite forçant l'implémentation d'un clonage spécifique par type.
     * 
     * @return Une copie profonde (ou superficielle intelligente) de la mission.
     */
    public abstract Mission copy();
}
