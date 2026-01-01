package com.spiga.core;

import com.spiga.environment.RestrictedZone;
import com.spiga.management.Mission;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
<<<<<<< HEAD
import java.util.logging.Logger;
import java.util.logging.Level;
=======
>>>>>>> 2e1c7d997378ffc2a62a0fdc8796641db0ce29fa

/**
 * Classe Abstraite : ActifMobile
 * 
 * CONCEPTS CLES (POO) :
 * 
 * 1. Abstraction (abstract class) :
 * - C'est quoi ? Une classe qu'on ne peut pas creer directement (pas de new
 * ActifMobile()).
 * - Pourquoi ici ? Un "ActifMobile" est un concept general. Dans la realite, on
 * a des Drones ou des Navires, pas juste des "mobiles" flous.
 * Cette classe sert de base commune a tous les types de vehicules.
 * 
 * 2. Encapsulation (protected/private) :
 * - C'est quoi ? Proteger les donnees de l'objet.
 * - Pourquoi ici ? Les attributs x, y, z sont protected pour que seuls cet
 * objet et ses ENFANTS (Drones, etc.) puissent les modifier directement.
 * 
 * 3. Polymorphisme (Interfaces) :
 * - C'est quoi ? Un objet peut prendre plusieurs formes.
 * - Pourquoi ici ? En implementant Deplacable, Pilotable, cet objet promet
 * qu'il sait "se deplacer" et "etre pilote".
 * Le systeme pourra donc traiter tous les ActifMobile de la meme facon.
 * 
 * Conforme a SPIGA-SPEC.txt section 1.1
 */
public abstract class ActifMobile implements Deplacable, Rechargeable, Communicable, Pilotable, Alertable {

    public enum EtatOperationnel {
        AU_SOL, EN_MISSION, EN_MAINTENANCE, EN_PANNE
    }

    public enum AssetState {
        IDLE, MOVING_TO_TARGET, EXECUTING_MISSION, RETURNING_LEVEL, RETURNING_TO_BASE, LOW_BATTERY, RECHARGING, STOPPED
    }

    private static final Logger logger = Logger.getLogger(ActifMobile.class.getName());

    // --- ENCAPSULATION ---
    public enum NavigationMode {
        NORMAL, AVOIDING
    }

    // Protected : Accessible par les classes filles (Héritage)
    protected String id;
    protected double x, y, z; // Position 3D
    protected double vitesseMax;
    protected double autonomieMax; // Heures

    protected NavigationMode navigationMode = NavigationMode.NORMAL;
    protected double tempTargetX, tempTargetY, tempTargetZ;
    protected long avoidanceEndTime = 0;
<<<<<<< HEAD
    protected boolean isDiverted = false; // True only if avoiding via Waypoint (Collision)

    // Potential Field Vectors
    protected double avoidForceX = 0;
    protected double avoidForceY = 0;
    protected double avoidForceZ = 0;
    protected double steeringBias = 0.0; // -1.0 (Right), 0.0 (None), 1.0 (Left)
=======
>>>>>>> 2e1c7d997378ffc2a62a0fdc8796641db0ce29fa

    protected Queue<Mission> missionQueue = new LinkedList<>(); // Mission Queue

    // Environment Awareness
    public static List<RestrictedZone> KNOWN_ZONES = new ArrayList<>();

    // Waypoint Chaining (for Obstacle/Zone Avoidance)
    protected Double finalTargetX, finalTargetY, finalTargetZ;

    // Getters/Setters for Navigation
    public NavigationMode getNavigationMode() {
        return navigationMode;
    }

    public void setNavigationMode(NavigationMode mode) {
        this.navigationMode = mode;
    }

    public long getAvoidanceEndTime() {
        return avoidanceEndTime;
    }

    public void setAvoidanceEndTime(long time) {
        this.avoidanceEndTime = time;
    }

