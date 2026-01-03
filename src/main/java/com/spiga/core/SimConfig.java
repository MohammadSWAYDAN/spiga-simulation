package com.spiga.core;

/**
 * Configuration statique globale de la simulation.
 * <p>
 * Cette classe regroupe toutes les constantes, paramètres physiques et seuils
 * utilisés par le moteur de simulation. Elle permet un réglage centralisé du
 * comportement.
 * </p>
 */
public class SimConfig {

    // --- PARAMÈTRES UI / RENDU ---

    /** Distance minimale d'affichage entre centres (pixels/mètres). */
    public static final double MIN_DISTANCE = 15.0;
    /** Rayon de la zone de sécurité personnelle pour l'affichage. */
    public static final double SAFETY_RADIUS = 20.0;
    /** Marge d'hystérésis pour éviter le clignotement des alertes UI. */
    public static final double HYSTERESIS = 5.0;

    // --- DIMENSIONS DU MONDE ---

    /** Largeur totale du monde simulé en mètres. */
    public static final double WORLD_WIDTH = 2000.0;
    /** Hauteur (Profondeur Y) totale du monde simulé en mètres. */
    public static final double WORLD_HEIGHT = 2000.0;

    // --- GESTION DU TEMPS ---

    /**
     * Échelle de temps par défaut.
     * 1.0 = Temps réel.
     * 300.0 = Accéléré (1 sec réelle = 5 min simulées).
     */
    public static final double DEFAULT_TIME_SCALE = 300.0;

    // --- PHYSIQUE ET COLLISIONS ---

    /** Distance déclenchant une alerte de proximité entre actifs (mètres). */
    public static final double COLLISION_THRESHOLD = 25.0;
    /** Force de répulsion appliquée lors d'une séparation forcée. */
    public static final double PUSH_FORCE = 2.0;
    /** Rayon de détection des obstacles environnementaux (mètres). */
    public static final double OBSTACLE_DETECTION_RADIUS = 300.0;
    /** Facteur de puissance de la répulsion des obstacles (Loi en 1/d²). */
    public static final double OBSTACLE_FORCE_FACTOR = 25000.0;

    // --- CONTRAINTES AÉRIENNES ---

    /** Altitude minimale de vol pour les drones (mètres). */
    public static final double DRONE_MIN_ALTITUDE = 1.0;
    /** Plafond opérationnel max pour les drones (mètres). */
    public static final double DRONE_MAX_ALTITUDE = 150.0;
    /** Seuil d'altitude déclenchant l'alerte "Approche Mer". */
    public static final double SEA_APPROACH_THRESHOLD = 20.0;
    /** Altitude de sécurité automatique en cas d'approche mer. */
    public static final double MIN_HOVER_ALTITUDE = 2.0;

    // --- GESTION DES ZONES ---

    /** Marge de sécurité autour des zones restreintes pour le contournement. */
    public static final double ZONE_SAFETY_MARGIN = 100.0;
    /** Distance d'avertissement avant d'atteindre une zone restreinte. */
    public static final double ZONE_WARNING_DISTANCE = 50.0;

    // --- CONTRAINTES SOUS-MARINES ---

    /** Profondeur maximale (fond marin) autorisée (mètres, valeur négative). */
    public static final double SUB_MAX_DEPTH = -150.0;

    // --- LISSAGE ET EVITEMENT ---

    /**
     * Facteur de lissage de la vélocité (0.0 = Instantané, 1.0 = Figé). Valeur
     * usuelle: 0.1
     */
    public static final double VELOCITY_SMOOTHING = 0.1;
    /** Plafond de la force d'évitement pour éviter les mouvements violents. */
    public static final double AVOIDANCE_FORCE_CAP = 3.0;
    /** Durée d'une manœuvre d'évitement temporaire (secondes). */
    public static final double AVOIDANCE_DURATION = 2.0;
    /** Distance de séparation cible lors d'un conflit de cibles (swarm). */
    public static final double SEPARATION_DISTANCE = 30.0;
}
