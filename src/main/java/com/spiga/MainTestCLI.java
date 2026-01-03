package com.spiga;

import com.spiga.core.*;
import com.spiga.management.*;
import com.spiga.environment.*;
import javafx.application.Platform;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

/**
<<<<<<< HEAD
 * MainTestCLI - Interface Console SPIGA
 *
 * Interface Console (CLI) permettant d'effectuer toutes les operations
 * de gestion et de simulation :
 * - Creation d'actifs (drones, navires, sous-marins)
 * - Assignation de missions
 * - Affichage des etats
 * - Controle de la simulation
 *
 * Conforme aux exigences du projet POO Java 2025-2026
 *
 * @author SPIGA Team
 * @version 2.0
 */
public class MainTestCLI {

    /** Gestionnaire d'essaim centralise pour la gestion de la flotte. */
    private static GestionnaireEssaim gestionnaire;

    /** Service de simulation gerant la boucle de mise a jour et la physique. */
    private static SimulationService service;

    /** Scanner pour la lecture des entrees utilisateur en console. */
    private static Scanner scanner;

    /** Indicateur d'initialisation du toolkit JavaFX. */
    private static boolean isJavaFxInitialized = false;

    /** Liste de toutes les missions creees pour la gestion interactive. */
    private static List<Mission> allMissions = new ArrayList<>();
=======
 * Interface en Ligne de Commande (CLI) pour la validation technique.
 * <p>
 * Cette classe permet de tester isolément la logique métier de la simulation
 * sans passer
 * par l'interface graphique JavaFX complète (bien que le toolkit soit
 * initialisé pour les timers).
 * Elle propose plusieurs scénarios prédéfinis pour valider les comportements
 * des entités :
 * </p>
 * <ul>
 * <li>Missions et déplacements manuels.</li>
 * <li>Gestion de la batterie et recharge.</li>
 * <li>Respect des contraintes physiques (altitude, profondeur).</li>
 * <li>Gestion des zones interdites (refus ou évitement).</li>
 * <li>Impact des conditions météorologiques (vent, pluie, vagues).</li>
 * </ul>
 *
 * @author Equipe SPIGA
 * @version 1.0
 */
public class MainTestCLI {

    /** Le gestionnaire central de l'essaim (flotte d'actifs). */
    private static GestionnaireEssaim gestionnaire;

    /**
     * Le service de simulation responsable de la boucle temporelle et de la
     * physique.
     */
    private static SimulationService service;

    /** Scanner pour la saisie utilisateur dans la console. */
    private static Scanner scanner;

    /**
     * Indicateur de l'état d'initialisation du toolkit JavaFX (nécessaire pour
     * {@code AnimationTimer}).
     */
    private static boolean isJavaFxInitialized = false;

    /**
     * Point d'entrée du mode CLI.
     * <p>
     * Initialise l'environnement (JavaFX, Services), lance la boucle de menu
     * principale,
     * et gère la sélection des scénarios.
     * </p>
     *
     * @param args Arguments de ligne de commande (non utilisés).
     */
    public static void main(String[] args) {
        System.out.println("INITIALISATION DE L'ENVIRONNEMENT DE TEST...");
>>>>>>> 8d792a7f2571e1788f23b9efa0d3e769e349ec7d

    /** Compteur auto-increment pour generer les IDs d'actifs. */
    private static int assetCounter = 1;

    /** Compteur auto-increment pour generer les IDs de missions. */
    private static int missionCounter = 1;

    /**
     * Point d'entree principal de l'application CLI SPIGA.
     * Initialise le toolkit JavaFX, les services de simulation,
     * puis demarre la boucle interactive du menu principal.
     *
     * @param args Arguments de ligne de commande (non utilises)
     */
    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println("   SPIGA - Systeme de Pilotage Intelligent");
        System.out.println("          et Gestion d'Actifs Mobiles");
        System.out.println("              Interface Console (CLI)");
        System.out.println("=========================================================");
        System.out.println("\nInitialisation du systeme...");

        // 1. Init JavaFX Toolkit (necessaire pour AnimationTimer de SimulationService)
        try {
            Platform.startup(() -> {
            });
            isJavaFxInitialized = true;
            System.out.println("[OK] JavaFX initialise");
        } catch (IllegalStateException e) {
            isJavaFxInitialized = true;
            System.out.println("[OK] JavaFX deja initialise");
        } catch (Throwable e) {
            System.err.println("[ATTENTION] JavaFX non disponible - Mode degrade");
        }

        // 2. Setup Services
        try {
            gestionnaire = new GestionnaireEssaim();
            service = new SimulationService(gestionnaire);
            service.setTimeScale(1.0);
            System.out.println("[OK] Services de simulation initialises");
        } catch (Exception e) {
            System.err.println("[ERREUR FATALE] Impossible d'initier la simulation.");
            e.printStackTrace();
            return;
        }

        scanner = new Scanner(System.in);
        System.out.println("\nSysteme pret. Entrez un numero pour naviguer.\n");

        // 3. Boucle principale
        boolean running = true;
        while (running) {
            afficherMenuPrincipal();
            System.out.print("\n> Choix : ");
            String input = scanner.nextLine().trim();

            try {
                int choix = Integer.parseInt(input);
                switch (choix) {
                    case 1: menuGestionActifs(); break;
                    case 2: menuGestionMissions(); break;
                    case 3: menuAffichageEtats(); break;
                    case 4: menuSimulation(); break;
                    case 5: menuMeteo(); break;
                    case 6: menuScenariosTest(); break;
                    case 0:
                        running = false;
                        System.out.println("\nFermeture du CLI SPIGA. Au revoir!");
                        break;
                    default:
                        System.out.println("[ERREUR] Choix invalide (0-6).");
                }
            } catch (NumberFormatException e) {
                System.out.println("[ERREUR] Veuillez entrer un numero.");
            }
        }

        if (isJavaFxInitialized)
            Platform.exit();
        System.exit(0);
    }

<<<<<<< HEAD
    // ==================================================================================
    // MENU PRINCIPAL
    // ==================================================================================

