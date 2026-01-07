package com.spiga.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour DroneReconnaissance.
 */
public class DroneReconnaissanceTest {

    @Test
    public void testConstructor() {
        DroneReconnaissance drone = new DroneReconnaissance("RECON-1", 100.0, 200.0, 50.0);
        assertEquals("RECON-1", drone.getId());
        assertEquals(100.0, drone.getX(), 0.01);
        assertEquals(200.0, drone.getY(), 0.01);
        assertEquals(50.0, drone.getZ(), 0.01);
    }

    @Test
    public void testSpeedAndAutonomy() {
        DroneReconnaissance drone = new DroneReconnaissance("RECON-1", 0, 0, 100);
        assertEquals(120.0, drone.getVitesseMax(), 0.01);
        assertEquals(4.0, drone.getAutonomieMax(), 0.01);
    }

    @Test
    public void testConsommation() {
        DroneReconnaissance drone = new DroneReconnaissance("RECON-1", 0, 0, 100);
        assertEquals(2.0, drone.getConsommation(), 0.01);
    }

    @Test
    public void testRayonSurveillance() {
        DroneReconnaissance drone = new DroneReconnaissance("RECON-1", 0, 0, 100);
        assertEquals(1000.0, drone.getRayonSurveillance(), 0.01);
    }

    @Test
    public void testAltitudeLimits() {
        DroneReconnaissance drone = new DroneReconnaissance("RECON-1", 0, 0, 100);
        assertEquals(3000.0, drone.getAltitudeMax(), 0.01);
    }

    @Test
    public void testInitialState() {
        DroneReconnaissance drone = new DroneReconnaissance("RECON-1", 0, 0, 100);
        assertEquals(ActifMobile.EtatOperationnel.AU_SOL, drone.getEtat());
        assertEquals(4.0, drone.getAutonomieActuelle(), 0.01);
    }

    @Test
    public void testDeplacerSetsTarget() {
        DroneReconnaissance drone = new DroneReconnaissance("RECON-1", 0, 0, 50);
        drone.demarrer();
        drone.deplacer(500.0, 600.0, 100.0);
        assertEquals(500.0, drone.getTargetX(), 0.01);
        assertEquals(600.0, drone.getTargetY(), 0.01);
        assertEquals(100.0, drone.getTargetZ(), 0.01);
    }

    @Test
    public void testSetTargetClampsAltitudeToMinimum() {
        DroneReconnaissance drone = new DroneReconnaissance("RECON-1", 0, 0, 50);
        drone.setTarget(100, 100, 0); // Z=0 should be clamped to 1
        assertEquals(1.0, drone.getTargetZ(), 0.01);
    }

    @Test
    public void testSetTargetClampsAltitudeToMaximum() {
        DroneReconnaissance drone = new DroneReconnaissance("RECON-1", 0, 0, 50);
        drone.setTarget(100, 100, 200); // Z=200 should be clamped to 150
        assertEquals(150.0, drone.getTargetZ(), 0.01);
    }
}
