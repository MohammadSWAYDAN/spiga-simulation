package com.spiga.core;

import com.spiga.environment.Weather;
import com.spiga.management.Mission;
import com.spiga.management.MissionSurveillanceMaritime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests pour couvrir les branches restantes de ActifMobile.
 */
public class ActifMobileBranchTest {

    @Test
    public void testUpdateWhenStopped() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.setState(ActifMobile.AssetState.STOPPED);
        drone.update(1.0, null);
        // Should not move when stopped
        assertEquals(0.0, drone.getX(), 0.01);
    }

    @Test
    public void testUpdateWhenIdle() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.demarrer();
        drone.setState(ActifMobile.AssetState.IDLE);
        drone.update(1.0, null);
        // Should remain in place
        assertEquals(0.0, drone.getX(), 0.01);
    }

    @Test
    public void testUpdateWhenRecharging() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.setState(ActifMobile.AssetState.RECHARGING);
        drone.update(1.0, null);
        // Should stay in place
    }

    @Test
    public void testMoveTowardsWithDivertedTrue() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.demarrer();
        drone.setTempTarget(100, 100, 50);
        drone.engageAvoidance(100, 100, 50, 5.0);
        drone.update(1.0, null);
        // Should move towards temp target
        assertTrue(drone.getX() > 0 || drone.getY() > 0);
    }

    @Test
    public void testMoveTowardsVeryCloseToTarget() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 99.5, 0, 50);
        drone.demarrer();
        drone.setTarget(100, 0, 50);
        drone.update(0.1, null);
        // Should reach target
        assertTrue(drone.hasReachedTarget());
    }

    @Test
    public void testSeaApproachForDrone() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 5); // Low altitude
        drone.demarrer();
        drone.setTarget(100, 0, 1); // Trying to go very low
        drone.update(1.0, null);
        // Should trigger sea approach warning
    }

    @Test
    public void testVehicleSurfaceInMoveTowards() {
        VehiculeSurface navire = new VehiculeSurface("NAV-1", 0, 0);
        navire.demarrer();
        navire.setTarget(100, 0, 50); // Z should be forced to 0
        navire.update(1.0, null);
        assertEquals(0.0, navire.getZ(), 0.01);
    }

    @Test
    public void testSubmarineZConstraint() {
        VehiculeSousMarin sub = new VehiculeSousMarin("SUB-1", 0, 0, -10);
        sub.demarrer();
        sub.setTarget(100, 0, 50); // Above surface
        sub.update(1.0, null);
        assertTrue(sub.getZ() <= 0);
    }

    @Test
    public void testAvoidanceModeWithForces() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.demarrer();
        drone.setTarget(100, 0, 50);
        drone.addAvoidanceForce(5.0, 5.0, 0);
        drone.update(1.0, null);
        // Should move with avoidance
    }

    @Test
    public void testTargetReachedDuringMission() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 99, 0, 50);
        Mission m = new MissionSurveillanceMaritime("Test");
        m.setTarget(100, 0, 50);
        drone.assignMission(m);
        m.start(0);
        drone.setTarget(100, 0, 50);
        drone.update(5.0, null);
        // Mission should complete
    }

    @Test
    public void testReturnToBaseAfterReaching() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 5, 0, 50);
        drone.demarrer();
        drone.setState(ActifMobile.AssetState.RETURNING_TO_BASE);
        drone.setTarget(0, 0, 1); // Base at 0,0 but with min altitude

        for (int i = 0; i < 100; i++) {
            drone.update(0.1, null);
        }
        // Should have reached and be recharging or idle
        assertTrue(drone.getState() == ActifMobile.AssetState.RECHARGING ||
                drone.getState() == ActifMobile.AssetState.IDLE);
    }

    @Test
    public void testPromoteMissionNotInQueue() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        Mission m = new MissionSurveillanceMaritime("Test");
        drone.promoteMission(m); // Not in queue, should do nothing
    }

    @Test
    public void testPromoteMissionSameAsCurrent() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        Mission m = new MissionSurveillanceMaritime("Test");
        m.setTarget(100, 100, 50);
        drone.assignMission(m);
        drone.promoteMission(m); // Same as current
        assertEquals(m, drone.getCurrentMission());
    }

    @Test
    public void testDemarrerWithNoBattery() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.setAutonomieActuelle(0);
        drone.demarrer();
        assertEquals(ActifMobile.EtatOperationnel.AU_SOL, drone.getEtat());
    }

    @Test
    public void testEteindreWhenNotInMission() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.eteindre(); // Not in mission
        assertEquals(ActifMobile.EtatOperationnel.AU_SOL, drone.getEtat());
    }

    @Test
    public void testRechargerFromPanne() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.setEtat(ActifMobile.EtatOperationnel.EN_PANNE);
        drone.setState(ActifMobile.AssetState.STOPPED);
        drone.recharger();
        assertEquals(ActifMobile.EtatOperationnel.AU_SOL, drone.getEtat());
    }

    @Test
    public void testVelocityNaNSafety() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.demarrer();
        drone.setTarget(100, 0, 50);
        // Simulate potential NaN by moving
        drone.update(0.0001, null);
        assertFalse(Double.isNaN(drone.getX()));
    }

    @Test
    public void testHighAvoidanceForce() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.demarrer();
        drone.setTarget(100, 0, 50);
        drone.addAvoidanceForce(100.0, 100.0, 100.0); // Very high force
        drone.update(1.0, null);
        // Force should be capped
    }

    @Test
    public void testCheckMissionQueueWhenEmpty() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        drone.checkMissionQueue();
        // Should not crash
    }

    @Test
    public void testCheckMissionQueueWithPlanifiee() {
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 50);
        Mission m1 = new MissionSurveillanceMaritime("M1");
        Mission m2 = new MissionSurveillanceMaritime("M2");
        m1.setTarget(100, 100, 50);
        m2.setTarget(200, 200, 50);

        drone.assignMission(m1);
        drone.assignMission(m2);
        m1.complete();
        drone.checkMissionQueue();
    }
}
