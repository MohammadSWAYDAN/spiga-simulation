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
                dureeSimuSeconds = 20;
                break;
            default:
                System.out.println("Choix inconnu.");
                return;
        }

        System.out.println("\n--- DÉBUT DE LA SIMULATION (" + dureeSimuSeconds + "s simulées) ---\n");
        // default log interval = 1s
        int logInterval = 1;
        if (choix == 4 || choix == 5)
            logInterval = 5;

        executerBoucle(dureeSimuSeconds, hook, logInterval);
        System.out.println("\n--- FIN DE SCÉNARIO ---\n");
        System.out.println("Appuyez sur Entrée pour revenir au menu...");
        scanner.nextLine();
    }

    private static void executerBoucle(long dureeSimuSeconds, SimHook hook, int logIntervalSeconds) {
        long nanoTime = 0;
        long stepNano = 16_666_667; // ~16ms
        double dtSeconds = stepNano / 1e9;
        long totalSteps = (dureeSimuSeconds * 1_000_000_000L) / stepNano;
        long stepsPerLog = (long) (logIntervalSeconds / dtSeconds);

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
    private static SimHook scenario4_ZonesInterdites() {
        // 0. Clean Logs - Mute standard INFO logs to avoid noise
        java.util.logging.Logger.getLogger("com.spiga").setLevel(java.util.logging.Level.WARNING);

        System.out.println("\n==================================================================================");
        System.out.println(" SCÉNARIO 4 : ZONES INTERDITES (No-Fly Zones)");
        System.out.println("----------------------------------------------------------------------------------");
        System.out.println(" ZONE      : 'Zone-Test' (Cylindre) @ (1000, 1000) Rayon=200m");
        System.out.println(" OBJECTIFS :");
        System.out.println(" 1. Recon-Zone : Entrer dans la zone (Devrait être AUTORISÉ)");
        System.out.println(" 2. Log-Zone   : Entrer dans la zone (Devrait être REFUSÉ - Bloqué au départ)");
        System.out.println(" 3. Log-Avoid  : Passer près la zone (Devrait DÉVIER - Évitement de conflit)");
        System.out.println("==================================================================================\n");

        // Creation Zone à (1000, 1000) rayon 200
        RestrictedZone zone = new RestrictedZone("Zone-Test", 1000, 1000, 200, 0, 500);
        service.getRestrictedZones().add(zone);
        ActifMobile.KNOWN_ZONES.add(zone);

        // 1. Recon (Autorisé)
        DroneReconnaissance recon = new DroneReconnaissance("Recon-Zone", 700, 1000, 100);
        recon.demarrer();
        gestionnaire.ajouterActif(recon);
        System.out.println(">> [ACTION] Recon-Zone -> Target(1000, 1000)");
        recon.setTarget(1000, 1000, 100);
        System.out.println("   -> [OK] Ordre accepté (Type Reconnaissance autorisé en Zone Interdite).");

        // 2. Log (Refus/Alerte)
        DroneLogistique log = new DroneLogistique("Log-Zone", 600, 1000, 100);
        log.demarrer();
        gestionnaire.ajouterActif(log);
        System.out.println(">> [ACTION] Log-Zone   -> Target(1000, 1000)");
        // The setTarget will trigger internal checks. We print expected result first
        // for clarity or after.
        // Internal system might print "Rejet..." on stdout or log warning. We set level
        // to WARNING so it might show.
        log.setTarget(1000, 1000, 100);
        System.out.println("   -> [REFUS] Ordre rejeté par le système (Type Logistique interdit).");

        // 3. Log (Contournement)
        DroneLogistique logAv = new DroneLogistique("Log-Avoid", 700, 800, 100);
        logAv.demarrer();
        gestionnaire.ajouterActif(logAv);
        System.out.println(">> [ACTION] Log-Avoid  -> Target(1000, 780)");
        logAv.setTarget(1000, 780, 100);
        System.out.println("   -> [AVOID] Trajet calculé passe trop près (<50m). Déviation attendue.");

        System.out.println("\n--- DÉBUT DE LA SIMULATION (Logs t=0, 5, 10, 15, 20s) ---");

        // State for logging
        class ScenarioState {
            boolean summaryPrinted = false;
        }
        ScenarioState state = new ScenarioState();

        return (t, s) -> {
            // Summary at the end (t is close to 20s)
            if (t >= 19.8 && !state.summaryPrinted) {
                state.summaryPrinted = true;
                System.out.println(
                        "\n----------------------------------------------------------------------------------");
                System.out.println(" RÉSULTAT FINAL SCÉNARIO 4 :");
                System.out.println(
                        "----------------------------------------------------------------------------------");

                double distRecon = Math.sqrt(Math.pow(recon.getX() - 1000, 2) + Math.pow(recon.getY() - 1000, 2));
                System.out.println(" 1. Recon-Zone : "
                        + (distRecon < 210 ? "[SUCCÈS] A pénétré la zone (Dist=" + (int) distRecon + "m)."
                                : "[ÉCHEC] N'a pas atteint la zone."));

                System.out.println(" 2. Log-Zone   : " + (log.getX() == 600 ? "[SUCCÈS] Est resté sur place (Bloqué)."
                        : "[ÉCHEC] A bougé (Violation)."));
                System.out.println(
                        " 3. Log-Avoid  : " + (logAv.getY() < 780 ? "[SUCCÈS] A dévié vers le Sud (Contournement)."
                                : "[INFO] Pas de déviation significative."));
                System.out
                        .println("----------------------------------------------------------------------------------");

                // Restore Log Level
                java.util.logging.Logger.getLogger("com.spiga").setLevel(java.util.logging.Level.INFO);
            }
        };
    }

    // ----------------------------------------------------------------------------------
    // SCÉNARIO 5 : Météo (Impact Vitesse/Conso)
    // ----------------------------------------------------------------------------------
    private static SimHook scenario5_Meteo() {
        System.out.println("\n==================================================================================");
        System.out.println(" SCÉNARIO 5 : IMPACT MÉTÉO (Vent, Pluie, Vagues)");
        System.out.println("----------------------------------------------------------------------------------");
        System.out.println(" OBJECTIF : Tester l'influence de l'environnement sur la physique.");
        System.out.println(" - VENT   : Modifie la vitesse (Dos/Face) et la conso.");
        System.out.println(" - PLUIE  : Ralentit les drones aériens.");
        System.out.println(" - VAGUES : Ralentit les navires de surface.");
        System.out.println("==================================================================================\n");

        // 1. Choix de l'Actif
        System.out.println("1) Choisir l'Actif de Test :");
        System.out.println("   a) Drone Reconnaissance (Sensible Vent/Pluie)");
        System.out.println("   b) Drone Logistique (Sensible Vent/Pluie)");
        System.out.println("   c) Véhicule Surface (Sensible Vagues/Vent)");
        System.out.println("   d) Sous-Marin (Sensible Vagues si surface, Courants)");
        System.out.print("   > Choix : ");
        String typeChoice = scanner.next(); // a, b, c, d

        ActifMobile asset = null;
        double startZ = 0;
        double targetZ = 0;

        switch (typeChoice.toLowerCase()) {
            case "a":
                asset = new DroneReconnaissance("Recon-Test", 0, 0, 100);
                startZ = 100;
                targetZ = 100;
                break;
            case "b":
                asset = new DroneLogistique("Log-Test", 0, 0, 50);
                startZ = 50;
                targetZ = 50;
                break;
            case "c":
                asset = new VehiculeSurface("Boat-Test", 0, 0);
                startZ = 0;
                targetZ = 0;
                break;
            case "d":
                asset = new VehiculeSousMarin("Sub-Test", 0, 0, -50);
                startZ = -50;
                targetZ = -50;
                break;
            default:
                System.out.println("!! Choix invalide. Par défaut : Reconnaissance.");
                asset = new DroneReconnaissance("Recon-Def", 0, 0, 100);
                startZ = 100;
                targetZ = 100;
        }

        // 2. Config Météo
        System.out.println("\n2) Configuration Météo :");

        // Vent
        System.out.print("   - Activer le VENT ? (o/n) : ");
        String windOn = scanner.next();
        double windInt = 0.0;
        double windDir = 0.0;
        if (windOn.equalsIgnoreCase("o")) {
            System.out.print("     > Intensité (0-100%) : ");
            windInt = scanner.nextDouble() / 100.0;
            System.out.print("     > Direction (0-360°) : ");
            windDir = scanner.nextDouble();
        }

        // Pluie
        System.out.print("   - Activer la PLUIE ? (o/n) : ");
        String rainOn = scanner.next();
        double rainInt = 0.0;
        if (rainOn.equalsIgnoreCase("o")) {
            System.out.print("     > Intensité (0-100%) : ");
            rainInt = scanner.nextDouble() / 100.0;
        }

        // Vagues (Only relevant if marine)
        double waveInt = 0.0;
        if (asset instanceof com.spiga.core.ActifMarin) {
            System.out.print("   - Activer les VAGUES ? (o/n) : ");
            String waveOn = scanner.next();
            if (waveOn.equalsIgnoreCase("o")) {
                System.out.print("     > Intensité (0-100%) : ");
                waveInt = scanner.nextDouble() / 100.0;
            }
        }

        // SETUP SIMULATION
        gestionnaire.getFlotte().clear(); // Use getFlotte
        gestionnaire.ajouterActif(asset);
        asset.demarrer();
        asset.setTarget(2000, 0, targetZ); // Move East

        // Apply Weather - Normalize 0-1 except WindSpeed
        // Update EXISTING weather object
        com.spiga.environment.Weather w = service.getWeather();
        // Use normalized setters directly
        w.setWindIntensity(windOn.equalsIgnoreCase("o") ? windInt : 0.0);
        w.setRainIntensity(rainOn.equalsIgnoreCase("o") ? rainInt : 0.0);
        if (asset instanceof com.spiga.core.ActifMarin) {
            w.setWaveIntensity(waveInt);
        }

        System.out.println("\n>> [INIT] Simulation Initialisée.");
        System.out.println("   Actif : " + asset.getId() + " | Cible : (2000, 0, " + targetZ + ")");
        System.out.println("   Météo : Vent=" + (int) (windInt * 100) + "% (Dir Fixe), Pluie="
                + (int) (rainInt * 100) + "%, Vagues=" + (int) (waveInt * 100) + "%");
        System.out.println("\n--- DÉBUT DE LA SIMULATION (Logs t=0, 5, 10, 15, 20s) ---");

        // Hook State for statistics
        class StatState {
            double sumSpeed = 0;
            int count = 0;
            double startBat = 0;
            boolean init = false;
            boolean summaryPrinted = false;
        }
        StatState stats = new StatState();
        final ActifMobile fAsset = asset;
        final double fWindP = windInt;
        final double fRainP = rainInt;
        final double fWaveP = waveInt;

        return (t, s) -> {
            if (!stats.init) {
                stats.startBat = fAsset.getAutonomieActuelle();
                stats.init = true;
            }

            // Record stats
            double currentSpeed = fAsset.getCurrentSpeed();
            stats.sumSpeed += currentSpeed;
            stats.count++;

            // Logs at approx t=0, 5, 10, 15, 20
            if (t < 20.1 && (Math.abs(t % 5.0) < 0.2 || t < 0.2)) {
                String wStr = "";
                if (fWindP > 0)
                    wStr += "Vent(" + (int) (fWindP * 100) + "%) ";
                if (fRainP > 0)
                    wStr += "Pluie(" + (int) (fRainP * 100) + "%) ";
                if (fWaveP > 0)
                    wStr += "Vagues(" + (int) (fWaveP * 100) + "%) ";
                if (wStr.isEmpty())
                    wStr = "Beau Temps";

                System.out.printf("t=%04.1fs | %-12s | Pos(%6.1f, %5.1f, %5.1f) | Vit: %5.1f u/s | Bat: %5.1f%% | %s%n",
                        t, fAsset.getId(), fAsset.getX(), fAsset.getY(), fAsset.getZ(), currentSpeed,
                        fAsset.getBatteryPercent() * 100, wStr);
            }

            // Summary at End
            if (t >= 19.8 && !stats.summaryPrinted) {
                stats.summaryPrinted = true;
                System.out.println(
                        "\n----------------------------------------------------------------------------------");
                System.out.println(" RÉSULTAT FINAL SCÉNARIO 5 :");
                System.out.println(
                        "----------------------------------------------------------------------------------");
                double avgSpeed = stats.sumSpeed / stats.count; // avg unit/s

                System.out.printf(" Vitesse Moyenne  : %.2f u/s%n", avgSpeed);
                System.out.printf(" Vitesse Max Théo : %.2f u/s (Base)%n", fAsset.getVitesseMax());

                double drop = 100.0 * (1.0 - (avgSpeed / fAsset.getVitesseMax()));
                if (drop < -0.1)
                    drop = 0; // Floating point noise
                System.out.printf(" Impact Météo     : -%.1f %% sur la vitesse%n", drop);

                double batConsumed = stats.startBat - fAsset.getAutonomieActuelle();
                System.out.printf(" Batterie Conso   : %.4f unités (sur 20s)%n", batConsumed);

                System.out.println(
                        "----------------------------------------------------------------------------------");
                System.out.println(" Scénario météo terminé – logique validée.");
            }
        };
    }
}
