package com.spiga.management;

import com.spiga.core.DroneReconnaissance;
import org.junit.jupiter.api.Test;
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
}
