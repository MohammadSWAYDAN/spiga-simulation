package com.spiga.environment;

/**
 * Classe representant la Meteo.
 * 
 * Concepts :
 * 
 * - Encapsulation : Les attributs (vitesse vent, pluie) sont prives.
 * - Modelisation : Represente l'etat de l'environnement qui influence les
 * actifs (via getWeatherImpact).
 */
public class Weather {
    // Authorized Normalized Fields [0.0 - 1.0]
    private double windIntensity; // 0.0 = Calm, 1.0 = Storm (100 km/h arbitrary max)
    private double rainIntensity; // 0.0 = Dry, 1.0 = Heavy Rain
    private double waveIntensity; // 0.0 = Flat, 1.0 = High Waves (5m arbitrary max)

    private double windDirection; // degrees (keep as is)

    public Weather(double windSpeed, double windDirection, double rainIntensity) {
<<<<<<< HEAD
        this(windSpeed, windDirection, rainIntensity, 0.0);
    }

    public Weather(double windSpeed, double windDirection, double rainIntensity, double waveIntensity) {
        this.windDirection = windDirection;
        setWindSpeed(windSpeed); // Convert to intensity
        setRainIntensity(rainIntensity); // Convert/Clamp
        setWaveIntensity(waveIntensity);
=======
        this.windDirection = windDirection;
        setWindSpeed(windSpeed); // Convert to intensity
        setRainIntensity(rainIntensity); // Convert/Clamp
        this.waveIntensity = 0.0;
>>>>>>> 2e1c7d997378ffc2a62a0fdc8796641db0ce29fa
    }

    // --- Normalized Getters/Setters [0.0 - 1.0] ---

    public double getWindIntensity() {
        return windIntensity;
    }

    public void setWindIntensity(double v) {
        this.windIntensity = clamp(v);
    }

    public double getRainIntensity() {
        return rainIntensity;
    }

    public void setRainIntensity(double v) {
        this.rainIntensity = clamp(v);
    }

    public double getWaveIntensity() {
        return waveIntensity;
    }

    public void setWaveIntensity(double v) {
        this.waveIntensity = clamp(v);
    }

    // --- Backward Compatibility / Helper Methods ---

    public double getWindSpeed() {
        return windIntensity * 100.0; // Max 100 km/h
    }

    public void setWindSpeed(double kmh) {
        this.windIntensity = clamp(kmh / 100.0);
    }

    public double getSeaWaveHeight() {
        return waveIntensity * 5.0; // Max 5m
    }

    public void setSeaWaveHeight(double h) {
        this.waveIntensity = clamp(h / 5.0);
    }

    // Rain legacy (0-100 scale in UI, but often used as 0-100)
    // The previous code had rainIntensity as 0-100.
    // The NEW requirement says "rainIntensity" [0..1].
    // To handle legacy calls (check other files), we might need to check how it's
    // used.
    // MainController passes slider values (0-100).
    // Let's assume setRainIntensity(double) handles the normalized logic,
    // and we create a specific legacy setter if needed, OR we adapt the caller.
    // WAIT: The prompt says "MODÈLE ... Assure 3 champs normalisés [0..1]".
    // This implies I should change the internal storage.
    // I will update MainController's binding to divide by 100.

    public double getWindDirection() {
        return windDirection;
    }

    private double clamp(double v) {
        if (v < 0)
            return 0.0;
        if (v > 1)
            return 1.0;
        return v;
    }
}
