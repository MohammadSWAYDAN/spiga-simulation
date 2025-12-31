package com.spiga.core;

public class SimConfig {
    // Distances en pixels (ou mètres selon l'échelle)
    public static final double MIN_DISTANCE = 15.0; // Distance min entre centres
    public static final double SAFETY_RADIUS = 20.0; // Rayon de la zone de sécurité personnelle
    public static final double HYSTERESIS = 5.0; // Marge pour éviter clignotement alerte

    // World Dimensions
    // World Dimensions
    public static final double WORLD_WIDTH = 2000.0;
    public static final double WORLD_HEIGHT = 2000.0;

    // Simulation Time Scale
    // 1.0 = Realtime (1s = 1s)
    // 30.0 = Very Fast (1s real = 30s simulated)
    // -> 2h Autonomy = 4 mins Real Time
    public static final double DEFAULT_TIME_SCALE = 300.0;
}
