package com.spiga.environment;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe RestrictedZone.
 */
public class RestrictedZoneTest {

    @Test
    public void testConstructorAndGetters() {
        RestrictedZone zone = new RestrictedZone("ZONE-A", 500.0, 600.0, 100.0, 0.0, 200.0);
        assertEquals("ZONE-A", zone.getId());
        assertEquals(500.0, zone.getX(), 0.01);
        assertEquals(600.0, zone.getY(), 0.01);
        assertEquals(100.0, zone.getRadius(), 0.01);
        assertEquals(0.0, zone.getMinZ(), 0.01);
        assertEquals(200.0, zone.getMaxZ(), 0.01);
    }

    @Test
    public void testIsInsideAtCenter() {
        RestrictedZone zone = new RestrictedZone("TEST", 100.0, 100.0, 50.0, 0.0, 100.0);
        // Center of zone at mid-altitude
        assertTrue(zone.isInside(100.0, 100.0, 50.0));
    }

    @Test
    public void testIsInsideWithinRadius() {
        RestrictedZone zone = new RestrictedZone("TEST", 100.0, 100.0, 50.0, 0.0, 100.0);
        // Inside radius, valid Z
        assertTrue(zone.isInside(120.0, 100.0, 50.0));
        assertTrue(zone.isInside(100.0, 130.0, 25.0));
        assertTrue(zone.isInside(80.0, 80.0, 75.0));
    }

    @Test
    public void testIsInsideAtBoundary() {
        RestrictedZone zone = new RestrictedZone("TEST", 0.0, 0.0, 100.0, 0.0, 100.0);
        // At radius boundary (exactly on edge)
        assertTrue(zone.isInside(100.0, 0.0, 50.0)); // distance = 100, 100^2 <= 100^2
        assertTrue(zone.isInside(0.0, 100.0, 50.0));
    }

    @Test
    public void testIsInsideAtZBoundaries() {
        RestrictedZone zone = new RestrictedZone("TEST", 100.0, 100.0, 50.0, 10.0, 90.0);
        // At minZ
        assertTrue(zone.isInside(100.0, 100.0, 10.0));
        // At maxZ
        assertTrue(zone.isInside(100.0, 100.0, 90.0));
    }

    @Test
    public void testOutsideHorizontally() {
        RestrictedZone zone = new RestrictedZone("TEST", 100.0, 100.0, 50.0, 0.0, 100.0);
        // Outside radius horizontally
        assertFalse(zone.isInside(200.0, 100.0, 50.0));
        assertFalse(zone.isInside(100.0, 200.0, 50.0));
        assertFalse(zone.isInside(0.0, 0.0, 50.0));
    }

    @Test
    public void testOutsideBelowMinZ() {
        RestrictedZone zone = new RestrictedZone("TEST", 100.0, 100.0, 50.0, 10.0, 90.0);
        // Below minZ but inside horizontal radius
        assertFalse(zone.isInside(100.0, 100.0, 5.0));
        assertFalse(zone.isInside(100.0, 100.0, -10.0));
    }

    @Test
    public void testOutsideAboveMaxZ() {
        RestrictedZone zone = new RestrictedZone("TEST", 100.0, 100.0, 50.0, 10.0, 90.0);
        // Above maxZ but inside horizontal radius
        assertFalse(zone.isInside(100.0, 100.0, 95.0));
        assertFalse(zone.isInside(100.0, 100.0, 150.0));
    }

    @Test
    public void testNegativeZRange() {
        // Underwater zone (like military submarine area)
        RestrictedZone zone = new RestrictedZone("SUBMARINE_ZONE", 500.0, 500.0, 200.0, -100.0, -10.0);
        assertTrue(zone.isInside(500.0, 500.0, -50.0));
        assertFalse(zone.isInside(500.0, 500.0, 0.0)); // At surface - outside
        assertFalse(zone.isInside(500.0, 500.0, -150.0)); // Too deep
    }

    @Test
    public void testThinVerticalZone() {
        // Very thin altitude slice
        RestrictedZone zone = new RestrictedZone("THIN", 0.0, 0.0, 100.0, 49.0, 51.0);
        assertTrue(zone.isInside(0.0, 0.0, 50.0));
        assertFalse(zone.isInside(0.0, 0.0, 48.0));
        assertFalse(zone.isInside(0.0, 0.0, 52.0));
    }

    @Test
    public void testLargeZone() {
        RestrictedZone zone = new RestrictedZone("LARGE", 0.0, 0.0, 10000.0, 0.0, 5000.0);
        assertTrue(zone.isInside(5000.0, 5000.0, 2500.0));
        assertTrue(zone.isInside(-5000.0, -5000.0, 100.0));
        assertFalse(zone.isInside(10000.0, 10000.0, 100.0)); // Outside radius
    }
}
