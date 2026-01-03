package com.spiga.ui;

import com.spiga.core.SimulationService;
import com.spiga.core.SimConfig;
import com.spiga.core.SwarmValidator;
import com.spiga.core.ActifMobile;
import com.spiga.core.DroneReconnaissance;
import com.spiga.core.DroneLogistique;
import com.spiga.core.SousMarinExploration;
import com.spiga.management.GestionnaireEssaim;
import com.spiga.core.VehiculeSurface;
import com.spiga.management.Mission;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
// import javafx.scene.paint.Color; // unused
import java.util.function.Consumer;
// import java.util.ArrayList; // unused
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.logging.Logger;

/**
 * Contrôleur Principal : Chef d'Orchestre de l'Interface (MVC).
 * <p>
 * Cette classe fait le lien entre la Vue définie en FXML (l'interface
 * graphique) et le Modèle
 * (la logique de simulation). Elle gère les interactions utilisateur, la mise à
 * jour de l'affichage
 * en temps réel, et la délégation des commandes vers le service de simulation.
 * </p>
 *
 * <h3>CONCEPTS CLÉS (JAVA FX & MVC) :</h3>
 * <ul>
 * <li><b>MVC (Modèle-Vue-Contrôleur) :</b> Séparation stricte entre les données
 * ({@link SimulationService}, {@link GestionnaireEssaim}),
 * l'affichage (MainView.fxml, {@link MapPane}) et la logique de contrôle (cette
 * classe).</li>
 * <li><b>Injection de Dépendances (@FXML) :</b> JavaFX injecte automatiquement
 * les composants graphiques définis
 * dans le fichier FXML dans les variables annotées {@code @FXML}. Cela évite
 * l'instanciation manuelle.</li>
 * <li><b>Boucle d'Animation (AnimationTimer) :</b> Remplace la boucle
 * `while(true)` classique pour mettre à jour
 * l'interface graphique de manière fluide (60 FPS) sans bloquer le thread
 * principal.</li>
 * </ul>
 *
 * @author Equipe SPIGA
 * @version 1.0
 */
public class MainController {

    // --- VUE (Injection @FXML) ---
    // Ces champs correspondent aux fx:id dans MainView.fxml

    /** Conteneur principal pour l'affichage de la carte (vue de dessus). */
    @FXML
    private StackPane mapContainer;

    /** Conteneur pour l'affichage de la vue de profil (coupe latérale Z). */
    @FXML
    private StackPane sideViewContainer;

    @FXML
    private Label lblWindValue;
    @FXML
    private Label lblRainValue;
    @FXML
    private Label lblWavesValue;

    /** Label de statut général en bas de la fenêtre. */
    @FXML
    private Label lblStatus;

    // Sliders pour contrôler le MODÈLE (Météo et Vitesse)
    @FXML
    private Slider sliderSpeed;
    @FXML
    private Slider sliderWind;
    @FXML
    private Slider sliderRain;
    @FXML
    private Slider sliderWaves;

    // --- SOUS-CONTRÔLEURS (Composition UI) ---
    // JavaFX injecte aussi les contrôleurs des fichiers inclus (<fx:include>)

    /** Contrôleur du panneau latéral droit (détails et alertes). */
    @FXML
    private SidebarController sidebarController;

    /** Contrôleur du panneau de gestion des missions. */
    @FXML
    private MissionController missionPanelController;

    /** Le gestionnaire de la flotte d'actifs (Modèle). */
    private GestionnaireEssaim gestionnaire;

    /** Le service principal de simulation (Moteur physique et temporel). */
    private SimulationService simulationService;

    /** Composant graphique personnalisé pour la carte. */
    private MapPane mapPane;

    /** Composant graphique personnalisé pour la vue latérale. */
    private SideViewPane sideViewPane;

    /** Timer pour la mise à jour de l'interface graphique à chaque frame. */
    private AnimationTimer uiUpdateTimer;

    /** Type d'actif en attente de placement (curseur mode création). */
    private String pendingAssetType = null;

    private static final Logger logger = Logger.getLogger(MainController.class.getName());

