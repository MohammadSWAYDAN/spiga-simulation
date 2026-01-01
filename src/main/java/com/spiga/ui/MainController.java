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
 * Controleur Principal : Chef d'Orchestre de l'Interface
 * 
 * CONCEPTS CLES (JAVA FX & MVC) :
 * 
 * 1. MVC (Modele-Vue-Controleur) :
 * - C'est quoi ? Separer les donnees (Modele), l'affichage (Vue FXML) et la
 * logique (Controleur).
 * - Ici : Cette classe est le Controleur. Elle fait le lien entre la Vue
 * (boutons, sliders) et le Modele (SimulationService).
 * 
 * 2. Injection de Dependances (@FXML) :
 * - C'est quoi ? JavaFX "injecte" automatiquement les objets graphiques dans
 * nos variables.
 * - Pourquoi ? Pas besoin de faire slider = new Slider(). Si l'ID dans le FXML
 * correspond au nom la variable, c'est magique !
 * 
 * 3. Gestion d'Evenements :
 * - C'est quoi ? Reagir aux actions de l'utilisateur.
 * - Exemple : Quand on clique sur "Play", la methode liee reagit.
 */
public class MainController {

    // --- VUE (Injection @FXML) ---
    // Ces champs correspondent aux fx:id dans MainView.fxml

    @FXML
    private StackPane mapContainer; // Zone d'affichage carte
    @FXML
    private StackPane sideViewContainer; // Zone d'affichage profil
    @FXML
    private Label lblWindValue;
    @FXML
    private Label lblRainValue;
    @FXML
    private Label lblWavesValue;

    @FXML
    private Label lblStatus;

    // Sliders pour contrôler le MODÈLE (Météo)
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
    @FXML
    private SidebarController sidebarController;
    @FXML
    private MissionController missionPanelController;

    private GestionnaireEssaim gestionnaire;
    private SimulationService simulationService;
    private MapPane mapPane; // Replaces MapCanvas
    private SideViewPane sideViewPane; // Replaces SideViewCanvas

    private AnimationTimer uiUpdateTimer;

    private String pendingAssetType = null;
    private static final Logger logger = Logger.getLogger(MainController.class.getName());

    // ID Counters
    private int countDroneRecon = 1;
    private int countDroneLog = 1;
    private int countBoat = 1;
    private int countSub = 1;

    public void initialize() {
        gestionnaire = new GestionnaireEssaim();
        simulationService = new SimulationService(gestionnaire);

        // 1. Map Pane (Scene Graph)
        mapPane = new MapPane();
        mapContainer.getChildren().add(mapPane);

        // 2. Side View Pane (Node-based)
        sideViewPane = new SideViewPane();
        sideViewContainer.getChildren().add(sideViewPane);
        // Bind size to container to ensure BorderPane fills space
        sideViewPane.prefWidthProperty().bind(sideViewContainer.widthProperty());
        sideViewPane.prefHeightProperty().bind(sideViewContainer.heightProperty());

        // Initialize MapPane Interactions
        mapPane.setOnSelectionChanged(this::handleSelectionChanged);
        mapPane.setOnMapClicked(this::handleMapClicked); // Primary handler
        // mapPane.setOnMapRightClicked(this::handleMapRightClicked);

        // Center View Logic
        javafx.application.Platform.runLater(this::centerOnWorld);

        // Initialize Sliders
        initSlider(sliderSpeed, SimConfig.DEFAULT_TIME_SCALE, val -> {
            simulationService.setTimeScale(val);
            updateStatusLabel();
        });

        // Wind/Rain/Waves with Labels
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

        simulationService.startSimulation();

        if (sidebarController != null) {
            sidebarController.setGestionnaire(gestionnaire);
            sidebarController.setMainController(this);
            // Setup Delete Callback
            // Setup Delete Callback (removed)
            // sidebarController.setOnDeleteAction(this::removeAsset);
        }
        if (missionPanelController != null) {
            missionPanelController.setGestionnaire(gestionnaire);
            missionPanelController.setMainController(this);
        }

        startUIUpdateLoop();
        lblStatus.setText("Simulation prête - Ajoutez des actifs pour commencer");
    }

    private void initSlider(Slider slider, double initialValue, Consumer<Double> action) {
        if (slider != null) {
            // Default min/max are usually 0-100, adjust if needed
            if (slider == sliderSpeed) {
                slider.setMin(1.0);
                slider.setMax(30.0);
            }
            slider.setValue(initialValue);
            action.accept(initialValue); // Apply initial value
            slider.valueProperty().addListener((obs, oldVal, newVal) -> action.accept(newVal.doubleValue()));
        }
    }

    private void centerOnWorld() {
        // Auto-centering handled by MapPane auto-scale
    }

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

