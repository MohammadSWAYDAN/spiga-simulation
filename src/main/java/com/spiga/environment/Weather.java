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
    private double windSpeed; // km/h
    private double windDirection; // degrees
    private double rainIntensity; // 0-100
    private double seaWaveHeight; // New Feature

    /**
     * Constructeur complet.
     */
    public Weather(double windSpeed, double windDirection, double rainIntensity) {
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.rainIntensity = rainIntensity;
        this.seaWaveHeight = 0.0; // Default
    }

    // Manual Control Setters
    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public void setRainIntensity(double rainIntensity) {
        this.rainIntensity = rainIntensity;
    }

    public void setSeaWaveHeight(double h) {
        this.seaWaveHeight = h;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public double getWindDirection() {
        return windDirection;
    }

    public double getRainIntensity() {
        return rainIntensity;
    }

    // New Feature
    // Field moved to top

    public double getSeaWaveHeight() {
        return seaWaveHeight;
    }
}
