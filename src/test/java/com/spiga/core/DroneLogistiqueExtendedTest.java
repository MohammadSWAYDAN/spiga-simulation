package com.spiga.core;

import com.spiga.environment.Weather;
import com.spiga.environment.RestrictedZone;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests supplémentaires pour DroneLogistique - zones restreintes et
 * contournement.
 */
public class DroneLogistiqueExtendedTest {

    @Test
    public void testSetTargetInRestrictedZone() {
        // Add a restricted zone
        RestrictedZone zone = new RestrictedZone("ZONE-TEST", 500, 500, 100, 0, 200);
        ActifMobile.KNOWN_ZONES.add(zone);

        try {
            DroneLogistique drone = new DroneLogistique("LOG-1", 0, 0, 50);
            drone.setTarget(500, 500, 50); // Center of zone

            // Target should be rejected or warning set
            assertNotNull(drone.getCollisionWarning());
        } finally {
            ActifMobile.KNOWN_ZONES.remove(zone);
        }
    }

    @Test
    public void testSetTargetWithPathIntersection() {
        RestrictedZone zone = new RestrictedZone("ZONE-TEST", 500, 0, 100, 0, 200);
        ActifMobile.KNOWN_ZONES.add(zone);

        try {
            DroneLogistique drone = new DroneLogistique("LOG-1", 0, 0, 50);
            drone.setTarget(1000, 0, 50); // Path goes through zone

            // Should calculate waypoint around zone
            // Target may not be 1000,0 anymore
        } finally {
            ActifMobile.KNOWN_ZONES.remove(zone);
        }
    }

    @Test
    public void testFinalTargetNavigation() {
        DroneLogistique drone = new DroneLogistique("LOG-1", 0, 0, 50);
        drone.demarrer();
        drone.setTarget(100, 100, 50);

        for (int i = 0; i < 300; i++) {
            drone.update(0.1, null);
        }

        // Should eventually reach near target
        double dist = Math.sqrt(
                Math.pow(drone.getX() - 100, 2) +
                        Math.pow(drone.getY() - 100, 2));
        assertTrue(dist < 50);
    }

    @Test
    public void testChargerSequence() {
        DroneLogistique drone = new DroneLogistique("LOG-1", 0, 0, 50);

        drone.charger(10);
        assertEquals(10.0, drone.getChargeActuelle(), 0.01);

        drone.charger(15);
        assertEquals(25.0, drone.getChargeActuelle(), 0.01);

        drone.charger(30); // Would exceed 50, so ignored
        assertEquals(25.0, drone.getChargeActuelle(), 0.01);

        drone.decharger();
        assertEquals(0.0, drone.getChargeActuelle(), 0.01);
    }

    @Test
    public void testConsommationVariation() {
        DroneLogistique drone = new DroneLogistique("LOG-1", 0, 0, 50);

        double baseConsumption = drone.getConsommation();
        assertEquals(2.4, baseConsumption, 0.01);

        drone.charger(40);
        double loadedConsumption = drone.getConsommation();
        assertEquals(4.4, loadedConsumption, 0.01); // 2.4 + 0.05 * 40
    }
}
