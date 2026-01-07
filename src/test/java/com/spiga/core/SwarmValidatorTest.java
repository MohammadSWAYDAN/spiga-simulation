package com.spiga.core;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests pour SwarmValidator (validation de placement d'essaim).
 */
public class SwarmValidatorTest {

    @Test
    public void testValidPlacementEmptyList() {
        List<ActifMobile> existing = new ArrayList<>();
        assertTrue(SwarmValidator.isPlacementValid(100, 100, 50, existing));
    }

    @Test
    public void testValidPlacementFarFromOthers() {
        List<ActifMobile> existing = new ArrayList<>();
        existing.add(new DroneReconnaissance("D1", 0, 0, 50));
        existing.add(new DroneReconnaissance("D2", 200, 200, 50));

        // New position far from all existing drones
        assertTrue(SwarmValidator.isPlacementValid(500, 500, 50, existing));
    }

    @Test
    public void testInvalidPlacementTooClose() {
        List<ActifMobile> existing = new ArrayList<>();
        existing.add(new DroneReconnaissance("D1", 100, 100, 50));

        // Position right on top of existing drone (distance = 0)
        assertFalse(SwarmValidator.isPlacementValid(100, 100, 50, existing));
    }

    @Test
    public void testInvalidPlacementWithinMinDistance() {
        List<ActifMobile> existing = new ArrayList<>();
        existing.add(new DroneReconnaissance("D1", 100, 100, 50));

        // Position within MIN_DISTANCE (15.0)
        assertFalse(SwarmValidator.isPlacementValid(110, 100, 50, existing)); // distance = 10
    }

    @Test
    public void testValidPlacementAtExactMinDistance() {
        List<ActifMobile> existing = new ArrayList<>();
        existing.add(new DroneReconnaissance("D1", 100, 100, 50));

        // Position exactly at MIN_DISTANCE (15.0) - should be valid (not <
        // MIN_DISTANCE)
        assertTrue(SwarmValidator.isPlacementValid(115, 100, 50, existing)); // distance = 15
    }

    @Test
    public void testPlacementConsiders3D() {
        List<ActifMobile> existing = new ArrayList<>();
        existing.add(new DroneReconnaissance("D1", 100, 100, 50));

        // Same XY but different Z - if Z difference is >= MIN_DISTANCE, it's valid
        assertTrue(SwarmValidator.isPlacementValid(100, 100, 70, existing)); // distance = 20 (Z only)
    }

    @Test
    public void testPlacementWithMultipleDrones() {
        List<ActifMobile> existing = new ArrayList<>();
        existing.add(new DroneReconnaissance("D1", 0, 0, 50));
        existing.add(new DroneReconnaissance("D2", 100, 0, 50));
        existing.add(new DroneReconnaissance("D3", 200, 0, 50));

        // Valid - between D1 and D2 but far enough from both
        assertTrue(SwarmValidator.isPlacementValid(50, 50, 50, existing));

        // Invalid - too close to D2
        assertFalse(SwarmValidator.isPlacementValid(105, 0, 50, existing));
    }
}
