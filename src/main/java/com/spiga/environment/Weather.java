package com.spiga.environment;

public class Weather {
    private double windSpeed; // km/h
    private double windDirection; // degrees
    private double rainIntensity; // 0-100

    public Weather(double windSpeed, double windDirection, double rainIntensity) {
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.rainIntensity = rainIntensity;
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
}
