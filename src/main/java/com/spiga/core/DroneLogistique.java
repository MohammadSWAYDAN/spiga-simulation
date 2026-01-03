package com.spiga.core;

import com.spiga.environment.RestrictedZone;

/**
 * Classe concrète représentant un drone de transport logistique.
 * <p>
 * Ce drone est spécialisé pour le transport de charge utile (médicaments,
 * pièces, etc.).
 * Il se caractérise par une vitesse modérée (60 km/h) et une autonomie élevée
 * (8h).
 * Il implémente des règles strictes de sécurité, notamment l'interdiction de
 * survol des zones restreintes.
 * </p>
 * <p>
 * <strong>Spécificités :</strong>
 * <ul>
 * <li>Capacité d'emport limitée par {@code chargeUtileMax}.</li>
 * <li>Algorithmes de contournement automatique des zones interdites.</li>
 * <li>Consommation d'énergie proportionnelle à la charge transportée.</li>
 * </ul>
 * </p>
 */
public class DroneLogistique extends ActifAerien {

    /** Charge utile maximale supportée en kg. */
    private double chargeUtileMax;
    /** Charge actuellement transportée en kg. */
    private double chargeActuelle;

    /**
     * Constructeur standard.
     * Initialise le drone avec une vitesse de 60 km/h et une autonomie de 8h.
     *
     * @param id       Identifiant unique.
     * @param x        Position X.
     * @param y        Position Y.
     * @param altitude Altitude Z.
     */
    public DroneLogistique(String id, double x, double y, double altitude) {
        super(id, x, y, altitude, 60.0, 8.0); // 60 km/h, 8h autonomie
        this.chargeUtileMax = 50.0; // 50kg
        this.chargeActuelle = 0.0;
    }

    /**
     * Calcule la consommation énergétique en tenant compte de la charge.
     * <p>
     * Formule : Consommation de base (2.4) + surcoût par kg de charge (0.05).
     * </p>
     *
     * @return Consommation en "unités par heure".
     */
    @Override
    public double getConsommation() {
        // FAST BATTERY DEMO (Calibrated for 10x time scale)
        double baseParams = 2.4;
        return baseParams + (chargeActuelle * 0.05);
    }

