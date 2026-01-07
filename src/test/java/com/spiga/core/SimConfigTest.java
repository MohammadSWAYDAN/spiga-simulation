package com.spiga.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests pour SimConfig (constantes statiques).
 */
public class SimConfigTest {

    @Test
    public void testMinDistanceConstant() {
        assertEquals(15.0, SimConfig.MIN_DISTANCE, 0.01);
    }

    @Test
    public void testSafetyRadiusConstant() {
        assertEquals(20.0, SimConfig.SAFETY_RADIUS, 0.01);
    }

    @Test
    public void testWorldDimensions() {
        assertEquals(2000.0, SimConfig.WORLD_WIDTH, 0.01);
        assertEquals(2000.0, SimConfig.WORLD_HEIGHT, 0.01);
    }

    @Test
    public void testDefaultTimeScale() {
        assertEquals(300.0, SimConfig.DEFAULT_TIME_SCALE, 0.01);
    }

    @Test
    public void testCollisionThreshold() {
        assertEquals(25.0, SimConfig.COLLISION_THRESHOLD, 0.01);
    }

    @Test
    public void testDroneAltitudeLimits() {
        assertEquals(1.0, SimConfig.DRONE_MIN_ALTITUDE, 0.01);
        assertEquals(150.0, SimConfig.DRONE_MAX_ALTITUDE, 0.01);
    }

    @Test
    public void testZoneSafetyMargin() {
        assertEquals(100.0, SimConfig.ZONE_SAFETY_MARGIN, 0.01);
    }

    @Test
    public void testSubMaxDepth() {
        assertEquals(-150.0, SimConfig.SUB_MAX_DEPTH, 0.01);
    }

    @Test
    public void testVelocitySmoothing() {
        assertEquals(0.1, SimConfig.VELOCITY_SMOOTHING, 0.01);
    }

    @Test
    public void testAvoidanceForceCapAndDuration() {
        assertEquals(3.0, SimConfig.AVOIDANCE_FORCE_CAP, 0.01);
        assertEquals(2.0, SimConfig.AVOIDANCE_DURATION, 0.01);
    }

    @Test
    public void testSeparationDistance() {
        assertEquals(30.0, SimConfig.SEPARATION_DISTANCE, 0.01);
    }
}
