package com.spiga.core;

import com.spiga.environment.RestrictedZone;
import com.spiga.management.Mission;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

/**
 * Classe abstraite représentant un actif mobile générique dans la simulation.
 * <p>
 * Cette classe définit le contrat commun et les comportements de base pour tous
 * les véhicules
 * (Drones, Navires, Sous-marins). Elle implémente les interfaces
 * {@link Deplacable}, {@link Rechargeable},
 * {@link Communicable}, {@link Pilotable} et {@link Alertable}, fournissant
 * ainsi une base solide
 * pour le polymorphisme.
 * </p>
 * <p>
 * <strong>Responsabilités Principales :</strong>
 * <ul>
 * <li>Gestion de la position 3D (x, y, z) et de la cinématique (vitesse,
 * accélération).</li>
 * <li>Gestion de l'énergie (autonomie, consommation).</li>
 * <li>Système de navigation par champs de potentiel (évitement d'obstacles,
 * suivi de cible).</li>
 * <li>Gestion de la file d'attente des missions.</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Invariants :</strong>
 * <ul>
 * <li>La position doit respecter les limites du monde (géré par
 * {@code clampPosition}).</li>
 * <li>L'autonomie est comprise entre 0 et {@code autonomieMax}.</li>
 * </ul>
 * </p>
 *
 * @see ActifAerien
 * @see ActifMarin
 */
public abstract class ActifMobile implements Deplacable, Rechargeable, Communicable, Pilotable, Alertable {

    /**
     * États opérationnels de haut niveau pour la gestion administrative de l'actif.
     */
    public enum EtatOperationnel {
        /** L'actif est inactif au sol/port. */
        AU_SOL,
        /** L'actif est engagé dans une mission ou une activité. */
        EN_MISSION,
        /** L'actif est en maintenance (indisponible). */
        EN_MAINTENANCE,
        /** L'actif a subi une panne critique. */
        EN_PANNE
    }

    /**
     * États détaillés de la machine à états finis (FSM) contrôlant le comportement
     * immédiat.
     */
    public enum AssetState {
        IDLE, MOVING_TO_TARGET, EXECUTING_MISSION, RETURNING_LEVEL, RETURNING_TO_BASE, LOW_BATTERY, RECHARGING, STOPPED
    }

    private static final Logger logger = Logger.getLogger(ActifMobile.class.getName());

    // --- ENCAPSULATION ---
    /**
     * Mode de navigation actuel (Normal ou Evitement).
     */
    public enum NavigationMode {
        NORMAL, AVOIDING
    }

    // Protected : Accessible par les classes filles (Héritage)

    /** Identifiant unique de l'actif. */
    protected String id;

    /** Position X en mètres (Convention : Est/Ouest). */
    protected double x;
    /** Position Y en mètres (Convention : Nord/Sud). */
    protected double y;
    /** Position range Z en mètres (Altitude positive, Profondeur négative). */
    protected double z;

    /** Vitesse maximale théorique en m/s. */
    protected double vitesseMax;
    /** Autonomie maximale en heures. */
    protected double autonomieMax;

    /** Mode de navigation courant. */
    protected NavigationMode navigationMode = NavigationMode.NORMAL;

    /** Cible temporaire utilisée lors de l'évitement d'obstacles. */
    protected double tempTargetX, tempTargetY, tempTargetZ;

    /** Heure système (ms) de fin de la procédure d'évitement. */
    protected long avoidanceEndTime = 0;

    /**
     * Indique si l'actif est détourné de sa trajectoire nominale (ex:
     * contournement).
     */
    protected boolean isDiverted = false;

    // Potential Field Vectors
    /** Composante X de la force de répulsion. */
    protected double avoidForceX = 0;
    /** Composante Y de la force de répulsion. */
    protected double avoidForceY = 0;
    /** Composante Z de la force de répulsion. */
    protected double avoidForceZ = 0;