    public void setTempTarget(double x, double y, double z) {
        this.tempTargetX = x;
        this.tempTargetY = y;
        this.tempTargetZ = z;
    }

<<<<<<< HEAD
    public double getSteeringBias() {
        return steeringBias;
    }

    public void setSteeringBias(double bias) {
        this.steeringBias = bias;
    }

=======
>>>>>>> 2e1c7d997378ffc2a62a0fdc8796641db0ce29fa
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

    protected double autonomieActuelle;
    protected EtatOperationnel etat; // Enumération d'états

    // NOUVEAUX ATTRIBUTS
    protected double velocityX, velocityY, velocityZ;
    protected double targetX, targetY, targetZ;
    protected AssetState state;
    protected Mission currentMission;
    protected boolean selected;
    protected double speedModifier = 1.0;
    protected double weatherSpeedModifier = 1.0;
    protected String collisionWarning = null; // Alert message for UI

    // AVOIDANCE FIELDS - Consolidated
    // navMode removed, using navigationMode defined above
    // tempTarget fields removed, using those defined above

<<<<<<< HEAD
    // Dynamic Validation Constants & State - use SimConfig
=======
    // Dynamic Validation Constants & State
    protected static final double SEA_APPROACH_THRESHOLD = 20.0;
    protected static final double MIN_HOVER_Z_SEA = 2.0;
>>>>>>> 2e1c7d997378ffc2a62a0fdc8796641db0ce29fa
    protected long lastSeaAlertTime = 0;
    protected static final long SEA_ALERT_COOLDOWN = 5000; // 5 seconds

    /**
     * Constructeur.
     * Appelé par les classes filles via <code>super()</code>.
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

    // Updated update signature to include Weather
    public void update(double dt, com.spiga.environment.Weather weather) {
<<<<<<< HEAD
        // speedModifier = 1.0; // REMOVED: Managed by SimulationService to persist
        // across checks
=======
        speedModifier = 1.0; // Reset every frame
>>>>>>> 2e1c7d997378ffc2a62a0fdc8796641db0ce29fa

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
<<<<<<< HEAD
                isDiverted = false; // Reset diversion
=======
>>>>>>> 2e1c7d997378ffc2a62a0fdc8796641db0ce29fa
                setCollisionWarning(null); // Clear warning
            }
        }

        if (state == AssetState.MOVING_TO_TARGET || state == AssetState.EXECUTING_MISSION
                || state == AssetState.RETURNING_TO_BASE || navigationMode == NavigationMode.AVOIDING) {

<<<<<<< HEAD
            // TARGET SELECTION LOGIC
            // If Diverted (Collision), use tempTarget.
            // If just Avoiding (Obstacle Force), use REAL target (forces will steer us).
            double effectiveTargetX = (isDiverted) ? tempTargetX : targetX;
            double effectiveTargetY = (isDiverted) ? tempTargetY : targetY;
            double effectiveTargetZ = (isDiverted) ? tempTargetZ : targetZ;
=======
            double effectiveTargetX = (navigationMode == NavigationMode.AVOIDING) ? tempTargetX : targetX;
            double effectiveTargetY = (navigationMode == NavigationMode.AVOIDING) ? tempTargetY : targetY;
            double effectiveTargetZ = (navigationMode == NavigationMode.AVOIDING) ? tempTargetZ : targetZ;
>>>>>>> 2e1c7d997378ffc2a62a0fdc8796641db0ce29fa

            moveTowards(effectiveTargetX, effectiveTargetY, effectiveTargetZ, dt, weather); // Pass weather for drag
            updateBattery(dt, weather);
        }
        clampPosition(); // Force constraints every frame
        checkBatteryState();
    }

    /**
     * Enforces strict physical domain constraints.
     * e.g. Surface vessels must have Z=0.
     */
    protected abstract void clampPosition();