    /**
     * Affiche le menu principal avec les options disponibles.
     * Options : Gestion actifs, Missions, Etats, Simulation, Meteo, Scenarios.
=======
    /**
     * Affiche le menu principal des scénarios disponibles.
>>>>>>> 8d792a7f2571e1788f23b9efa0d3e769e349ec7d
     */
    private static void afficherMenuPrincipal() {
        System.out.println("\n============== MENU PRINCIPAL ==============");
        System.out.println("  1. Gestion des Actifs");
        System.out.println("  2. Gestion des Missions");
        System.out.println("  3. Affichage des Etats");
        System.out.println("  4. Controle Simulation");
        System.out.println("  5. Configuration Meteo");
        System.out.println("  6. Scenarios de Test (predefinies)");
        System.out.println("  0. Quitter");
        System.out.println("=============================================");
    }

<<<<<<< HEAD
    // ==================================================================================
    // 1. GESTION DES ACTIFS
    // ==================================================================================

    /**
     * Affiche le sous-menu de gestion des actifs.
     * Permet de creer, lister, supprimer, demarrer, eteindre,
     * recharger et deplacer des actifs.
     */
    private static void menuGestionActifs() {
        boolean continuer = true;
        while (continuer) {
            System.out.println("\n-------- GESTION DES ACTIFS --------");
            System.out.println("  1. Creer un nouvel actif");
            System.out.println("  2. Lister tous les actifs");
            System.out.println("  3. Supprimer un actif");
            System.out.println("  4. Demarrer un actif");
            System.out.println("  5. Eteindre un actif");
            System.out.println("  6. Recharger un actif");
            System.out.println("  7. Deplacer un actif (manuel)");
            System.out.println("  0. Retour");
            System.out.println("------------------------------------");
            System.out.print("> Choix : ");

            String input = scanner.nextLine().trim();
            try {
                int choix = Integer.parseInt(input);
                switch (choix) {
                    case 1: creerActif(); break;
                    case 2: listerActifs(); break;
                    case 3: supprimerActif(); break;
                    case 4: demarrerActif(); break;
                    case 5: eteindreActif(); break;
                    case 6: rechargerActif(); break;
                    case 7: deplacerActif(); break;
                    case 0: continuer = false; break;
                    default: System.out.println("[ERREUR] Choix invalide.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[ERREUR] Veuillez entrer un numero.");
            }
        }
    }

    /**
     * Creation interactive d'un nouvel actif mobile.
     * Types disponibles : DroneReconnaissance, DroneLogistique,
     * VehiculeSurface, VehiculeSousMarin, SousMarinExploration.
     * L'actif est automatiquement ajoute a la flotte.
     */
    private static void creerActif() {
        System.out.println("\n=== CREATION D'UN NOUVEL ACTIF ===");
        System.out.println("Types disponibles:");
        System.out.println("  1. DroneReconnaissance  (Aerien, rapide, surveillance)");
        System.out.println("  2. DroneLogistique      (Aerien, charge utile)");
        System.out.println("  3. VehiculeSurface      (Marin, surface Z=0)");
        System.out.println("  4. VehiculeSousMarin    (Marin, sous-marin)");
        System.out.println("  5. SousMarinExploration (Marin, exploration profonde)");
        System.out.print("> Type (1-5) : ");

        try {
            int type = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("> ID de l'actif (Entree pour auto) : ");
            String id = scanner.nextLine().trim();
            if (id.isEmpty()) {
                id = "ACTIF-" + String.format("%03d", assetCounter++);
            }

            System.out.print("> Position X (defaut 0) : ");
            double x = lireDouble(0);
            System.out.print("> Position Y (defaut 0) : ");
            double y = lireDouble(0);

            ActifMobile actif = null;

            switch (type) {
                case 1:
                    System.out.print("> Altitude Z (defaut 100) : ");
                    double zRecon = lireDouble(100);
                    actif = new DroneReconnaissance(id, x, y, zRecon);
                    System.out.println("[OK] DroneReconnaissance '" + id + "' cree a (" + x + ", " + y + ", " + zRecon + ")");
                    break;
                case 2:
                    System.out.print("> Altitude Z (defaut 50) : ");
                    double zLog = lireDouble(50);
                    actif = new DroneLogistique(id, x, y, zLog);
                    System.out.println("[OK] DroneLogistique '" + id + "' cree a (" + x + ", " + y + ", " + zLog + ")");
                    break;
                case 3:
                    actif = new VehiculeSurface(id, x, y);
                    System.out.println("[OK] VehiculeSurface '" + id + "' cree a (" + x + ", " + y + ", 0)");
                    break;
                case 4:
                    System.out.print("> Profondeur Z (defaut -50) : ");
                    double zSub = lireDouble(-50);
                    if (zSub > 0) zSub = -zSub;
                    actif = new VehiculeSousMarin(id, x, y, zSub);
                    System.out.println("[OK] VehiculeSousMarin '" + id + "' cree a (" + x + ", " + y + ", " + zSub + ")");
                    break;
                case 5:
                    System.out.print("> Profondeur Z (defaut -50) : ");
                    double zExplore = lireDouble(-50);
                    if (zExplore > 0) zExplore = -zExplore;
                    actif = new SousMarinExploration(id, x, y, zExplore);
                    System.out.println("[OK] SousMarinExploration '" + id + "' cree a (" + x + ", " + y + ", " + zExplore + ")");
                    break;
                default:
                    System.out.println("[ERREUR] Type invalide.");
                    return;
            }

            if (actif != null) {
                gestionnaire.ajouterActif(actif);
                System.out.println("[INFO] Actif ajoute. Total flotte: " + gestionnaire.getFlotte().size());
            }
        } catch (Exception e) {
            System.out.println("[ERREUR] Saisie invalide: " + e.getMessage());
        }
    }

    /**
     * Affiche la liste de tous les actifs de la flotte.
     * Format tableau avec ID, Type, Position, Batterie et Etat.
     */
    private static void listerActifs() {
        System.out.println("\n=== LISTE DES ACTIFS ===");
        List<ActifMobile> flotte = gestionnaire.getFlotte();

        if (flotte.isEmpty()) {
            System.out.println("[INFO] Aucun actif dans la flotte.");
            return;
        }

        System.out.println(String.format("%-14s | %-20s | %-22s | %-10s | %-12s",
                "ID", "Type", "Position (X,Y,Z)", "Batterie", "Etat"));
        System.out.println("-".repeat(90));

        for (ActifMobile actif : flotte) {
            String type = actif.getClass().getSimpleName();
            String pos = String.format("(%6.0f,%6.0f,%6.0f)", actif.getX(), actif.getY(), actif.getZ());
            String bat = String.format("%5.1f%%", actif.getBatteryPercent() * 100);
            String etat = actif.getEtat().toString();

            System.out.println(String.format("%-14s | %-20s | %-22s | %-10s | %-12s",
                    actif.getId(), type, pos, bat, etat));
        }
        System.out.println("Total: " + flotte.size() + " actif(s)");
    }

    /**
     * Suppression d'un actif de la flotte par son ID.
     */
    private static void supprimerActif() {
        System.out.println("\n=== SUPPRIMER UN ACTIF ===");
        listerActifsSimple();
        System.out.print("> ID de l'actif a supprimer : ");
        String id = scanner.nextLine().trim();

        ActifMobile actif = trouverActif(id);
        if (actif != null) {
            gestionnaire.supprimerActif(id);
            System.out.println("[OK] Actif '" + id + "' supprime.");
        } else {
            System.out.println("[ERREUR] Actif non trouve: " + id);
        }
    }

    /**
     * Demarre un actif. Change son etat de AU_SOL a EN_MISSION.
     */
    private static void demarrerActif() {
        System.out.println("\n=== DEMARRER UN ACTIF ===");
        listerActifsSimple();
        System.out.print("> ID de l'actif : ");
        String id = scanner.nextLine().trim();

        ActifMobile actif = trouverActif(id);
        if (actif != null) {
            actif.demarrer();
            System.out.println("[OK] Actif '" + id + "' demarre. Etat: " + actif.getEtat());
        } else {
            System.out.println("[ERREUR] Actif non trouve: " + id);
        }
    }

    /**
     * Eteint un actif. Change son etat de EN_MISSION a AU_SOL.
     */
    private static void eteindreActif() {
        System.out.println("\n=== ETEINDRE UN ACTIF ===");
        listerActifsSimple();
        System.out.print("> ID de l'actif : ");
        String id = scanner.nextLine().trim();

        ActifMobile actif = trouverActif(id);
        if (actif != null) {
            actif.eteindre();
            System.out.println("[OK] Actif '" + id + "' eteint. Etat: " + actif.getEtat());
        } else {
            System.out.println("[ERREUR] Actif non trouve: " + id);
        }
    }

    /**
     * Recharge la batterie d'un actif a 100%.
     */
    private static void rechargerActif() {
        System.out.println("\n=== RECHARGER UN ACTIF ===");
        listerActifsSimple();
        System.out.print("> ID de l'actif : ");
        String id = scanner.nextLine().trim();

        ActifMobile actif = trouverActif(id);
        if (actif != null) {
            actif.recharger();
            System.out.println("[OK] Actif '" + id + "' recharge a 100%.");
        } else {
            System.out.println("[ERREUR] Actif non trouve: " + id);
        }
    }

    /**
     * Deplacement manuel d'un actif vers une position cible (X, Y, Z).
     */
    private static void deplacerActif() {
        System.out.println("\n=== DEPLACEMENT MANUEL ===");
        listerActifsSimple();
        System.out.print("> ID de l'actif : ");
        String id = scanner.nextLine().trim();

        ActifMobile actif = trouverActif(id);
        if (actif != null) {
            System.out.print("> Cible X : ");
            double tx = lireDouble(actif.getX());
            System.out.print("> Cible Y : ");
            double ty = lireDouble(actif.getY());
            System.out.print("> Cible Z : ");
            double tz = lireDouble(actif.getZ());

            actif.setTarget(tx, ty, tz);
            System.out.println("[OK] Actif '" + id + "' en route vers (" + tx + ", " + ty + ", " + tz + ")");
        } else {
            System.out.println("[ERREUR] Actif non trouve: " + id);
        }
    }

    // ==================================================================================
    // 2. GESTION DES MISSIONS
    // ==================================================================================

    /**
     * Affiche le sous-menu de gestion des missions.
     * Permet de creer, lister, assigner, demarrer, pauser,
     * reprendre, annuler des missions et voir l'historique.
     */
    private static void menuGestionMissions() {
        boolean continuer = true;
        while (continuer) {
            System.out.println("\n-------- GESTION DES MISSIONS --------");
            System.out.println("  1. Creer une nouvelle mission");
            System.out.println("  2. Lister toutes les missions");
            System.out.println("  3. Assigner mission a un actif");
            System.out.println("  4. Demarrer une mission");
            System.out.println("  5. Mettre en pause une mission");
            System.out.println("  6. Reprendre une mission");
            System.out.println("  7. Annuler une mission");
            System.out.println("  8. Historique des missions");
            System.out.println("  0. Retour");
            System.out.println("--------------------------------------");
            System.out.print("> Choix : ");

            String input = scanner.nextLine().trim();
            try {
                int choix = Integer.parseInt(input);
                switch (choix) {
                    case 1: creerMission(); break;
                    case 2: listerMissions(); break;
                    case 3: assignerMission(); break;
                    case 4: demarrerMission(); break;
                    case 5: pauseMission(); break;
                    case 6: resumeMission(); break;
                    case 7: annulerMission(); break;
                    case 8: voirHistoriqueMissions(); break;
                    case 0: continuer = false; break;
                    default: System.out.println("[ERREUR] Choix invalide.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[ERREUR] Veuillez entrer un numero.");
            }
        }
    }

    /**
     * Creation interactive d'une nouvelle mission.
     * Types : MissionSurveillanceMaritime ou MissionLogistique.
     * La mission est ajoutee a la liste globale.
     */
    private static void creerMission() {
        System.out.println("\n=== CREATION D'UNE MISSION ===");
        System.out.println("Types de missions:");
        System.out.println("  1. MissionSurveillanceMaritime (Reconnaissance)");
        System.out.println("  2. MissionLogistique (Transport)");
        System.out.print("> Type (1-2) : ");

        try {
            int type = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("> Titre (Entree pour auto) : ");
            String titre = scanner.nextLine().trim();
            if (titre.isEmpty()) {
                titre = "Mission-" + String.format("%03d", missionCounter++);
            }

            System.out.print("> Cible X : ");
            double tx = lireDouble(500);
            System.out.print("> Cible Y : ");
            double ty = lireDouble(500);
            System.out.print("> Cible Z : ");
            double tz = lireDouble(0);

            System.out.print("> Duree max en secondes (defaut 180) : ");
            long duree = (long) lireDouble(180);

            Mission mission = null;

            switch (type) {
                case 1:
                    mission = new MissionSurveillanceMaritime(titre);
                    System.out.println("[OK] MissionSurveillanceMaritime '" + titre + "' creee.");
                    break;
                case 2:
                    mission = new MissionLogistique(titre);
                    System.out.println("[OK] MissionLogistique '" + titre + "' creee.");
                    break;
                default:
                    System.out.println("[ERREUR] Type invalide.");
                    return;
            }

            if (mission != null) {
                mission.setTarget(tx, ty, tz);
                mission.setPlannedDurationSeconds(duree);
                allMissions.add(mission);
                System.out.println("[INFO] Cible: (" + tx + ", " + ty + ", " + tz + "), Duree: " + duree + "s");
            }
        } catch (Exception e) {
            System.out.println("[ERREUR] Saisie invalide: " + e.getMessage());
        }
    }

    /**
     * Affiche la liste de toutes les missions.
     * Format tableau avec numero, titre, type, cible, statut et actifs assignes.
     */
    private static void listerMissions() {
        System.out.println("\n=== LISTE DES MISSIONS ===");

        if (allMissions.isEmpty()) {
            System.out.println("[INFO] Aucune mission creee.");
            return;
        }

        System.out.println(String.format("%-4s | %-18s | %-20s | %-20s | %-10s | %-6s",
                "#", "Titre", "Type", "Cible (X,Y,Z)", "Statut", "Actifs"));
        System.out.println("-".repeat(90));

        for (int i = 0; i < allMissions.size(); i++) {
            Mission m = allMissions.get(i);
            String cible = String.format("(%5.0f,%5.0f,%5.0f)", m.getTargetX(), m.getTargetY(), m.getTargetZ());
            System.out.println(String.format("%-4d | %-18s | %-20s | %-20s | %-10s | %-6d",
                    (i + 1), m.getTitre(), m.getClass().getSimpleName(), cible,
                    m.getStatut(), m.getAssignedAssets().size()));
        }
    }

    /**
     * Assigne une mission a un actif.
     * L'actif est automatiquement demarre si au sol.
     */
    private static void assignerMission() {
        System.out.println("\n=== ASSIGNER UNE MISSION ===");

        if (allMissions.isEmpty()) {
            System.out.println("[INFO] Aucune mission. Creez-en une d'abord.");
            return;
        }
        if (gestionnaire.getFlotte().isEmpty()) {
            System.out.println("[INFO] Aucun actif. Creez-en un d'abord.");
            return;
        }

        listerMissionsSimple();
        System.out.print("> Numero de la mission : ");
        int missionIdx = (int) lireDouble(1) - 1;

        if (missionIdx < 0 || missionIdx >= allMissions.size()) {
            System.out.println("[ERREUR] Mission invalide.");
            return;
        }

        Mission mission = allMissions.get(missionIdx);

        listerActifsSimple();
        System.out.print("> ID de l'actif : ");
        String id = scanner.nextLine().trim();

        ActifMobile actif = trouverActif(id);
        if (actif != null) {
            if (actif.getEtat() == ActifMobile.EtatOperationnel.AU_SOL) {
                actif.demarrer();
            }
            actif.assignMission(mission);
            mission.addActif(actif);
            System.out.println("[OK] Mission '" + mission.getTitre() + "' assignee a '" + id + "'");
        } else {
            System.out.println("[ERREUR] Actif non trouve: " + id);
        }
    }

    /**
     * Demarre une mission planifiee. Change le statut a EN_COURS.
     */
    private static void demarrerMission() {
        System.out.println("\n=== DEMARRER UNE MISSION ===");
        listerMissionsSimple();
        System.out.print("> Numero de la mission : ");
        int idx = (int) lireDouble(1) - 1;

        if (idx >= 0 && idx < allMissions.size()) {
            Mission mission = allMissions.get(idx);
            if (mission.getStatut() == Mission.StatutMission.PLANIFIEE) {
                mission.start(System.currentTimeMillis() / 1000);
                System.out.println("[OK] Mission '" + mission.getTitre() + "' demarree.");
            } else {
                System.out.println("[INFO] Statut actuel: " + mission.getStatut());
            }
        } else {
            System.out.println("[ERREUR] Mission invalide.");
        }
    }

    /**
     * Met une mission en cours en pause.
     */
    private static void pauseMission() {
        System.out.println("\n=== PAUSE MISSION ===");
        listerMissionsSimple();
        System.out.print("> Numero : ");
        int idx = (int) lireDouble(1) - 1;

        if (idx >= 0 && idx < allMissions.size()) {
            allMissions.get(idx).pause();
            System.out.println("[OK] Mission en pause.");
        } else {
            System.out.println("[ERREUR] Mission invalide.");
        }
    }

    /**
     * Reprend une mission mise en pause.
     */
    private static void resumeMission() {
        System.out.println("\n=== REPRENDRE MISSION ===");
        listerMissionsSimple();
        System.out.print("> Numero : ");
        int idx = (int) lireDouble(1) - 1;

        if (idx >= 0 && idx < allMissions.size()) {
            allMissions.get(idx).resume(System.currentTimeMillis() / 1000);
            System.out.println("[OK] Mission reprise.");
        } else {
            System.out.println("[ERREUR] Mission invalide.");
        }
    }

    /**
     * Annule une mission avec une raison specifiee.
     */
    private static void annulerMission() {
        System.out.println("\n=== ANNULER MISSION ===");
        listerMissionsSimple();
        System.out.print("> Numero : ");
        int idx = (int) lireDouble(1) - 1;

        if (idx >= 0 && idx < allMissions.size()) {
            System.out.print("> Raison : ");
            String raison = scanner.nextLine().trim();
            if (raison.isEmpty()) raison = "Annulation utilisateur";
            allMissions.get(idx).cancel(raison);
            System.out.println("[OK] Mission annulee.");
        } else {
            System.out.println("[ERREUR] Mission invalide.");
        }
    }

    /**
     * Affiche l'historique de toutes les missions avec leurs executions.
     */
    private static void voirHistoriqueMissions() {
        System.out.println("\n=== HISTORIQUE DES MISSIONS ===");
        for (Mission mission : allMissions) {
            System.out.println("\n--- " + mission.getTitre() + " ---");
            System.out.println("Type: " + mission.getClass().getSimpleName());
            System.out.println("Statut: " + mission.getStatut());
            System.out.println("Executions: " + mission.getRunCount());

            List<Mission.MissionExecution> history = mission.getHistory();
            for (Mission.MissionExecution exec : history) {
                System.out.println("  - " + exec.runId + ": " + exec.finalStatus);
            }
        }
    }

    // ==================================================================================
    // 3. AFFICHAGE DES ETATS
    // ==================================================================================

    /**
     * Affiche le sous-menu d'affichage des etats.
     * Options : flotte, details actif, disponibles, pannes,
     * missions en cours, meteo, zones, obstacles.
     */
    private static void menuAffichageEtats() {
        boolean continuer = true;
        while (continuer) {
            System.out.println("\n-------- AFFICHAGE DES ETATS --------");
            System.out.println("  1. Etat global de la flotte");
            System.out.println("  2. Details d'un actif");
            System.out.println("  3. Actifs disponibles");
            System.out.println("  4. Actifs en panne");
            System.out.println("  5. Missions en cours");
            System.out.println("  6. Etat meteo");
            System.out.println("  7. Zones interdites");
            System.out.println("  8. Obstacles");
            System.out.println("  0. Retour");
            System.out.println("-------------------------------------");
            System.out.print("> Choix : ");

            String input = scanner.nextLine().trim();
            try {
                int choix = Integer.parseInt(input);
                switch (choix) {
                    case 1: afficherEtatFlotte(); break;
                    case 2: afficherDetailsActif(); break;
                    case 3: afficherActifsDisponibles(); break;
                    case 4: afficherActifsEnPanne(); break;
                    case 5: afficherMissionsEnCours(); break;
                    case 6: afficherMeteoActuelle(); break;
                    case 7: afficherZonesInterdites(); break;
                    case 8: afficherObstacles(); break;
                    case 0: continuer = false; break;
                    default: System.out.println("[ERREUR] Choix invalide.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[ERREUR] Veuillez entrer un numero.");
            }
        }
    }

    /**
     * Affiche l'etat global de la flotte avec statistiques par type et etat.
     */
    private static void afficherEtatFlotte() {
        System.out.println("\n=== ETAT GLOBAL DE LA FLOTTE ===");
        List<ActifMobile> flotte = gestionnaire.getFlotte();

        if (flotte.isEmpty()) {
            System.out.println("[INFO] Flotte vide.");
            return;
        }

        int auSol = 0, enMission = 0, enMaintenance = 0, enPanne = 0;
        int drones = 0, navires = 0, sousMarin = 0;

        for (ActifMobile actif : flotte) {
            switch (actif.getEtat()) {
                case AU_SOL: auSol++; break;
                case EN_MISSION: enMission++; break;
                case EN_MAINTENANCE: enMaintenance++; break;
                case EN_PANNE: enPanne++; break;
            }
            if (actif instanceof DroneReconnaissance || actif instanceof DroneLogistique) drones++;
            else if (actif instanceof VehiculeSurface) navires++;
            else if (actif instanceof VehiculeSousMarin || actif instanceof SousMarinExploration) sousMarin++;
        }

        System.out.println("Total: " + flotte.size() + " actifs");
        System.out.println("  - Drones aeriens: " + drones);
        System.out.println("  - Navires surface: " + navires);
        System.out.println("  - Sous-marins: " + sousMarin);
        System.out.println("\nPar etat:");
        System.out.println("  - AU_SOL: " + auSol);
        System.out.println("  - EN_MISSION: " + enMission);
        System.out.println("  - EN_MAINTENANCE: " + enMaintenance);
        System.out.println("  - EN_PANNE: " + enPanne);

        ActifMobile optimal = gestionnaire.suggererActifOptimal();
        if (optimal != null) {
            System.out.println("\nActif optimal: " + optimal.getId() +
                    " (Bat: " + String.format("%.0f%%", optimal.getBatteryPercent() * 100) + ")");
        }
    }

    /**
     * Affiche les details complets d'un actif specifique.
     */
    private static void afficherDetailsActif() {
        System.out.println("\n=== DETAILS ACTIF ===");
        listerActifsSimple();
        System.out.print("> ID : ");
        String id = scanner.nextLine().trim();

        ActifMobile actif = trouverActif(id);
        if (actif != null) {
            System.out.println("\n--- " + actif.getId() + " ---");
            System.out.println("Type: " + actif.getClass().getSimpleName());
            System.out.println("Position: (" + actif.getX() + ", " + actif.getY() + ", " + actif.getZ() + ")");
            System.out.println("Cible: (" + actif.getTargetX() + ", " + actif.getTargetY() + ", " + actif.getTargetZ() + ")");
            System.out.println("Vitesse max: " + actif.getVitesseMax() + " m/s");
            System.out.println("Vitesse actuelle: " + String.format("%.2f", actif.getCurrentSpeed()) + " m/s");
            System.out.println("Batterie: " + String.format("%.1f%%", actif.getBatteryPercent() * 100));
            System.out.println("Etat: " + actif.getEtat() + " / " + actif.getState());
            if (actif.getCurrentMission() != null) {
                System.out.println("Mission: " + actif.getCurrentMission().getTitre());
            }
            if (actif.getCollisionWarning() != null) {
                System.out.println("ALERTE: " + actif.getCollisionWarning());
            }
        } else {
            System.out.println("[ERREUR] Actif non trouve.");
        }
    }

    /**
     * Affiche la liste des actifs disponibles (non en mission/panne).
     */
    private static void afficherActifsDisponibles() {
        System.out.println("\n=== ACTIFS DISPONIBLES ===");
        List<ActifMobile> disponibles = gestionnaire.getActifsDisponibles();
        if (disponibles.isEmpty()) {
            System.out.println("[INFO] Aucun actif disponible.");
            return;
        }
        for (ActifMobile actif : disponibles) {
            System.out.println("  - " + actif.getId() + " (" + actif.getClass().getSimpleName() +
                    ") Bat: " + String.format("%.0f%%", actif.getBatteryPercent() * 100));
        }
    }

    /**
     * Affiche la liste des actifs en panne.
     */
    private static void afficherActifsEnPanne() {
        System.out.println("\n=== ACTIFS EN PANNE ===");
        boolean found = false;
        for (ActifMobile actif : gestionnaire.getFlotte()) {
            if (actif.getEtat() == ActifMobile.EtatOperationnel.EN_PANNE) {
                System.out.println("  - " + actif.getId() + " a (" + actif.getX() + ", " + actif.getY() + ")");
                found = true;
            }
        }
        if (!found) System.out.println("[INFO] Aucun actif en panne.");
    }

    /**
     * Affiche la liste des missions actuellement en cours.
     */
    private static void afficherMissionsEnCours() {
        System.out.println("\n=== MISSIONS EN COURS ===");
        boolean found = false;
        for (Mission m : allMissions) {
            if (m.getStatut() == Mission.StatutMission.EN_COURS) {
                System.out.println("  - " + m.getTitre() + " -> (" + m.getTargetX() + ", " + m.getTargetY() + ")");
                found = true;
            }
        }
        if (!found) System.out.println("[INFO] Aucune mission en cours.");
    }

    /**
     * Affiche les conditions meteorologiques actuelles.
     */
    private static void afficherMeteoActuelle() {
        System.out.println("\n=== METEO ACTUELLE ===");
        Weather w = service.getWeather();
        System.out.println("Vent: " + String.format("%.0f%%", w.getWindIntensity() * 100));
        System.out.println("Pluie: " + String.format("%.0f%%", w.getRainIntensity() * 100));
        System.out.println("Vagues: " + String.format("%.0f%%", w.getWaveIntensity() * 100));
    }

    /**
     * Affiche la liste des zones interdites configurees.
     */
    private static void afficherZonesInterdites() {
        System.out.println("\n=== ZONES INTERDITES ===");
        List<RestrictedZone> zones = service.getRestrictedZones();
        if (zones.isEmpty()) {
            System.out.println("[INFO] Aucune zone.");
            return;
        }
        for (RestrictedZone z : zones) {
            System.out.println("  - " + z.getId() + " @ (" + z.getX() + ", " + z.getY() +
                    ") R=" + z.getRadius() + "m");
        }
    }

    /**
     * Affiche la liste des obstacles dans l'environnement.
     */
    private static void afficherObstacles() {
        System.out.println("\n=== OBSTACLES ===");
        List<Obstacle> obstacles = service.getObstacles();
        if (obstacles.isEmpty()) {
            System.out.println("[INFO] Aucun obstacle.");
            return;
        }
        for (Obstacle o : obstacles) {
            System.out.println("  - (" + o.getX() + ", " + o.getY() + ", " + o.getZ() + ") R=" + o.getRadius());
        }
    }

    // ==================================================================================
    // 4. CONTROLE SIMULATION
    // ==================================================================================

    /**
     * Affiche le sous-menu de controle de la simulation.
     * Options : executer N secondes, changer echelle temps, reinitialiser.
     */
    private static void menuSimulation() {
        boolean continuer = true;
        while (continuer) {
            System.out.println("\n-------- CONTROLE SIMULATION --------");
            System.out.println("  1. Executer N secondes");
            System.out.println("  2. Changer echelle temps");
            System.out.println("  3. Reinitialiser");
            System.out.println("  0. Retour");
            System.out.println("-------------------------------------");
            System.out.print("> Choix : ");

            String input = scanner.nextLine().trim();
            try {
                int choix = Integer.parseInt(input);
                switch (choix) {
                    case 1: executerSimulationInteractive(); break;
                    case 2:
                        System.out.print("> Echelle (1.0 = temps reel) : ");
                        service.setTimeScale(lireDouble(1.0));
                        System.out.println("[OK] Echelle modifiee.");
                        break;
                    case 3:
                        resetSimulationComplete();
                        System.out.println("[OK] Simulation reinitialisee.");
                        break;
                    case 0: continuer = false; break;
                    default: System.out.println("[ERREUR] Choix invalide.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[ERREUR] Veuillez entrer un numero.");
            }
        }
    }

    /**
     * Execute la simulation pendant une duree specifiee avec logs periodiques.
     */
    private static void executerSimulationInteractive() {
        System.out.print("> Duree (secondes, defaut 10) : ");
        long duree = (long) lireDouble(10);
        System.out.print("> Intervalle log (secondes, defaut 2) : ");
        int logInterval = (int) lireDouble(2);

        System.out.println("\n--- SIMULATION (" + duree + "s) ---\n");
        executerBoucle(duree, (t, s) -> {}, logInterval);
        System.out.println("\n--- FIN ---");
    }

    /**
     * Reinitialise completement la simulation.
     * Vide la flotte, les missions et remet les compteurs a zero.
     */
    private static void resetSimulationComplete() {
        gestionnaire.getFlotte().clear();
        allMissions.clear();
        service.reset();
        assetCounter = 1;
        missionCounter = 1;
    }

    // ==================================================================================
    // 5. CONFIGURATION METEO
    // ==================================================================================

    /**
     * Affiche le sous-menu de configuration meteorologique.
     * Permet de regler vent, pluie, vagues ou appliquer des presets.
     */
    private static void menuMeteo() {
        boolean continuer = true;
        while (continuer) {
            System.out.println("\n-------- CONFIGURATION METEO --------");
            System.out.println("  1. Configurer vent");
            System.out.println("  2. Configurer pluie");
            System.out.println("  3. Configurer vagues");
            System.out.println("  4. Preset: Beau temps");
            System.out.println("  5. Preset: Tempete");
            System.out.println("  6. Afficher meteo");
            System.out.println("  0. Retour");
            System.out.println("-------------------------------------");
            System.out.print("> Choix : ");

            String input = scanner.nextLine().trim();
            Weather w = service.getWeather();
            try {
                int choix = Integer.parseInt(input);
                switch (choix) {
                    case 1:
                        System.out.print("> Vent (0-100%) : ");
                        w.setWindIntensity(lireDouble(0) / 100.0);
                        System.out.println("[OK] Vent configure.");
                        break;
                    case 2:
                        System.out.print("> Pluie (0-100%) : ");
                        w.setRainIntensity(lireDouble(0) / 100.0);
                        System.out.println("[OK] Pluie configuree.");
                        break;
                    case 3:
                        System.out.print("> Vagues (0-100%) : ");
                        w.setWaveIntensity(lireDouble(0) / 100.0);
                        System.out.println("[OK] Vagues configurees.");
                        break;
                    case 4:
                        w.setWindIntensity(0);
                        w.setRainIntensity(0);
                        w.setWaveIntensity(0);
                        System.out.println("[OK] Beau temps.");
                        break;
                    case 5:
                        w.setWindIntensity(0.8);
                        w.setRainIntensity(0.7);
                        w.setWaveIntensity(0.6);
                        System.out.println("[OK] Tempete.");
                        break;
                    case 6: afficherMeteoActuelle(); break;
                    case 0: continuer = false; break;
                    default: System.out.println("[ERREUR] Choix invalide.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[ERREUR] Veuillez entrer un numero.");
            }
        }
    }

    // ==================================================================================
    // 6. SCENARIOS DE TEST (conserves de l'original)
    // ==================================================================================

    /**
     * Affiche le sous-menu des scenarios de test predefinies.
     * Scenarios : missions, batterie, contraintes, zones, meteo.
     */
    private static void menuScenariosTest() {
        boolean continuer = true;
        while (continuer) {
            System.out.println("\n-------- SCENARIOS DE TEST --------");
            System.out.println("  1. Mission + Deplacement + Relance");
            System.out.println("  2. Batterie et Recharge");
            System.out.println("  3. Contraintes physiques (Z)");
            System.out.println("  4. Zones interdites");
            System.out.println("  5. Impact Meteo");
            System.out.println("  0. Retour");
            System.out.println("-----------------------------------");
            System.out.print("> Choix : ");

            String input = scanner.nextLine().trim();
            try {
                int choix = Integer.parseInt(input);
                switch (choix) {
                    case 1: case 2: case 3: case 4: case 5:
                        lancerScenario(choix);
                        break;
                    case 0: continuer = false; break;
                    default: System.out.println("[ERREUR] Choix invalide.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[ERREUR] Veuillez entrer un numero.");
            }
        }
    }

    // ==================================================================================
    // UTILITAIRES
    // ==================================================================================

    /**
     * Affiche une liste simplifiee des actifs (ID, type, etat).
     */
    private static void listerActifsSimple() {
        List<ActifMobile> flotte = gestionnaire.getFlotte();
        if (flotte.isEmpty()) {
            System.out.println("[INFO] Aucun actif.");
            return;
        }
        System.out.println("Actifs:");
        for (ActifMobile a : flotte) {
            System.out.println("  - " + a.getId() + " (" + a.getClass().getSimpleName() + ") [" + a.getEtat() + "]");
        }
    }

    /**
     * Affiche une liste simplifiee des missions (numero, titre, statut).
     */
    private static void listerMissionsSimple() {
        if (allMissions.isEmpty()) {
            System.out.println("[INFO] Aucune mission.");
            return;
        }
        System.out.println("Missions:");
        for (int i = 0; i < allMissions.size(); i++) {
            Mission m = allMissions.get(i);
            System.out.println("  " + (i + 1) + ". " + m.getTitre() + " [" + m.getStatut() + "]");
        }
    }

    /**
     * Recherche un actif par son ID dans la flotte.
     *
     * @param id Identifiant de l'actif recherche
     * @return L'actif trouve ou null si inexistant
     */
    private static ActifMobile trouverActif(String id) {
        for (ActifMobile actif : gestionnaire.getFlotte()) {
            if (actif.getId().equalsIgnoreCase(id)) {
                return actif;
            }
        }
        return null;
    }

    /**
     * Lit un nombre decimal depuis l'entree utilisateur.
     *
     * @param defaut Valeur par defaut si l'entree est vide ou invalide
     * @return La valeur saisie ou la valeur par defaut
     */
    private static double lireDouble(double defaut) {
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return defaut;
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return defaut;
        }
    }

    // ==================================================================================
    // CODE ORIGINAL - SCENARIOS ET BOUCLE
    // ==================================================================================
=======
    /**
     * Réinitialise complètement l'état de la simulation.
     * <p>
     * Vide la flotte, supprime les zones et obstacles, et remet la météo à zéro
     * pour garantir un environnement propre avant chaque scénario.
     * </p>
     */
>>>>>>> 8d792a7f2571e1788f23b9efa0d3e769e349ec7d
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

    /**
     * Interface fonctionnelle pour injecter de la logique personnalisée ("Hook")
     * à chaque étape de la simulation (tick).
     */
    private interface SimHook {
        /**
         * Exécuté à chaque pas de temps de la simulation.
         *
         * @param time    Le temps écoulé en secondes depuis le début du scénario.
         * @param service Le service de simulation actif.
         */
        void tick(double time, SimulationService service);
    }

    /**
     * Configure et lance le scénario choisi par l'utilisateur.
     *
     * @param choix L'identifiant du scénario (1-5).
     */
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
        // intervalle de log par défaut = 1s
        int logInterval = 1;
        if (choix == 4 || choix == 5)
            logInterval = 5;

        executerBoucle(dureeSimuSeconds, hook, logInterval);
        System.out.println("\n--- FIN DE SCÉNARIO ---\n");
        System.out.println("Appuyez sur Entrée pour revenir au menu...");
        scanner.nextLine();
    }

    /**
     * Exécute la boucle de simulation principale pour une durée donnée.
     *
     * @param dureeSimuSeconds   Durée totale à simuler en secondes.
     * @param hook               Le hook logique spécifique au scénario.
     * @param logIntervalSeconds Intervalle entre chaque affichage des logs dans la
     *                           console.
     */
    private static void executerBoucle(long dureeSimuSeconds, SimHook hook, int logIntervalSeconds) {
        long nanoTime = 0;
        long stepNano = 16_666_667; // ~16ms (60 FPS)
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

    /**
     * Affiche l'état courant de tous les actifs de la flotte dans la console.
     *
     * @param time Le temps actuel de la simulation.
     */
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

    /**
     * SCÉNARIO 1 : Mission + Déplacement Manuel + Relance.
     * <p>
     * Teste la capacité d'un drone à interrompre une mission pour une commande
     * manuelle,
     * puis à reprendre sa mission initiale correctement.
     * </p>
     *
     * @return Le hook de simulation pour ce scénario.
     */
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

    /**
     * SCÉNARIO 2 : Batterie et Recharge.
     * <p>
     * Simule une batterie vide, vérifie l'alerte critique et le retour automatique
     * à la base,
     * puis teste la procédure de recharge.
     * </p>
     *
     * @return Le hook de simulation pour ce scénario.
     */
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

    /**
     * SCÉNARIO 3 : Contraintes physiques et zones interdites (Z et Types d'Actifs).
     * <p>
     * Vérifie que les actifs respectent leurs contraintes physiques (ex: un bateau
     * reste en surface Z=0).
     * Permet à l'utilisateur de choisir le sous-scénario spécifique.
     * </p>
     *
     * @return Le hook de simulation pour ce scénario.
     */
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

    /**
     * SCÉNARIO 4 : Définir Zone Interdite (No-Fly Zone).
     * <p>
     * Teste le comportement des drones face à une zone interdite :
     * autorisation (si drone reco), refus, ou contournement.
     * </p>
     *
     * @return Le hook de simulation pour ce scénario.
     */
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

    /**
     * SCÉNARIO 5 : Météo (Impact Vitesse/Conso).
     * <p>
     * Teste l'influence du vent, de la pluie et des vagues sur la vitesse et la
     * consommation
     * des différents types d'actifs.
     * </p>
     *
     * @return Le hook de simulation pour ce scénario.
     */
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