    // Compteurs pour la génération d'IDs uniques
    private int countDroneRecon = 1;
    private int countDroneLog = 1;
    private int countBoat = 1;
    private int countSub = 1;

    /**
     * Méthode d'initialisation appelée automatiquement par JavaFX après l'injection
     * des composants FXML.
     * <p>
     * Configure le modèle, initialise les vues personnalisées ({@link MapPane},
     * {@link SideViewPane}),
     * configure les écouteurs d'événements (sliders, clics carte), et lance la
     * boucle de simulation.
     * </p>
     */
    public void initialize() {
        gestionnaire = new GestionnaireEssaim();
        simulationService = new SimulationService(gestionnaire);

        // 1. Initialisation de la Carte (Map Pane)
        mapPane = new MapPane();
        mapContainer.getChildren().add(mapPane);

        // 2. Initialisation de la Vue Latérale (Side View Pane)
        sideViewPane = new SideViewPane();
        sideViewContainer.getChildren().add(sideViewPane);
        // Liaison des dimensions pour le redimensionnement automatique
        sideViewPane.prefWidthProperty().bind(sideViewContainer.widthProperty());
        sideViewPane.prefHeightProperty().bind(sideViewContainer.heightProperty());

        // Initialisation des interactions Carte (Clics, Sélection)
        mapPane.setOnSelectionChanged(this::handleSelectionChanged);
        mapPane.setOnMapClicked(this::handleMapClicked);

        // Logique de centrage initial
        javafx.application.Platform.runLater(this::centerOnWorld);

        // Initialisation des Sliders de contrôle
        initSlider(sliderSpeed, SimConfig.DEFAULT_TIME_SCALE, val -> {
            simulationService.setTimeScale(val);
            updateStatusLabel();
        });

        // Configuration des Sliders Météo (Vent, Pluie, Vagues)
        initSlider(sliderWind, 0, val -> {
            if (simulationService.getWeather() != null)
                simulationService.getWeather().setWindIntensity(val / 100.0);
            if (lblWindValue != null)
                lblWindValue.setText(String.format("%.0f%%", val));
        });
        initSlider(sliderRain, 0, val -> {
            if (simulationService.getWeather() != null)
                simulationService.getWeather().setRainIntensity(val / 100.0);
            if (lblRainValue != null)
                lblRainValue.setText(String.format("%.0f%%", val));
        });
        initSlider(sliderWaves, 0, val -> {
            if (simulationService.getWeather() != null)
                simulationService.getWeather().setWaveIntensity(val / 100.0);
            if (lblWavesValue != null)
                lblWavesValue.setText(String.format("%.0f%%", val));
        });

        // Démarrage du moteur de simulation (Thread séparé implicite ou Timer interne)
        simulationService.startSimulation();

        // Configuration des sous-contrôleurs
        if (sidebarController != null) {
            sidebarController.setGestionnaire(gestionnaire);
            sidebarController.setMainController(this);
        }
        if (missionPanelController != null) {
            missionPanelController.setGestionnaire(gestionnaire);
            missionPanelController.setMainController(this);
        }

        // Lancement de la boucle de rafraîchissement UI
        startUIUpdateLoop();
        lblStatus.setText("Simulation prête - Ajoutez des actifs pour commencer");
    }

    /**
     * Initialise un slider avec une valeur de départ et une action à exécuter lors
     * du changement.
     *
     * @param slider       Le composant Slider à configurer.
     * @param initialValue La valeur initiale.
     * @param action       L'action (Consumer) à exécuter avec la nouvelle valeur.
     */
    private void initSlider(Slider slider, double initialValue, Consumer<Double> action) {
        if (slider != null) {
            // Ajustement spécifique pour le slider de vitesse (Min 1.0, Max 30.0)
            if (slider == sliderSpeed) {
                slider.setMin(1.0);
                slider.setMax(30.0);
            }
            slider.setValue(initialValue);
            action.accept(initialValue); // Appliquer la valeur initiale
            slider.valueProperty().addListener((obs, oldVal, newVal) -> action.accept(newVal.doubleValue()));
        }
    }

