package com.spiga.core;

import com.spiga.environment.Weather;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour ActifMarin (testé via VehiculeSurface et
 * VehiculeSousMarin).
 */
public class ActifMarinTest {

    @Test
    public void testVehiculeSurfaceConstructor() {
        VehiculeSurface navire = new VehiculeSurface("NAVIRE-1", 100.0, 200.0);
        assertEquals("NAVIRE-1", navire.getId());
        assertEquals(100.0, navire.getX(), 0.01);
        assertEquals(200.0, navire.getY(), 0.01);
        assertEquals(0.0, navire.getZ(), 0.01);
    }

    @Test
    public void testVehiculeSousMarinConstructor() {
        VehiculeSousMarin sub = new VehiculeSousMarin("SUB-1", 100.0, 200.0, -50.0);
        assertEquals("SUB-1", sub.getId());
        assertEquals(100.0, sub.getX(), 0.01);
        assertEquals(200.0, sub.getY(), 0.01);
        assertEquals(-50.0, sub.getZ(), 0.01);
    }

    @Test
    public void testSpeedEfficiencyWithWaves() {
        VehiculeSurface navire = new VehiculeSurface("NAVIRE-1", 0, 0);
        Weather wavy = new Weather(0, 0, 0, 1.0); // Max waves (5m)
        navire.demarrer();

        // With waves, speed efficiency should decrease
        double initialX = navire.getX();
        navire.setTarget(1000, 0, 0);
        navire.update(1.0, wavy);

        double distanceWavy = navire.getX() - initialX;

        // Compare with calm seas
        VehiculeSurface navire2 = new VehiculeSurface("NAVIRE-2", 0, 0);
        Weather calm = new Weather(0, 0, 0, 0);
        navire2.demarrer();
        navire2.setTarget(1000, 0, 0);
        navire2.update(1.0, calm);
        double distanceCalm = navire2.getX();

        assertTrue(distanceWavy < distanceCalm);
    }

    @Test
    public void testProfondeurConstraints() {
        VehiculeSurface navire = new VehiculeSurface("NAVIRE-1", 0, 0);
        assertEquals(0.0, navire.getProfondeurMax(), 0.01);
        assertEquals(0.0, navire.getProfondeurMin(), 0.01);
    }

    @Test
    public void testSousMarinProfondeurConstraints() {
        VehiculeSousMarin sub = new VehiculeSousMarin("SUB-1", 0, 0, -50);
        assertEquals(0.0, sub.getProfondeurMax(), 0.01);
        assertEquals(-150.0, sub.getProfondeurMin(), 0.01);
    }

    @Test
    public void testDeplacerRespectsProfondeurForSurface() {
        VehiculeSurface navire = new VehiculeSurface("NAVIRE-1", 0, 0);
        navire.demarrer();
        navire.deplacer(100, 100, -50); // Try to go underwater

        // Target Z should be forced to 0
        assertEquals(0.0, navire.getTargetZ(), 0.01);
    }

    @Test
    public void testDeplacerRespectsProfondeurForSubmarine() {
        VehiculeSousMarin sub = new VehiculeSousMarin("SUB-1", 0, 0, -50);
        sub.demarrer();
        sub.deplacer(100, 100, -100);

        // Target Z should be within allowed range
        assertTrue(sub.getTargetZ() <= 0);
        assertTrue(sub.getTargetZ() >= -150);
    }

    @Test
    public void testSetProfondeurMax() {
        VehiculeSousMarin sub = new VehiculeSousMarin("SUB-1", 0, 0, -50);
        sub.setProfondeurMax(-10); // Change max depth
        assertEquals(-10.0, sub.getProfondeurMax(), 0.01);
    }
}