    private void updateUI() {
        List<ActifMobile> assets = gestionnaire.getFlotte();
        // Use MapPane to update (Scene Graph)
        mapPane.update(assets, simulationService.getObstacles(), simulationService.getRestrictedZones());

        if (sideViewPane != null) {
            ActifMobile selected = null;
            if (mapPane != null && !mapPane.getSelectedAssets().isEmpty()) {
                selected = mapPane.getSelectedAssets().get(0);
            }
            sideViewPane.update(assets, simulationService.getRestrictedZones(), simulationService.getObstacles(),
                    selected);
        }

        // Periodic Health Check (every ~60 frames or 1 sec)
        frameCount++;
        if (frameCount % 60 == 0) {
            checkProximityAndAlert(assets);
        }

        if (sidebarController != null)
            sidebarController.refresh();
        if (missionPanelController != null)
            missionPanelController.refresh();

        updateStatusLabel();
    }

    private void checkProximityAndAlert(List<ActifMobile> assets) {
        if (sidebarController == null) {
            return;
        }

        for (ActifMobile a1 : assets) {
            // 1. Proximity Check (SwarmValidator)
            List<ActifMobile> others = assets.stream().filter(a -> a != a1).collect(Collectors.toList());
            if (!SwarmValidator.isPlacementValid(a1.getX(), a1.getY(), a1.getZ(), others)) {
                String msg = a1.getId() + " Trop Proche! (Avoidance Active)";
                sidebarController.addAlert(msg);
            }

            // 1b. PHYSICS/EARLY WARNING CHECK
            if (a1.getCollisionWarning() != null) {
                String msg = a1.getId() + ": " + a1.getCollisionWarning();
                sidebarController.addAlert(msg);
                a1.setCollisionWarning(null);
            }

            // 2. Sea Level Alert & Block (Splashdown Protection)
            if (a1 instanceof com.spiga.core.ActifAerien) {
                // Splashdown Check
                if (a1.getZ() <= 1.0) { // Tolerance < 1m
                    if (a1.getState() != ActifMobile.AssetState.STOPPED) {
                        a1.setState(ActifMobile.AssetState.STOPPED);
                        a1.setZ(2.0); // Hover
                        a1.setTarget(a1.getX(), a1.getY(), 2.0);

                        String msg = "ALERTE MER: " + a1.getId() + " (ARRÊT)";
                        sidebarController.addAlert(msg);
                    }
                }
                // Max Altitude Check (150m)
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

            // 3. Submarine Depth Constraints (-150m)
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

    public void onSidebarSelection(List<ActifMobile> assets) {
        mapPane.selectAll(assets);
        updateStatusLabel();
    }

    private void handleSelectionChanged(List<ActifMobile> selection) {
        if (sidebarController != null) {
            sidebarController.selectAssets(selection);
        }

        if (selection.isEmpty()) {
            // cleared
        } else if (selection.size() == 1) {
            // detail updated by selectAssets or separate call?
            // selectAssets should handle details if single.
        } else {
            lblStatus.setText(selection.size() + " actifs sélectionnés");
        }
        updateStatusLabel();
    }

    private void handleMapClicked(double[] coords) {
        // 1. Asset Creation Mode (Priority)
        if (pendingAssetType != null) {
            createAssetAt(pendingAssetType, coords[0], coords[1]);
            pendingAssetType = null;
            disableMissionTargetMode();
            lblStatus.setText("Actif ajouté à (" + (int) coords[0] + ", " + (int) coords[1] + ")");
            return;
        }

        // 2. Mission Target Mode
        if (mapPane.isMissionTargetMode()) {
            // Ask for Z
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

        // 3. Implicit Manual Move (If assets selected)
        List<ActifMobile> selected = mapPane.getSelectedAssets();
        if (selected != null && !selected.isEmpty()) {
            executeManualMove(selected, coords);
            return;
        }

        // 4. Default: Deselect (Background Click)
        mapPane.deselectAll();
    }

    private void executeManualMove(List<ActifMobile> selected, double[] coords) {
        if (selected == null || selected.isEmpty())
            return;

        // HEAVY REFACTOR: Check if we can skip the dialog (Boats only)
        boolean allSurface = selected.stream().allMatch(a -> a instanceof com.spiga.core.VehiculeSurface);

        if (allSurface) {
            // Auto Z=0 for Boats
            performManualMoveInternal(selected, coords[0], coords[1], 0.0);
        } else {
            // Calculate Average Z for pre-fill or default
            double avgZ = selected.stream().mapToDouble(ActifMobile::getZ).average().orElse(0.0);

            // Show Dialog for Z
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

    private void performManualMoveInternal(List<ActifMobile> selected, double x, double y, double z) {
        boolean violationDetected = false;

        // SWARM LOGIC: Distribute targets if > 1 asset
        boolean distribute = selected.size() > 1;
        double radius = 50.0; // 50m radius formation
        double angleStep = (2 * Math.PI) / selected.size();
        double currentAngle = 0.0;

        for (ActifMobile asset : selected) {
            // Check for active mission and PAUSE it
            if (asset.getCurrentMission() != null) {
                com.spiga.management.Mission m = asset.getCurrentMission();

                if (m.getStatut() == com.spiga.management.Mission.StatutMission.EN_COURS) {
                    m.pause();
                    if (sidebarController != null)
                        sidebarController.addAlert("Mission PAUSED for " + asset.getId());
                }
            }

            // Calculate Target Coordinates
            double tx = x;
            double ty = y;

            if (distribute) {
                tx = x + radius * Math.cos(currentAngle);
                ty = y + radius * Math.sin(currentAngle);
                currentAngle += angleStep;
            }

            // Set Target (Manual override)
            asset.setTarget(tx, ty, z);

            // Check for zone violation via warning (replaces exception pattern)
            if (asset.getCollisionWarning() != null &&
                    asset.getCollisionWarning().contains("ZONE_VIOLATION")) {
                violationDetected = true;
                asset.setCollisionWarning(null); // Clear after handling
            } else {
                // Force state update to ensure movement registers
                asset.setState(ActifMobile.AssetState.MOVING_TO_TARGET);
            }
        }

        if (violationDetected) {
            // Show POPUP as requested
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

    // Deprecated method removed

    @SuppressWarnings("unused")
    private void handleSideViewZSelection(double targetZ) {
        List<ActifMobile> selected = mapPane.getSelectedAssets();
        if (selected == null || selected.isEmpty()) {
            return;
        }

        // Filter valid assets (ignore Mission state)
        List<ActifMobile> eligibleAssets = selected.stream()
                .filter(a -> a.getEtat() != ActifMobile.EtatOperationnel.EN_MISSION)
                .collect(Collectors.toList());

        if (eligibleAssets.isEmpty()) {
            sidebarController.addAlert("Impossible de changer l'altitude (En Mission)");
            return;
        }

        for (ActifMobile asset : eligibleAssets) {
            // Check physical constraints
            if (asset instanceof com.spiga.core.VehiculeSurface) {
                // Surface boats cannot change Z
                continue;
            }
            if (asset instanceof com.spiga.core.ActifAerien && targetZ < 0) {
                // Prevent drones underwater
                continue;
            }
            if (asset instanceof com.spiga.core.VehiculeSousMarin && targetZ > 0) {
                // Prevent subs flying
                continue;
            }

            // Apply Z change, keep current TargetXY
            asset.setTarget(asset.getTargetX(), asset.getTargetY(), targetZ);
            asset.setState(ActifMobile.AssetState.MOVING_TO_TARGET);
        }

        lblStatus.setText(String.format("Altitude/Profondeur ajustée : %.0fm", targetZ));
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

    // --- Asset Creation with Dialogs ---

    @FXML
    private void handleAddDrone() {
        // Ask for Drone Type
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

    // --- SM MULTI-MISSION START ---
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
                logger.warning("⚠️ Asset " + asset.getId() + ": No mission assigned.");
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
                // Ignore
            }
        }

        if (actionCount > 0) {
            lblStatus.setText("SM Start: " + actionCount + " missions lancées/re-lancées.");
            sidebarController.addAlert("SM Start: " + actionCount + " missions started/restarted.");
            refreshSidebar(); // Assume this method exists or remove if not needed, but context implies we
                              // should refresh sidebar
        } else {
            sidebarController.addAlert("Aucune mission éligible (Planifiée/Terminée) sur la sélection.");
        }
    }

    // refreshSidebar() moved to bottom of class to avoid duplicates

    // --- DEMO COLLISION START ---
    @FXML
    public void handleDemoCollision() {
        // Clear existing
        try {
            gestionnaire.getFlotte().clear();
            mapPane.deselectAll();
            if (sidebarController != null)
                sidebarController.clearDetails();

            // Spawn 2 Drones
            DroneReconnaissance d1 = new DroneReconnaissance("Drone Demo 1", 200, 500, 100);
            DroneReconnaissance d2 = new DroneReconnaissance("Drone Demo 2", 800, 500, 100);

            gestionnaire.ajouterActif(d1);
            gestionnaire.ajouterActif(d2);

            // Force Targets Head-On
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
        // --- SAFETY CHECK ---
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
            // --- SAFETY CHECK ---
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

    // Duplicate handleStartMission removed.
    // The actual logic is located around line 626.

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
            // Use Manager to remove (SimulationService wraps it but might not expose remove
            // directly)
            gestionnaire.supprimerActif(asset.getId());
            mapPane.deselectAll();
            if (sidebarController != null) {
                sidebarController.refresh();
            }
        }
    }
}
