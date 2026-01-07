package com.spiga.core;

import com.spiga.environment.Weather;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests pour VehiculeSurface et ActifMarin - couvrir les méthodes marine.
 */
public class ActifMarinExtendedTest {

    @Test
    public void testVehiculeSurfaceWeatherImpact() {
        VehiculeSurface navire = new VehiculeSurface("NAV-1", 0, 0);
        Weather wavy = new Weather(50, 0, 0, 1.0); // Max waves
        navire.demarrer();
        navire.setTarget(1000, 0, 0);

        double speed1 = navire.getSpeedMultiplier(wavy);
        assertTrue(speed1 < 1.0); // Waves should reduce speed
    }

    @Test
    public void testVehiculeSurfaceBatteryMultiplier() {
        VehiculeSurface navire = new VehiculeSurface("NAV-1", 0, 0);
        Weather wavy = new Weather(50, 0, 0, 1.0);

        double mult = navire.getBatteryMultiplier(wavy);
        assertTrue(mult >= 1.0); // Waves should increase consumption
    }

    @Test
    public void testVehiculeSousMarinWeatherAttenuation() {
        VehiculeSousMarin sub = new VehiculeSousMarin("SUB-1", 0, 0, -100);
        Weather stormy = new Weather(100, 0, 1.0, 1.0);

        // At -100m depth, wave impact should be attenuated
        double speed = sub.getSpeedEfficiency(stormy);
        assertTrue(speed > 0 && speed <= 1.0);
    }

    @Test
    public void testActifMarinDepthConstraints() {
        VehiculeSousMarin sub = new VehiculeSousMarin("SUB-1", 0, 0, -50);
        sub.setProfondeurMax(-10);
        assertEquals(-10.0, sub.getProfondeurMax(), 0.01);
    }

    @Test
    public void testVehiculeSurfaceDeplacer() {
        VehiculeSurface navire = new VehiculeSurface("NAV-1", 0, 0);
        navire.demarrer();
        navire.deplacer(500, 600, 100); // Z should be forced to 0

        assertEquals(0.0, navire.getTargetZ(), 0.01);
        assertEquals(500.0, navire.getTargetX(), 0.01);
        assertEquals(600.0, navire.getTargetY(), 0.01);
    }

    @Test
    public void testVehiculeSousMarinMovement() {
        VehiculeSousMarin sub = new VehiculeSousMarin("SUB-1", 0, 0, -50);
        sub.demarrer();
        sub.setTarget(500, 0, -50);

        double initialX = sub.getX();
        for (int i = 0; i < 50; i++) {
            sub.update(0.1, null);
        }

        assertTrue(sub.getX() > initialX);
    }

    @Test
    public void testSousMarinExplorationFullTest() {
        SousMarinExploration explo = new SousMarinExploration("EXPLO-1", 0, 0, -100);
        explo.demarrer();
        explo.setTarget(200, 0, -100);

        for (int i = 0; i < 50; i++) {
            explo.update(0.1, null);
        }

        assertTrue(explo.getX() > 0);
        assertEquals(-100.0, explo.getZ(), 1.0);
    }

    @Test
    public void testMarineSpeedWithCalm() {
        VehiculeSurface navire = new VehiculeSurface("NAV-1", 0, 0);
        Weather calm = new Weather(0, 0, 0, 0);

        assertEquals(1.0, navire.getSpeedMultiplier(calm), 0.01);
    }

    @Test
    public void testSubmarineSpeedWithWind() {
        VehiculeSousMarin sub = new VehiculeSousMarin("SUB-1", 0, 0, -100);
        Weather windy = new Weather(100, 0, 0, 0); // Wind but no waves

        // At depth, wind should have reduced effect
        double efficiency = sub.getSpeedEfficiency(windy);
        assertTrue(efficiency > 0.5);
    }
}
