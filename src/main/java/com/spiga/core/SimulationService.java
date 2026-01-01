package com.spiga.core;

import com.spiga.environment.Obstacle;
import com.spiga.environment.RestrictedZone;
import com.spiga.environment.Weather;
import com.spiga.management.Communication;
import com.spiga.management.GestionnaireEssaim;
// import com.spiga.management.Mission; // Keep if needed, or remove if truly unused
import javafx.animation.AnimationTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Moteur de Simulation (Le Cerveau)
 * ...
 */
public class SimulationService extends AnimationTimer {
    private static final double TARGET_FPS = 60.0;
    private static final double FRAME_TIME = 1.0 / TARGET_FPS;
    private static final Logger logger = Logger.getLogger(SimulationService.class.getName());

    // --- COMPOSITION : Mes composants ---
    private GestionnaireEssaim gestionnaire;
    private Communication communication;

    // Utilisation de Collections (List) pour gérer dynamiquement des groupes
    // d'objets
    private List<Obstacle> obstacles;
    private List<RestrictedZone> restrictedZones;
    private Weather weather;
    private double timeScale = 1.0;

    private long lastTime = 0;
    private double accumulator = 0;

    // SWARM AVOIDANCE STATE
    private Map<String, Long> lastAlertTime = new HashMap<>(); // Key: "ID1-ID2", Value: TimeMs

    /**
     * Constructeur : Initialisation du service.
     * Instanciation des listes et des objets dépendants avec `new`.
     */
    public SimulationService(GestionnaireEssaim gestionnaire) {
        this.gestionnaire = gestionnaire;
        this.communication = new Communication(gestionnaire);

        // Allocation mémoire (Heap) pour les listes
        this.obstacles = new ArrayList<>();
        this.restrictedZones = new ArrayList<>();

        // Création de l'objet Météo initial
        this.weather = new Weather(10, 0, 0);

        initializeObstacles();
        initializeRestrictedZones();

        // Share Zones with Static Assets
        ActifMobile.KNOWN_ZONES.clear();
        ActifMobile.KNOWN_ZONES.addAll(restrictedZones);
    }

    public void startSimulation() {
        lastTime = 0; // Reset time to avoid jump
        super.start();
    }

    public void stopSimulation() {
        super.stop();
    }

    private void initializeObstacles() {
        // --- 6 WELL-SEPARATED OBSTACLES ---
        // World is 2000x2000, obstacles spread across corners and center

        // 1. SURFACE OBSTACLES (Z=0) - Islands
        obstacles.add(new Obstacle(300, 300, 0, 50)); // Island NW corner
        obstacles.add(new Obstacle(1700, 1600, 0, 40)); // Island SE corner

        // 2. UNDERWATER OBSTACLES (Z<0) - Reefs
        obstacles.add(new Obstacle(1500, 400, -60, 35)); // Reef NE area
        obstacles.add(new Obstacle(400, 1500, -40, 30)); // Reef SW area

        // 3. AERIAL OBSTACLES (Z>0) - Mountains/Hazards
        obstacles.add(new Obstacle(1000, 1000, 80, 70)); // Central Mountain
        obstacles.add(new Obstacle(1600, 300, 50, 30)); // Floating Hazard NE
    }

    private void initializeRestrictedZones() {
        // "Black Zone" - Military Base or similar
        // MOVED to Border (1800, 500)
        // Radius: 150m
        // Height: 0 to 120m
        restrictedZones.add(new RestrictedZone("Zone Interdite 01", 1800, 500, 150, 0, 120));
    }

    public List<RestrictedZone> getRestrictedZones() {
        return restrictedZones;
    }

    @Override
    public void handle(long now) {
        if (lastTime == 0) {
            lastTime = now;
            return;
        }

        double dt = (now - lastTime) / 1e9;
        lastTime = now;

        dt *= timeScale;
        accumulator += dt;

        while (accumulator >= FRAME_TIME) {
            updateSimulation(FRAME_TIME);
            accumulator -= FRAME_TIME;
        }
    }

