package com.spiga.management;

import com.spiga.core.DroneReconnaissance;
import com.spiga.core.DroneLogistique;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests pour Mission - couvrir toutes les branches restantes.
 */
public class MissionBranchTest {

    @Test
    public void testTickWhenNotEnCours() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.tick(1000); // Not started, should not do anything
        assertEquals(Mission.StatutMission.PLANIFIEE, m.getStatut());
    }

    @Test
    public void testTickWithNoAssets() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.start(0);
        m.tick(100);
        // Should continue without crash
        assertEquals(Mission.StatutMission.EN_COURS, m.getStatut());
    }

    @Test
    public void testTickWithAssetsNotArrived() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.setTarget(1000, 1000, 50);
        m.setPlannedDurationSeconds(1000);
        DroneReconnaissance d1 = new DroneReconnaissance("D1", 0, 0, 50);
        m.addActif(d1);
        m.start(0);
        m.tick(10); // Not enough time to arrive
        assertEquals(Mission.StatutMission.EN_COURS, m.getStatut());
    }

    @Test
    public void testTickANYWithOneArrived() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.setTarget(0, 0, 50);
        m.setPlannedDurationSeconds(1000);
        m.setCompletionRule(Mission.CompletionRule.ANY);
        DroneReconnaissance d1 = new DroneReconnaissance("D1", 0, 0, 50);
        DroneReconnaissance d2 = new DroneReconnaissance("D2", 500, 500, 50);
        m.addActif(d1);
        m.addActif(d2);
        m.start(0);
        m.tick(1);
        assertEquals(Mission.StatutMission.TERMINEE, m.getStatut());
    }

    @Test
    public void testTickALLWithAllArrived() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.setTarget(0, 0, 50);
        m.setPlannedDurationSeconds(1000);
        m.setCompletionRule(Mission.CompletionRule.ALL);
        DroneReconnaissance d1 = new DroneReconnaissance("D1", 0, 0, 50);
        DroneReconnaissance d2 = new DroneReconnaissance("D2", 0, 0, 50);
        m.addActif(d1);
        m.addActif(d2);
        m.start(0);
        m.tick(1);
        assertEquals(Mission.StatutMission.TERMINEE, m.getStatut());
    }

    @Test
    public void testResumeWhenNotPaused() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.start(0);
        m.resume(100); // Not paused
        assertEquals(Mission.StatutMission.EN_COURS, m.getStatut());
    }

    @Test
    public void testPauseWhenNotEnCours() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.pause(); // Not started
        assertEquals(Mission.StatutMission.PLANIFIEE, m.getStatut());
    }

    @Test
    public void testStartWhenNotPlanifiee() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.start(0);
        m.start(100); // Already started
        assertEquals(Mission.StatutMission.EN_COURS, m.getStatut());
    }

    @Test
    public void testRestartFromAnnulee() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.start(0);
        m.cancel();
        m.restart(100);
        assertEquals(Mission.StatutMission.EN_COURS, m.getStatut());
    }

    @Test
    public void testCancelWithAssetsAssigned() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.setTarget(100, 100, 50);
        DroneReconnaissance d1 = new DroneReconnaissance("D1", 0, 0, 50);
        d1.assignMission(m);
        m.addActif(d1);
        m.start(0);
        m.cancel("Test cancel");
        assertEquals(Mission.StatutMission.ANNULEE, m.getStatut());
    }

    @Test
    public void testGetElapsedSecondsEnCours() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.start(100);
        assertEquals(50, m.getElapsedSeconds(150));
    }

    @Test
    public void testGetElapsedSecondsTerminee() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.start(100);
        m.complete(200);
        assertEquals(100, m.getElapsedSeconds(300));
    }

    @Test
    public void testGetElapsedSecondsPlanifiee() {
        Mission m = new MissionSurveillanceMaritime("Test");
        assertEquals(0, m.getElapsedSeconds(100));
    }

    @Test
    public void testSetStatut() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.setStatut(Mission.StatutMission.EN_COURS);
        assertEquals(Mission.StatutMission.EN_COURS, m.getStatut());
    }

    @Test
    public void testSetObjectives() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.setObjectives("New objectives");
        assertEquals("New objectives", m.getObjectives());
    }

    @Test
    public void testGetActualTimes() {
        Mission m = new MissionSurveillanceMaritime("Test");
        m.start(100);
        m.complete(200);
        assertEquals(100, m.getActualStartTime());
        assertEquals(200, m.getActualEndTime());
    }

    @Test
    public void testGetAssignedAssets() {
        Mission m = new MissionSurveillanceMaritime("Test");
        assertNotNull(m.getAssignedAssets());
        assertEquals(0, m.getAssignedAssets().size());
    }
}
