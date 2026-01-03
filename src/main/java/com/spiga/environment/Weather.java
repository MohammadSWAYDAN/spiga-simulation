package com.spiga.environment;

/**
 * Classe représentant les conditions météorologiques globales de la simulation.
 * <p>
 * <strong>Encapsulation et Modélisation :</strong>
 * Cette classe centralise les paramètres environnementaux (Vent, Pluie, Vagues)
 * et expose des méthodes normalisées (0.0 à 1.0) pour que les actifs puissent
 * calculer leur impact (vitesse réduite, surconsommation).
 * </p>
 */
public class Weather {
    // Authorized Normalized Fields [0.0 - 1.0]

    /** Intensité du vent (0.0 = Calme, 1.0 = Tempête max ~100 km/h). */
    private double windIntensity;
    /** Intensité de la pluie (0.0 = Sec, 1.0 = Pluie torrentielle). */
    private double rainIntensity;
    /** Intensité des vagues (0.0 = Mer calme, 1.0 = Vagues max ~5m). */
    private double waveIntensity;

    /** Direction du vent en degrés (0-360). */
    private double windDirection;

    /**
     * Constructeur simplifié (sans vagues).
     * 
     * @param windSpeed     Vitesse vent brute (km/h).
     * @param windDirection Direction vent (degrés).
     * @param rainIntensity Intensité pluie (0-100 ou 0-1).
     */
    public Weather(double windSpeed, double windDirection, double rainIntensity) {
        this(windSpeed, windDirection, rainIntensity, 0.0);
    }

    /**
     * Constructeur complet.
     * 
     * @param windSpeed     Vitesse vent brute (km/h).
     * @param windDirection Direction vent (degrés).
     * @param rainIntensity Intensité pluie (0-100 ou 0-1).
     * @param waveIntensity Hauteur vagues brute (mètres).
     */
    public Weather(double windSpeed, double windDirection, double rainIntensity, double waveIntensity) {
        this.windDirection = windDirection;
        setWindSpeed(windSpeed); // Convert to intensity
        setRainIntensity(rainIntensity); // Convert/Clamp
        setWaveIntensity(waveIntensity);
    }

    // --- Normalized Getters/Setters [0.0 - 1.0] ---

    /**
     * Retourne l'intensité du vent normalisée.
     * 
     * @return Valeur entre 0.0 et 1.0.
     */
    public double getWindIntensity() {
        return windIntensity;
    }

    public void setWindIntensity(double v) {
        this.windIntensity = clamp(v);
    }

    /**
     * Retourne l'intensité de la pluie normalisée.
     * 
     * @return Valeur entre 0.0 et 1.0.
     */
    public double getRainIntensity() {
        return rainIntensity;
    }

    public void setRainIntensity(double v) {
        this.rainIntensity = clamp(v);
    }

    /**
     * Retourne l'intensité des vagues normalisée.
     * 
     * @return Valeur entre 0.0 et 1.0.
     */
    public double getWaveIntensity() {
        return waveIntensity;
    }

    public void setWaveIntensity(double v) {
        this.waveIntensity = clamp(v);
    }

    // --- Backward Compatibility / Helper Methods ---

    /**
     * Retourne la vitesse du vent estimée en km/h.
     * 
     * @return Intensité * 100.
     */
    public double getWindSpeed() {
        return windIntensity * 100.0; // Max 100 km/h
    }

    /**
     * Définit le vent via une vitesse en km/h.
     * 
     * @param kmh Vitesse en km/h (sera normalisée sur 100).
     */
    public void setWindSpeed(double kmh) {
        this.windIntensity = clamp(kmh / 100.0);
    }

    /**
     * Retourne la hauteur des vagues estimée en mètres.
     * 
     * @return Intensité * 5.
     */
    public double getSeaWaveHeight() {
        return waveIntensity * 5.0; // Max 5m
    }

    /**
     * Définit les vagues via une hauteur en mètres.
     * 
     * @param h Hauteur en mètres (sera normalisée sur 5).
     */
    public void setSeaWaveHeight(double h) {
        this.waveIntensity = clamp(h / 5.0);
    }

    public double getWindDirection() {
        return windDirection;
    }

    /**
     * Utilitaire de bornage [0.0, 1.0].
     */
    private double clamp(double v) {
        if (v < 0)
            return 0.0;
        if (v > 1)
            return 1.0;
        return v;
    }
}
