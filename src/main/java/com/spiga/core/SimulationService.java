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

/**
 * Moteur de Simulation (Le Cerveau)
 * ...
 */
public class SimulationService extends AnimationTimer {
    private static final double TARGET_FPS = 60.0;
    private static final double FRAME_TIME = 1.0 / TARGET_FPS;
    private static final double PUSH_FORCE = 2.0;

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
    private static final double THRESH_ALERT = 25.0; // Trigger Avoidance

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
        // REDUCED obstacle count and size for better visibility
        // Surface Obstacles (Islands) - Z=0
        obstacles.add(new Obstacle(300, 300, 0, 30));
        obstacles.add(new Obstacle(700, 600, 0, 40));

        // Underwater Obstacles (Reefs) - Z<0
        obstacles.add(new Obstacle(500, 400, -50, 20));
        obstacles.add(new Obstacle(800, 700, -100, 25));
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

        List<ActifMobile> fleet = gestionnaire.getFlotte();

        updateAllAssets(fleet, dt);
        checkCollisions(fleet);
        checkBoundaries(fleet);

        checkMissions(fleet);
        checkObstacles(fleet);
        checkRestrictedZones(fleet);
        checkTargetConflicts(fleet); // NEW: Swarm Deconfliction

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
        // Only auto-update if not in manual mode (simplified: if timer is positive)
        // For now, we will disable random weather changes to allow manual control as
        // requested.
        // Users can set weather via getters/setters on the Weather object exposed by
        // Service.
        /*
         * weatherTimer -= dt;
         * if (weatherTimer <= 0) {
         * double wind = random.nextDouble() * 50;
         * double rain = random.nextDouble() * 100;
         * weather = new Weather(wind, random.nextDouble() * 360, rain);
         * 
         * weatherDuration = 30 + random.nextDouble() * 30;
         * weatherTimer = weatherDuration;
         * }
         */
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
        double minSeparation = 50.0; // Distance to push away (each), total 100m
        double alertDist = THRESH_ALERT; // 25.0
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

                        a1.engageAvoidance(t1x, t1y, t1z, 5.0); // 5.0s duration for clear separation
                        a2.engageAvoidance(t2x, t2y, t2z, 5.0);

                        // Added visible UI alert
                        a1.setCollisionWarning("Trop Proche! (Avoidance Active)");
                        a2.setCollisionWarning("Trop Proche! (Avoidance Active)");

                        System.out.println("ALERT: Proximity " + a1.getId() + " <-> " + a2.getId());
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
            for (RestrictedZone zone : restrictedZones) {
                // 1. Vertical Check/Type Check
                // RECON DRONES: Authorized to enter (Can pass freely)
                if (asset instanceof DroneReconnaissance)
                    continue;

                // OTHERS (Logistics, etc): Must be ABOVE the zone to pass
                if (asset.getZ() > zone.getMaxZ()) {
                    // But wait, Logistics are NOT allowed even high?
                    // User said "drone logistics try to enter -> block"
                    // If we strictly block Logistics everywhere in this cylinder:
                    if (asset instanceof DroneLogistique) {
                        // Logistics blocked REGARDLESS of height (Unauthorized Airspace)
                        // Fall through to distance check
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

                    // --- HARD WALL PHYSICS FOR LOGISTICS ---
                    if (asset instanceof DroneLogistique) {
                        // "La zone est un obstacle" -> SOLID WALL.
                        // Force position to be EXACTLY on the edge (plus margin).
                        if (dist < 0.1) {
                            dx = 1;
                            dy = 0;
                            dist = 1;
                        } // Prevent 0 div

                        double wallRadius = zone.getRadius() + 2.0; // 2m Margin

                        asset.setX(zone.getX() + (dx / dist) * wallRadius);
                        asset.setY(zone.getY() + (dy / dist) * wallRadius);

                        // Stop momentum?
                        // asset.setTarget(asset.getX(), asset.getY(), asset.getZ()); // Reset target??
                        // No, pathfinding handles that.

                        asset.setCollisionWarning("MUR ZONE (BLOQUÉ)");

                    } else {
                        // Standard Push (Soft Wall)
                        double push = PUSH_FORCE * 5.0; // Increased from 3.0
                        if (dist < 0.1)
                            dist = 0.1;

                        asset.setX(asset.getX() + (dx / dist) * push);
                        asset.setY(asset.getY() + (dy / dist) * push);

                        asset.setCollisionWarning("VIOLATION ZONE (MISSION ÉCHOUÉE)");
                        asset.setSpeedModifier(0.0);
                        asset.setState(ActifMobile.AssetState.STOPPED);

                        // Fail Mission
                        if (asset.getCurrentMission() != null) {
                            asset.getCurrentMission().fail("Violation Zone Interdite");
                        }
                    }
                }
                // 4. Warning (Approaching) - < 40m from edge
                else if (proximity < 40.0) {
                    asset.setCollisionWarning("Zone Interdite Proche (<" + (int) proximity + "m)");
                    // Gentle push back
                    double push = PUSH_FORCE * 0.5;
                    asset.setX(asset.getX() + (dx / dist) * push);
                    asset.setY(asset.getY() + (dy / dist) * push);
                }
            }
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
        for (ActifMobile asset : fleet) {
            for (Obstacle obs : obstacles) {

                if (obs.isCollision(asset.getX(), asset.getY(), asset.getZ())) {
                    double dx = asset.getX() - obs.getX();
                    double dy = asset.getY() - obs.getY();
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist < 0.1)
                        dist = 0.1;

                    double pushX = (dx / dist) * PUSH_FORCE * 2;
                    double pushY = (dy / dist) * PUSH_FORCE * 2;

                    // SMART AVOIDANCE LOGIC - IMPROVED
                    // 1. Slow Down
                    asset.setSpeedModifier(0.3); // Reduce speed to 30% for caution

                    if (asset instanceof ActifAerien) {
                        // Drones: Fly OVER (Strong Increase in Altitude)
                        // Push Z hard to clear obstacle
                        asset.setZ(asset.getZ() + PUSH_FORCE * 5);

                        // Mild sideways push to ensure we don't get stuck perfectly center
                        asset.setX(asset.getX() + pushX * 0.2);
                        asset.setY(asset.getY() + pushY * 0.2);

                    } else if (asset instanceof VehiculeSousMarin) {
                        // Subs: Dive UNDER (Decrease Depth) if possible
                        // Limit max depth to -200 for safety
                        if (asset.getZ() > -200) {
                            asset.setZ(asset.getZ() - PUSH_FORCE * 5);
                        }
                        asset.setX(asset.getX() + pushX * 0.5);
                        asset.setY(asset.getY() + pushY * 0.5);
                    } else {
                        // Surface Vessels: Go AROUND
                        asset.setX(asset.getX() + pushX);
                        asset.setY(asset.getY() + pushY);
                    }
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
