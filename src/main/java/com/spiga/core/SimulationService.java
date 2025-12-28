package com.spiga.core;

import com.spiga.environment.Obstacle;
import com.spiga.environment.RestrictedZone;
import com.spiga.environment.Weather;
import com.spiga.management.Communication;
import com.spiga.management.GestionnaireEssaim;
import com.spiga.management.Mission;
import javafx.animation.AnimationTimer;

import java.util.ArrayList;
import java.util.List;

/**
 * Moteur de Simulation (Le Cerveau)
 * 
 * CONCEPTS CLES :
 * 
 * 1. Heritage (AnimationTimer) :
 * - Pourquoi ? JavaFX fournit cette classe pour creer une "boucle de jeu" (Game
 * Loop).
 * En etendant AnimationTimer, on peut redefinir la methode handle() qui sera
 * appelee 60 fois par seconde.
 * 
 * 2. Composition (Has-A relation) :
 * - C'est quoi ? Une classe composee d'autres objets.
 * - Ici : SimulationService POSEDE (has-a) un GestionnaireEssaim, une Weather,
 * des Obstacles.
 * Il orchestre leurs interactions.
 * 
 * 3. Boucle de Simulation (Game Loop) :
 * - Ou ? Methode handle() -> updateSimulation().
 * - Principe : A chaque "frame", on met a jour la position de tous les objets,
 * on verifie les collisions, etc.
 */
public class SimulationService extends AnimationTimer {
    private static final double TARGET_FPS = 60.0;
    private static final double FRAME_TIME = 1.0 / TARGET_FPS;
    private static final double COLLISION_THRESHOLD = SimConfig.SAFETY_RADIUS * 2.0;
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

        // Handle Mission Dispatching
        communication.handleMissions();
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
        }
    }

    private void checkCollisions(List<ActifMobile> fleet) {
        for (int i = 0; i < fleet.size(); i++) {
            for (int j = i + 1; j < fleet.size(); j++) {
                ActifMobile a1 = fleet.get(i);
                ActifMobile a2 = fleet.get(j);

                double dx = a1.getX() - a2.getX();
                double dy = a1.getY() - a2.getY();
                double dz = a1.getZ() - a2.getZ();
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

                // Early Warning Zone: 60m (1.5x Collision Threshold)
                // Warn BEFORE action
                if (dist < 60.0 && dist >= COLLISION_THRESHOLD) {
                    if (a1 instanceof ActifAerien && a2 instanceof ActifAerien) {
                        // Only warn if they are converging? For simplicity, prox warning
                        a1.setCollisionWarning("Proximité... (Préparation)");
                        a2.setCollisionWarning("Proximité... (Préparation)");
                    }
                }

                if (dist < COLLISION_THRESHOLD) {
                    handleCollision(a1, a2, dx, dy, dz, dist);
                }
            }
        }
    }

    private void handleCollision(ActifMobile a1, ActifMobile a2, double dx, double dy, double dz, double dist) {
        if (dist < 0.1)
            dist = 0.1;

        // Basic Separation Force
        double pushX = (dx / dist) * PUSH_FORCE;
        double pushY = (dy / dist) * PUSH_FORCE;
        double pushZ = (dz / dist) * PUSH_FORCE;

        boolean a1Moving = isMoving(a1);
        boolean a2Moving = isMoving(a2);

        // --- 1. AVOIDANCE PHYSICS (Horizontal & Vertical) ---
        if (a1Moving && a2Moving) {
            // Both active: Mutual avoidance
            a1.setX(a1.getX() + pushX);
            a1.setY(a1.getY() + pushY);
            a1.setZ(a1.getZ() + pushZ);
            a2.setX(a2.getX() - pushX);
            a2.setY(a2.getY() - pushY);
            a2.setZ(a2.getZ() - pushZ);

            if (a1 instanceof ActifAerien && a2 instanceof ActifAerien) {
                ActifMobile higher = (a1.getZ() >= a2.getZ()) ? a1 : a2;
                ActifMobile lower = (a1 == higher) ? a2 : a1;
                // Active vs Active: Both adjust slighly
                higher.setZ(higher.getZ() + 2.0); // Gentle +2m
                higher.setCollisionWarning("Evitement (MONTER)");
                lower.setCollisionWarning("Evitement (DESCENDRE)");
            }
        } else if (a1Moving && !a2Moving) {
            // Only A1 moves (A2 is static target/obstacle)
            a1.setX(a1.getX() + pushX * 2.0); // Double force to clear gap alone
            a1.setY(a1.getY() + pushY * 2.0);
            a1.setZ(a1.getZ() + pushZ * 2.0);

            if (a1 instanceof ActifAerien && a2 instanceof ActifAerien) {
                a1.setZ(a1.getZ() + 2.0); // Fly Over +2m
                a1.setCollisionWarning("Evitement (SURVOL)");
            }
        } else if (!a1Moving && a2Moving) {
            // Only A2 moves
            a2.setX(a2.getX() - pushX * 2.0);
            a2.setY(a2.getY() - pushY * 2.0);
            a2.setZ(a2.getZ() - pushZ * 2.0);

            if (a1 instanceof ActifAerien && a2 instanceof ActifAerien) {
                a2.setZ(a2.getZ() + 2.0); // Fly Over +2m
                a2.setCollisionWarning("Evitement (SURVOL)");
            }
        } else {
            // Both Static: Slight nudging to prevent perfect overlap issues
            a1.setX(a1.getX() + pushX * 0.1);
            a2.setX(a2.getX() - pushX * 0.1);
        }

        // Slow down active participants
        if (a1Moving)
            a1.setSpeedModifier(0.5);
        if (a2Moving)
            a2.setSpeedModifier(0.5);
    }

    private boolean isMoving(ActifMobile a) {
        return a.getState() == ActifMobile.AssetState.MOVING_TO_TARGET ||
                a.getState() == ActifMobile.AssetState.EXECUTING_MISSION ||
                a.getState() == ActifMobile.AssetState.RETURNING_TO_BASE;
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
                    // Inside! Push OUT Hard
                    double push = PUSH_FORCE * 3.0;
                    if (dist < 0.1)
                        dist = 0.1;

                    asset.setX(asset.getX() + (dx / dist) * push);
                    asset.setY(asset.getY() + (dy / dist) * push);

                    asset.setCollisionWarning("VIOLATION ZONE (MISSION ÉCHOUÉE)");
                    asset.setSpeedModifier(0.0); // Stop?
                    asset.setState(ActifMobile.AssetState.STOPPED);

                    // Fail Mission
                    if (asset.getCurrentMission() != null) {
                        asset.getCurrentMission().fail("Violation Zone Interdite");
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
        for (ActifMobile asset : fleet) {
            if (asset.getCurrentMission() != null) {
                var mission = asset.getCurrentMission();
                if (asset.hasReachedTarget() &&
                        mission.getStatut() == com.spiga.management.Mission.StatutMission.EN_COURS) {
                    mission.complete();
                }
                if (asset.getState() == ActifMobile.AssetState.STOPPED &&
                        mission.getStatut() == com.spiga.management.Mission.StatutMission.EN_COURS) {
                    mission.fail("Batterie épuisée");
                }
            }
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
