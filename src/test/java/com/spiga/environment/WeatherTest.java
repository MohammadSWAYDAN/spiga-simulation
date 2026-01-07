package com.spiga.environment;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe Weather.
 */
public class WeatherTest {

    @Test
    public void testConstructorSimple() {
        Weather w = new Weather(50.0, 180.0, 0.5);
        assertEquals(0.5, w.getWindIntensity(), 0.01); // 50 km/h -> 0.5
        assertEquals(180.0, w.getWindDirection(), 0.01);
        assertEquals(0.5, w.getRainIntensity(), 0.01);
        assertEquals(0.0, w.getWaveIntensity(), 0.01);
    }

    @Test
    public void testConstructorFull() {
        Weather w = new Weather(100.0, 90.0, 0.8, 0.6);
        assertEquals(1.0, w.getWindIntensity(), 0.01); // 100 km/h -> 1.0
        assertEquals(90.0, w.getWindDirection(), 0.01);
        assertEquals(0.8, w.getRainIntensity(), 0.01);
        assertEquals(0.6, w.getWaveIntensity(), 0.01);
    }

    @Test
    public void testWindIntensityClamping() {
        Weather w = new Weather(0, 0, 0);

        // Test lower bound
        w.setWindIntensity(-0.5);
        assertEquals(0.0, w.getWindIntensity(), 0.01);

        // Test upper bound
        w.setWindIntensity(1.5);
        assertEquals(1.0, w.getWindIntensity(), 0.01);

        // Test normal value
        w.setWindIntensity(0.7);
        assertEquals(0.7, w.getWindIntensity(), 0.01);
    }

    @Test
    public void testRainIntensityClamping() {
        Weather w = new Weather(0, 0, 0);

        w.setRainIntensity(-1.0);
        assertEquals(0.0, w.getRainIntensity(), 0.01);

        w.setRainIntensity(2.0);
        assertEquals(1.0, w.getRainIntensity(), 0.01);

        w.setRainIntensity(0.3);
        assertEquals(0.3, w.getRainIntensity(), 0.01);
    }

    @Test
    public void testWaveIntensityClamping() {
        Weather w = new Weather(0, 0, 0, 0);

        w.setWaveIntensity(-0.1);
        assertEquals(0.0, w.getWaveIntensity(), 0.01);

        w.setWaveIntensity(1.2);
        assertEquals(1.0, w.getWaveIntensity(), 0.01);

        w.setWaveIntensity(0.5);
        assertEquals(0.5, w.getWaveIntensity(), 0.01);
    }

    @Test
    public void testWindSpeedConversion() {
        Weather w = new Weather(0, 0, 0);

        w.setWindSpeed(75.0);
        assertEquals(0.75, w.getWindIntensity(), 0.01);
        assertEquals(75.0, w.getWindSpeed(), 0.01);

        // Test max clamping
        w.setWindSpeed(150.0);
        assertEquals(1.0, w.getWindIntensity(), 0.01);
        assertEquals(100.0, w.getWindSpeed(), 0.01);
    }

    @Test
    public void testSeaWaveHeightConversion() {
        Weather w = new Weather(0, 0, 0, 0);

        w.setSeaWaveHeight(2.5);
        assertEquals(0.5, w.getWaveIntensity(), 0.01);
        assertEquals(2.5, w.getSeaWaveHeight(), 0.01);

        // Test max clamping
        w.setSeaWaveHeight(10.0);
        assertEquals(1.0, w.getWaveIntensity(), 0.01);
        assertEquals(5.0, w.getSeaWaveHeight(), 0.01);
    }

    @Test
    public void testZeroWeather() {
        Weather w = new Weather(0, 0, 0, 0);
        assertEquals(0.0, w.getWindIntensity(), 0.01);
        assertEquals(0.0, w.getRainIntensity(), 0.01);
        assertEquals(0.0, w.getWaveIntensity(), 0.01);
        assertEquals(0.0, w.getWindSpeed(), 0.01);
        assertEquals(0.0, w.getSeaWaveHeight(), 0.01);
    }

    @Test
    public void testExtremeWeather() {
        Weather w = new Weather(100.0, 360.0, 1.0, 1.0);
        assertEquals(1.0, w.getWindIntensity(), 0.01);
        assertEquals(1.0, w.getRainIntensity(), 0.01);
        assertEquals(1.0, w.getWaveIntensity(), 0.01);
        assertEquals(100.0, w.getWindSpeed(), 0.01);
        assertEquals(5.0, w.getSeaWaveHeight(), 0.01);
    }
}
