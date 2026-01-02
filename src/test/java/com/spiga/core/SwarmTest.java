package com.spiga.core;

import com.spiga.management.GestionnaireEssaim;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SwarmTest {

    @Test
    public void testAvoidanceTrigger() {
        // Setup
        GestionnaireEssaim manager = new GestionnaireEssaim();
        SimulationService sim = new SimulationService(manager);

        // 2 Drones converging
        DroneReconnaissance d1 = new DroneReconnaissance("D1", 0, 0, 100);
        DroneReconnaissance d2 = new DroneReconnaissance("D2", 50, 0, 100); // 50m apart (X-axis)

        manager.ajouterActif(d1);
        manager.ajouterActif(d2);

        d1.demarrer();
        d2.demarrer();

        // Target: Meet in middle (25, 0)
        d1.deplacer(25, 0, 100);
        d2.deplacer(25, 0, 100);

        // Initially Mission Mode
        assertEquals(ActifMobile.NavigationMode.NORMAL, d1.getNavigationMode());

        // Simulate approach
        // At 50m distance -> No Alert (Thresh 25)
        // Let's force them close manually for test speed
        d1.setX(10);
        d2.setX(30); // Dist = 20m (< 25m)

        // Run Sim Loop
        long t0 = System.nanoTime();
        sim.handle(t0);

        // Advance time by 100ms (approx 6 frames) to let collision check run
        long t1 = t0 + 100_000_000L;
        sim.handle(t1);

        // ASSERTIONS
        // 1. Check Avoidance triggered (Collision Warning set)
        assertNotNull(d1.getCollisionWarning(), "D1 should have a collision warning");
        assertEquals("Trop Proche! (Avoidance Active)", d1.getCollisionWarning());

        // 2. Check Navigation Mode (Protected field access allowed in same package)
        assertEquals(ActifMobile.NavigationMode.AVOIDING, d1.getNavigationMode(), "D1 should be in AVOIDING mode");
        assertEquals(ActifMobile.NavigationMode.AVOIDING, d2.getNavigationMode(), "D2 should be in AVOIDING mode");

        // 3. Verify Movement Direction
        // D1 was at 10. D2 was at 30.
        // D1 should move LEFT (< 10). D2 should move RIGHT (> 30).

        // Run more frames to accumulate movement
        for (int i = 0; i < 30; i++) { // Run 0.5s of simulation
            t1 += 16_666_666L; // +16ms
            sim.handle(t1);
        }

        System.out.println("D1 X: " + d1.getX());
        System.out.println("D2 X: " + d2.getX());

        assertTrue(d1.getX() < 10.0, "D1 should move Left (Avoidance)");
        assertTrue(d2.getX() > 30.0, "D2 should move Right (Avoidance)");

        // 4. Verify vertical stability (Mission demand)
        assertEquals(100.0, d1.getZ(), 0.1, "D1 Z should remain constant during avoidance");
    }
}