    /**
     * Définit la cible avec validation avancée des zones interdites.
     * <p>
     * Vérifie si la cible est dans une zone interdite (Rejet) ou si le trajet la
     * traverse
     * (Calcul automatique de contournement).
     * </p>
     *
     * @param tx Coordonnée X cible.
     * @param ty Coordonnée Y cible.
     * @param tz Coordonnée Z cible.
     */
    @Override
    public void setTarget(double tx, double ty, double tz) {
        // 1. Check Zones
        for (RestrictedZone zone : ActifMobile.KNOWN_ZONES) {

            // A. Check if TARGET is inside Zone
            // "refus si target dedans"
            // Note: isInside checks Z too. Logistics blocked at any height?
            // SimulationService line 258 said "Logistics blocked REGARDLESS of height".
            // So we check 2D inside + ignore Z check if simplified, or rely on
            // zone.isInside logic?
            // Zone.isInside checks Z [minZ, maxZ].
            // If the Zone is 0-120m, and Drone is at 150m, is it OK?
            // Req said "Logistics + zone interdite". Usually Logistics fly low.
            // I'll assume strict 2D Check for Logistics as implied by SimService logic.

            double dx = tx - zone.getX();
            double dy = ty - zone.getY();
            double distSq = dx * dx + dy * dy;
            double radiusSq = zone.getRadius() * zone.getRadius();

            if (distSq < radiusSq) {
                System.out.println("⛔ " + id + ": Rejet commande. Cible dans Zone Interdite " + zone.getId());
                // Set warning instead of throwing exception - let caller handle UI
                setCollisionWarning("ZONE_VIOLATION: Cible dans zone interdite!");
                return; // Reject the command silently
            }

            // B. Check Path Intersection (Contournement)
            // "si target est hors zone mais la ligne droite traverse"
            // Simple segment-circle intersection check (With Robust Padding)
            if (intersects(x, y, tx, ty, zone.getX(), zone.getY(), zone.getRadius() + 40.0)) {
                System.out.println("⚠️ " + id + ": Trajet traverse Zone Interdite! Calcul contournement...");

                double distToCenter = Math.sqrt(Math.pow(x - zone.getX(), 2) + Math.pow(y - zone.getY(), 2));
                double safetyRadius = zone.getRadius() + 100.0;

                double wayX, wayY;

                // --- ROBUST TANGENT STRATEGY ---
                // "Déterminer un point tangent/offset sur le bord"

                if (distToCenter <= safetyRadius + 1.0) {
                    // Case A: Creating Tangent from "ON/INSIDE" Safety Margin
                    // Fallback to Slide/Orbit (Perpendicular to Radius) to get out/around
                    // Current Angle
                    double angC = Math.atan2(y - zone.getY(), x - zone.getX());
                    // Target Angle
                    double angT = Math.atan2(ty - zone.getY(), tx - zone.getX());
                    // Direction
                    double diff = angT - angC;
                    while (diff <= -Math.PI)
                        diff += 2 * Math.PI;
                    while (diff > Math.PI)
                        diff -= 2 * Math.PI;

                    double step = 0.5 * Math.signum(diff); // 30 deg step
                    if (Math.abs(diff) < 0.5)
                        step = diff;

                    double angNext = angC + step;
                    wayX = zone.getX() + Math.cos(angNext) * safetyRadius;
                    wayY = zone.getY() + Math.sin(angNext) * safetyRadius;

                } else {
                    // Case B: Creating Tangent from DISTANCE (Straight Line Waypoint)
                    // Math: Angle of tangent line from P to Circle(C, r) is asin(r/d) offset from
                    // PC.

                    double D = distToCenter;
                    double R = safetyRadius;
                    // Angle from Drone P to Center C
                    double angPC = Math.atan2(zone.getY() - y, zone.getX() - x);

                    // Offset angle alpha
                    double alpha = Math.asin(R / D);

                    // Two Tangent Directions: +alpha and -alpha relative to PC
                    double t1_ang = angPC + alpha;
                    double t2_ang = angPC - alpha;

                    // Distance to Reach Tangent Point
                    double L = Math.sqrt(D * D - R * R);

                    // Two Candidate Waypoints
                    double w1x = x + Math.cos(t1_ang) * L;
                    double w1y = y + Math.sin(t1_ang) * L;

                    double w2x = x + Math.cos(t2_ang) * L;
                    double w2y = y + Math.sin(t2_ang) * L;

                    // Choose the one closer to Final Target (Euclidean SQ)
                    double d1sq = Math.pow(w1x - tx, 2) + Math.pow(w1y - ty, 2);
                    double d2sq = Math.pow(w2x - tx, 2) + Math.pow(w2y - ty, 2);

                    if (d1sq < d2sq) {
                        wayX = w1x;
                        wayY = w1y;
                    } else {
                        wayX = w2x;
                        wayY = w2y;
                    }
                }

                // Set Waypoint + Final Target
                this.finalTargetX = tx;
                this.finalTargetY = ty;
                this.finalTargetZ = tz;

                super.setTarget(wayX, wayY, tz);
                return;
            }
        }

        // No violation
        super.setTarget(tx, ty, tz);
    }

    // Helper: Circle-Segment Intersection Check
    private boolean intersects(double ax, double ay, double bx, double by, double cx, double cy, double r) {
        // Compute distance from C to segment AB
        double a_c_x = cx - ax;
        double a_c_y = cy - ay;
        double a_b_x = bx - ax;
        double a_b_y = by - ay;
        double len_sq = a_b_x * a_b_x + a_b_y * a_b_y;

        if (len_sq == 0)
            return false;

        double dot = a_c_x * a_b_x + a_c_y * a_b_y;
        double t = Math.max(0, Math.min(1, dot / len_sq));

        double proj_x = ax + t * a_b_x;
        double proj_y = ay + t * a_b_y;

        double dist_sq = (proj_x - cx) * (proj_x - cx) + (proj_y - cy) * (proj_y - cy);

        // Intersects if closest point is inside radius
        return dist_sq < (r * r);
    }

    @Override
    public void deplacer(double tx, double ty, double tz) {
        setTarget(tx, ty, tz);
    }

    /**
     * Ajoute une charge à transporter.
     * 
     * @param poids Poids en kg à ajouter.
     */
    public void charger(double poids) {
        if (chargeActuelle + poids <= chargeUtileMax) {
            chargeActuelle += poids;
        }
    }

    /**
     * Décharge complètement le drone.
     */
    public void decharger() {
        chargeActuelle = 0;
    }

    public double getChargeUtileMax() {
        return chargeUtileMax;
    }

    public double getChargeActuelle() {
        return chargeActuelle;
    }
}
