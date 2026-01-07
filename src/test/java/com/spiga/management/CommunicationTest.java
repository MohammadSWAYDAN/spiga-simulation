package com.spiga.management;

import com.spiga.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour Communication (système de dispatch).
 */
public class CommunicationTest {

    private GestionnaireEssaim fleetManager;
    private Communication comm;

    @BeforeEach
    public void setUp() {
        fleetManager = new GestionnaireEssaim();
        comm = new Communication(fleetManager);
    }

    @Test
    public void testConstructor() {
        assertNotNull(comm);
    }

    @Test
    public void testAddAerialMission() {
        Mission mission = new MissionSurveillanceMaritime("Aerial Surveillance");
        comm.addMission(mission, "AERIAL");
        // No exception should be thrown
    }

    @Test
    public void testAddMarineMission() {
        Mission mission = new MissionSurveillanceMaritime("Marine Patrol");
        comm.addMission(mission, "MARINE");
        // No exception should be thrown
    }

    @Test
    public void testAddMissionUnknownType() {
        Mission mission = new MissionSurveillanceMaritime("Unknown Mission");
        comm.addMission(mission, "UNKNOWN");
        // Should print warning but not throw
    }

    @Test
    public void testAddMissionCaseInsensitive() {
        Mission mission1 = new MissionSurveillanceMaritime("Test 1");
        Mission mission2 = new MissionSurveillanceMaritime("Test 2");
        comm.addMission(mission1, "aerial");
        comm.addMission(mission2, "MARINE");
        // Both should work
    }

    @Test
    public void testHandleMissionsWithNoAssets() {
        Mission mission = new MissionSurveillanceMaritime("Test");
        comm.addMission(mission, "AERIAL");

        // Should not throw even with no available assets
        comm.handleMissions();
    }

    @Test
    public void testHandleMissionsDispatchesToDrone() {
        DroneReconnaissance drone = new DroneReconnaissance("RECON-1", 0, 0, 50);
        fleetManager.ajouterActif(drone);

        Mission mission = new MissionSurveillanceMaritime("Surveillance Test");
        comm.addMission(mission, "AERIAL");

        comm.handleMissions();
        // The mission should be dispatched if the drone matches
    }

    @Test
    public void testHandleMissionsDispatchesLogisticsDrone() {
        DroneLogistique drone = new DroneLogistique("LOG-1", 0, 0, 50);
        fleetManager.ajouterActif(drone);

        Mission mission = new MissionLogistique("Logistique Delivery");
        comm.addMission(mission, "AERIAL");

        comm.handleMissions();
        // Should dispatch to logistics drone
    }

    @Test
    public void testHandleMissionsDispatchesToSurfaceVessel() {
        VehiculeSurface navire = new VehiculeSurface("NAVIRE-1", 0, 0);
        fleetManager.ajouterActif(navire);

        Mission mission = new MissionSurveillanceMaritime("Surface Patrol");
        comm.addMission(mission, "MARINE");

        comm.handleMissions();
    }

    @Test
    public void testHandleMissionsDispatchesToSubmarine() {
        VehiculeSousMarin sub = new VehiculeSousMarin("SUB-1", 0, 0, -50);
        fleetManager.ajouterActif(sub);

        Mission mission = new MissionSurveillanceMaritime("Underwater Reconnaissance");
        comm.addMission(mission, "MARINE");

        comm.handleMissions();
    }

    @Test
    public void testMultipleMissionsQueued() {
        DroneReconnaissance drone1 = new DroneReconnaissance("RECON-1", 0, 0, 50);
        DroneReconnaissance drone2 = new DroneReconnaissance("RECON-2", 100, 0, 50);
        fleetManager.ajouterActif(drone1);
        fleetManager.ajouterActif(drone2);

        Mission mission1 = new MissionSurveillanceMaritime("Surveillance 1");
        Mission mission2 = new MissionSurveillanceMaritime("Surveillance 2");
        comm.addMission(mission1, "AERIAL");
        comm.addMission(mission2, "AERIAL");

        comm.handleMissions();
    }

    @Test
    public void testEmptyMissionQueues() {
        // Handle with no missions added
        comm.handleMissions();
        // Should not throw
    }

    @Test
    public void testMixedMissionTypes() {
        DroneReconnaissance drone = new DroneReconnaissance("RECON-1", 0, 0, 50);
        VehiculeSurface navire = new VehiculeSurface("NAVIRE-1", 0, 0);
        fleetManager.ajouterActif(drone);
        fleetManager.ajouterActif(navire);

        Mission aerialMission = new MissionSurveillanceMaritime("Aerial Surveillance");
        Mission marineMission = new MissionSurveillanceMaritime("Surface Patrol");
        comm.addMission(aerialMission, "AERIAL");
        comm.addMission(marineMission, "MARINE");

        comm.handleMissions();
    }
}
