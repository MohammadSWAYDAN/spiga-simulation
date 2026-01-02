package com.spiga;

import com.spiga.core.*;
import com.spiga.management.*;
import com.spiga.environment.*;
import javafx.application.Platform;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

/**
 * MainTestCLI
 * 
 * Version Console pour validation Logique Métier.
 * Scénarios :
 * 1. Mission + Déplacement manuel + Relance
 * 2. Batterie et Recharge
 * 3. Contraintes Z et Zones Interdites (Validation Contraintes)
 * 4. Zone Interdite (Zone + Logistique Avoidance)
 * 5. Météo et Impact
 */
public class MainTestCLI {

    private static GestionnaireEssaim gestionnaire;
    private static SimulationService service;
    private static Scanner scanner;
    private static boolean isJavaFxInitialized = false;

    public static void main(String[] args) {
        System.out.println("INITIALISATION DE L'ENVIRONNEMENT DE TEST...");

        // 1. Init JavaFX Toolkit (nécessaire pour AnimationTimer de SimulationService)
        try {
            Platform.startup(() -> {
            });
            isJavaFxInitialized = true;
        } catch (IllegalStateException e) {
            isJavaFxInitialized = true;
        } catch (Throwable e) {
            System.err.println("ATTENTION: JavaFX non disponible.");
        }

        // 2. Setup Services
        try {
            gestionnaire = new GestionnaireEssaim();
            service = new SimulationService(gestionnaire);
            service.setTimeScale(1.0);
        } catch (Exception e) {
            System.err.println("ERREUR FATALE: Impossible d'initier la simulation.");
            e.printStackTrace();
            return;
        }

        scanner = new Scanner(System.in);

        // 3. Boucle principale
        boolean running = true;
        while (running) {
            afficherMenuPrincipal();
            System.out.print("Choix : ");
            String input = scanner.nextLine();

            try {
                int choix = Integer.parseInt(input);
                if (choix == 6) {
                    running = false;
                    System.out.println("Fermeture du CLI.");
                } else {
                    lancerScenario(choix);
                }
            } catch (NumberFormatException e) {
                System.out.println(">> Entrée invalide !");
            }

            try {
                Thread.sleep(500);
            } catch (Exception e) {
            }
        }

        if (isJavaFxInitialized)
            Platform.exit();
        System.exit(0);
    }

    private static void afficherMenuPrincipal() {
        System.out.println("\n====================================");
        System.out.println(" SPIGA - VALIDATION LOGIQUE MÉTIER");
        System.out.println("====================================");
        System.out.println("1) SCÉNARIO 1 : Mission + Déplacement Manuel + Relance");
        System.out.println("2) SCÉNARIO 2 : Batterie et Recharge");
        System.out.println("3) SCÉNARIO 3 : Contraintes physiques (Z) et alertes");
        System.out.println("4) SCÉNARIO 4 : Zones interdites (Validation Comportement)");
        System.out.println("5) SCÉNARIO 5 : Météo (Impact Vitesse/Conso)");
        System.out.println("6) Quitter");
        System.out.println("====================================");
    }

    private static void resetSimulation() {
        gestionnaire.getFlotte().clear();
        service.reset();
        service.getRestrictedZones().clear();
        ActifMobile.KNOWN_ZONES.clear();
        service.getWeather().setWindSpeed(0);
        service.getWeather().setRainIntensity(0);
        service.getWeather().setWaveIntensity(0);
        System.out.println(">> Simulation Reset (Flotte vide, Météo reset).");
    }

    private interface SimHook {
        void tick(double time, SimulationService service);
    }

    private static void lancerScenario(int choix) {
        resetSimulation();
        long dureeSimuSeconds = 20;
        SimHook hook = (t, s) -> {
        };

        switch (choix) {
            case 1:
                hook = scenario1_MissionManuelRelance();
                dureeSimuSeconds = 25;
                break;
            case 2:
                hook = scenario2_BatterieRecharge();
                dureeSimuSeconds = 15;
                break;
            case 3:
                hook = scenario3_ContraintesPhysiques();
                dureeSimuSeconds = 8;
                break;
            case 4:
                hook = scenario4_ZonesInterdites();
                dureeSimuSeconds = 20;
                break;
            case 5:
                hook = scenario5_Meteo();
                dureeSimuSeconds = 15;
                break;
            default:
                System.out.println("Choix inconnu.");
                return;
        }

        System.out.println("\n--- DÉBUT DE LA SIMULATION (" + dureeSimuSeconds + "s simulées) ---\n");
        executerBoucle(dureeSimuSeconds, hook);
        System.out.println("\n--- FIN DE SCÉNARIO ---\n");
        System.out.println("Appuyez sur Entrée pour revenir au menu...");
        scanner.nextLine();
    }

