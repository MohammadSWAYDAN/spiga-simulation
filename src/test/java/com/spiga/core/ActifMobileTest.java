package com.spiga.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ActifMobileTest {

    @Test
    public void testDroneInitialization() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 100);
        assertEquals("D1", drone.getId());
        assertEquals(ActifMobile.EtatOperationnel.AU_SOL, drone.getEtat());
    }

    @Test
    public void testMovement() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 100);
        drone.demarrer();
        assertEquals(ActifMobile.EtatOperationnel.EN_MISSION, drone.getEtat());

        // Test deplacer sets target
        drone.deplacer(100.0, 200.0, 50.0);
        assertEquals(100.0, drone.getTargetX(), 0.01);
        assertEquals(200.0, drone.getTargetY(), 0.01);
        assertEquals(50.0, drone.getTargetZ(), 0.01);

        // Simulate time passing to reach target
        // Speed ~120 m/s (approx). Distance ~229m.
        // Use dt=0.01s (step ~1.2m) to land within 1.0m tolerance.
        for (int i = 0; i < 1000; i++) {
            drone.update(0.01, null);
            if (drone.hasReachedTarget())
                break;
        }

        assertEquals(100.0, drone.getX(), 5.0);
        assertEquals(200.0, drone.getY(), 5.0);
        assertEquals(50.0, drone.getTargetZ(), 0.1);
    }

    @Test
    public void testRecharge() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 100);
        drone.demarrer();
        drone.update(2.0, null); // Partial drain

        drone.recharger();
        assertEquals(drone.getAutonomieMax(), drone.getAutonomieActuelle(), 0.01);
    }
}
