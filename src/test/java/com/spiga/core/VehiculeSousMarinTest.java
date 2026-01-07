package com.spiga.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour VehiculeSousMarin.
 */
public class VehiculeSousMarinTest {

    @Test
    public void testConstructor() {
        VehiculeSousMarin sub = new VehiculeSousMarin("SUB-1", 100.0, 200.0, -50.0);
        assertEquals("SUB-1", sub.getId());
        assertEquals(100.0, sub.getX(), 0.01);
        assertEquals(200.0, sub.getY(), 0.01);
        assertEquals(-50.0, sub.getZ(), 0.01);
    }

    @Test
    public void testSpeedAndAutonomy() {
        VehiculeSousMarin sub = new VehiculeSousMarin("SUB-1", 0, 0, -50);
        assertEquals(20.0, sub.getVitesseMax(), 0.01);
        assertEquals(72.0, sub.getAutonomieMax(), 0.01);
    }

    @Test
    public void testConsommation() {
        VehiculeSousMarin sub = new VehiculeSousMarin("SUB-1", 0, 0, -50);
        assertEquals(1.0, sub.getConsommation(), 0.01);
    }

    @Test
    public void testProfondeurLimits() {
        VehiculeSousMarin sub = new VehiculeSousMarin("SUB-1", 0, 0, -50);
        assertEquals(0.0, sub.getProfondeurMax(), 0.01);
        assertEquals(-150.0, sub.getProfondeurMin(), 0.01);
    }

    @Test
    public void testClampPositionEnforcesMaxDepth() {
        VehiculeSousMarin sub = new VehiculeSousMarin("SUB-1", 0, 0, 10); // Above surface
        sub.demarrer();
        sub.update(0.01, null);
        assertTrue(sub.getZ() <= 0);
    }

    @Test
    public void testClampPositionEnforcesMinDepth() {
        VehiculeSousMarin sub = new VehiculeSousMarin("SUB-1", 0, 0, -200); // Too deep
        sub.demarrer();
        sub.update(0.01, null);
        assertTrue(sub.getZ() >= -150);
    }

    @Test
    public void testSetTargetClampsToSurface() {
        VehiculeSousMarin sub = new VehiculeSousMarin("SUB-1", 0, 0, -50);
        sub.setTarget(100, 100, 50); // Try to go above surface
        assertEquals(0.0, sub.getTargetZ(), 0.01);
    }

    @Test
    public void testSetTargetClampsToMaxDepth() {
        VehiculeSousMarin sub = new VehiculeSousMarin("SUB-1", 0, 0, -50);
        sub.setTarget(100, 100, -200); // Too deep
        assertEquals(-150.0, sub.getTargetZ(), 0.01);
    }

    @Test
    public void testSetTargetValidDepth() {
        VehiculeSousMarin sub = new VehiculeSousMarin("SUB-1", 0, 0, -50);
        sub.setTarget(100, 100, -75);
        assertEquals(-75.0, sub.getTargetZ(), 0.01);
    }

    @Test
    public void testDeplacerDelegatesToSetTarget() {
        VehiculeSousMarin sub = new VehiculeSousMarin("SUB-1", 0, 0, -50);
        sub.deplacer(200, 300, -100);
        assertEquals(-100.0, sub.getTargetZ(), 0.01);
    }

    @Test
    public void testDeplacerWithInvalidDepth() {
        VehiculeSousMarin sub = new VehiculeSousMarin("SUB-1", 0, 0, -50);
        sub.deplacer(200, 300, 100); // Above water
        assertEquals(0.0, sub.getTargetZ(), 0.01);
    }

    @Test
    public void testMovementUnderwater() {
        VehiculeSousMarin sub = new VehiculeSousMarin("SUB-1", 0, 0, -50);
        sub.demarrer();
        sub.setTarget(1000, 0, -50);

        double initialX = sub.getX();
        sub.update(1.0, null);

        assertTrue(sub.getX() > initialX);
        assertEquals(-50.0, sub.getZ(), 1.0); // Should stay at same depth
    }

    @Test
    public void testInitialState() {
        VehiculeSousMarin sub = new VehiculeSousMarin("SUB-1", 0, 0, -50);
        assertEquals(ActifMobile.EtatOperationnel.AU_SOL, sub.getEtat());
        assertEquals(72.0, sub.getAutonomieActuelle(), 0.01);
    }

    @Test
    public void testAtSurface() {
        VehiculeSousMarin sub = new VehiculeSousMarin("SUB-1", 0, 0, 0);
        assertEquals(0.0, sub.getZ(), 0.01);
        sub.demarrer();
        sub.update(0.01, null);
        assertEquals(0.0, sub.getZ(), 0.01);
    }

    @Test
    public void testAtMaxDepth() {
        VehiculeSousMarin sub = new VehiculeSousMarin("SUB-1", 0, 0, -150);
        sub.demarrer();
        sub.update(0.01, null);
        assertEquals(-150.0, sub.getZ(), 0.01);
    }
}
