package com.spiga.core;

import java.util.List;

public class SwarmValidator {

    public static boolean isPlacementValid(double x, double y, double z, List<ActifMobile> existingDrones) {
        for (ActifMobile drone : existingDrones) {
            double dist = Math.sqrt(
                    Math.pow(drone.getX() - x, 2) + Math.pow(drone.getY() - y, 2) + Math.pow(drone.getZ() - z, 2));
            if (dist < SimConfig.MIN_DISTANCE) {
                return false;
            }
        }
        return true;
    }
}