    private static void executerBoucle(long dureeSimuSeconds, SimHook hook) {
        long nanoTime = 0;
        long stepNano = 16_666_667; // ~16ms
        double dtSeconds = stepNano / 1e9;
        long totalSteps = (dureeSimuSeconds * 1_000_000_000L) / stepNano;
        long stepsPerLog = 60; // 1 sec

        service.handle(nanoTime); // Init

        for (long i = 0; i <= totalSteps; i++) {
            nanoTime += stepNano;
            service.handle(nanoTime);

            hook.tick(i * dtSeconds, service);

            if (i % stepsPerLog == 0) {
                afficherLogsFlotte(i * dtSeconds);
            }
        }
    }

    private static void afficherLogsFlotte(double time) {
        String msgTime = String.format("t=%04.1fs", time);
        for (ActifMobile a : gestionnaire.getFlotte()) {
            StringBuilder sb = new StringBuilder();
            sb.append(msgTime).append(" | ");
            sb.append(String.format("%-10s", a.getId())).append(" | ");
            sb.append(String.format("Pos(%03.0f, %03.0f, %03.0f)", a.getX(), a.getY(), a.getZ())).append(" | ");
            sb.append(String.format("Bat:%03.0f%%", a.getBatteryPercent() * 100)).append(" | ");

            String etat = a.getState().toString();
            if (a.getCurrentMission() != null) {
                etat += " (M:" + a.getCurrentMission().getStatut() + ")";
            }
            sb.append(etat);

            if (a.getCollisionWarning() != null) {
                sb.append(" | ⚠️ ").append(a.getCollisionWarning());
            }
            System.out.println(sb.toString());
        }
    }

    // ==================================================================================
    // IMPLEMENTATION DES SCÉNARIOS
    // ==================================================================================

    // ----------------------------------------------------------------------------------
    // SCÉNARIO 1 : Mission + Déplacement manuel + Relance
    // ----------------------------------------------------------------------------------
    private static SimHook scenario1_MissionManuelRelance() {
        System.out.println(">> [S1] INIT: Création Drone + Mission PLANIFIEE vers (500,500,100)");

        DroneReconnaissance d1 = new DroneReconnaissance("Drone-01", 0, 0, 100);
        d1.demarrer();
        gestionnaire.ajouterActif(d1);

        MissionSurveillanceMaritime mission = new MissionSurveillanceMaritime("Mission Alpha");
        mission.setTarget(500, 500, 100);
        d1.assignMission(mission);

        System.out.println(">> [S1] ACTION: Lancement de la mission...");
        mission.start(System.currentTimeMillis() / 1000);

        // State wrapper to allow modification inside lambda
        class ScenarioState {
            boolean manualMoveDone = false;
            boolean relaunchDone = false;
        }
        ScenarioState state = new ScenarioState();

        return (t, s) -> {
            // T=10s : Déplacement Manuel (Une seule fois)
            if (t >= 10.0 && !state.manualMoveDone) {
                state.manualMoveDone = true;
                System.out.println("\n>> [S1] INTERRUPTION: Déplacement MANUEL vers (800, 0, 100)");
                System.out.println("   (Drone détourné de sa mission)");
                d1.setTarget(800, 0, 100);
            }

            // T=18s : Relance Mission (Une seule fois)
            if (t >= 18.0 && !state.relaunchDone) {
                state.relaunchDone = true;
                System.out.println("\n>> [S1] ACTION: Relance de la mission (Assignation forcée)");

                // Si la mission est toujours marquée "EN_COURS" mais que le drone fait autre
                // chose
                if (mission.getStatut() == Mission.StatutMission.EN_COURS && d1.getCurrentMission() == mission) {
                    System.out.println("   -> Force Return to Mission Target");
                    d1.setTarget(mission.getTargetX(), mission.getTargetY(), mission.getTargetZ());
                    d1.setState(ActifMobile.AssetState.EXECUTING_MISSION);
                } else {
                    // Cas classique (Pause ou autre)
                    d1.assignMission(mission);
                    if (mission.getStatut() == Mission.StatutMission.PAUSED) {
                        mission.resume(System.currentTimeMillis() / 1000);
                    }
                }
            }
        };
    }

