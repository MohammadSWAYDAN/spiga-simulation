package com.spiga.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests pour SousMarinExploration.
 */
public class SousMarinExplorationTest {

    @Test
    public void testConstructor() {
        SousMarinExploration sub = new SousMarinExploration("EXPLO-1", 100.0, 200.0, -50.0);
        assertEquals("EXPLO-1", sub.getId());
        assertEquals(100.0, sub.getX(), 0.01);
        assertEquals(200.0, sub.getY(), 0.01);
        assertEquals(-50.0, sub.getZ(), 0.01);
    }

    @Test
    public void testSpeedAndAutonomy() {
        SousMarinExploration sub = new SousMarinExploration("EXPLO-1", 0, 0, -50);
        assertEquals(15.0, sub.getVitesseMax(), 0.01); // 15 km/h (slower than VehiculeSousMarin)
        assertEquals(120.0, sub.getAutonomieMax(), 0.01); // 120h = 5 days
    }

    @Test
    public void testConsommation() {
        SousMarinExploration sub = new SousMarinExploration("EXPLO-1", 0, 0, -50);
        assertEquals(0.008, sub.getConsommation(), 0.0001); // 0.8% per hour
    }

    @Test
    public void testProfondeurMin() {
        SousMarinExploration sub = new SousMarinExploration("EXPLO-1", 0, 0, -50);
        assertEquals(-150.0, sub.getProfondeurMin(), 0.01);
    }

    @Test
    public void testInitialBattery() {
        SousMarinExploration sub = new SousMarinExploration("EXPLO-1", 0, 0, -50);
        assertEquals(120.0, sub.getAutonomieActuelle(), 0.01);
    }

    @Test
    public void testInheritanceFromVehiculeSousMarin() {
        SousMarinExploration sub = new SousMarinExploration("EXPLO-1", 0, 0, -50);
        // Should inherit depth constraint enforcement
        sub.setTarget(100, 100, 50); // Above surface - should be clamped
        assertEquals(0.0, sub.getTargetZ(), 0.01);
    }

    @Test
    public void testMovementUnderwater() {
        SousMarinExploration sub = new SousMarinExploration("EXPLO-1", 0, 0, -50);
        sub.demarrer();
        sub.setTarget(1000, 0, -50);
        double initialX = sub.getX();
        sub.update(1.0, null);
        assertTrue(sub.getX() > initialX);
    }
}
