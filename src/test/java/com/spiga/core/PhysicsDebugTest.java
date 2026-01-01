package com.spiga.core;

import com.spiga.environment.Obstacle;
import com.spiga.management.GestionnaireEssaim;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

public class PhysicsDebugTest {

    @Test
    public void testObstacleAvoidancePath() {
        System.out.println("=== STARTING PHYSICS DEBUG SIMULATION ===");

        // 1. Setup
        GestionnaireEssaim manager = new GestionnaireEssaim();
        SimulationService sim = new SimulationService(manager);

        // Clear default obstacles and add a specific test obstacle
        sim.getObstacles().clear();
        Obstacle rock = new Obstacle(500, 0, 0, 50); // Rock at X=500
        sim.getObstacles().add(rock);

        // Add Drone
        // Subclass concrete for testing
        ActifMobile drone = new DroneReconnaissance("TEST-DRONE", 0, 0, 0);
        // Vitesse 20 m/s

        manager.getFlotte().add(drone);

        // 2. Set Target BEYOND the obstacle
        drone.demarrer();
        drone.setTarget(1000, 0, 0);

        System.out.println("Drone Start: (0,0) -> Target: (1000,0)");
        System.out.println("Obstacle: (500,0), Radius: 50");

        // 3. Run Loop
        double dt = 0.016; // 60 FPS
        int maxFrames = 2000; // ~30 seconds of sim

        boolean reached = false;

        for (int i = 0; i < maxFrames; i++) {
            // Manually trigger service update logic
            // We need to mimic updateSimulation
            List<ActifMobile> fleet = new ArrayList<>(manager.getFlotte());

            // Private method access reflection? No, let's just use the public methods if
            // possible
            // or copy the logic logic snippet for testing.
            // Accessing private methods is hard.
            // But ActifMobile.update and checkObstacles are the core.
            // We can use reflection to invoke CheckObstacles or just modify SimService to
            // be testable.
            // OR, better: We rely on the fact that we can fix the code if we see the logs.
            // Let's just print the state from the drone AFTER a mocked update.

            // Since we can't easily call private SimService methods, let's look at
            // ActifMobile state
            // But ActifMobile state depends on SimService.checkObstacles setting forces.

            // Workaround: We will use Reflection to call checkObstacles for this test
            try {
                java.lang.reflect.Method checkObs = SimulationService.class.getDeclaredMethod("checkObstacles",
                        List.class);
                checkObs.setAccessible(true);
                checkObs.invoke(sim, fleet);

                // Also update
                for (ActifMobile a : fleet) {
                    a.update(dt, null);
                }

                // Log every 60 frames (1 sec) or if close
                double distToRock = Math.sqrt(Math.pow(drone.getX() - 500, 2) + Math.pow(drone.getY(), 2));

                if (i % 60 == 0 || distToRock < 60) {
                    System.out.printf(
                            "Frame %4d | Pos: (%.1f, %.1f) | Tgt: (%.1f, %.1f) | Mode: %s | Div: %s | Bias: %.2f | DistRock: %.1f\n",
                            i, drone.getX(), drone.getY(),
                            // We want to see EFFECTIVE target. Since strict encapsulation, we guess based
                            // on isDiverted
                            (drone instanceof ActifMobile) ? drone.getTargetX() : 0, drone.getTargetY(),
                            drone.getNavigationMode(),
                            // Check isDiverted via reflection or assume fixed if we see behavior
                            "?", // Cannot access protected field easily without more reflection
                            drone.getSteeringBias(),
                            distToRock);
                }

                if (drone.hasReachedTarget()) {
                    System.out.println("✅ TARGET REACHED at Frame " + i);
                    reached = true;
                    break;
                }

            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }

        if (!reached) {
            System.out.println("❌ FAILED TO REACH TARGET");
        }
    }
}