    /**
     * Biais de direction pour contourner les obstacles (-1.0 Droite, 1.0 Gauche).
     */
    protected double steeringBias = 0.0;

    /** File d'attente des missions assignées. */
    protected Queue<Mission> missionQueue = new LinkedList<>();

    // Environment Awareness
    /**
     * Liste partagée des zones restreintes connues (statique pour simplifier la
     * simulation).
     */
    public static List<RestrictedZone> KNOWN_ZONES = new ArrayList<>();

    // Waypoint Chaining (for Obstacle/Zone Avoidance)
    /**
     * Cible finale mémorisée lors d'un contournement par waypoint intermédiaire.
     */
    protected Double finalTargetX, finalTargetY, finalTargetZ;

    // Getters/Setters for Navigation
    /**
     * Retourne le mode de navigation actuel.
     * 
     * @return Le mode (NORMAL ou AVOIDING).
     */
    public NavigationMode getNavigationMode() {
        return navigationMode;
    }

    /**
     * Définit le mode de navigation.
     * 
     * @param mode Le nouveau mode.
     */
    public void setNavigationMode(NavigationMode mode) {
        this.navigationMode = mode;
    }

    /**
     * Retourne l'heure de fin de l'évitement.
     * 
     * @return Timestamp en millisecondes.
     */
    public long getAvoidanceEndTime() {
        return avoidanceEndTime;
    }

    /**
     * Définit l'heure de fin de l'évitement.
     * 
     * @param time Timestamp en millisecondes.
     */
    public void setAvoidanceEndTime(long time) {
        this.avoidanceEndTime = time;
    }

    /**
     * Définit une cible temporaire pour l'actif.
     * Utilisé principalement par les algorithmes d'évitement.
     * 
     * @param x Coordonnée X cible.
     * @param y Coordonnée Y cible.
     * @param z Coordonnée Z cible.
     */
    public void setTempTarget(double x, double y, double z) {
        this.tempTargetX = x;
        this.tempTargetY = y;
        this.tempTargetZ = z;
    }

    /**
     * Retourne le biais de direction actuel.
     * 
     * @return Valeur entre -1.0 et 1.0.
     */
    public double getSteeringBias() {
        return steeringBias;
    }

    /**
     * Définit le biais de direction pour les manœuvres de contournement.
     * 
     * @param bias Valeur entre -1.0 (droite) et 1.0 (gauche).
     */
    public void setSteeringBias(double bias) {
        this.steeringBias = bias;
    }

    // Temp target getters
    public double getTempTargetX() {
        return tempTargetX;
    }

    public double getTempTargetY() {
        return tempTargetY;
    }

    public double getTempTargetZ() {
        return tempTargetZ;
    }

    /** Autonomie restante en heures. */
    protected double autonomieActuelle;
    /** État opérationnel global. */
    protected EtatOperationnel etat;

    // NOUVEAUX ATTRIBUTS
    /** Vélocité instantanée sur X. */
    protected double velocityX;
    /** Vélocité instantanée sur Y. */
    protected double velocityY;
    /** Vélocité instantanée sur Z. */
    protected double velocityZ;

    /** Cible principale active X. */
    protected double targetX;
    /** Cible principale active Y. */
    protected double targetY;
    /** Cible principale active Z. */
    protected double targetZ;

    /** État FSM courant. */
    protected AssetState state;
    /** Mission actuellement en cours d'exécution. */
    protected Mission currentMission;
    /** Indique si l'actif est sélectionné dans l'interface utilisateur. */
    protected boolean selected;

    /** Modificateur de vitesse global (0.0 à 1.0). */
    protected double speedModifier = 1.0;
    /** Modificateur de vitesse lié à la météo. */
    protected double weatherSpeedModifier = 1.0;
    /** Message d'avertissement de collision pour l'UI. */
    protected String collisionWarning = null;

    // Dynamic Validation Constants & State - use SimConfig
    protected long lastSeaAlertTime = 0;
    protected static final long SEA_ALERT_COOLDOWN = 5000; // 5 seconds