    private void updateSimulation(double dt) {
        updateWeather(dt);

        // Create a thread-safe copy of the fleet to avoid
        // ConcurrentModificationException
        // if the UI adds/removes drones while we iterate.
        List<ActifMobile> fleet = new ArrayList<>(gestionnaire.getFlotte());

        // 1. RESET PHASE (Prepare for new frame)
        for (ActifMobile asset : fleet) {
            asset.setSpeedModifier(1.0);
        }

        // 2. ENVIRONMENT & CONSTRAINTS CHECK (Before Movement)
        // Check obstacles FIRST so they can reduce speed BEFORE update() moves the
        // asset.
        checkObstacles(fleet);
        checkRestrictedZones(fleet);
        checkTargetConflicts(fleet); // Swarm Deconfliction

        // 3. MOVEMENT & LOGIC UPDATE
        updateAllAssets(fleet, dt);

        // 4. REACTIVE CHECKS (After Movement)
        // Collisions must be checked after move to see if we hit something despite
        // precautions
        checkCollisions(fleet);
        checkBoundaries(fleet);

        // 5. MISSION LOGIC
        checkMissions(fleet);

        // Handle Mission Dispatching
        communication.handleMissions();
    }

    private void checkTargetConflicts(List<ActifMobile> fleet) {
        long now = System.currentTimeMillis();
        double collisionDist = 2.0; // Targets considered "same" if within 2m
        // double influenceDist = 300.0; // Unused
        double separationDist = 100.0; // Total separation requested
        double offset = separationDist / 2.0; // +/- 50m
        long cooldown = 10000; // 10s cooldown per pair

        for (int i = 0; i < fleet.size(); i++) {
            ActifMobile a1 = fleet.get(i);
            if (a1.getEtat() == ActifMobile.EtatOperationnel.AU_SOL
                    || a1.getEtat() == ActifMobile.EtatOperationnel.EN_PANNE)
                continue;

            for (int j = i + 1; j < fleet.size(); j++) {
                ActifMobile a2 = fleet.get(j);
                if (a2.getEtat() == ActifMobile.EtatOperationnel.AU_SOL
                        || a2.getEtat() == ActifMobile.EtatOperationnel.EN_PANNE)
                    continue;

                // 1. Check if they have the SAME TARGET (approx)
                double dTx = a1.getTargetX() - a2.getTargetX();
                double dTy = a1.getTargetY() - a2.getTargetY();
                double targetDist = Math.sqrt(dTx * dTx + dTy * dTy);

                if (targetDist < collisionDist) {
                    // 2. Check Relevance (are they active/moving?)
                    // Simplified: if they are both moving to target
                    // String pairId = "TARGET-" + (a1.getId().compareTo(a2.getId()) < 0 ?
                    // a1.getId() + "-" + a2.getId() : a2.getId() + "-" + a1.getId());
                    // Use simple ID sum for key to avoid complex string ops in loop if possible,
                    // but string is safer
                    String pairId = "TGT-" + (a1.getId().compareTo(a2.getId()) < 0 ? a1.getId() + "-" + a2.getId()
                            : a2.getId() + "-" + a1.getId());

                    if (!lastAlertTime.containsKey(pairId) || (now - lastAlertTime.get(pairId) > cooldown)) {
                        lastAlertTime.put(pairId, now);

                        // 3. APPLY SEPARATION
                        // Calculate separation vector based on current positions
                        double dx = a1.getX() - a2.getX();
                        double dy = a1.getY() - a2.getY();
                        double distCurrent = Math.sqrt(dx * dx + dy * dy);

                        double nx, ny;
                        if (distCurrent < 0.1) {
                            nx = 1.0;
                            ny = 0.0; // Arbitrary X split if stacked
                        } else {
                            nx = dx / distCurrent;
                            ny = dy / distCurrent;
                        }

                        // A. Trigger Visual/Log Alert
                        a1.setCollisionWarning("MÊME CIBLE! SÉPARATION (+50m)");
                        a2.setCollisionWarning("MÊME CIBLE! SÉPARATION (-50m)");

                        // C. OFFSET FINAL TARGETS (Permanent) - THIS IS THE KEY FIX
                        // We push the targets apart along the same vector
                        // 50m separation each = 100m total
                        a1.setTarget(a1.getTargetX() + nx * offset, a1.getTargetY() + ny * offset, a1.getTargetZ());
                        a2.setTarget(a2.getTargetX() - nx * offset, a2.getTargetY() - ny * offset, a2.getTargetZ());
                    }
                }
            }
        }
    }