    // ----------------------------------------------------------------------------------
    // SCÉNARIO 2 : Batterie et Recharge
    // ----------------------------------------------------------------------------------
    private static SimHook scenario2_BatterieRecharge() {
        System.out.println(">> [S2] INIT: Drone avec Batterie 100%.");
        DroneReconnaissance d1 = new DroneReconnaissance("Drone-Bat", 0, 0, 100);
        d1.demarrer();
        gestionnaire.ajouterActif(d1);

        // Local state used to ensure events are triggered only once
        class ScenarioState {
            boolean batteryDrained = false;
            boolean alertShown = false;
            boolean rechargeOrdered = false;
        }
        ScenarioState state = new ScenarioState();

        return (t, s) -> {
            if (t >= 5.0 && !state.batteryDrained) {
                state.batteryDrained = true;
                System.out.println("\n>> [S2] SIMULATION: Batterie drainée à 5% (Test Alerte)...");
                double max = d1.getAutonomieMax();
                d1.setAutonomieActuelle(max * 0.05);
            }

            // Check for Low Battery Alert / Smart Return
            if (!state.alertShown && d1.getState() == ActifMobile.AssetState.RETURNING_TO_BASE) {
                state.alertShown = true;
                System.out.println(">> [S2] ALERTE DÉTECTÉE: Niveau Critique -> Retour Base Automatique !");
            }

            if (t >= 10.0 && !state.rechargeOrdered) {
                state.rechargeOrdered = true;
                System.out
                        .println("\n>> [S2] ACTION: Ordre de Recharge (Retour Base / Recharge Instantanée pour test)");
                d1.recharger();
                System.out.println("   -> Batterie recharge déclenchée.");
            }
        };
    }

    // ----------------------------------------------------------------------------------
    // SCÉNARIO 3 : Contraintes physiques et zones interdites (Z et Asset Types)
    // ----------------------------------------------------------------------------------
    // ----------------------------------------------------------------------------------
    // SCÉNARIO 3 : Contraintes physiques et zones interdites (Z et Asset Types)
    // ----------------------------------------------------------------------------------
    // ----------------------------------------------------------------------------------
    // SCÉNARIO 3 : Contraintes physiques et zones interdites (Z et Asset Types)
    // ----------------------------------------------------------------------------------
    private static SimHook scenario3_ContraintesPhysiques() {
        System.out.println("\n--- SCÉNARIO 3 : VALIDATION CONTRAINTES ---");
        System.out.println("1 - Drone Reconnaissance : Cible Z=200 -> Force à 150m (Clamped)");
        System.out.println("2 - Drone Logistique     : Cible Z=200 -> Force à 150m (Clamped)");
        System.out.println("3 - Bateau (Surface)     : Cible Z!=0  -> Rejet (-1 -> 0)");
        System.out.println("4 - Sous-Marin (-50m)    : Cible Z=-200 -> Bloque à -150m");
        System.out.print(">> Choix : ");

        String subChoiceStr = scanner.nextLine();
        int subChoice = 1;
        try {
            subChoice = Integer.parseInt(subChoiceStr);
        } catch (NumberFormatException e) {
            System.out.println("Choix invalide, défaut 1.");
        }

        final int choice = subChoice;

        class ScenarioState {
            boolean test1Done = false;
        }
        ScenarioState state = new ScenarioState();

        return (t, s) -> {
            // Trigger logic ONCE at t=0.5s
            if (t >= 0.5 && !state.test1Done) {
                state.test1Done = true; // Mark as done to prevent duplicates
                switch (choice) {
                    case 1:
                        System.out.println("\n>> [S3-1] TEST RECON: Start(0,0,100) -> Target(..., 200)");
                        DroneReconnaissance recon = new DroneReconnaissance("Recon-1", 0, 0, 100);
                        recon.demarrer();
                        gestionnaire.ajouterActif(recon);
                        // ActifAerien clamps and prints warning
                        recon.setTarget(100, 100, 200);
                        break;
                    case 2:
                        System.out.println("\n>> [S3-2] TEST LOGISTIQUE: Start(0,0,100) -> Target(..., 200)");
                        DroneLogistique log = new DroneLogistique("Log-1", 0, 0, 100);
                        log.demarrer();
                        gestionnaire.ajouterActif(log);
                        log.setTarget(100, 100, 200);
                        break;
                    case 3:
                        System.out.println("\n>> [S3-3] TEST BATEAU: Start(0,0,0) -> Target(..., 50)");
                        VehiculeSurface boat = new VehiculeSurface("Boat-1", 0, 0);
                        boat.demarrer();
                        gestionnaire.ajouterActif(boat);
                        // VehiculeSurface logic should clamp/reject
                        boat.setTarget(100, 100, 50);
                        break;
                    case 4:
                        System.out.println("\n>> [S3-4] TEST SOUS-MARIN: Start(0,0,-50) -> Target(..., -200)");
                        SousMarinExploration sub = new SousMarinExploration("Sub-1", 0, 0, -50);
                        sub.demarrer();
                        gestionnaire.ajouterActif(sub);
                        // VehiculeSousMarin logic checks minDepth (-150 for Exploration)
                        sub.setTarget(100, 100, -200);
                        break;
                    default:
                        System.out.println("Sous-choix inconnu.");
                }
            }
        };
    }

