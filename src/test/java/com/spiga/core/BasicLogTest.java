package com.spiga.core;

import com.spiga.management.GestionnaireEssaim;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BasicLogTest {

    @Test
    public void testSimulationServiceInitialization() {
        GestionnaireEssaim gestionnaire = new GestionnaireEssaim();
        SimulationService service = new SimulationService(gestionnaire);

        assertNotNull(service.getObstacles(), "Obstacles should be initialized");
        assertNotNull(service.getWeather(), "Weather should be initialized");

        // basic stepping
        // We cannot easily test the animation timer without JavaFX platform,
        // but we can test the update logic if we extract it or mock it.
        // For now, valid construction is a good smoke test for the Logger
        // initialization.
    }
}