    /**
     * Centre la vue sur le monde (placeholder pour future implémentation de
     * centrage).
     * Actuellement géré par l'auto-scale du {@link MapPane}.
     */
    private void centerOnWorld() {
        // Auto-centering handled by MapPane auto-scale
    }

    /**
     * Démarre la boucle d'incrustation (Game Loop) pour l'interface graphique.
     * Utilise {@link AnimationTimer} pour viser 60 FPS.
     */
    private void startUIUpdateLoop() {
        uiUpdateTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateUI();
            }
        };
        uiUpdateTimer.start();
    }

    private long frameCount = 0;

    /**
     * Met à jour tous les éléments de l'interface graphique à chaque frame.
     * <p>
     * Synchronise la vue avec l'état actuel du modèle (positions, statuts, météo).
     * Effectue également des vérifications périodiques (santé, proximité) moins
     * fréquemment.
     * </p>
     */
    private void updateUI() {
        List<ActifMobile> assets = gestionnaire.getFlotte();
        // Mise à jour de la Carte
        mapPane.update(assets, simulationService.getObstacles(), simulationService.getRestrictedZones());

        // Mise à jour de la Vue Latérale
        if (sideViewPane != null) {
            ActifMobile selected = null;
            if (mapPane != null && !mapPane.getSelectedAssets().isEmpty()) {
                selected = mapPane.getSelectedAssets().get(0);
            }
            sideViewPane.update(assets, simulationService.getRestrictedZones(), simulationService.getObstacles(),
                    selected);
        }

        // Vérifications périodiques (toutes les ~60 frames, soit environ 1 seconde)
        frameCount++;
        if (frameCount % 60 == 0) {
            checkProximityAndAlert(assets);
        }

        // Rafraîchissement des panneaux latéraux
        if (sidebarController != null)
            sidebarController.refresh();
        if (missionPanelController != null)
            missionPanelController.refresh();

        updateStatusLabel();
    }

    /**
     * Vérifie les règles de proximité et génère des alertes UI si nécessaire.
     *
     * @param assets La liste des actifs à vérifier.
     */
    private void checkProximityAndAlert(List<ActifMobile> assets) {
        if (sidebarController == null) {
            return;
        }

        for (ActifMobile a1 : assets) {
            // 1. Vérification de Proximité (SwarmValidator)
            List<ActifMobile> others = assets.stream().filter(a -> a != a1).collect(Collectors.toList());
            if (!SwarmValidator.isPlacementValid(a1.getX(), a1.getY(), a1.getZ(), others)) {
                String msg = a1.getId() + " Trop Proche! (Avoidance Active)";
                sidebarController.addAlert(msg);
            }

            // 1b. Vérification Physique / Collisions
            if (a1.getCollisionWarning() != null) {
                String msg = a1.getId() + ": " + a1.getCollisionWarning();
                sidebarController.addAlert(msg);
                a1.setCollisionWarning(null); // Reset après affichage
            }

            // 2. Alertes Niveau de la Mer (Protection Splashdown pour Aériens)
            if (a1 instanceof com.spiga.core.ActifAerien) {
                if (a1.getZ() <= 1.0) { // Tolérance < 1m
                    if (a1.getState() != ActifMobile.AssetState.STOPPED) {
                        a1.setState(ActifMobile.AssetState.STOPPED);
                        a1.setZ(2.0); // Hover de sécurité
                        a1.setTarget(a1.getX(), a1.getY(), 2.0);

                        String msg = "ALERTE MER: " + a1.getId() + " (ARRÊT)";
                        sidebarController.addAlert(msg);
                    }
                }
                // Vérification Plafond Max (150m)
                if (a1.getZ() >= 150.0) {
                    if (a1.getState() != ActifMobile.AssetState.STOPPED) {
                        a1.setState(ActifMobile.AssetState.STOPPED);
                        a1.setZ(149.0); // Clamp
                        a1.setTarget(a1.getX(), a1.getY(), 149.0);

                        String msg = "PLAFOND (150m): " + a1.getId() + " (ARRÊT)";
                        sidebarController.addAlert(msg);
                    }
                }
            }

            // 3. Contraintes de Profondeur Sous-Marins (-150m)
            if (a1 instanceof com.spiga.core.VehiculeSousMarin) {
                if (a1.getZ() <= -150.0) {
                    if (a1.getState() != ActifMobile.AssetState.STOPPED) {
                        a1.setState(ActifMobile.AssetState.STOPPED);
                        a1.setZ(-149.0); // Clamp
                        a1.setTarget(a1.getX(), a1.getY(), -149.0);

                        String msg = "FOND (-150m): " + a1.getId() + " (ARRÊT)";
                        sidebarController.addAlert(msg);
                    }
                }
            }
        }
    }

    /**
     * Met à jour le label de statut principal avec les infos météo ou la sélection
     * courante.
     */
    private void updateStatusLabel() {
        if (lblStatus == null)
            return;

        if (mapPane.isMissionTargetMode())
            return;

        List<ActifMobile> selected = mapPane.getSelectedAssets();
        if (!selected.isEmpty()) {
            if (selected.size() == 1) {
                ActifMobile a = selected.get(0);
                lblStatus.setText("Sélectionné: " + a.getId() + " (" + a.getClass().getSimpleName() + ")");
            } else {
                lblStatus.setText(selected.size() + " actifs sélectionnés");
            }
            return;
        }

        if (simulationService.getWeather() != null) {
            String weatherInfo = String.format("Vent: %.0f km/h | Pluie: %.0f%%",
                    simulationService.getWeather().getWindSpeed(),
                    simulationService.getWeather().getRainIntensity() * 100);
            lblStatus.setText(weatherInfo + " | Vitesse: x" + String.format("%.1f", sliderSpeed.getValue()));
        }
    }

    /**
     * Callback déclenché lors d'une sélection via le panneau latéral.
     * Synchronise la sélection sur la carte.
     *
     * @param assets La liste des actifs sélectionnés dans la liste latérale.
     */
    public void onSidebarSelection(List<ActifMobile> assets) {
        mapPane.selectAll(assets);
        updateStatusLabel();
    }

    /**
     * Callback déclenché lors d'un changement de sélection sur la carte.
     * Synchronise la sélection dans le panneau latéral.
     *
     * @param selection La liste des actifs nouvellement sélectionnés sur la carte.
     */
    private void handleSelectionChanged(List<ActifMobile> selection) {
        if (sidebarController != null) {
            sidebarController.selectAssets(selection);
        }

        if (selection.isEmpty()) {
            // cleared
        } else if (selection.size() == 1) {
            // detail updated by selectAssets or separate call?
        } else {
            lblStatus.setText(selection.size() + " actifs sélectionnés");
        }
        updateStatusLabel();
    }

    /**
     * Gère les clics sur la carte principale.
     * <p>
     * Le comportement dépend du contexte :
     * 1. Mode Création (Ajout d'actif).
     * 2. Mode Ciblage de Mission (Définition d'un point destination).
     * 3. Mode Déplacement Manuel (Si des actifs sont sélectionnés).
     * 4. Sinon, désélection générale.
     * </p>
     *
     * @param coords Les coordonnées [x, y] du clic dans le monde.
     */
    private void handleMapClicked(double[] coords) {
        // 1. Mode Création d'Actif (Priorité)
        if (pendingAssetType != null) {
            createAssetAt(pendingAssetType, coords[0], coords[1]);
            pendingAssetType = null;
            disableMissionTargetMode();
            lblStatus.setText("Actif ajouté à (" + (int) coords[0] + ", " + (int) coords[1] + ")");
            return;
        }

        // 2. Mode Ciblage de Mission
        if (mapPane.isMissionTargetMode()) {
            // Demande de l'altitude Z
            TextInputDialog dialog = new TextInputDialog("0");
            dialog.setTitle("Cible Mission");
            dialog.setHeaderText("Définir l'altitude/profondeur de la cible");
            dialog.setContentText("Destination Z (m) :");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                try {
                    double z = Double.parseDouble(result.get().replace(",", "."));
                    if (missionPanelController != null) {
                        missionPanelController.setTargetCoordinates(coords[0], coords[1], z);
                        disableMissionTargetMode();
                    }
                    lblStatus.setText(
                            "Cible définie: (" + (int) coords[0] + ", " + (int) coords[1] + ", " + (int) z + ")");
                } catch (NumberFormatException e) {
                    sidebarController.addAlert("Z Invalide. Cible ignorée.");
                }
            } else {
                sidebarController.addAlert("Définition cible annulée.");
                disableMissionTargetMode();
            }
            return;
        }

        // 3. Déplacement Manuel Implicite (Si actifs sélectionnés)
        List<ActifMobile> selected = mapPane.getSelectedAssets();
        if (selected != null && !selected.isEmpty()) {
            executeManualMove(selected, coords);
            return;
        }

        // 4. Par défaut : Désélection (Clic dans le vide)
        mapPane.deselectAll();
    }

    /**
     * Exécute une commande de déplacement manuel pour un groupe d'actifs.
     * <p>
     * Calcule la destination et demande éventuellement une altitude Z si nécessaire
     * (sauf pour les navires de surface qui restent à Z=0).
     * </p>
     */
    private void executeManualMove(List<ActifMobile> selected, double[] coords) {
        if (selected == null || selected.isEmpty())
            return;

        boolean allSurface = selected.stream().allMatch(a -> a instanceof com.spiga.core.VehiculeSurface);

        if (allSurface) {
            // Auto Z=0 pour les bateaux
            performManualMoveInternal(selected, coords[0], coords[1], 0.0);
        } else {
            // Calculer Z moyen pour pré-remplir
            double avgZ = selected.stream().mapToDouble(ActifMobile::getZ).average().orElse(0.0);

            // Dialogue pour Z
            TextInputDialog dialog = new TextInputDialog(String.format("%.1f", avgZ));
            dialog.setTitle("Déplacement Manuel");
            dialog.setHeaderText("Définir destination");
            dialog.setContentText("Altitude / Profondeur Z (m) :");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(zStr -> {
                try {
                    double targetZ = Double.parseDouble(zStr.replace(",", "."));
                    performManualMoveInternal(selected, coords[0], coords[1], targetZ);
                } catch (NumberFormatException e) {
                    sidebarController.addAlert("Valeur Z invalide. Déplacement annulé.");
                }
            });
        }
    }

    /**
     * Applique le déplacement manuel interne, gère la formation en essaim et les
     * pauses de mission.
     */
    private void performManualMoveInternal(List<ActifMobile> selected, double x, double y, double z) {
        boolean violationDetected = false;

        // Logique d'essaim : Distribuer les cibles en cercle si > 1 actif
        boolean distribute = selected.size() > 1;
        double radius = 50.0; // Rayon de formation 50m
        double angleStep = (2 * Math.PI) / selected.size();
        double currentAngle = 0.0;

        for (ActifMobile asset : selected) {
            // Mettre en PAUSE toute mission en cours
            if (asset.getCurrentMission() != null) {
                com.spiga.management.Mission m = asset.getCurrentMission();

                if (m.getStatut() == com.spiga.management.Mission.StatutMission.EN_COURS) {
                    m.pause();
                    if (sidebarController != null)
                        sidebarController.addAlert("Mission PAUSED for " + asset.getId());
                }
            }

            // Calcul Coordonnées Cible
            double tx = x;
            double ty = y;

            if (distribute) {
                tx = x + radius * Math.cos(currentAngle);
                ty = y + radius * Math.sin(currentAngle);
                currentAngle += angleStep;
            }

            // Définition de la cible (Override)
            asset.setTarget(tx, ty, z);

            // Vérification de validation de zone interdite (via warning collision)
            if (asset.getCollisionWarning() != null &&
                    asset.getCollisionWarning().contains("ZONE_VIOLATION")) {
                violationDetected = true;
                asset.setCollisionWarning(null);
            } else {
                // Forcer l'état pour assurer le mouvement
                asset.setState(ActifMobile.AssetState.MOVING_TO_TARGET);
            }
        }

        if (violationDetected) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Zone Interdite");
            alert.setHeaderText("Déplacement Refusé");
            alert.setContentText("La cible se trouve dans une Zone Interdite !");
            alert.showAndWait();
        } else {
            String msg = String.format("Déplacement: %d actifs vers (%.0f, %.0f, %.0f) [Formation Dispersée]",
                    selected.size(), x, y, z);
            if (lblStatus != null)
                lblStatus.setText(msg);
        }
    }

    // --- VISUAL HELPERS ---

    public void showMissionTargetMarker(double x, double y, double z) {
        if (mapPane != null) {
            mapPane.setTemporaryTarget(x, y);
        }

    }

    public void hideMissionTargetMarker() {
        if (mapPane != null) {
            mapPane.clearTemporaryTarget();
        }

    }

    // --- Gestion de la Création d'Actifs (Dialogues) ---

    @FXML
    private void handleAddDrone() {
        // Demande type Drone
        List<String> choices = List.of("Reconnaissance", "Logistique");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Reconnaissance", choices);
        dialog.setTitle("Choix du Drone");
        dialog.setHeaderText("Sélectionnez le type de drone :");
        dialog.setContentText("Type:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(typeChoice -> {
            String subType = typeChoice.equals("Reconnaissance") ? "DRONE_RECON" : "DRONE_LOG";
            promptForCreationMethod("Ajouter Drone (" + typeChoice + ")", subType);
        });
    }

    @FXML
    private void handleAddBoat() {
        promptForCreationMethod("Ajouter Navire", "BOAT");
    }

    @FXML
    private void handleAddSub() {
        promptForCreationMethod("Ajouter Sous-Marin", "SUB");
    }

    @FXML
    private void handleSelectAll() {
        mapPane.selectAll(gestionnaire.getFlotte());
        lblStatus.setText("Tous les actifs sélectionnés");
    }

    // --- Démarrage Multi-Mission ---
    @FXML
    public void handleStartSelectedMissions() {
        List<ActifMobile> selected = mapPane.getSelectedAssets();
        if (selected == null || selected.isEmpty()) {
            showAlert("Start SM", "Aucun actif sélectionné.");
            return;
        }

        long simTime = System.currentTimeMillis() / 1000;
        int actionCount = 0;

        for (ActifMobile asset : selected) {
            Mission m = asset.getCurrentMission();
            if (m == null) {
                logger.warning("⚠️ Asset " + asset.getId() + ": Pas de mission assignée.");
                continue;
            }

            if (m.getStatut() == Mission.StatutMission.PLANIFIEE) {
                m.start(simTime);
                actionCount++;
            } else if (m.getStatut() == Mission.StatutMission.TERMINEE ||
                    m.getStatut() == Mission.StatutMission.ECHOUEE ||
                    m.getStatut() == Mission.StatutMission.ANNULEE) {
                m.restart(simTime);
                actionCount++;
            } else if (m.getStatut() == Mission.StatutMission.PAUSED) {
                m.resume(simTime);
                actionCount++;
            } else if (m.getStatut() == Mission.StatutMission.EN_COURS) {
                // Déjà en cours, on ignore
            }
        }

        if (actionCount > 0) {
            lblStatus.setText("SM Start: " + actionCount + " missions lancées/re-lancées.");
            sidebarController.addAlert("SM Start: " + actionCount + " missions started/restarted.");
            refreshSidebar();
        } else {
            sidebarController.addAlert("Aucune mission éligible (Planifiée/Terminée) sur la sélection.");
        }
    }

    // --- DEMO COLLISION START ---
    @FXML
    public void handleDemoCollision() {
        try {
            gestionnaire.getFlotte().clear();
            mapPane.deselectAll();
            if (sidebarController != null)
                sidebarController.clearDetails();

            // Création de 2 Drones pour collision frontale
            DroneReconnaissance d1 = new DroneReconnaissance("Drone Demo 1", 200, 500, 100);
            DroneReconnaissance d2 = new DroneReconnaissance("Drone Demo 2", 800, 500, 100);

            gestionnaire.ajouterActif(d1);
            gestionnaire.ajouterActif(d2);

            // Forces Cibles Frontales
            d1.setTarget(800, 500, 100);
            d2.setTarget(200, 500, 100);

            d1.demarrer();
            d2.demarrer();
            d1.setState(ActifMobile.AssetState.MOVING_TO_TARGET);
            d2.setState(ActifMobile.AssetState.MOVING_TO_TARGET);

            lblStatus.setText("DEMO: Collision Course Engaged!");
            if (sidebarController != null)
                sidebarController.addAlert("DEMO STARTED: Collision Course");
        } catch (Exception e) {
            logger.warning("Demo Error: " + e.getMessage());
        }
    }

    private void promptForCreationMethod(String title, String type) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText("Choisissez la méthode de placement");

        ButtonType cursorBtn = new ButtonType("Curseur (Carte)", ButtonBar.ButtonData.OK_DONE);
        ButtonType manualBtn = new ButtonType("Manuel (Coordonnées)", ButtonBar.ButtonData.APPLY);
        ButtonType cancelBtn = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(cursorBtn, manualBtn, cancelBtn);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == cursorBtn)
                return "CURSOR";
            if (buttonType == manualBtn)
                return "MANUAL";
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(mode -> {
            if ("CURSOR".equals(mode)) {
                pendingAssetType = type;
                enableMissionTargetMode();
                lblStatus.setText("Cliquez sur la carte pour placer l'actif...");
            } else if ("MANUAL".equals(mode)) {
                if (type.startsWith("DRONE"))
                    promptForDroneManual(type);
                else if ("BOAT".equals(type))
                    promptForBoatManual();
                else if ("SUB".equals(type))
                    promptForSubManual();
            }
        });
    }

    private void createAssetAt(String type, double x, double y) {
        // --- VÉRIFICATION SÉCURITÉ (Placement) ---
        if (!SwarmValidator.isPlacementValid(x, y, 0, gestionnaire.getFlotte())) {
            String msg = "Placement Curseur refusé (Prox.)";
            if (sidebarController != null)
                sidebarController.addAlert(msg);
            showAlert("Placement Invalide",
                    "Trop proche d'un autre actif !\nDistance min requise: " + SimConfig.MIN_DISTANCE);
            return;
        }

        if (type.startsWith("DRONE")) {
            promptForZ("Altitude Drone", "Entrez l'altitude [1 - 150]:", 50.0).ifPresent(z -> {
                if (z < 1 || z > 150) {
                    showAlert("Erreur", "Altitude hors limite [1 - 150].");
                    return;
                }
                if ("DRONE_RECON".equals(type)) {
                    String id = String.format("Drone Recon %03d", countDroneRecon++);
                    gestionnaire.ajouterActif(new DroneReconnaissance(id, x, y, z));
                } else {
                    String id = String.format("Drone Logistique %03d", countDroneLog++);
                    gestionnaire.ajouterActif(new DroneLogistique(id, x, y, z));
                }
            });
        } else if ("BOAT".equals(type)) {
            String id = String.format("Navire %03d", countBoat++);
            gestionnaire.ajouterActif(new VehiculeSurface(id, x, y));
        } else if ("SUB".equals(type)) {
            promptForZ("Profondeur Sous-Marin", "Entrez la profondeur (Z < 0):", -50.0).ifPresent(z -> {
                if (z >= 0) {
                    showAlert("Erreur", "Profondeur < 0 requise.");
                    return;
                }
                String id = String.format("Sous-Marin %03d", countSub++);
                gestionnaire.ajouterActif(new SousMarinExploration(id, x, y, z));
            });
        }
    }

    private Optional<Double> promptForZ(String title, String content, double defaultZ) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(defaultZ));
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(content);

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                return Optional.of(Double.parseDouble(result.get()));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private void promptForDroneManual(String type) {
        promptForCoordinates("Ajouter Drone", "Zone Aérienne (Z > 0)", (x, y, z) -> {
            if (z <= 0) {
                if (sidebarController != null)
                    sidebarController.addAlert("Placement Drone refusé (Alt. invalide)");
                showAlert("Erreur", "Altitude > 0 requise.");
                return;
            }
            // --- VÉRIFICATION SÉCURITÉ ---
            if (!SwarmValidator.isPlacementValid(x, y, z, gestionnaire.getFlotte())) {
                String msg = "Placement refusé (Prox.): Drone vs Essaim";
                if (sidebarController != null)
                    sidebarController.addAlert(msg);
                showAlert("Placement Invalide",
                        "Trop proche d'un autre actif !\nDistance min requise: " + SimConfig.MIN_DISTANCE + "m");
                return;
            }

            if ("DRONE_RECON".equals(type)) {
                String id = String.format("Drone Recon %03d", countDroneRecon++);
                gestionnaire.ajouterActif(new DroneReconnaissance(id, x, y, z));
            } else {
                String id = String.format("Drone Logistique %03d", countDroneLog++);
                gestionnaire.ajouterActif(new DroneLogistique(id, x, y, z));
            }
        });
    }

    private void promptForBoatManual() {
        promptForCoordinates("Ajouter Navire", "Surface (Z = 0)", (x, y, z) -> {
            if (!SwarmValidator.isPlacementValid(x, y, z, gestionnaire.getFlotte())) {
                if (sidebarController != null)
                    sidebarController.addAlert("Placement Navire refusé (Prox.)");
                showAlert("Placement Invalide", "Trop proche d'un autre actif !");
                return;
            }
            String id = String.format("Navire %03d", countBoat++);
            gestionnaire.ajouterActif(new VehiculeSurface(id, x, y));
        });
    }

    private void promptForSubManual() {
        promptForCoordinates("Ajouter Sous-Marin", "Zone Sous-Marine (Z < 0)", (x, y, z) -> {
            if (z >= 0) {
                if (sidebarController != null)
                    sidebarController.addAlert("Placement Sous-Marin refusé (Prof. invalide)");
                showAlert("Erreur", "Profondeur < 0 requise.");
                return;
            }
            if (!SwarmValidator.isPlacementValid(x, y, z, gestionnaire.getFlotte())) {
                if (sidebarController != null)
                    sidebarController.addAlert("Placement Sous-Marin refusé (Prox.)");
                showAlert("Placement Invalide", "Trop proche d'un autre actif !");
                return;
            }
            String id = String.format("Sous-Marin %03d", countSub++);
            gestionnaire.ajouterActif(new SousMarinExploration(id, x, y, z));
        });
    }

    private interface CoordinateConsumer {
        void accept(double x, double y, double z);
    }

    private void promptForCoordinates(String title, String guide, CoordinateConsumer action) {
        TextInputDialog dialog = new TextInputDialog("500,500,50");
        dialog.setTitle(title);
        dialog.setHeaderText("Entrez les coordonnées (x,y,z)");
        dialog.setContentText("Format: x,y,z\nGuide: " + guide);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try {
                String[] parts = input.split(",");
                if (parts.length == 3) {
                    double x = Double.parseDouble(parts[0].trim());
                    double y = Double.parseDouble(parts[1].trim());
                    double z = Double.parseDouble(parts[2].trim());
                    logger.info("DEBUG: Manual Input Recu: " + x + ", " + y + ", " + z);
                    action.accept(x, y, z);
                } else {
                    showAlert("Erreur", "Format invalide. Utilisez x,y,z");
                }
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Valeurs numériques requises.");
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void enableMissionTargetMode() {
        mapPane.setMissionTargetMode(true);
    }

    public void disableMissionTargetMode() {
        mapPane.setMissionTargetMode(false);
    }

    public void assignMissionToSelected(Mission mission) {
        List<ActifMobile> selected = mapPane.getSelectedAssets();
        if (!selected.isEmpty()) {
            gestionnaire.demarrerMission(mission, selected);
            lblStatus.setText("Mission assignée à " + selected.size() + " actifs");
        } else {
            showAlert("Attention", "Aucun actif sélectionné pour la mission.");
        }
    }

    public void refreshSidebar() {
        if (sidebarController != null) {
            sidebarController.refresh();
        }
    }

    public List<ActifMobile> getSelectedAssets() {
        return mapPane.getSelectedAssets();
    }

    public void removeAsset(ActifMobile asset) {
        if (asset != null && gestionnaire != null) {
            gestionnaire.supprimerActif(asset.getId());
            mapPane.deselectAll();
            if (sidebarController != null) {
                sidebarController.refresh();
            }
        }
    }
}