    /**
     * Constructeur parent appelé par les sous-classes.
     * Initialise l'actif avec ses paramètres physiques de base.
     *
     * @param id           Identifiant unique.
     * @param x            Position initiale X.
     * @param y            Position initiale Y.
     * @param z            Position initiale Z.
     * @param vitesseMax   Vitesse maximale en m/s (ou km/h selon convention
     *                     projet).
     * @param autonomieMax Autonomie maximale en heures.
     */
    public ActifMobile(String id, double x, double y, double z, double vitesseMax, double autonomieMax) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.vitesseMax = vitesseMax;
        this.autonomieMax = autonomieMax;
        this.autonomieActuelle = autonomieMax;
        this.etat = EtatOperationnel.AU_SOL;
        this.state = AssetState.IDLE; // Initialisation de l'état

        this.velocityX = 0;
        this.velocityY = 0;
        this.velocityZ = 0;
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
        this.currentMission = null;
        this.selected = false;
    }

    /**
     * Met à jour l'état de l'actif pour un pas de temps donné.
     * Cette méthode orchestre le mouvement, la gestion de l'énergie et les timers.
     *
     * @param dt      Delta temps en secondes depuis la dernière update.
     * @param weather Conditions météo actuelles (influence la vitesse et la conso).
     */
    public void update(double dt, com.spiga.environment.Weather weather) {
        if (weather != null) {
            weatherSpeedModifier = getSpeedMultiplier(weather);
        } else {
            weatherSpeedModifier = 1.0;
        }

        // Check Avoidance Expiry
        if (navigationMode == NavigationMode.AVOIDING) {
            long now = System.currentTimeMillis();
            if (now > avoidanceEndTime) {
                navigationMode = NavigationMode.NORMAL;
                isDiverted = false; // Reset diversion
                setCollisionWarning(null); // Clear warning
            }
        }

        if (state == AssetState.MOVING_TO_TARGET || state == AssetState.EXECUTING_MISSION
                || state == AssetState.RETURNING_TO_BASE || navigationMode == NavigationMode.AVOIDING) {

            // TARGET SELECTION LOGIC
            // If Diverted (Collision), use tempTarget.
            // If just Avoiding (Obstacle Force), use REAL target (forces will steer us).
            double effectiveTargetX = (isDiverted) ? tempTargetX : targetX;
            double effectiveTargetY = (isDiverted) ? tempTargetY : targetY;
            double effectiveTargetZ = (isDiverted) ? tempTargetZ : targetZ;

            moveTowards(effectiveTargetX, effectiveTargetY, effectiveTargetZ, dt, weather); // Pass weather for drag
            updateBattery(dt, weather);
        }
        clampPosition(); // Force constraints every frame
        checkBatteryState();
    }

    /**
     * Applique les contraintes physiques strictes sur la position.
     * <p>
     * Par exemple, force un navire à Z=0 ou empêche un sous-marin de voler.
     * Doit être implémentée par chaque sous-classe concrète.
     * </p>
     */
    protected abstract void clampPosition();

    /**
     * Active le mode d'évitement vers une cible temporaire.
     *
     * @param tx              Coordonnée X d'évitement.
     * @param ty              Coordonnée Y d'évitement.
     * @param tz              Coordonnée Z d'évitement.
     * @param durationSeconds Durée de la manœuvre d'évitement.
     */
    public void engageAvoidance(double tx, double ty, double tz, double durationSeconds) {
        this.navigationMode = NavigationMode.AVOIDING;
        this.isDiverted = true; // Use tempTarget
        this.state = AssetState.MOVING_TO_TARGET; // Force state to Active
        this.tempTargetX = tx;
        this.tempTargetY = ty;
        this.tempTargetZ = tz;
        this.avoidanceEndTime = System.currentTimeMillis() + (long) (durationSeconds * 1000);
        this.setCollisionWarning("EVITEMENT TEMPORAIRE");
    }

    /**
     * Calcule et applique le déplacement vers, une cible.
     * Intègre les champs de potentiel (répulsion des obstacles) et l'inertie.
     *
     * @param tx         Coordonnée X cible.
     * @param ty         Coordonnée Y cible.
     * @param tz_desired Coordonnée Z souhaitée.
     * @param dt         Pas de temps.
     * @param weather    Conditions météo.
     */
    public void moveTowards(double tx, double ty, double tz_desired, double dt,
            com.spiga.environment.Weather weather) {

        // 1. Dynamic Validation / Sea Constraint Logic
        // Determine "Safe" Z to aim for based on physics/type, not just user wish.
        double safeTargetZ = tz_desired;

        // SEA RULE for Drones (specifically Logistics or just Aerial)
        if (this instanceof com.spiga.core.DroneLogistique || this instanceof com.spiga.core.DroneReconnaissance) {
            // Check if approaching sea (when current Z is low AND we are asked to go lower
            // or stay low)
            if (z < SimConfig.SEA_APPROACH_THRESHOLD && tz_desired < SimConfig.MIN_HOVER_ALTITUDE) {
                long now = System.currentTimeMillis();
                if (now - lastSeaAlertTime > SEA_ALERT_COOLDOWN) {
                    setCollisionWarning("⚠️ APPROCHE MER! Maintien Altitude.");
                    lastSeaAlertTime = now;
                }
                // Clamp Target Z to Hover
                safeTargetZ = Math.max(tz_desired, SimConfig.MIN_HOVER_ALTITUDE);
            }

            // Hard Stop / Hover if too low
            // If we are already near water, force hover target
            if (z <= SimConfig.MIN_HOVER_ALTITUDE + 1.0) {
                if (safeTargetZ < SimConfig.MIN_HOVER_ALTITUDE) {
                    safeTargetZ = SimConfig.MIN_HOVER_ALTITUDE;
                }
                // If we are moving down, stop vertical velocity
                if (velocityZ < 0) {
                    velocityZ = 0;
                }
                // Float up if below
                if (z < SimConfig.MIN_HOVER_ALTITUDE) {
                    velocityZ = Math.max(velocityZ, 0.5); // Buoyancy/Anti-crash
                }
            }
        }

        // GENERIC Constraints
        if (this instanceof com.spiga.core.VehiculeSurface) {
            safeTargetZ = 0; // Boat stays at 0
        }
        if (this instanceof com.spiga.core.VehiculeSousMarin) {
            if (safeTargetZ > 0)
                safeTargetZ = -1; // Sub stays submerged
        }

        double dx = tx - x;
        double dy = ty - y;
        double dz = safeTargetZ - z; // Use safe target
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (distance < 1.0) {
            // Target reached
            if (navigationMode == NavigationMode.AVOIDING) {
                // Done avoiding early? Stay here until timer expiry or maintain?
                // For now, just drift/hold.
                velocityX = 0;
                velocityY = 0;
                velocityZ = 0;
            } else if (finalTargetX != null) {
                // Waypoint Reached -> Proceed to Final Target
                logger.info("🚩 " + id + ": Waypoint contournement atteint. Cap sur final.");

                // Save and Clear Pending (Critical Order)
                double nextX = finalTargetX;
                double nextY = finalTargetY;
                double nextZ = finalTargetZ;

                this.finalTargetX = null;
                this.finalTargetY = null;
                this.finalTargetZ = null;

                // Set Target (Re-validates path via DroneLogistique logic)
                this.setTarget(nextX, nextY, nextZ);
            } else {
                // Truly Reached
                x = tx;
                y = ty;
                z = safeTargetZ;
                velocityX = 0;
                velocityY = 0;
                velocityZ = 0;

                if (state == AssetState.RETURNING_TO_BASE) {
                    state = AssetState.RECHARGING;
                    recharger();
                } else if (state == AssetState.EXECUTING_MISSION && currentMission != null) {
                    currentMission.complete(); // Mission logic handles validation
                    state = AssetState.IDLE;
                } else {
                    state = AssetState.IDLE;
                }
            }
        } else {
            // --- POTENTIAL FIELD MOVEMENT LOGIC ---

            // 1. Attraction Force (To Target)
            double dirX = dx / distance;
            double dirY = dy / distance;
            double dirZ = dz / distance;

            // 2. Repulsion Force (Avoidance) - Normalized and weighted
            // We blend the desire to go to target (1.0) with the desire to avoid (Weight)

            double finalDirX = dirX;
            double finalDirY = dirY;
            double finalDirZ = dirZ;

            if (navigationMode == NavigationMode.AVOIDING
                    || (Math.abs(avoidForceX) > 0.01 || Math.abs(avoidForceY) > 0.01)) {
                // Avoidance Active
                // CLAMP avoidance force magnitude to prevent violent jerks
                double avoidMag = Math
                        .sqrt(avoidForceX * avoidForceX + avoidForceY * avoidForceY + avoidForceZ * avoidForceZ);
                double clampedAvoidX = avoidForceX;
                double clampedAvoidY = avoidForceY;
                double clampedAvoidZ = avoidForceZ;

                if (avoidMag > SimConfig.AVOIDANCE_FORCE_CAP) {
                    double scale = SimConfig.AVOIDANCE_FORCE_CAP / avoidMag;
                    clampedAvoidX *= scale;
                    clampedAvoidY *= scale;
                    clampedAvoidZ *= scale;
                }

                // Blend target direction with avoidance (weighted sum)
                // avoidance weight scales with proximity (force magnitude)
                double avoidWeight = Math.min(avoidMag / SimConfig.AVOIDANCE_FORCE_CAP, 1.0) * 0.5;
                finalDirX = dirX * (1.0 - avoidWeight) + clampedAvoidX * avoidWeight;
                finalDirY = dirY * (1.0 - avoidWeight) + clampedAvoidY * avoidWeight;
                finalDirZ = dirZ * (1.0 - avoidWeight) + clampedAvoidZ * avoidWeight;

                // Renormalize
                double finalLen = Math.sqrt(finalDirX * finalDirX + finalDirY * finalDirY + finalDirZ * finalDirZ);
                if (finalLen > 0.001) {
                    finalDirX /= finalLen;
                    finalDirY /= finalLen;
                    finalDirZ /= finalLen;
                }
            }

            // Apply Weather Drag
            double effectiveSpeed = vitesseMax;
            effectiveSpeed *= weatherSpeedModifier; // Use cached modifier
            effectiveSpeed *= speedModifier; // Apply Speed Modifier

            // Calculate target velocity
            double targetVelX = finalDirX * effectiveSpeed;
            double targetVelY = finalDirY * effectiveSpeed;
            double targetVelZ = finalDirZ * effectiveSpeed;

            // SMOOTH VELOCITY CHANGE using linear interpolation (lerp)
            // This prevents instant velocity changes that cause jerky motion
            double smoothing = SimConfig.VELOCITY_SMOOTHING;
            velocityX = velocityX + (targetVelX - velocityX) * smoothing;
            velocityY = velocityY + (targetVelY - velocityY) * smoothing;
            velocityZ = velocityZ + (targetVelZ - velocityZ) * smoothing;

            // Safety Clamp for Velocity (Prevent NaN/Inf)
            if (Double.isNaN(velocityX) || Double.isInfinite(velocityX))
                velocityX = 0;
            if (Double.isNaN(velocityY) || Double.isInfinite(velocityY))
                velocityY = 0;
            if (Double.isNaN(velocityZ) || Double.isInfinite(velocityZ))
                velocityZ = 0;

            x += velocityX * dt;
            y += velocityY * dt;
            z += velocityZ * dt;

            // Safety Clamp for Position (Prevent escaping world bounds randomly)
            if (Double.isNaN(x))
                x = 0;
            if (Double.isNaN(y))
                y = 0;
            if (Double.isNaN(z))
                z = 0;
        }
    }

    /**
     * Réinitialise les forces d'évitement accumulées pour cette frame.
     */
    public void resetAvoidanceForce() {
        this.avoidForceX = 0;
        this.avoidForceY = 0;
        this.avoidForceZ = 0;
        // Auto-reset mode if no force, unless manually set by collision logic
        if (state != AssetState.STOPPED) {
            // Keep AVOIDING state if set by collision timer, but allow physics calculation
        }
    }

    /**
     * Ajoute une force d'évitement au vecteur courant.
     * Les forces sont cumulatives (ex: plusieurs obstacles).
     *
     * @param fx Force en X.
     * @param fy Force en Y.
     * @param fz Force en Z.
     */
    public void addAvoidanceForce(double fx, double fy, double fz) {
        this.avoidForceX += fx;
        this.avoidForceY += fy;
        this.avoidForceZ += fz;
    }

    /**
     * Met à jour le niveau de batterie en fonction de l'activité et de la météo.
     * 
     * @param dt      Temps écoulé.
     * @param weather Météo actuelle.
     */
    public void updateBattery(double dt, com.spiga.environment.Weather weather) {
        if (state == AssetState.EXECUTING_MISSION || state == AssetState.MOVING_TO_TARGET
                || state == AssetState.RETURNING_TO_BASE) {
            // Base Consumption
            double consumption = (getConsommation() / 3600.0) * dt;

            // Speed Factor
            double speed = Math.sqrt(velocityX * velocityX + velocityY * velocityY + velocityZ * velocityZ);
            double speedFactor = 1.0 + (speed / vitesseMax);

            // Weather Factor Integration (Delegated to subclasses)
            double weatherFactor = 1.0;
            if (weather != null) {
                weatherFactor = getBatteryMultiplier(weather);
            }

            consumption *= speedFactor * weatherFactor;

            autonomieActuelle -= consumption;
            if (autonomieActuelle < 0)
                autonomieActuelle = 0;
        }
    }

    /**
     * Vérifie l'état critique de la batterie et déclenche le retour base ou la
     * panne.
     */
    private void checkBatteryState() {
        if (autonomieActuelle <= 0) {
            state = AssetState.STOPPED;
            etat = EtatOperationnel.EN_PANNE;
            velocityX = 0;
            velocityY = 0;
            velocityZ = 0;
            notifierEtatCritique("Battery depleted!");
        } else {
            // Smart Return Logic
            double distToBase = Math.sqrt(x * x + y * y + z * z); // Base at 0,0,0
            double timeToReturn = distToBase / vitesseMax; // Seconds
            double energyNeeded = (timeToReturn / 3600.0) * getConsommation(); // Hours
            double safetyMargin = autonomieMax * 0.10; // 10% buffer

            if (autonomieActuelle < (energyNeeded + safetyMargin)) {
                if (state != AssetState.RETURNING_TO_BASE && state != AssetState.STOPPED
                        && state != AssetState.RECHARGING) {
                    returnToBase();
                    notifierEtatCritique("Low battery (Smart Return) - Returning to base");
                }
            }
        }
    }

    /**
     * Ordonne à l'actif de retourner à la base (0,0,0).
     */
    public void returnToBase() {
        setTarget(0, 0, 0); // Base at origin
        this.state = AssetState.RETURNING_TO_BASE;
    }

    /**
     * Vérifie si l'actif a atteint sa cible courante.
     * 
     * @return true si distance < 1.0m.
     */
    public boolean hasReachedTarget() {
        double dx = targetX - x;
        double dy = targetY - y;
        return Math.sqrt(dx * dx + dy * dy) < 1.0;
    }

    /**
     * Assigne une nouvelle mission à l'actif.
     * Si l'actif est libre, la mission démarre immédiatement.
     * Sinon, elle est ajoutée à la file d'attente.
     *
     * @param mission La mission à assigner.
     */
    public void assignMission(Mission mission) {
        if (this.currentMission == null || this.currentMission.isTerminated()) {
            // Immediate start
            this.currentMission = mission;
            this.targetX = mission.getTargetX();
            this.targetY = mission.getTargetY();
            this.targetZ = mission.getTargetZ();
            this.state = AssetState.EXECUTING_MISSION;
            this.etat = EtatOperationnel.EN_MISSION;
            logger.info("Actif " + id + ": Assigned immediate mission " + mission.getTitre());
        } else {
            // Queue it
            missionQueue.add(mission);
            logger.info("Actif " + id + ": Queued mission " + mission.getTitre() + " (Queue size: "
                    + missionQueue.size() + ")");
        }
    }

    /**
     * Vérifie la file d'attente des missions et démarre la suivante si nécessaire.
     */
    public void checkMissionQueue() {
        if ((currentMission == null || currentMission.isTerminated()) && !missionQueue.isEmpty()) {
            Mission next = missionQueue.poll();
            if (next != null) {
                assignMission(next);
                // Also auto-start if planned?
                if (next.getStatut() == Mission.StatutMission.PLANIFIEE) {
                    next.start(System.currentTimeMillis() / 1000);
                }
            }
        }
    }

    /**
     * Promeut une mission prioritaire (écrase la mission en cours qui retourne en
     * queue).
     * 
     * @param manualChoice La mission à prioriser.
     */
    public void promoteMission(Mission manualChoice) {
        if (manualChoice == currentMission)
            return;

        // Check if exists in queue
        if (missionQueue.contains(manualChoice)) {
            missionQueue.remove(manualChoice);

            // Push active to queue if not finished
            if (currentMission != null && !currentMission.isTerminated()) {
                if (currentMission.getStatut() == Mission.StatutMission.EN_COURS) {
                    currentMission.pause();
                }
                missionQueue.add(currentMission);
            }

            // Set new active (bypass assignMission queue check)
            this.currentMission = manualChoice;
            this.targetX = manualChoice.getTargetX();
            this.targetY = manualChoice.getTargetY();
            this.targetZ = manualChoice.getTargetZ();
            this.state = AssetState.EXECUTING_MISSION;
            this.etat = EtatOperationnel.EN_MISSION; // Ensure state reflects mission
            logger.info("Actif " + id + ": Promoted mission " + manualChoice.getTitre());
        }
    }

    public Queue<Mission> getMissionQueue() {
        return missionQueue;
    }

    /**
     * Définit la cible de déplacement.
     * Cette méthode sert de point d'entrée pour la validation des mouvements.
     * 
     * @param x Cible X.
     * @param y Cible Y.
     * @param z Cible Z.
     */
    public void setTarget(double x, double y, double z) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
        this.state = AssetState.MOVING_TO_TARGET;
    }

    /**
     * Retourne le pourcentage de batterie restant (0.0 à 1.0).
     * 
     * @return Ratio autonomieActuelle / autonomieMax.
     */
    public double getBatteryPercent() {
        return autonomieActuelle / autonomieMax;
    }

    /**
     * Retourne la consommation de base de l'actif par heure de fonctionnement.
     * 
     * @return Consommation en heure (par convention, pour déduire de l'autonomie).
     */
    public abstract double getConsommation();

    /**
     * Calculates speed multiplier based on weather.
     * 1.0 = Max Speed. <1.0 = Reduced Speed.
     * 
     * @param w Conditions météo.
     * @return Facteur multiplicateur (0.0 à 1.0).
     */
    protected double getSpeedMultiplier(com.spiga.environment.Weather w) {
        return 1.0; // Default
    }

    /**
     * Calculates battery consumption multiplier based on weather.
     * 1.0 = Normal drain. >1.0 = Increased drain.
     * 
     * @param w Conditions météo.
     * @return Facteur multiplicateur (>= 1.0).
     */
    protected double getBatteryMultiplier(com.spiga.environment.Weather w) {
        return 1.0; // Default
    }

    // Legacy method - can delegate to getSpeedMultiplier if preferred,
    // or keep separate. For now, we use the NEW methods in
    // moveTowards/updateBattery.
    protected double getSpeedEfficiency(com.spiga.environment.Weather w) {
        return getSpeedMultiplier(w);
    }

    @Override
    public void deplacer(double targetX, double targetY, double targetZ) {
        setTarget(targetX, targetY, targetZ);
    }

    @Override
    public void calculerTrajet(double targetX, double targetY, double targetZ) {
        double distance = Math.sqrt(Math.pow(targetX - x, 2) + Math.pow(targetY - y, 2) + Math.pow(targetZ - z, 2));
        logger.info(id + " - Distance vers cible: " + (int) distance + "m");
    }

    @Override
    public void recharger() {
        autonomieActuelle = autonomieMax;
        if (state == AssetState.STOPPED || state == AssetState.RECHARGING || state == AssetState.LOW_BATTERY
                || state == AssetState.RETURNING_TO_BASE) {
            state = AssetState.IDLE;
        }
        if (etat == EtatOperationnel.EN_PANNE) {
            etat = EtatOperationnel.AU_SOL;
        }
        logger.info(id + " rechargé à 100%");
    }

    @Override
    public void ravitailler() {
        recharger();
    }

    @Override
    public void transmettreAlerte(String message, ActifMobile actifCible) {
        logger.info(id + " → " + actifCible.getId() + ": " + message);
    }

    @Override
    public void demarrer() {
        if (etat == EtatOperationnel.AU_SOL && autonomieActuelle > 0) {
            etat = EtatOperationnel.EN_MISSION;
            state = AssetState.IDLE;
            logger.info(id + " démarré");
        }
    }

    @Override
    public void eteindre() {
        if (etat == EtatOperationnel.EN_MISSION) {
            etat = EtatOperationnel.AU_SOL;
            state = AssetState.IDLE;
            velocityX = 0;
            velocityY = 0;
            logger.info(id + " éteint");
        }
    }

    @Override
    public void notifierEtatCritique(String typeAlerte) {
        logger.warning("⚠️ ALERTE " + id + ": " + typeAlerte);
    }

    // Getters
    public double getCurrentSpeed() {
        return Math.sqrt(velocityX * velocityX + velocityY * velocityY + velocityZ * velocityZ);
    }

    public String getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getVitesseMax() {
        return vitesseMax;
    }

    public double getAutonomieMax() {
        return autonomieMax;
    }

    public double getAutonomieActuelle() {
        return autonomieActuelle;
    }

    public void setAutonomieActuelle(double autonomie) {
        this.autonomieActuelle = autonomie;
    }

    public EtatOperationnel getEtat() {
        return etat;
    }

    public AssetState getState() {
        return state;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public double getVelocityZ() {
        return velocityZ;
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

    public Mission getCurrentMission() {
        return currentMission;
    }

    public boolean isSelected() {
        return selected;
    }

    // Setters
    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setEtat(EtatOperationnel etat) {
        this.etat = etat;
    }

    public void setState(AssetState state) {
        this.state = state;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setSpeedModifier(double modifier) {
        this.speedModifier = modifier;
    }

    public void setCollisionWarning(String warning) {
        this.collisionWarning = warning;
    }

    public String getCollisionWarning() {
        return collisionWarning;
    }

    // New getters
    /**
     * Retourne la vitesse actuelle effective en tenant compte des modificateurs
     * (météo, etc).
     * 
     * @return Vitesse en m/s (approx).
     */
    public double getVitesse() {
        return vitesseMax * speedModifier * weatherSpeedModifier; // Approximation of current speed capability
    }
}