    private void updateWeather(double dt) {
        // Weather is now controlled manually via UI sliders
        // No automatic random weather changes
    }

    private void updateAllAssets(List<ActifMobile> fleet, double dt) {
        if (weather.getWindSpeed() > 40) {
            // weatherFactor *= 0.9; // Logic moved to ActifMobile.update
        }

        for (ActifMobile asset : fleet) {
            asset.update(dt, weather);
            asset.checkMissionQueue(); // Check for next mission
        }
    }

    private void checkCollisions(List<ActifMobile> fleet) {
        long now = System.currentTimeMillis();
        double minSeparation = SimConfig.SEPARATION_DISTANCE; // Distance to push away
        double alertDist = SimConfig.COLLISION_THRESHOLD;
        long cooldown = 2000; // 2s

        for (int i = 0; i < fleet.size(); i++) {
            ActifMobile a1 = fleet.get(i);
            if (a1.getEtat() == ActifMobile.EtatOperationnel.AU_SOL ||
                    a1.getEtat() == ActifMobile.EtatOperationnel.EN_PANNE)
                continue;

            for (int j = i + 1; j < fleet.size(); j++) {
                ActifMobile a2 = fleet.get(j);
                if (a2.getEtat() == ActifMobile.EtatOperationnel.AU_SOL ||
                        a2.getEtat() == ActifMobile.EtatOperationnel.EN_PANNE)
                    continue;

                double dx = a1.getX() - a2.getX();
                double dy = a1.getY() - a2.getY();
                double dz = a1.getZ() - a2.getZ();
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz); // 3D distance

                String pairId = a1.getId().compareTo(a2.getId()) < 0 ? a1.getId() + "-" + a2.getId()
                        : a2.getId() + "-" + a1.getId();

                // 1. TRIGGER AVOIDANCE (Entry Threshold)
                if (dist < alertDist) {
                    // Check Cooldown
                    if (!lastAlertTime.containsKey(pairId) || (now - lastAlertTime.get(pairId) > cooldown)) {

                        // Trigger Avoidance !
                        lastAlertTime.put(pairId, now);

                        // Calculate Avoidance Vector (XY Plane mostly, unless stacked)
                        // Normalize vector A -> B
                        double nx = dx / dist;
                        double ny = dy / dist;

                        // If stacked perfectly, pick random direction
                        if (dist < 0.1) {
                            nx = 1.0;
                            ny = 0.0;
                        }

                        // Avoidance Distance (e.g., 40m away from CURRENT position)
                        // Target A = Pos A + (Vector away from B) * minSeparation

                        double t1x = a1.getX() + nx * minSeparation;
                        double t1y = a1.getY() + ny * minSeparation;
                        double t1z = a1.getZ(); // Keep Z as requested

                        double t2x = a2.getX() - nx * minSeparation;
                        double t2y = a2.getY() - ny * minSeparation;
                        double t2z = a2.getZ();

                        a1.engageAvoidance(t1x, t1y, t1z, SimConfig.AVOIDANCE_DURATION);
                        a2.engageAvoidance(t2x, t2y, t2z, SimConfig.AVOIDANCE_DURATION);

                        // Added visible UI alert
                        a1.setCollisionWarning("Trop Proche! (Avoidance Active)");
                        a2.setCollisionWarning("Trop Proche! (Avoidance Active)");

                        logger.warning("ALERT: Proximity " + a1.getId() + " <-> " + a2.getId());
                    }
                }
            }
        }
    }

    private void checkBoundaries(List<ActifMobile> fleet) {
        double maxX = SimConfig.WORLD_WIDTH;
        double maxY = SimConfig.WORLD_HEIGHT;

        for (ActifMobile asset : fleet) {
            if (asset.getX() < 0)
                asset.setX(0);
            if (asset.getX() > maxX)
                asset.setX(maxX);
            if (asset.getY() < 0)
                asset.setY(0);
            if (asset.getY() > maxY)
                asset.setY(maxY);
        }
    }

    private void checkRestrictedZones(List<ActifMobile> fleet) {
        if (restrictedZones == null)
            return;

        for (ActifMobile asset : fleet) {
            boolean isInfluenced = false;

            for (RestrictedZone zone : restrictedZones) {
                // 1. Vertical Check/Type Check
                // RECON DRONES: Authorized to enter (Can pass freely)
                if (asset instanceof DroneReconnaissance)
                    continue;

                // OTHERS (Logistics, etc): Must be ABOVE the zone to pass
                if (asset.getZ() > zone.getMaxZ()) {
                    if (asset instanceof DroneLogistique) {
                        // Logistics blocked REGARDLESS of height
                    } else {
                        // Other assets (standard) can fly over
                        continue;
                    }
                }
                if (asset.getZ() < zone.getMinZ())
                    continue; // Below zone

                // 2. Horizontal Distance Check
                double dx = asset.getX() - zone.getX();
                double dy = asset.getY() - zone.getY();
                double dist = Math.sqrt(dx * dx + dy * dy);
                double proximity = dist - zone.getRadius();

                // 3. Violation (Inside)
                if (proximity <= 0) {
                    isInfluenced = true;
                    // --- HARD WALL PHYSICS FOR LOGISTICS ---
                    if (asset instanceof DroneLogistique) {
                        if (dist < 0.1) {
                            dx = 1;
                            dy = 0;
                            dist = 1;
                        }
                        double wallRadius = zone.getRadius() + 2.0;
                        asset.setX(zone.getX() + (dx / dist) * wallRadius);
                        asset.setY(zone.getY() + (dy / dist) * wallRadius);
                        asset.setCollisionWarning("MUR ZONE (BLOQUÉ)");
                    } else {
                        // Standard Push (Soft Wall)
                        double push = SimConfig.PUSH_FORCE * 5.0;
                        if (dist < 0.1)
                            dist = 0.1;

                        asset.setX(asset.getX() + (dx / dist) * push);
                        asset.setY(asset.getY() + (dy / dist) * push);
                        asset.setCollisionWarning("VIOLATION ZONE (MISSION ÉCHOUÉE)");
                        asset.setSpeedModifier(0.0);
                        asset.setState(ActifMobile.AssetState.STOPPED);
                        if (asset.getCurrentMission() != null) {
                            asset.getCurrentMission().fail("Violation Zone Interdite");
                        }
                    }
                }
                // 4. Warning (Approaching) - < 50m from edge
                else if (proximity < 50.0) {
                    isInfluenced = true;
                    asset.setCollisionWarning("Zone Interdite Proche (<" + (int) proximity + "m)");

                    // Smooth Physics Push (Avoidance Force)
                    double forceMag = (50.0 - proximity) * 2.0;
                    if (forceMag > 10.0)
                        forceMag = 10.0;

                    double nx = dx / dist;
                    double ny = dy / dist;
                    double tx = -ny;
                    double ty = nx;

                    // Steering (Tangential)
                    double steerDir = asset.getSteeringBias();
                    if (Math.abs(steerDir) < 0.1) {
                        steerDir = 1.0;
                        double vx = asset.getVelocityX();
                        double vy = asset.getVelocityY();
                        if (Math.abs(vx) > 0.1 || Math.abs(vy) > 0.1) {
                            double cp = dx * vy - dy * vx;
                            if (cp > 0)
                                steerDir = 1.0;
                            else
                                steerDir = -1.0;
                        }
                        asset.setSteeringBias(steerDir);
                    }

                    double steeringMag = forceMag * 0.5;

                    double fx = nx * forceMag + tx * steerDir * steeringMag;
                    double fy = ny * forceMag + ty * steerDir * steeringMag;

                    asset.addAvoidanceForce(fx, fy, 0);

                    // Force AVOIDING mode
                    asset.setNavigationMode(ActifMobile.NavigationMode.AVOIDING);
                    asset.setAvoidanceEndTime(System.currentTimeMillis() + 500);
                }
            } // End Zone Loop

            // Check if we need to reset bias (if not influenced by ANY zone)
            // Note: checkObstacles also manages this. If checkObstacles reset it, and we
            // don't set it...
            // If we are influenced here, we set it.
            // If we are NOT influenced here, but we WERE influenced by Obstacles?
            // Obstacles logic runs first. It sets bias if influenced.
            // If Obstacles didn't influence (reset to 0), and Zone influences (sets to 1),
            // good.
            // If Obstacles influenced (set to 1), and Zone DOES NOT influence?
            // We should NOT reset it here blindly.
            // Only reset if we were supposedly avoiding a Zone?
            // Actually, `steeringBias` is shared.
            // If avoiding a Rock, we use bias.
            // If we fly near a Zone, do we fight?
            // If Zone sets bias, it overwrites Rock bias?
            // Since we check `if (Math.abs(steerDir) < 0.1)`, we RESPECT existing bias!
            // So if Rock set it, Zone uses it. Seamless transition!
            // But what about resetting?
            // If Rock is far, Obstacle logic resets it.
            // Zone sees 0. Zone sets it.
            // If Zone is far, Zone logic resets it?
            // If I add `if (!isInfluenced && !isObstacleInfluenced) reset`.
            // But I don't know obstacle state here easily.
            // However, `avoidanceEndTime` handles the mode switch.
            // The bias reset logic in `checkObstacles` relies on `avoidanceEndTime`.
            // `checkObstacles` runs FIRST.
            // If `avoidanceEndTime` is active (from Zone), `checkObstacles` (else block)
            // will NOT reset it.
            // (Because `now < endTime`).
            // So `checkObstacles` respects Zone's timer.
            // So I don't need to duplicate the reset logic here?
            // Wait. `checkObstacles` has the reset in `else`.
            // `else` means "Not Influenced by Obstacles".
            // It checks `if (endTime < now)`.
            // If Zone set endTime to future, `checkObstacles` sees active timer and DOES
            // NOT reset.
            // PERFECT! The logic in `checkObstacles` handles the reset for BOTH!

            // So I just need to SET it here.
            // I do NOT need the reset block here.
        }
    }

    private void checkMissions(List<ActifMobile> fleet) {
        long currentSimTime = System.currentTimeMillis() / 1000; // Simple wall-clock time for now

        // We need a unique set of active missions to avoid double-ticking
        // Since missions are attached to assets, iterate assets and collect unique
        // missions
        java.util.Set<com.spiga.management.Mission> activeMissions = new java.util.HashSet<>();

        for (ActifMobile asset : fleet) {
            if (asset.getCurrentMission() != null &&
                    asset.getCurrentMission().getStatut() == com.spiga.management.Mission.StatutMission.EN_COURS) {
                activeMissions.add(asset.getCurrentMission());
            }
        }

        for (com.spiga.management.Mission m : activeMissions) {
            m.tick(currentSimTime);
        }
    }

    private void checkObstacles(List<ActifMobile> fleet) {
        // Constants for Potential Field - use SimConfig
        double DETECTION_RADIUS = SimConfig.OBSTACLE_DETECTION_RADIUS;
        double FORCE_FACTOR = SimConfig.OBSTACLE_FORCE_FACTOR;

        for (ActifMobile asset : fleet) {

            // 0. Reset Forces
            asset.resetAvoidanceForce();

            boolean isInfluenced = false;

            for (Obstacle obs : obstacles) {
                // Calculate Vector Asset -> Obstacle
                double dx = asset.getX() - obs.getX();
                double dy = asset.getY() - obs.getY();
                double distSq = dx * dx + dy * dy;
                double dist = Math.sqrt(distSq);

                // Reduce distance by radius (distance to surface)
                double distToSurface = dist - obs.getRadius();
                if (distToSurface < 0.1)
                    distToSurface = 0.1; // Clamp

                // 1. Check if within influence range
                if (distToSurface < DETECTION_RADIUS) {
                    isInfluenced = true;

                    // 2. Physical Push if actual collision (Hard Wall)
                    if (obs.isCollision(asset.getX(), asset.getY(), asset.getZ())) {
                        // Emergency Shove
                        double push = 5.0; // Hard push
                        asset.setX(asset.getX() + (dx / dist) * push);
                        asset.setY(asset.getY() + (dy / dist) * push);
                        asset.setCollisionWarning("COLLISION OBSTACLE!");
                    }

                    // 3. Calculate Repulsive Force (Inverse Square Law)
                    // F = k * (1/d^2) * Direction(Away)
                    double forceMagnitude = FORCE_FACTOR / (distToSurface * distToSurface);

                    // Cap force to prevent teleportation (use SimConfig)
                    if (forceMagnitude > SimConfig.AVOIDANCE_FORCE_CAP)
                        forceMagnitude = SimConfig.AVOIDANCE_FORCE_CAP;

                    // Normal Direction (Directly AWAY from obstacle)
                    double nx = dx / dist;
                    double ny = dy / dist;

                    // 4. Tangential Force (Steering) - "Slide Around"
                    // Tangent vector (-ny, nx) is perpendicular to normal
                    double tx = -ny;
                    double ty = nx;

                    // Decide steering direction based on Target + Hysteresis
                    // Cross Product of (Asset->Obstacle) and (Asset->Target)
                    double steerDir = asset.getSteeringBias();

                    if (Math.abs(steerDir) < 0.1) {
                        // No bias yet, calculate initial direction
                        steerDir = 1.0;
                        double vx = asset.getVelocityX();
                        double vy = asset.getVelocityY();
                        if (Math.abs(vx) > 0.1 || Math.abs(vy) > 0.1) {
                            double cp = dx * vy - dy * vx;
                            if (cp > 0)
                                steerDir = 1.0; // Target/Path is left
                            else
                                steerDir = -1.0; // Target/Path is right
                        }
                        asset.setSteeringBias(steerDir); // Commit to this direction
                    }

                    // Tangential component (20-40% of repulsive force)
                    double steeringMagnitude = forceMagnitude * 0.5;

                    // Combined Force: Repulsion + Steering
                    double fx = nx * forceMagnitude + tx * steerDir * steeringMagnitude;
                    double fy = ny * forceMagnitude + ty * steerDir * steeringMagnitude;

                    asset.addAvoidanceForce(fx, fy, 0);
                }
            }

            if (isInfluenced) {
                asset.setNavigationMode(ActifMobile.NavigationMode.AVOIDING);
                // Hysteresis: Stay in avoidance mode for at least 500ms after last influence
                // to prevent rapid toggling at the boundary.
                asset.setAvoidanceEndTime(System.currentTimeMillis() + 500);
            } else {
                // Only reset to normal if not in temporary diversion mode (timer based)
                if (asset.getAvoidanceEndTime() < System.currentTimeMillis()) {
                    asset.setNavigationMode(ActifMobile.NavigationMode.NORMAL);
                    asset.setSteeringBias(0.0); // Reset bias so we can turn the other way next time
                }
            }
        }
    }

    public void setTimeScale(double scale) {
        this.timeScale = scale;
    }

    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    public Weather getWeather() {
        return weather;
    }

    public Communication getCommunication() {
        return communication;
    }

    public void reset() {
        lastTime = 0;
        accumulator = 0;
    }
}
