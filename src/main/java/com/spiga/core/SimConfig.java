package com.spiga.core;

public class SimConfig {
    // Distances en pixels (ou mètres selon l'échelle)
    public static final double MIN_DISTANCE = 15.0; // Distance min entre centres
    public static final double SAFETY_RADIUS = 20.0; // Rayon de la zone de sécurité personnelle
    public static final double HYSTERESIS = 5.0; // Marge pour éviter clignotement alerte

    // World Dimensions
    public static final double WORLD_WIDTH = 2000.0;
    public static final double WORLD_HEIGHT = 2000.0;

    // Simulation Time Scale
    // 1.0 = Realtime (1s = 1s)
    // 30.0 = Very Fast (1s real = 30s simulated)
    // -> 2h Autonomy = 4 mins Real Time
    public static final double DEFAULT_TIME_SCALE = 300.0;

    // Physics Constants
    public static final double COLLISION_THRESHOLD = 25.0; // Distance to trigger avoidance
    public static final double PUSH_FORCE = 2.0; // Force applied when separating assets
    public static final double OBSTACLE_DETECTION_RADIUS = 300.0; // Range to "feel" obstacles
    public static final double OBSTACLE_FORCE_FACTOR = 25000.0; // Repulsion strength

    // Drone Altitude Constraints
    public static final double DRONE_MIN_ALTITUDE = 1.0;
    public static final double DRONE_MAX_ALTITUDE = 150.0;
    public static final double SEA_APPROACH_THRESHOLD = 20.0;
    public static final double MIN_HOVER_ALTITUDE = 2.0;

    // Zone Avoidance
    public static final double ZONE_SAFETY_MARGIN = 100.0; // Extra margin around zones
    public static final double ZONE_WARNING_DISTANCE = 50.0; // Distance to show warning

    // Submarine Depth Constraints
    public static final double SUB_MAX_DEPTH = -150.0;

    // Velocity Smoothing (for smooth acceleration/deceleration)
    public static final double VELOCITY_SMOOTHING = 0.1; // 0.0 = instant, 1.0 = no change (0.1 = smooth)
    public static final double AVOIDANCE_FORCE_CAP = 3.0; // Max avoidance force magnitude (was 15.0)
    public static final double AVOIDANCE_DURATION = 2.0; // Seconds (was 5.0)
    public static final double SEPARATION_DISTANCE = 30.0; // Meters (was 50.0)
}
