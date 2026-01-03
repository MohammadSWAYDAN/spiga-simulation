package com.spiga.management;

import com.spiga.core.ActifMobile;
import com.spiga.core.ActifAerien;
import com.spiga.core.ActifMarin;
import com.spiga.core.DroneLogistique;
import com.spiga.core.DroneReconnaissance;
import com.spiga.core.VehiculeSousMarin;
import com.spiga.core.VehiculeSurface;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Système central de dispatch et de communication.
 * <p>
 * Agit comme une <strong>Tour de Contrôle</strong> ou un Centre de
 * Commandement.
 * Elle reçoit les demandes de missions catégorisées ("AERIAL", "MARINE") et
 * tente
 * de les allouer intelligemment aux actifs disponibles via le
 * {@link GestionnaireEssaim}.
 * </p>
 */
public class Communication {

    private List<Mission> aerialMissions;
    private List<Mission> marineMissions;
    private GestionnaireEssaim fleetManager;

    /**
     * Initialise le centre de communication.
     * 
     * @param fleetManager Référence vers le gestionnaire de flotte (Association).
     */
    public Communication(GestionnaireEssaim fleetManager) {
        this.fleetManager = fleetManager;
        this.aerialMissions = new ArrayList<>();
        this.marineMissions = new ArrayList<>();
    }

    /**
     * Ajoute une mission à la file d'attente globale.
     * 
     * @param mission La mission à planifier.
     * @param type    Catégorie ("AERIAL" ou "MARINE").
     */
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

    /**
     * Tente de distribuer les missions en attente aux actifs libres.
     * Appelée périodiquement par le moteur de simulation.
     */
    public void handleMissions() {
        dispatchAerialMissions();
        dispatchMarineMissions();
    }

    private void dispatchAerialMissions() {
        if (aerialMissions.isEmpty())
            return;

        // Filtre : Drones Libres
        List<ActifMobile> availableDrones = fleetManager.getFlotte().stream()
                .filter(a -> a instanceof ActifAerien)
                .filter(a -> a.getState() == ActifMobile.AssetState.IDLE)
                .collect(Collectors.toList());

        for (ActifMobile drone : availableDrones) {
            if (aerialMissions.isEmpty())
                break;

            Mission mission = aerialMissions.get(0);
            boolean assigned = false;

            // Logique de Matching simple
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
