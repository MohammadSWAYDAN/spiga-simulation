package com.spiga.core;

/**
 * Classe abstraite intermédiaire représentant un actif aérien (Drone, etc.).
 * <p>
 * Cette classe spécialise {@link ActifMobile} pour le domaine aérien en
 * introduisant
 * des contraintes d'altitude (min/max) et une sensibilité spécifique à la météo
 * (vent, pluie)
 * pour le calcul de vitesse et de consommation.
 * </p>
 * <p>
 * <strong>Responsabilités :</strong>
 * <ul>
 * <li>Gérer les plages d'altitude valides (entre {@code altitudeMin} et
 * {@code altitudeMax}).</li>
 * <li>Implémenter l'impact aérodynamique de la météo.</li>
 * </ul>
 * </p>
 *
 * @see DroneReconnaissance
 * @see DroneLogistique
 */
public abstract class ActifAerien extends ActifMobile {

    /** Altitude maximale autorisée en mètres. */
    protected double altitudeMax;
    /** Altitude minimale autorisée en mètres. */
    protected double altitudeMin;

    /**
     * Constructeur pour un actif aérien.
     *
     * @param id           Identifiant unique.
     * @param x            Position X initiale.
     * @param y            Position Y initiale.
     * @param altitude     Altitude initiale Z.
     * @param vitesseMax   Vitesse max en m/s (ou km/h).
     * @param autonomieMax Autonomie max en heures.
     */
    public ActifAerien(String id, double x, double y, double altitude, double vitesseMax, double autonomieMax) {
        super(id, x, y, altitude, vitesseMax, autonomieMax);
        this.altitudeMax = 5000;
        this.altitudeMin = 0;
    }

    /**
     * Calcule le facteur de réduction de vitesse dû à la météo.
     * <p>
     * Modèle : La vitesse est réduite par la pluie (50% max) et le vent (20% max).
     * Le facteur est borné entre 0.4 (40%) et 1.0 (100%).
     * </p>
     *
     * @param w Objet Météo courant.
     * @return Facteur multiplicateur (0.4 à 1.0).
     */
    @Override
    protected double getSpeedMultiplier(com.spiga.environment.Weather w) {
        // speed = vmax * clamp(1 - 0.5*rain - 0.2*wind, 0.4, 1)
        double rain = w.getRainIntensity();
        double wind = w.getWindIntensity();
        double factor = 1.0 - (0.5 * rain) - (0.2 * wind);
        return Math.max(0.4, Math.min(1.0, factor));
    }

    /**
     * Legacy method for speed calculation.
     * 
     * @deprecated Use {@link #getSpeedMultiplier(com.spiga.environment.Weather)}
     *             instead.
     */
    @Override
    @Deprecated
    protected double getSpeedEfficiency(com.spiga.environment.Weather w) {
        double efficiency = super.getSpeedEfficiency(w);

        if (w.getRainIntensity() > 0) {
            double rainPenalty = (w.getRainIntensity() / 100.0) * 0.5; // Max 0.5
            efficiency -= rainPenalty;
        }

        if (efficiency < 0.1)
            efficiency = 0.1;

        return efficiency;
    }

    /**
     * Calcule l'impact de la météo sur la consommation de batterie.
     * <p>
     * Modèle : La consommation augmente avec la pluie (+60% max) et le vent (+30%
     * max)
     * car les moteurs doivent compenser la résistance.
     * </p>
     *
     * @param w Objet Météo courant.
     * @return Facteur multiplicateur (>= 1.0).
     */
    @Override
    protected double getBatteryMultiplier(com.spiga.environment.Weather w) {
        // batteryMult = 1 + 0.6*rain + 0.3*wind
        double rain = w.getRainIntensity();
        double wind = w.getWindIntensity();
        return 1.0 + (0.6 * rain) + (0.3 * wind);
    }

    /**
     * Applique les contraintes d'altitude strictes [1m, 150m].
     * (Valeurs codées en dur pour la démo, devraient être paramétrables).
     */
    @Override
    protected void clampPosition() {
        if (z < 1)
            z = 1;
        if (z > 150)
            z = 150;
    }

    /**
     * Définit la cible avec validation de l'altitude.
     * <p>
     * Si la cible Z est hors limites, elle est clampée et une alerte est générée.
     * </p>
     *
     * @param x Cible X.
     * @param y Cible Y.
     * @param z Cible Z.
     */
    @Override
    public void setTarget(double x, double y, double z) {
        double clampedZ = z;
        if (clampedZ < 1) {
            System.out.println("⚠️ " + id + ": Rejet cible Z=" + clampedZ + " (Sous l'eau/Sol). Force à 1m.");
            clampedZ = 1;
        }
        if (clampedZ > 150) {
            System.out.println("⚠️ " + id + ": Rejet cible Z=" + clampedZ + " (Trop haut). Force à 150m.");
            clampedZ = 150;
            // Visible Alert for Log Status
            setCollisionWarning("PLAFOND ATTEINT (150m)");
        }
        super.setTarget(x, y, clampedZ);
    }

    @Override
    public void deplacer(double targetX, double targetY, double targetZ) {
        setTarget(targetX, targetY, targetZ);
    }

    public double getAltitudeMax() {
        return altitudeMax;
    }

    public double getAltitudeMin() {
        return altitudeMin;
    }
}
