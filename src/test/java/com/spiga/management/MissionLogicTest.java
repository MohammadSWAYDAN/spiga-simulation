package com.spiga.management;

import com.spiga.core.DroneReconnaissance;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

public class MissionLogicTest {

    @Test
    public void testMissionTimeout() {
        Mission m = new MissionSurveillanceMaritime("Test Timeout");
        m.setPlannedDurationSeconds(10);

        // Mock start at t=1000
        m.start(1000);
        assertEquals(Mission.StatutMission.EN_COURS, m.getStatut());

        // Tick at t=1005 (elapsed 5s) -> Should be running
        m.tick(1005);
        assertEquals(Mission.StatutMission.EN_COURS, m.getStatut());

        // Tick at t=1011 (elapsed 11s) -> Should fail
        m.tick(1011);
        assertEquals(Mission.StatutMission.ECHOUEE, m.getStatut());
    }

    @Test
    public void testCompletionAny() {
        Mission m = new MissionSurveillanceMaritime("Test Any");
        m.setCompletionRule(Mission.CompletionRule.ANY);
        m.setTarget(100, 100, 50);
        m.setPlannedDurationSeconds(60);

        // D1 arrived, D2 far
        DroneReconnaissance d1 = new DroneReconnaissance("D1", 100, 100, 50);
        DroneReconnaissance d2 = new DroneReconnaissance("D2", 0, 0, 50);

        m.assignActifs(Arrays.asList(d1, d2));
        m.start(0);

        m.tick(1);
        // ANY -> 1 arrived is enough
        assertEquals(Mission.StatutMission.TERMINEE, m.getStatut());
    }

    @Test
    public void testCompletionAll_Fail() {
        Mission m = new MissionSurveillanceMaritime("Test All Fail");
        m.setCompletionRule(Mission.CompletionRule.ALL);
        m.setTarget(100, 100, 50);
        m.setPlannedDurationSeconds(60);

        // D1 arrived, D2 far
        DroneReconnaissance d1 = new DroneReconnaissance("D1", 100, 100, 50);
        DroneReconnaissance d2 = new DroneReconnaissance("D2", 0, 0, 50);

        m.assignActifs(Arrays.asList(d1, d2));
        m.start(0);

        m.tick(1);
        // ALL -> 1/2 arrived -> Not done
        assertEquals(Mission.StatutMission.EN_COURS, m.getStatut());
    }

    @Test
    public void testCompletionAll_Success() {
        Mission m = new MissionSurveillanceMaritime("Test All Success");
        m.setCompletionRule(Mission.CompletionRule.ALL);
        m.setTarget(100, 100, 50);
        m.setPlannedDurationSeconds(60);

        // Both arrived
        DroneReconnaissance d1 = new DroneReconnaissance("D1", 100, 100, 50);
        DroneReconnaissance d2 = new DroneReconnaissance("D2", 100, 100, 50);

        m.assignActifs(Arrays.asList(d1, d2));
        m.start(0);

        m.tick(1);
        // ALL -> 2/2 arrived -> Done
        assertEquals(Mission.StatutMission.TERMINEE, m.getStatut());
    }
}
