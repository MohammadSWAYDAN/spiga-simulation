package com.spiga.environment;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe Obstacle.
 */
public class ObstacleTest {

    @Test
    public void testConstructorAndGetters() {
        Obstacle obs = new Obstacle(100.0, 200.0, 50.0, 25.0);
        assertEquals(100.0, obs.getX(), 0.01);
        assertEquals(200.0, obs.getY(), 0.01);
        assertEquals(50.0, obs.getZ(), 0.01);
        assertEquals(25.0, obs.getRadius(), 0.01);
    }

    @Test
    public void testCollisionAtCenter() {
        Obstacle obs = new Obstacle(100.0, 100.0, 100.0, 50.0);
        // Point exactly at center
        assertTrue(obs.isCollision(100.0, 100.0, 100.0));
    }

    @Test
    public void testCollisionInsideObstacle() {
        Obstacle obs = new Obstacle(100.0, 100.0, 100.0, 50.0);
        // Point inside the sphere (distance < radius)
        assertTrue(obs.isCollision(110.0, 110.0, 100.0));
        assertTrue(obs.isCollision(100.0, 100.0, 130.0));
        assertTrue(obs.isCollision(90.0, 90.0, 90.0));
    }

    @Test
    public void testNoCollisionOutsideObstacle() {
        Obstacle obs = new Obstacle(100.0, 100.0, 100.0, 50.0);
        // Point outside the sphere (distance > radius)
        assertFalse(obs.isCollision(200.0, 100.0, 100.0));
        assertFalse(obs.isCollision(100.0, 200.0, 100.0));
        assertFalse(obs.isCollision(100.0, 100.0, 200.0));
        assertFalse(obs.isCollision(0.0, 0.0, 0.0));
    }

    @Test
    public void testCollisionAtBoundary() {
        Obstacle obs = new Obstacle(0.0, 0.0, 0.0, 10.0);
        // Point exactly at radius distance - should NOT be collision (distance < radius
        // is collision)
        // At (10, 0, 0), distance = 10, but collision is distance < radius, so false
        assertFalse(obs.isCollision(10.0, 0.0, 0.0));

        // Just inside boundary
        assertTrue(obs.isCollision(9.9, 0.0, 0.0));
    }

    @Test
    public void testCollisionWithNegativeCoordinates() {
        Obstacle obs = new Obstacle(-50.0, -50.0, -50.0, 20.0);
        assertTrue(obs.isCollision(-50.0, -50.0, -50.0));
        assertTrue(obs.isCollision(-45.0, -50.0, -50.0));
        assertFalse(obs.isCollision(0.0, 0.0, 0.0));
    }

    @Test
    public void testSmallObstacle() {
        Obstacle obs = new Obstacle(500.0, 500.0, 0.0, 1.0);
        assertTrue(obs.isCollision(500.0, 500.0, 0.0));
        assertFalse(obs.isCollision(502.0, 500.0, 0.0));
    }

    @Test
    public void testLargeObstacle() {
        Obstacle obs = new Obstacle(0.0, 0.0, 0.0, 1000.0);
        assertTrue(obs.isCollision(500.0, 500.0, 0.0));
        assertTrue(obs.isCollision(-500.0, -500.0, 0.0));
        assertFalse(obs.isCollision(1000.0, 1000.0, 0.0));
    }

    @Test
    public void test3DDistance() {
        Obstacle obs = new Obstacle(0.0, 0.0, 0.0, 100.0);
        // Distance in 3D: sqrt(50^2 + 50^2 + 50^2) = sqrt(7500) ≈ 86.6 < 100
        assertTrue(obs.isCollision(50.0, 50.0, 50.0));
        // Distance in 3D: sqrt(60^2 + 60^2 + 60^2) = sqrt(10800) ≈ 103.9 > 100
        assertFalse(obs.isCollision(60.0, 60.0, 60.0));
    }
}
