package com.spiga.core;

import com.spiga.management.GestionnaireEssaim;
import com.spiga.environment.Obstacle;
import com.spiga.environment.Weather;
import com.spiga.environment.ZoneOperation;
import javafx.animation.AnimationTimer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * SimulationService - PRODUCTION READY
 * Handles 60 FPS loop, physics, weather cycles, and obstacle avoidance.
 */
public class SimulationService extends AnimationTimer {
    private static final double TARGET_FPS = 60.0;
    private static final double FRAME_TIME = 1.0 / TARGET_FPS;
    private static final double COLLISION_THRESHOLD = 15.0;
    private static final double PUSH_FORCE = 2.0;

    private GestionnaireEssaim gestionnaire;
    private List<Obstacle> obstacles;
    private Weather weather;
    private ZoneOperation zone;
    private double timeScale = 1.0;

    private long lastTime = 0;
    private double accumulator = 0;

    // Weather Cycle
    private double weatherTimer = 30.0;
    private double weatherDuration = 30.0;
    private Random random = new Random();

    public SimulationService(GestionnaireEssaim gestionnaire, ZoneOperation zone) {
        this.gestionnaire = gestionnaire;
        this.zone = zone;
        this.obstacles = new ArrayList<>();
        this.weather = new Weather(10, 0, 0);

        initializeObstacles();
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
    }

    private void updateWeather(double dt) {
        weatherTimer -= dt;
        if (weatherTimer <= 0) {
            double wind = random.nextDouble() * 50;
            double rain = random.nextDouble() * 100;
            weather = new Weather(wind, random.nextDouble() * 360, rain);

            weatherDuration = 30 + random.nextDouble() * 30;
            weatherTimer = weatherDuration;
        }
    }

    private void updateAllAssets(List<ActifMobile> fleet, double dt) {
        double weatherFactor = 1.0;
        if (weather.getRainIntensity() > 50) {
            weatherFactor = 0.8;
        }
        if (weather.getWindSpeed() > 40) {
            weatherFactor *= 0.9;
        }

        for (ActifMobile asset : fleet) {
            asset.update(dt * weatherFactor);
        }
    }

    private void checkCollisions(List<ActifMobile> fleet) {
        for (int i = 0; i < fleet.size(); i++) {
            for (int j = i + 1; j < fleet.size(); j++) {
                ActifMobile a1 = fleet.get(i);
                ActifMobile a2 = fleet.get(j);

                if (Math.abs(a1.getZ() - a2.getZ()) > 50)
                    continue;

                double dx = a1.getX() - a2.getX();
                double dy = a1.getY() - a2.getY();
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < COLLISION_THRESHOLD) {
                    handleCollision(a1, a2, dx, dy, dist);
                }
            }
        }
    }

    private void handleCollision(ActifMobile a1, ActifMobile a2, double dx, double dy, double dist) {
        if (dist < 0.1)
            dist = 0.1;
        double pushX = (dx / dist) * PUSH_FORCE;
        double pushY = (dy / dist) * PUSH_FORCE;

        a1.setX(a1.getX() + pushX);
        a1.setY(a1.getY() + pushY);
        a2.setX(a2.getX() - pushX);
        a2.setY(a2.getY() - pushY);
    }

    private void checkBoundaries(List<ActifMobile> fleet) {
        double maxX = (zone != null) ? zone.getMaxX() : 1000;
        double maxY = (zone != null) ? zone.getMaxY() : 1000;

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
                // Check if vertically aligned (within 50m)
                if (Math.abs(asset.getZ() - obs.getZ()) > 50)
                    continue;

                if (obs.isCollision(asset.getX(), asset.getY(), asset.getZ())) {
                    double dx = asset.getX() - obs.getX();
                    double dy = asset.getY() - obs.getY();
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist < 0.1)
                        dist = 0.1;

                    double pushX = (dx / dist) * PUSH_FORCE * 2;
                    double pushY = (dy / dist) * PUSH_FORCE * 2;

                    // SMART AVOIDANCE LOGIC
                    if (asset instanceof ActifAerien) {
                        // Drones: Fly OVER (Increase Altitude)
                        // Also push slightly away to clear the edge
                        asset.setZ(asset.getZ() + PUSH_FORCE * 2);
                        asset.setX(asset.getX() + pushX * 0.5);
                        asset.setY(asset.getY() + pushY * 0.5);
                    } else if (asset instanceof VehiculeSousMarin) {
                        // Subs: Dive UNDER (Decrease Depth) if possible
                        // Limit max depth to -200 for safety
                        if (asset.getZ() > -200) {
                            asset.setZ(asset.getZ() - PUSH_FORCE * 2);
                        }
                        // Also push sideways
                        asset.setX(asset.getX() + pushX);
                        asset.setY(asset.getY() + pushY);
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

    public void reset() {
        lastTime = 0;
        accumulator = 0;
    }
}
