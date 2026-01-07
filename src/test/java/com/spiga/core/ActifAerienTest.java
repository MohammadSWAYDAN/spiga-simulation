package com.spiga.core;

import com.spiga.environment.Weather;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour ActifAerien (testé via DroneReconnaissance).
 */
public class ActifAerienTest {

    @Test
    public void testSpeedMultiplierNoWeather() {
        DroneReconnaissance drone = new DroneReconnaissance("TEST", 0, 0, 50);
        Weather calm = new Weather(0, 0, 0, 0);
        drone.demarrer();

        // With no weather effects, speed should be at 100%
        // Test by simulating movement
        double initialX = drone.getX();
        drone.setTarget(1000, 0, 50);
        drone.update(1.0, calm);

        // Should have moved at full speed
        assertTrue(drone.getX() > initialX);
    }

    @Test
    public void testSpeedMultiplierWithRain() {
        DroneReconnaissance drone = new DroneReconnaissance("TEST", 0, 0, 50);
        Weather rainy = new Weather(0, 0, 1.0, 0); // Full rain
        drone.demarrer();

        // Rain should reduce speed by up to 50%
        double initialX = drone.getX();
        drone.setTarget(1000, 0, 50);
        drone.update(1.0, rainy);

        double distanceWithRain = drone.getX() - initialX;

        // Reset and compare with calm weather
        DroneReconnaissance drone2 = new DroneReconnaissance("TEST2", 0, 0, 50);
        Weather calm = new Weather(0, 0, 0, 0);
        drone2.demarrer();
        drone2.setTarget(1000, 0, 50);
        drone2.update(1.0, calm);
        double distanceCalm = drone2.getX();

        // Distance with rain should be less
        assertTrue(distanceWithRain < distanceCalm);
    }

    @Test
    public void testSpeedMultiplierWithWind() {
        DroneReconnaissance drone = new DroneReconnaissance("TEST", 0, 0, 50);
        Weather windy = new Weather(100, 0, 0, 0); // Full wind (100 km/h)
        drone.demarrer();

        double initialX = drone.getX();
        drone.setTarget(1000, 0, 50);
        drone.update(1.0, windy);

        // Wind should also reduce speed
        assertTrue(drone.getX() > initialX);
    }

    @Test
    public void testBatteryMultiplierIncreasesWithWeather() {
        DroneReconnaissance drone = new DroneReconnaissance("TEST", 0, 0, 50);
        Weather stormy = new Weather(100, 0, 1.0, 0); // Full wind and rain
        drone.demarrer();
        double initialBattery = drone.getAutonomieActuelle();

        drone.setTarget(1000, 0, 50);
        drone.update(1.0, stormy);

        double batteryUsedStormy = initialBattery - drone.getAutonomieActuelle();

        // Compare with calm weather
        DroneReconnaissance drone2 = new DroneReconnaissance("TEST2", 0, 0, 50);
        Weather calm = new Weather(0, 0, 0, 0);
        drone2.demarrer();
        double initialBattery2 = drone2.getAutonomieActuelle();
        drone2.setTarget(1000, 0, 50);
        drone2.update(1.0, calm);
        double batteryUsedCalm = initialBattery2 - drone2.getAutonomieActuelle();

        // Stormy weather should consume more battery
        assertTrue(batteryUsedStormy > batteryUsedCalm);
    }

    @Test
    public void testClampPositionEnforcesMinAltitude() {
        DroneReconnaissance drone = new DroneReconnaissance("TEST", 100, 100, 0.5);
        drone.demarrer();

        // Position should be clamped to minimum 1m
        drone.update(0.01, null);
        assertTrue(drone.getZ() >= 1);
    }

    @Test
    public void testClampPositionEnforcesMaxAltitude() {
        DroneReconnaissance drone = new DroneReconnaissance("TEST", 100, 100, 200);
        drone.demarrer();

        // Position should be clamped to maximum 150m
        drone.update(0.01, null);
        assertTrue(drone.getZ() <= 150);
    }

    @Test
    public void testAltitudeGetters() {
        DroneReconnaissance drone = new DroneReconnaissance("TEST", 0, 0, 50);
        assertEquals(3000.0, drone.getAltitudeMax(), 0.01);
        assertEquals(0.0, drone.getAltitudeMin(), 0.01);
    }

    @Test
    public void testSetTargetWithValidAltitude() {
        DroneReconnaissance drone = new DroneReconnaissance("TEST", 0, 0, 50);
        drone.setTarget(100, 100, 75);
        assertEquals(75.0, drone.getTargetZ(), 0.01);
    }

    @Test
    public void testSetTargetWithTooLowAltitude() {
        DroneReconnaissance drone = new DroneReconnaissance("TEST", 0, 0, 50);
        drone.setTarget(100, 100, -10);
        assertEquals(1.0, drone.getTargetZ(), 0.01);
    }

    @Test
    public void testSetTargetWithTooHighAltitude() {
        DroneReconnaissance drone = new DroneReconnaissance("TEST", 0, 0, 50);
        drone.setTarget(100, 100, 1000);
        assertEquals(150.0, drone.getTargetZ(), 0.01);
    }
}
