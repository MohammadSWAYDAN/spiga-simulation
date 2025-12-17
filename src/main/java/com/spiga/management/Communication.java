package com.spiga.management;

import com.spiga.core.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Communication - Mission Communication Logic
 * Handles separate mission queues for Aerial and Marine assets.
 */
public class Communication {

    private List<Mission> aerialMissions;
    private List<Mission> marineMissions;
    private GestionnaireEssaim fleetManager;

    public Communication(GestionnaireEssaim fleetManager) {
        this.fleetManager = fleetManager;
        this.aerialMissions = new ArrayList<>();
        this.marineMissions = new ArrayList<>();
    }

    public void addMission(Mission mission, String type) {
        if ("AERIAL".equalsIgnoreCase(type)) {
            aerialMissions.add(mission);
            System.out.println("Comm: Added Aerial Mission - " + mission.getTitre());
        } else if ("MARINE".equalsIgnoreCase(type)) {
            marineMissions.add(mission);
            System.out.println("Comm: Added Marine Mission - " + mission.getTitre());
        } else {
            System.out.println("Comm: Unknown mission type " + type);
        }
    }

    public void handleMissions() {
        dispatchAerialMissions();
        dispatchMarineMissions();
    }

    private void dispatchAerialMissions() {
        if (aerialMissions.isEmpty())
            return;

        List<ActifMobile> availableDrones = fleetManager.getFlotte().stream()
                .filter(a -> a instanceof ActifAerien)
                .filter(a -> a.getState() == ActifMobile.AssetState.IDLE)
                .collect(Collectors.toList());

        for (ActifMobile drone : availableDrones) {
            if (aerialMissions.isEmpty())
                break;

            Mission mission = aerialMissions.get(0);

            // Specialized Dispatching
            boolean assigned = false;

            if (drone instanceof DroneReconnaissance && mission.getTitre().contains("Surveillance")) {
                drone.assignMission(mission);
                assigned = true;
            } else if (drone instanceof DroneLogistique && mission.getTitre().contains("Logistique")) {
                drone.assignMission(mission);
                assigned = true;
            }

            if (assigned) {
                aerialMissions.remove(0);
                System.out.println("Comm: Assigned " + mission.getTitre() + " to " + drone.getId());
            }
        }
    }

    private void dispatchMarineMissions() {
        if (marineMissions.isEmpty())
            return;

        List<ActifMobile> availableMarine = fleetManager.getFlotte().stream()
                .filter(a -> a instanceof ActifMarin)
                .filter(a -> a.getState() == ActifMobile.AssetState.IDLE)
                .collect(Collectors.toList());

        for (ActifMobile unit : availableMarine) {
            if (marineMissions.isEmpty())
                break;

            Mission mission = marineMissions.get(0);
            boolean assigned = false;

            if (unit instanceof VehiculeSurface && mission.getTitre().contains("Surface")) {
                unit.assignMission(mission);
                assigned = true;
            } else if (unit instanceof VehiculeSousMarin && mission.getTitre().contains("Underwater")) {
                unit.assignMission(mission);
                assigned = true;
            }

            if (assigned) {
                marineMissions.remove(0);
                System.out.println("Comm: Assigned " + mission.getTitre() + " to " + unit.getId());
            }
        }
    }
}
