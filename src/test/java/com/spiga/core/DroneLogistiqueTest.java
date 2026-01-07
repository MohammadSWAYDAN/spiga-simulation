package com.spiga.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour DroneLogistique.
 */
public class DroneLogistiqueTest {

    @Test
    public void testConstructor() {
        DroneLogistique drone = new DroneLogistique("LOG-1", 100.0, 200.0, 50.0);
        assertEquals("LOG-1", drone.getId());
        assertEquals(100.0, drone.getX(), 0.01);
        assertEquals(200.0, drone.getY(), 0.01);
        assertEquals(50.0, drone.getZ(), 0.01);
    }

    @Test
    public void testSpeedAndAutonomy() {
        DroneLogistique drone = new DroneLogistique("LOG-1", 0, 0, 100);
        assertEquals(60.0, drone.getVitesseMax(), 0.01); // 60 km/h as per implementation
        assertEquals(8.0, drone.getAutonomieMax(), 0.01); // 8h autonomy as per implementation
    }

    @Test
    public void testChargeUtileMax() {
        DroneLogistique drone = new DroneLogistique("LOG-1", 0, 0, 100);
        assertEquals(50.0, drone.getChargeUtileMax(), 0.01);
    }

    @Test
    public void testChargerNormal() {
        DroneLogistique drone = new DroneLogistique("LOG-1", 0, 0, 100);
        drone.charger(20.0);
        assertEquals(20.0, drone.getChargeActuelle(), 0.01);
    }

    @Test
    public void testChargerMultiple() {
        DroneLogistique drone = new DroneLogistique("LOG-1", 0, 0, 100);
        drone.charger(20.0);
        drone.charger(15.0);
        assertEquals(35.0, drone.getChargeActuelle(), 0.01);
    }

    @Test
    public void testChargerOverMax() {
        DroneLogistique drone = new DroneLogistique("LOG-1", 0, 0, 100);
        drone.charger(60.0); // Attempt to charge 60kg but max is 50, so it's ignored
        assertEquals(0.0, drone.getChargeActuelle(), 0.01); // Nothing charged because 60 > 50

        // Now try within limits
        drone.charger(50.0); // This should work (50 <= 50)
        assertEquals(50.0, drone.getChargeActuelle(), 0.01);
    }

    @Test
    public void testDecharger() {
        DroneLogistique drone = new DroneLogistique("LOG-1", 0, 0, 100);
        drone.charger(30.0);
        drone.decharger();
        assertEquals(0.0, drone.getChargeActuelle(), 0.01);
    }

    @Test
    public void testConsommationEmpty() {
        DroneLogistique drone = new DroneLogistique("LOG-1", 0, 0, 100);
        // Base consumption: 2.4
        assertEquals(2.4, drone.getConsommation(), 0.01);
    }

    @Test
    public void testConsommationWithLoad() {
        DroneLogistique drone = new DroneLogistique("LOG-1", 0, 0, 100);
        drone.charger(20.0);
        // Consumption: 2.4 + (0.05 * 20) = 2.4 + 1.0 = 3.4
        assertEquals(3.4, drone.getConsommation(), 0.01);
    }

    @Test
    public void testConsommationMaxLoad() {
        DroneLogistique drone = new DroneLogistique("LOG-1", 0, 0, 100);
        drone.charger(50.0);
        // Consumption: 2.4 + (0.05 * 50) = 2.4 + 2.5 = 4.9
        assertEquals(4.9, drone.getConsommation(), 0.01);
    }

    @Test
    public void testDeplacerDelegatesToSetTarget() {
        DroneLogistique drone = new DroneLogistique("LOG-1", 0, 0, 50);
        drone.demarrer();
        drone.deplacer(200.0, 300.0, 75.0);
        // Should set target (possibly adjusted)
        assertNotNull(drone.getTargetX());
    }

    @Test
    public void testInitialChargeIsZero() {
        DroneLogistique drone = new DroneLogistique("LOG-1", 0, 0, 100);
        assertEquals(0.0, drone.getChargeActuelle(), 0.01);
    }
}
