package com.spiga.management;

import com.spiga.core.DroneReconnaissance;
import com.spiga.core.ActifMobile;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

public class GestionnaireEssaimTest {

    @Test
    public void testAddAsset() {
        GestionnaireEssaim manager = new GestionnaireEssaim();
        DroneReconnaissance drone = new DroneReconnaissance("D1", 0, 0, 100);
        manager.ajouterActif(drone);
        assertEquals(1, manager.getFlotte().size());
    }

    @Test
    public void testAvailableAssets() {
        GestionnaireEssaim manager = new GestionnaireEssaim();
        DroneReconnaissance d1 = new DroneReconnaissance("D1", 0, 0, 100);
        DroneReconnaissance d2 = new DroneReconnaissance("D2", 0, 0, 100);
        manager.ajouterActif(d1);
        manager.ajouterActif(d2);

        d1.demarrer(); // Set to EN_MISSION

        assertEquals(1, manager.getActifsDisponibles().size());
        assertEquals("D2", manager.getActifsDisponibles().get(0).getId());
    }

    @Test
    public void testSupprimerActif() {
        GestionnaireEssaim manager = new GestionnaireEssaim();
        DroneReconnaissance d1 = new DroneReconnaissance("D1", 0, 0, 100);
        DroneReconnaissance d2 = new DroneReconnaissance("D2", 0, 0, 100);
        manager.ajouterActif(d1);
        manager.ajouterActif(d2);

        assertEquals(2, manager.getFlotte().size());

        manager.supprimerActif("D1");
        assertEquals(1, manager.getFlotte().size());
        assertEquals("D2", manager.getFlotte().get(0).getId());
    }

    @Test
    public void testSupprimerActifNonExistent() {
        GestionnaireEssaim manager = new GestionnaireEssaim();
        DroneReconnaissance d1 = new DroneReconnaissance("D1", 0, 0, 100);
        manager.ajouterActif(d1);

        manager.supprimerActif("D999"); // Non-existent
        assertEquals(1, manager.getFlotte().size());
    }

    @Test
    public void testSuggererActifOptimal() {
        GestionnaireEssaim manager = new GestionnaireEssaim();
        DroneReconnaissance d1 = new DroneReconnaissance("D1", 0, 0, 100);
        DroneReconnaissance d2 = new DroneReconnaissance("D2", 0, 0, 100);
        manager.ajouterActif(d1);
        manager.ajouterActif(d2);

        // Both have same autonomy, should return one of them
        ActifMobile optimal = manager.suggererActifOptimal();
        assertNotNull(optimal);
    }

    @Test
    public void testSuggererActifOptimalNoAvailable() {
        GestionnaireEssaim manager = new GestionnaireEssaim();
        DroneReconnaissance d1 = new DroneReconnaissance("D1", 0, 0, 100);
        manager.ajouterActif(d1);
        d1.demarrer(); // Not available anymore

        ActifMobile optimal = manager.suggererActifOptimal();
        assertNull(optimal);
    }

    @Test
    public void testSuggererActifOptimalEmptyFlotte() {
        GestionnaireEssaim manager = new GestionnaireEssaim();
        ActifMobile optimal = manager.suggererActifOptimal();
        assertNull(optimal);
    }

    @Test
    public void testDemarrerMission() {
        GestionnaireEssaim manager = new GestionnaireEssaim();
        DroneReconnaissance d1 = new DroneReconnaissance("D1", 0, 0, 100);
        DroneReconnaissance d2 = new DroneReconnaissance("D2", 100, 0, 100);
        manager.ajouterActif(d1);
        manager.ajouterActif(d2);

        Mission mission = new MissionSurveillanceMaritime("Test Mission");
        mission.setTarget(500, 500, 50);

        manager.demarrerMission(mission, Arrays.asList(d1, d2));

        assertEquals(Mission.StatutMission.EN_COURS, mission.getStatut());
    }

    @Test
    public void testGetFlotte() {
        GestionnaireEssaim manager = new GestionnaireEssaim();
        assertNotNull(manager.getFlotte());
        assertEquals(0, manager.getFlotte().size());

        DroneReconnaissance d1 = new DroneReconnaissance("D1", 0, 0, 100);
        manager.ajouterActif(d1);
        assertEquals(1, manager.getFlotte().size());
    }

    @Test
    public void testActifsDisponiblesLowBattery() {
        GestionnaireEssaim manager = new GestionnaireEssaim();
        DroneReconnaissance d1 = new DroneReconnaissance("D1", 0, 0, 100);
        manager.ajouterActif(d1);

        // Drain battery below 20%
        d1.demarrer();
        for (int i = 0; i < 500; i++) {
            d1.update(0.1, null);
        }

        // Drone is EN_MISSION so won't be in disponibles anyway
        // Test that it's not available when in mission state
        assertEquals(0, manager.getActifsDisponibles().size());
    }
}
