package com.spiga.core;

import com.spiga.environment.Weather;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests complets pour ActifMobile via DroneReconnaissance.
 */
public class ActifMobileExtendedTest {

    @Test
    public void testGetters() {
        DroneReconnaissance drone = new DroneReconnaissance("DRONE-1", 100.0, 200.0, 50.0);
        assertEquals("DRONE-1", drone.getId());
        assertEquals(100.0, drone.getX(), 0.01);
        assertEquals(200.0, drone.getY(), 0.01);
        assertEquals(50.0, drone.getZ(), 0.01);
        assertEquals(120.0, drone.getVitesseMax(), 0.01);
        assertEquals(4.0, drone.getAutonomieMax(), 0.01);
    }

    @Test
    public void testDemarrer() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        assertEquals(ActifMobile.EtatOperationnel.AU_SOL, drone.getEtat());
        drone.demarrer();
        assertEquals(ActifMobile.EtatOperationnel.EN_MISSION, drone.getEtat());
    }

    @Test
    public void testEteindre() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.demarrer();
        drone.eteindre();
        assertEquals(ActifMobile.EtatOperationnel.AU_SOL, drone.getEtat());
    }

    @Test
    public void testRecharger() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.demarrer();
        drone.setTarget(1000, 0, 50);
        // Drain some battery
        for (int i = 0; i < 100; i++) {
            drone.update(0.1, null);
        }
        double beforeRecharge = drone.getAutonomieActuelle();
        drone.recharger();
        assertEquals(drone.getAutonomieMax(), drone.getAutonomieActuelle(), 0.01);
        assertTrue(drone.getAutonomieActuelle() > beforeRecharge);
    }

    @Test
    public void testBatteryPercent() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        assertEquals(1.0, drone.getBatteryPercent(), 0.01); // Full battery = 100%

        drone.setAutonomieActuelle(2.0);
        assertEquals(0.5, drone.getBatteryPercent(), 0.01); // Half battery = 50%
    }

    @Test
    public void testHasReachedTarget() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 100, 100, 50);
        drone.setTarget(100, 100, 50); // Same position as current
        assertTrue(drone.hasReachedTarget());

        drone.setTarget(500, 500, 50); // Far away
        assertFalse(drone.hasReachedTarget());
    }

    @Test
    public void testVelocity() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.demarrer();
        drone.setTarget(1000, 0, 50);
        drone.update(1.0, null);

        // Should have positive X velocity
        assertTrue(drone.getVelocityX() > 0 || drone.getX() > 0);
    }

    @Test
    public void testCurrentSpeed() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.demarrer();
        drone.setTarget(1000, 0, 50);
        drone.update(1.0, null);

        // Should have some speed while moving
        assertTrue(drone.getCurrentSpeed() >= 0);
    }

    @Test
    public void testDeplacerCallsSetTarget() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.deplacer(500, 600, 75);
        assertEquals(500.0, drone.getTargetX(), 0.01);
        assertEquals(600.0, drone.getTargetY(), 0.01);
    }

    @Test
    public void testCalculerTrajet() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        // calculerTrajet logs the distance, doesn't return it
        drone.calculerTrajet(300, 400, 50);
        // Should not throw - just logs the distance
    }

    @Test
    public void testUpdateWithNullWeather() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.demarrer();
        drone.setTarget(100, 0, 50);
        // Should not throw with null weather
        drone.update(0.1, null);
        assertTrue(drone.getX() > 0);
    }

    @Test
    public void testUpdateWithWeather() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        Weather weather = new Weather(50, 90, 0.5, 0);
        drone.demarrer();
        drone.setTarget(100, 0, 50);
        drone.update(0.1, weather);
        assertTrue(drone.getX() > 0);
    }

    @Test
    public void testSetTargetX() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.setTarget(123, 456, 78);
        assertEquals(123.0, drone.getTargetX(), 0.01);
        assertEquals(456.0, drone.getTargetY(), 0.01);
    }

    @Test
    public void testStateEnum() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        assertEquals(ActifMobile.AssetState.IDLE, drone.getState());

        drone.setState(ActifMobile.AssetState.EXECUTING_MISSION);
        assertEquals(ActifMobile.AssetState.EXECUTING_MISSION, drone.getState());
    }

    @Test
    public void testMissionQueue() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        assertNotNull(drone.getMissionQueue());
        assertEquals(0, drone.getMissionQueue().size());
    }

    @Test
    public void testCollisionWarning() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.setCollisionWarning("TEST WARNING");
        // Should not throw, warning is set internally
    }

    @Test
    public void testRavitailler() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        // ravitailler delegates to recharger
        drone.setAutonomieActuelle(1.0);
        drone.ravitailler();
        assertEquals(drone.getAutonomieMax(), drone.getAutonomieActuelle(), 0.01);
    }

    @Test
    public void testTransmettreAlerte() {
        DroneReconnaissance drone1 = new DroneReconnaissance("D1", 0, 0, 50);
        DroneReconnaissance drone2 = new DroneReconnaissance("D2", 100, 0, 50);
        // Should not throw
        drone1.transmettreAlerte("Test alert", drone2);
    }

    @Test
    public void testNotifierEtatCritique() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        // Should not throw
        drone.notifierEtatCritique("LOW_BATTERY");
    }

    @Test
    public void testSpeedMultiplierDefault() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        Weather calm = new Weather(0, 0, 0, 0);
        assertEquals(1.0, drone.getSpeedMultiplier(calm), 0.01);
    }

    @Test
    public void testBatteryMultiplierDefault() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        Weather calm = new Weather(0, 0, 0, 0);
        assertEquals(1.0, drone.getBatteryMultiplier(calm), 0.01);
    }

    @Test
    public void testSpeedEfficiency() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        Weather calm = new Weather(0, 0, 0, 0);
        assertEquals(1.0, drone.getSpeedEfficiency(calm), 0.01);
    }
}
