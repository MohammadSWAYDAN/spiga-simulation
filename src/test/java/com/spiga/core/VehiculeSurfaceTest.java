package com.spiga.core;

import com.spiga.environment.Weather;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour VehiculeSurface.
 */
public class VehiculeSurfaceTest {

    @Test
    public void testConstructor() {
        VehiculeSurface navire = new VehiculeSurface("SURFACE-1", 150.0, 250.0);
        assertEquals("SURFACE-1", navire.getId());
        assertEquals(150.0, navire.getX(), 0.01);
        assertEquals(250.0, navire.getY(), 0.01);
        assertEquals(0.0, navire.getZ(), 0.01);
    }

    @Test
    public void testSpeedAndAutonomy() {
        VehiculeSurface navire = new VehiculeSurface("SURFACE-1", 0, 0);
        assertEquals(40.0, navire.getVitesseMax(), 0.01);
        assertEquals(5.0, navire.getAutonomieMax(), 0.01);
    }

    @Test
    public void testConsommation() {
        VehiculeSurface navire = new VehiculeSurface("SURFACE-1", 0, 0);
        assertEquals(1.0, navire.getConsommation(), 0.01);
    }

    @Test
    public void testRayonRadar() {
        VehiculeSurface navire = new VehiculeSurface("SURFACE-1", 0, 0);
        assertEquals(50.0, navire.getRayonRadar(), 0.01);
    }

    @Test
    public void testClampPositionForcesZToZero() {
        VehiculeSurface navire = new VehiculeSurface("SURFACE-1", 0, 0);
        navire.demarrer();
        navire.update(0.01, null);
        assertEquals(0.0, navire.getZ(), 0.01);
    }

    @Test
    public void testSetTargetIgnoresNonZeroZ() {
        VehiculeSurface navire = new VehiculeSurface("SURFACE-1", 0, 0);
        navire.setTarget(100, 100, 50); // Try to set Z=50
        assertEquals(0.0, navire.getTargetZ(), 0.01);
    }

    @Test
    public void testSetTargetWithNegativeZ() {
        VehiculeSurface navire = new VehiculeSurface("SURFACE-1", 0, 0);
        navire.setTarget(100, 100, -30); // Try to go underwater
        assertEquals(0.0, navire.getTargetZ(), 0.01);
    }

    @Test
    public void testDeplacerDelegatesToSetTarget() {
        VehiculeSurface navire = new VehiculeSurface("SURFACE-1", 0, 0);
        navire.deplacer(200, 300, 100);
        assertEquals(0.0, navire.getTargetZ(), 0.01);
        assertEquals(200.0, navire.getTargetX(), 0.01);
        assertEquals(300.0, navire.getTargetY(), 0.01);
    }

    @Test
    public void testSpeedMultiplierCalm() {
        VehiculeSurface navire = new VehiculeSurface("SURFACE-1", 0, 0);
        Weather calm = new Weather(0, 0, 0, 0);
        navire.demarrer();
        navire.setTarget(1000, 0, 0);

        double initialX = navire.getX();
        navire.update(1.0, calm);

        // Should move at full speed
        assertTrue(navire.getX() > initialX);
    }

    @Test
    public void testSpeedMultiplierWithWaves() {
        VehiculeSurface navire = new VehiculeSurface("SURFACE-1", 0, 0);
        Weather wavy = new Weather(0, 0, 0, 1.0); // Max waves
        navire.demarrer();
        navire.setTarget(1000, 0, 0);

        double initialX = navire.getX();
        navire.update(1.0, wavy);
        double distanceWavy = navire.getX() - initialX;

        // Compare with calm
        VehiculeSurface navire2 = new VehiculeSurface("TEST2", 0, 0);
        Weather calm = new Weather(0, 0, 0, 0);
        navire2.demarrer();
        navire2.setTarget(1000, 0, 0);
        navire2.update(1.0, calm);
        double distanceCalm = navire2.getX();

        assertTrue(distanceWavy < distanceCalm);
    }

    @Test
    public void testBatteryMultiplierWaves() {
        VehiculeSurface navire = new VehiculeSurface("SURFACE-1", 0, 0);
        Weather wavy = new Weather(0, 0, 0, 1.0);
        navire.demarrer();
        double initialBattery = navire.getAutonomieActuelle();
        navire.setTarget(1000, 0, 0);
        navire.update(1.0, wavy);
        double usedWavy = initialBattery - navire.getAutonomieActuelle();

        VehiculeSurface navire2 = new VehiculeSurface("TEST2", 0, 0);
        Weather calm = new Weather(0, 0, 0, 0);
        navire2.demarrer();
        double initialBattery2 = navire2.getAutonomieActuelle();
        navire2.setTarget(1000, 0, 0);
        navire2.update(1.0, calm);
        double usedCalm = initialBattery2 - navire2.getAutonomieActuelle();

        assertTrue(usedWavy > usedCalm);
    }

    @Test
    public void testProfondeurLimits() {
        VehiculeSurface navire = new VehiculeSurface("SURFACE-1", 0, 0);
        assertEquals(0.0, navire.getProfondeurMax(), 0.01);
        assertEquals(0.0, navire.getProfondeurMin(), 0.01);
    }

    @Test
    public void testInitialState() {
        VehiculeSurface navire = new VehiculeSurface("SURFACE-1", 0, 0);
        assertEquals(ActifMobile.EtatOperationnel.AU_SOL, navire.getEtat());
    }
}