    public void engageAvoidance(double tx, double ty, double tz, double durationSeconds) {
        this.navigationMode = NavigationMode.AVOIDING;
<<<<<<< HEAD
        this.isDiverted = true; // Use tempTarget
=======
>>>>>>> 2e1c7d997378ffc2a62a0fdc8796641db0ce29fa
        this.state = AssetState.MOVING_TO_TARGET; // Force state to Active
        this.tempTargetX = tx;
        this.tempTargetY = ty;
        this.tempTargetZ = tz;
        this.avoidanceEndTime = System.currentTimeMillis() + (long) (durationSeconds * 1000);
        this.setCollisionWarning("EVITEMENT TEMPORAIRE");
    }

    public void moveTowards(double tx, double ty, double tz_desired, double dt,
            com.spiga.environment.Weather weather) {

        // 1. Dynamic Validation / Sea Constraint Logic
        // Determine "Safe" Z to aim for based on physics/type, not just user wish.
        double safeTargetZ = tz_desired;

        // SEA RULE for Drones (specifically Logistics or just Aerial)
        if (this instanceof com.spiga.core.DroneLogistique || this instanceof com.spiga.core.DroneReconnaissance) {
            // Check if approaching sea (when current Z is low AND we are asked to go lower
            // or stay low)
<<<<<<< HEAD
            if (z < SimConfig.SEA_APPROACH_THRESHOLD && tz_desired < SimConfig.MIN_HOVER_ALTITUDE) {
=======
            if (z < SEA_APPROACH_THRESHOLD && tz_desired < MIN_HOVER_Z_SEA) {
>>>>>>> 2e1c7d997378ffc2a62a0fdc8796641db0ce29fa
                long now = System.currentTimeMillis();
                if (now - lastSeaAlertTime > SEA_ALERT_COOLDOWN) {
                    setCollisionWarning("⚠️ APPROCHE MER! Maintien Altitude.");
                    lastSeaAlertTime = now;
                }
                // Clamp Target Z to Hover
<<<<<<< HEAD
                safeTargetZ = Math.max(tz_desired, SimConfig.MIN_HOVER_ALTITUDE);
=======
                safeTargetZ = Math.max(tz_desired, MIN_HOVER_Z_SEA);
>>>>>>> 2e1c7d997378ffc2a62a0fdc8796641db0ce29fa
            }

            // Hard Stop / Hover if too low
            // If we are already near water, force hover target
<<<<<<< HEAD
            if (z <= SimConfig.MIN_HOVER_ALTITUDE + 1.0) {
                if (safeTargetZ < SimConfig.MIN_HOVER_ALTITUDE) {
                    safeTargetZ = SimConfig.MIN_HOVER_ALTITUDE;
=======
            if (z <= MIN_HOVER_Z_SEA + 1.0) {
                if (safeTargetZ < MIN_HOVER_Z_SEA) {
                    safeTargetZ = MIN_HOVER_Z_SEA;
>>>>>>> 2e1c7d997378ffc2a62a0fdc8796641db0ce29fa
                }
                // If we are moving down, stop vertical velocity
                if (velocityZ < 0) {
                    velocityZ = 0;
                }
                // Float up if below
<<<<<<< HEAD
                if (z < SimConfig.MIN_HOVER_ALTITUDE) {
=======
                if (z < MIN_HOVER_Z_SEA) {
>>>>>>> 2e1c7d997378ffc2a62a0fdc8796641db0ce29fa
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
<<<<<<< HEAD
                logger.info("🚩 " + id + ": Waypoint contournement atteint. Cap sur final.");
=======
                System.out.println("🚩 " + id + ": Waypoint contournement atteint. Cap sur final.");
>>>>>>> 2e1c7d997378ffc2a62a0fdc8796641db0ce29fa

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

<<<<<<< HEAD
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
=======
            // Apply Weather Drag
            double effectiveSpeed = vitesseMax;
            effectiveSpeed *= weatherSpeedModifier; // Use cached modifier
>>>>>>> 2e1c7d997378ffc2a62a0fdc8796641db0ce29fa

            // Apply Weather Drag
            double effectiveSpeed = vitesseMax;
            effectiveSpeed *= weatherSpeedModifier; // Use cached modifier
            effectiveSpeed *= speedModifier; // Apply Speed Modifier

<<<<<<< HEAD
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
=======
            if (navigationMode == NavigationMode.AVOIDING) {
                effectiveSpeed *= 2.0; // Boost speed for avoidance
            }

            velocityX = dirX * effectiveSpeed;
            velocityY = dirY * effectiveSpeed;
            velocityZ = dirZ * effectiveSpeed;
>>>>>>> 2e1c7d997378ffc2a62a0fdc8796641db0ce29fa

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

    public void resetAvoidanceForce() {
        this.avoidForceX = 0;
        this.avoidForceY = 0;
        this.avoidForceZ = 0;
        // Auto-reset mode if no force, unless manually set by collision logic
        if (state != AssetState.STOPPED) {
            // Keep AVOIDING state if set by collision timer, but allow physics calculation
        }
    }

    public void addAvoidanceForce(double fx, double fy, double fz) {
        this.avoidForceX += fx;
        this.avoidForceY += fy;
        this.avoidForceZ += fz;
    }

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

    public void returnToBase() {
        setTarget(0, 0, 0); // Base at origin
        this.state = AssetState.RETURNING_TO_BASE;
    }

    public boolean hasReachedTarget() {
        double dx = targetX - x;
        double dy = targetY - y;
        return Math.sqrt(dx * dx + dy * dy) < 1.0;
    }

    public void assignMission(Mission mission) {
        if (this.currentMission == null || this.currentMission.isTerminated()) {
            // Immediate start
            this.currentMission = mission;
            this.targetX = mission.getTargetX();
            this.targetY = mission.getTargetY();
            this.targetZ = mission.getTargetZ();
            this.state = AssetState.EXECUTING_MISSION;
            this.etat = EtatOperationnel.EN_MISSION;
<<<<<<< HEAD
            logger.info("Actif " + id + ": Assigned immediate mission " + mission.getTitre());
        } else {
            // Queue it
            missionQueue.add(mission);
            logger.info("Actif " + id + ": Queued mission " + mission.getTitre() + " (Queue size: "
=======
            System.out.println("Actif " + id + ": Assigned immediate mission " + mission.getTitre());
        } else {
            // Queue it
            missionQueue.add(mission);
            System.out.println("Actif " + id + ": Queued mission " + mission.getTitre() + " (Queue size: "
>>>>>>> 2e1c7d997378ffc2a62a0fdc8796641db0ce29fa
                    + missionQueue.size() + ")");
        }
    }

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
<<<<<<< HEAD
            logger.info("Actif " + id + ": Promoted mission " + manualChoice.getTitre());
=======
            System.out.println("Actif " + id + ": Promoted mission " + manualChoice.getTitre());
>>>>>>> 2e1c7d997378ffc2a62a0fdc8796641db0ce29fa
        }
    }

    public Queue<Mission> getMissionQueue() {
        return missionQueue;
    }

    public void setTarget(double x, double y, double z) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
        this.state = AssetState.MOVING_TO_TARGET;
    }

    public double getBatteryPercent() {
        return autonomieActuelle / autonomieMax;
    }

    public abstract double getConsommation();

    /**
     * Calculates impact of weather on consumption.
     * 1.0 = No impact. >1.0 = Increased consumption.
     */
    /**
     * Calculates speed multiplier based on weather.
     * 1.0 = Max Speed. <1.0 = Reduced Speed.
     */
    protected double getSpeedMultiplier(com.spiga.environment.Weather w) {
        return 1.0; // Default
    }

    /**
     * Calculates battery consumption multiplier based on weather.
     * 1.0 = Normal drain. >1.0 = Increased drain.
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
    public double getVitesse() {
        return vitesseMax * speedModifier * weatherSpeedModifier; // Approximation of current speed capability
    }
}
