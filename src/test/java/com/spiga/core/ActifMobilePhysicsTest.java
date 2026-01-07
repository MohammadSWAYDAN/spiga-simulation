package com.spiga.core;

import com.spiga.environment.Weather;
import com.spiga.management.Mission;
import com.spiga.management.MissionSurveillanceMaritime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests supplémentaires pour ActifMobile - couverture des méthodes critiques.
 */
public class ActifMobilePhysicsTest {

    @Test
    public void testMoveTowardsReachesTarget() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.demarrer();
        drone.setTarget(10, 0, 50);

        for (int i = 0; i < 100; i++) {
            drone.update(0.1, null);
        }

        assertTrue(drone.hasReachedTarget() || drone.getX() > 5);
    }

    @Test
    public void testMoveTowardsWithWeather() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        Weather stormy = new Weather(100, 0, 1.0, 1.0);
        drone.demarrer();
        drone.setTarget(100, 0, 50);

        double initialX = drone.getX();
        drone.update(1.0, stormy);

        assertTrue(drone.getX() > initialX);
    }

    @Test
    public void testEngageAvoidance() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.demarrer();
        drone.engageAvoidance(100, 100, 50, 2.0);

        assertEquals(ActifMobile.NavigationMode.AVOIDING, drone.getNavigationMode());
        assertEquals(100.0, drone.getTempTargetX(), 0.01);
        assertEquals(100.0, drone.getTempTargetY(), 0.01);
    }

    @Test
    public void testResetAvoidanceForce() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.addAvoidanceForce(10, 20, 5);
        drone.resetAvoidanceForce();
        // Should not throw and forces should be reset
    }

    @Test
    public void testAddAvoidanceForce() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.addAvoidanceForce(5, 10, 2);
        drone.addAvoidanceForce(5, 10, 2);
        // Cumulative force should be applied during movement
    }

    @Test
    public void testReturnToBase() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 500, 500, 50);
        drone.demarrer();
        drone.returnToBase();

        assertEquals(ActifMobile.AssetState.RETURNING_TO_BASE, drone.getState());
        assertEquals(0.0, drone.getTargetX(), 0.01);
        assertEquals(0.0, drone.getTargetY(), 0.01);
    }

    @Test
    public void testAssignMission() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        Mission mission = new MissionSurveillanceMaritime("Test Mission");
        mission.setTarget(200, 200, 50);

        drone.assignMission(mission);

        assertEquals(mission, drone.getCurrentMission());
        assertEquals(ActifMobile.AssetState.EXECUTING_MISSION, drone.getState());
    }

    @Test
    public void testMissionQueue() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        Mission m1 = new MissionSurveillanceMaritime("Mission 1");
        Mission m2 = new MissionSurveillanceMaritime("Mission 2");
        m1.setTarget(100, 100, 50);
        m2.setTarget(200, 200, 50);

        drone.assignMission(m1); // Immediate
        drone.assignMission(m2); // Queued

        assertEquals(1, drone.getMissionQueue().size());
    }

    @Test
    public void testCheckMissionQueue() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        Mission m1 = new MissionSurveillanceMaritime("Mission 1");
        Mission m2 = new MissionSurveillanceMaritime("Mission 2");
        m1.setTarget(100, 100, 50);
        m2.setTarget(200, 200, 50);

        drone.assignMission(m1);
        drone.assignMission(m2);

        m1.complete(); // Complete first mission
        drone.checkMissionQueue();

        assertEquals(m2, drone.getCurrentMission());
    }

    @Test
    public void testSettersAndGetters() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);

        drone.setX(100);
        drone.setY(200);
        drone.setZ(75);

        assertEquals(100.0, drone.getX(), 0.01);
        assertEquals(200.0, drone.getY(), 0.01);
        assertEquals(75.0, drone.getZ(), 0.01);
    }

    @Test
    public void testSetEtat() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.setEtat(ActifMobile.EtatOperationnel.EN_PANNE);
        assertEquals(ActifMobile.EtatOperationnel.EN_PANNE, drone.getEtat());
    }

    @Test
    public void testSelected() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        assertFalse(drone.isSelected());
        drone.setSelected(true);
        assertTrue(drone.isSelected());
    }

    @Test
    public void testSpeedModifier() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.setSpeedModifier(0.5);
        assertEquals(drone.getVitesseMax() * 0.5, drone.getVitesse(), 1.0);
    }

    @Test
    public void testCollisionWarningGetSet() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        assertNull(drone.getCollisionWarning());
        drone.setCollisionWarning("WARNING!");
        assertEquals("WARNING!", drone.getCollisionWarning());
    }

    @Test
    public void testGetVelocityZ() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        assertEquals(0.0, drone.getVelocityZ(), 0.01);
    }

    @Test
    public void testTempTargetGetters() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.setTempTarget(111, 222, 333);
        assertEquals(111.0, drone.getTempTargetX(), 0.01);
        assertEquals(222.0, drone.getTempTargetY(), 0.01);
        assertEquals(333.0, drone.getTempTargetZ(), 0.01);
    }

    @Test
    public void testSteeringBias() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        assertEquals(0.0, drone.getSteeringBias(), 0.01);
        drone.setSteeringBias(0.5);
        assertEquals(0.5, drone.getSteeringBias(), 0.01);
    }

    @Test
    public void testNavigationModeDefault() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        assertEquals(ActifMobile.NavigationMode.NORMAL, drone.getNavigationMode());
    }

    @Test
    public void testGetVitesse() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        assertEquals(120.0, drone.getVitesse(), 0.01);
    }

    @Test
    public void testCurrentMissionNull() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        assertNull(drone.getCurrentMission());
    }

    @Test
    public void testBatteryDepleted() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.demarrer();
        drone.setAutonomieActuelle(0);
        drone.setTarget(1000, 0, 50);
        drone.update(0.1, null);

        assertEquals(ActifMobile.EtatOperationnel.EN_PANNE, drone.getEtat());
        assertEquals(ActifMobile.AssetState.STOPPED, drone.getState());
    }

    @Test
    public void testLowBatteryReturnToBase() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 1000, 1000, 50);
        drone.demarrer();
        drone.setAutonomieActuelle(0.1); // Very low
        drone.setTarget(500, 500, 50);
        drone.update(0.1, null);

        // Should trigger return to base
        assertEquals(ActifMobile.AssetState.RETURNING_TO_BASE, drone.getState());
    }

    @Test
    public void testAvoidanceExpiry() throws InterruptedException {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.demarrer();
        drone.engageAvoidance(50, 50, 50, 0.001); // Very short duration

        Thread.sleep(10); // Wait for expiry
        drone.update(0.01, null);

        assertEquals(ActifMobile.NavigationMode.NORMAL, drone.getNavigationMode());
    }

    @Test
    public void testPromoteMission() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        Mission m1 = new MissionSurveillanceMaritime("Mission 1");
        Mission m2 = new MissionSurveillanceMaritime("Mission 2");
        m1.setTarget(100, 100, 50);
        m2.setTarget(200, 200, 50);

        drone.assignMission(m1);
        drone.assignMission(m2); // Queued

        drone.promoteMission(m2); // Should become active
        assertEquals(m2, drone.getCurrentMission());
    }

    @Test
    public void testFinalTargetClearing() {
        DroneLogistique drone = new DroneLogistique("LOG-1", 0, 0, 50);
        drone.demarrer();
        // Set a target that triggers waypoint
        drone.setTarget(100, 100, 50);

        for (int i = 0; i < 200; i++) {
            drone.update(0.1, null);
        }
        // Should complete without errors
    }
}
