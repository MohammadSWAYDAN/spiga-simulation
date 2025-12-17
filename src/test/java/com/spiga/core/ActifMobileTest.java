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

        // Test movement
        drone.deplacer(100.0, 200.0, 50.0);
        assertEquals(100.0, drone.getX(), 0.01);
        assertEquals(200.0, drone.getY(), 0.01);
        assertEquals(50.0, drone.getZ(), 0.01);
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
