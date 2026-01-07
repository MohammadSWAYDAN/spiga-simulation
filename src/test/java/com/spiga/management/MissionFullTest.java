package com.spiga.management;

import com.spiga.core.DroneReconnaissance;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests complets pour le cycle de vie des missions.
 */
public class MissionFullTest {

    @Test
    public void testMissionCreation() {
        Mission m = new MissionSurveillanceMaritime("Test Mission");
        assertEquals("Test Mission", m.getTitre());
        assertEquals(Mission.StatutMission.PLANIFIEE, m.getStatut());
    }

    @Test
    public void testMissionStart() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.setTarget(100, 100, 50);
        m.start(1000);
        assertEquals(Mission.StatutMission.EN_COURS, m.getStatut());
    }

    @Test
    public void testMissionStartWithoutTime() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.setTarget(100, 100, 50);
        m.start();
        assertEquals(Mission.StatutMission.EN_COURS, m.getStatut());
    }

    @Test
    public void testMissionPause() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.setTarget(100, 100, 50);
        m.start(1000);
        m.pause();
        assertEquals(Mission.StatutMission.PAUSED, m.getStatut());
    }

    @Test
    public void testMissionResume() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.setTarget(100, 100, 50);
        m.start(1000);
        m.pause();
        m.resume(2000);
        assertEquals(Mission.StatutMission.EN_COURS, m.getStatut());
    }

    @Test
    public void testMissionRestart() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.setTarget(100, 100, 50);
        m.setPlannedDurationSeconds(10);
        m.start(0);
        m.tick(15); // Timeout -> ECHOUEE
        assertEquals(Mission.StatutMission.ECHOUEE, m.getStatut());

        m.restart(20);
        assertEquals(Mission.StatutMission.EN_COURS, m.getStatut());
    }

    @Test
    public void testMissionComplete() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.start(1000);
        m.complete(2000);
        assertEquals(Mission.StatutMission.TERMINEE, m.getStatut());
    }

    @Test
    public void testMissionCompleteWithoutTime() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.start(1000);
        m.complete();
        assertEquals(Mission.StatutMission.TERMINEE, m.getStatut());
    }

    @Test
    public void testMissionFail() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.start(1000);
        m.fail("Drone lost");
        assertEquals(Mission.StatutMission.ECHOUEE, m.getStatut());
    }

    @Test
    public void testMissionCancel() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.start(1000);
        m.cancel();
        assertEquals(Mission.StatutMission.ANNULEE, m.getStatut());
    }

    @Test
    public void testMissionCancelWithReason() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.start(1000);
        m.cancel("Weather too severe");
        assertEquals(Mission.StatutMission.ANNULEE, m.getStatut());
    }

    @Test
    public void testMissionHistory() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.setTarget(100, 100, 50);
        m.setPlannedDurationSeconds(10);
        m.start(0);
        m.complete(5);

        assertEquals(1, m.getRunCount());
        assertNotNull(m.getHistory());
    }

    @Test
    public void testMissionMultipleRuns() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.setTarget(100, 100, 50);
        m.setPlannedDurationSeconds(10);

        m.start(0);
        m.complete(5); // Run 1

        m.restart(10);
        m.complete(15); // Run 2

        assertEquals(2, m.getRunCount());
    }

    @Test
    public void testMissionGetters() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.setTarget(100, 200, 50);

        assertNotNull(m.getId());
        assertEquals("Test", m.getTitre());
        assertEquals(100.0, m.getTargetX(), 0.01);
        assertEquals(200.0, m.getTargetY(), 0.01);
        assertEquals(50.0, m.getTargetZ(), 0.01);
    }

    @Test
    public void testMissionType() {
        Mission surveillance = new MissionSurveillanceMaritime("Surveillance");
        Mission logistics = new MissionLogistique("Logistics");

        assertEquals(Mission.MissionType.SURVEILLANCE, surveillance.getType());
        assertEquals(Mission.MissionType.LOGISTICS, logistics.getType());
    }

    @Test
    public void testMissionIsTerminated() {
        Mission m = new MissionSurveillanceMaritime("Test");
        assertFalse(m.isTerminated());

        m.start(0);
        assertFalse(m.isTerminated());

        m.complete();
        assertTrue(m.isTerminated());
    }

    @Test
    public void testMissionCopy() throws InterruptedException {
        MissionSurveillanceMaritime original = new MissionSurveillanceMaritime("Original");
        original.setTarget(100, 200, 50);

        Thread.sleep(2); // Ensure different millisecond for ID generation
        Mission copy = original.copy();

        assertEquals(original.getTitre(), copy.getTitre());
        assertEquals(original.getTargetX(), copy.getTargetX(), 0.01);
        assertEquals(original.getTargetY(), copy.getTargetY(), 0.01);
        assertEquals(original.getTargetZ(), copy.getTargetZ(), 0.01);

        // IDs should be different after sleep
        assertNotEquals(original.getId(), copy.getId());
    }

    @Test
    public void testMissionLogistiqueCopy() {
        MissionLogistique original = new MissionLogistique("Delivery");
        original.setTarget(500, 600, 75);

        Mission copy = original.copy();

        assertEquals("Delivery", copy.getTitre());
        assertEquals(500.0, copy.getTargetX(), 0.01);
    }

    @Test
    public void testMissionAssignActifs() {
        Mission m = new MissionSurveillanceMaritime("Test");
        DroneReconnaissance d1 = new DroneReconnaissance("D1", 0, 0, 50);
        DroneReconnaissance d2 = new DroneReconnaissance("D2", 100, 0, 50);

        m.assignActifs(Arrays.asList(d1, d2));
        // Should not throw
    }

    @Test
    public void testMissionAddActif() {
        Mission m = new MissionSurveillanceMaritime("Test");
        DroneReconnaissance d1 = new DroneReconnaissance("D1", 0, 0, 50);

        m.addActif(d1);
        // Should not throw
    }

    @Test
    public void testMissionObjectivesAndResults() {
        Mission m = new MissionSurveillanceMaritime("Test");
        assertNotNull(m.getObjectives());
        // Results is null until mission completes - this is expected behavior
        // assertNotNull(m.getResults()); // Would fail before completion

        m.start(0);
        m.complete(100);
        assertNotNull(m.getResults()); // Now results should be set
    }

    @Test
    public void testMissionCompletionRules() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.setCompletionRule(Mission.CompletionRule.ANY);
        // Should not throw

        m.setCompletionRule(Mission.CompletionRule.ALL);
        // Should not throw
    }

    @Test
    public void testMissionPlannedDuration() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.setPlannedDurationSeconds(60);
        assertEquals(60, m.getPlannedDurationSeconds());
    }

    @Test
    public void testGetCurrentRun() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.setTarget(100, 100, 50);
        m.start(0);

        assertNotNull(m.getCurrentRun());
    }
}