    // ----------------------------------------------------------------------------------
    // SCÉNARIO 4 : Définir Zone Interdite
    // ----------------------------------------------------------------------------------
    // ----------------------------------------------------------------------------------
    // SCÉNARIO 4 : Définir Zone Interdite
    // ----------------------------------------------------------------------------------
    private static SimHook scenario4_ZonesInterdites() {
        System.out.println(">> [S4] INIT: Définition Zone Interdite");
        // Creation Zone à (1000, 1000) rayon 200
        RestrictedZone zone = new RestrictedZone("Zone-Test", 1000, 1000, 200, 0, 500);
        service.getRestrictedZones().add(zone);
        ActifMobile.KNOWN_ZONES.add(zone);

        System.out.println(
                "   -> Zone: " + zone.getId() + " à (" + zone.getX() + "," + zone.getY() + ") R=" + zone.getRadius());

        // 1. Recon (Autorisé)
        DroneReconnaissance recon = new DroneReconnaissance("Recon-Zone", 700, 1000, 100);
        recon.demarrer();
        gestionnaire.ajouterActif(recon);
        System.out.println(">> [S4] ACTION: Recon envoyé DANS la zone (1000,1000). Autorisé.");
        recon.setTarget(1000, 1000, 100);

        // 2. Log (Refus/Alerte) - Tentative Traversée
        DroneLogistique log = new DroneLogistique("Log-Zone", 600, 1000, 100);
        log.demarrer();
        gestionnaire.ajouterActif(log);
        System.out.println(">> [S4] ACTION: Logistique envoyé DANS la zone. Alerte/Refus attendu.");
        log.setTarget(1000, 1000, 100);

        // 3. Log (Contournement) - Target proche
        DroneLogistique logAv = new DroneLogistique("Log-Avoid", 700, 800, 100);
        logAv.demarrer();
        gestionnaire.ajouterActif(logAv);
        System.out.println(">> [S4] ACTION: Logistique envoyé PRÈS de la zone (Frontière)... Alerte Proximité ?");
        // Trajectoire qui rase la zone : (700, 780) -> (1000, 780). Zone (1000,1000)
        // R=200 -> Bord sud = 800.
        // Si on passe à Y=780, on est à 20m de la zone.
        logAv.setTarget(1000, 780, 100);

        // State for logging
        class ScenarioState {
            boolean crossingAttempted = false;
        }
        ScenarioState state = new ScenarioState();

        return (t, s) -> {
            // Monitor alerts
            if (t > 1.0 && t < 1.2) {
                System.out.println(">> [S4] Vérification des statuts en vol...");
            }
        };
    }

    // ----------------------------------------------------------------------------------
    // SCÉNARIO 5 : Météo
    // ----------------------------------------------------------------------------------
    private static SimHook scenario5_Meteo() {
        System.out.println(">> [S5] INIT: Drones en vol.");
        DroneLogistique d1 = new DroneLogistique("Drone-Meteo", 0, 0, 100);
        d1.demarrer();
        gestionnaire.ajouterActif(d1);
        d1.setTarget(1000, 0, 100); // Vol continu sur X

        return (t, s) -> {
            if (Math.abs(t - 5.0) < 0.05) {
                System.out.println("\n>> [S5] ACTION: Application VENT FORT (face) + PLUIE");
                // Vent de face (direction 180 si drone va vers 0? non drone va vers +X (0
                // deg)).
                // Vent venant de 0 deg (face? non 0 est East).
                // Si drone va 0->1000, il va East (0 deg).
                // Vent venant de 180 (West) le pousse. Vent venant de 0 (East) le freine.
                Weather w = s.getWeather();
                w.setWindSpeed(80); // km/h
                w.setRainIntensity(0.9);
                System.out.println("   -> Vent: 80 km/h, Pluie: 90%");
            }
        };
    }
}
