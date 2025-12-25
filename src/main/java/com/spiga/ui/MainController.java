package com.spiga.ui;

import com.spiga.core.*;
import com.spiga.management.*;
import com.spiga.environment.ZoneOperation;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.animation.AnimationTimer;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MainController - PRODUCTION READY
 * Handles asset creation with validation, mission assignment, and UI
 * coordination.
 */
public class MainController {

    @FXML
    private StackPane mapContainer;
    @FXML
    private StackPane sideViewContainer;
    @FXML
    private Label lblStatus;
    @FXML
    private Slider sliderSpeed;
    @FXML
    private Slider sliderWind;
    @FXML
    private Slider sliderRain;
    @FXML
    private Slider sliderWaves;
    @FXML
    private SidebarController sidebarController;
    @FXML
    private MissionController missionPanelController;
    @FXML
    private Button btnPlayPause;

    private GestionnaireEssaim gestionnaire;
    private SimulationService simulationService;
    private MapCanvas mapCanvas;
    private SideViewCanvas sideViewCanvas;
    private AnimationTimer uiUpdateTimer;
    private ZoneOperation zone;
    private boolean isPaused = false;
    private String pendingAssetType = null;

    // ID Counters
    private int countDroneRecon = 1;
    private int countDroneLog = 1;
    private int countBoat = 1;
    private int countSub = 1;

    public void initialize() {
        gestionnaire = new GestionnaireEssaim();
        zone = new ZoneOperation(0, 1000, 0, 1000);
        simulationService = new SimulationService(gestionnaire, zone);

        // 1. Map Canvas (Top Down)
        mapCanvas = new MapCanvas(1200, 900, zone);
        mapContainer.getChildren().add(mapCanvas);

        mapCanvas.setOnSelectionChanged(this::handleSelectionChanged);
        mapCanvas.setOnMapClicked(this::handleMapClicked);

        // 2. Side View Canvas (Profile)
        if (sideViewContainer != null) {
            sideViewCanvas = new SideViewCanvas(800, 200);
            sideViewContainer.getChildren().add(sideViewCanvas);
            sideViewCanvas.widthProperty().bind(sideViewContainer.widthProperty());
            sideViewCanvas.heightProperty().bind(sideViewContainer.heightProperty());
        }

        if (sliderSpeed != null) {
            sliderSpeed.setMin(0.1);
            sliderSpeed.setMax(5.0);
            sliderSpeed.setValue(1.0);
            sliderSpeed.valueProperty().addListener((obs, oldVal, newVal) -> {
                simulationService.setTimeScale(newVal.doubleValue());
                updateStatusLabel();
            });
        }

        // Weather Binds
        if (sliderWind != null) {
            sliderWind.valueProperty().addListener((obs, o, n) -> {
                if (simulationService.getWeather() != null)
                    simulationService.getWeather().setWindSpeed(n.doubleValue());
            });
        }
        if (sliderRain != null) {
            sliderRain.valueProperty().addListener((obs, o, n) -> {
                if (simulationService.getWeather() != null)
                    simulationService.getWeather().setRainIntensity(n.doubleValue());
            });
        }
        if (sliderWaves != null) {
            sliderWaves.valueProperty().addListener((obs, o, n) -> {
                if (simulationService.getWeather() != null)
                    simulationService.getWeather().setSeaWaveHeight(n.doubleValue());
            });
        }

        simulationService.startSimulation();

        if (sidebarController != null) {
            sidebarController.setGestionnaire(gestionnaire);
            sidebarController.setMainController(this);
        }
        if (missionPanelController != null) {
            missionPanelController.setGestionnaire(gestionnaire);
            missionPanelController.setMainController(this);
        }

        startUIUpdateLoop();
        lblStatus.setText("Simulation prête - Ajoutez des actifs pour commencer");
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
        // simulationService.update(); // Removed if undefined, usually managed by timer
        // or internal logic
        List<ActifMobile> assets = gestionnaire.getFlotte();
        // Use MapCanvas to draw
        mapCanvas.draw(assets, simulationService.getObstacles());
        if (sideViewCanvas != null) {
            sideViewCanvas.draw(assets, simulationService.getObstacles());
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
            System.out.println("DEBUG: SidebarController is NULL inside checkProximityAndAlert");
            return;
        }

        for (ActifMobile a1 : assets) {
            // 1. Proximity Check
            List<ActifMobile> others = assets.stream().filter(a -> a != a1).collect(Collectors.toList());
            if (!SwarmValidator.isPlacementValid(a1.getX(), a1.getY(), others)) {
                // STOP THE DRONE
                if (a1.getState() != ActifMobile.AssetState.STOPPED) {
                    a1.setState(ActifMobile.AssetState.STOPPED);
                    a1.setTarget(a1.getX(), a1.getY(), a1.getZ()); // Reset Target

                    String msg = "⚠️ " + a1.getId() + " Trop Proche! (ARRÊT)";
                    sidebarController.addAlert(msg);
                    System.out.println("DEBUG: Violation stop triggered for " + a1.getId());
                }
            }

            // 2. Sea Level Alert & Block (Splashdown Protection)
            if (a1 instanceof com.spiga.core.ActifAerien) {
                // Splashdown Check
                if (a1.getZ() <= 1.0) { // Tolerance < 1m
                    if (a1.getState() != ActifMobile.AssetState.STOPPED) {
                        a1.setState(ActifMobile.AssetState.STOPPED);
                        a1.setZ(2.0); // Hover
                        a1.setTarget(a1.getX(), a1.getY(), 2.0);

                        String msg = "🌊 ALERTE MER: " + a1.getId() + " (ARRÊT)";
                        sidebarController.addAlert(msg);
                        System.out.println("DEBUG: Sea level stop triggered for " + a1.getId());
                    }
                }
                // Max Altitude Check (150m)
                if (a1.getZ() >= 150.0) {
                    if (a1.getState() != ActifMobile.AssetState.STOPPED) {
                        a1.setState(ActifMobile.AssetState.STOPPED);
                        a1.setZ(149.0); // Clamp
                        a1.setTarget(a1.getX(), a1.getY(), 149.0);

                        String msg = "⚠️ PLAFOND (150m): " + a1.getId() + " (ARRÊT)";
                        sidebarController.addAlert(msg);
                        System.out.println("DEBUG: Max Altitude stop for " + a1.getId());
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

                        String msg = "⚠️ FOND (-150m): " + a1.getId() + " (ARRÊT)";
                        sidebarController.addAlert(msg);
                        System.out.println("DEBUG: Max Depth stop for " + a1.getId());
                    }
                }
            }
        }
    }

    private void updateStatusLabel() {
        if (lblStatus == null)
            return;

        if (mapCanvas.isMissionTargetMode())
            return;

        List<ActifMobile> selected = mapCanvas.getSelectedAssets();
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
                    simulationService.getWeather().getRainIntensity());
            lblStatus.setText(weatherInfo + " | Vitesse: x" + String.format("%.1f", sliderSpeed.getValue()));
        }
    }

    public void onSidebarSelection(ActifMobile asset) {
        mapCanvas.selectAsset(asset);
    }

    private void handleSelectionChanged(List<ActifMobile> selectedAssets) {
        ActifMobile selected = selectedAssets.isEmpty() ? null : selectedAssets.get(0);

        if (sidebarController != null) {
            sidebarController.selectAsset(selected);
        }

        if (missionPanelController != null) {
            missionPanelController.onAssetSelected(selected);
        }

        updateStatusLabel();
    }

    private void handleMapClicked(double[] coords) {
        // 1. Creation Mode
        if (pendingAssetType != null) {
            createAssetAt(pendingAssetType, coords[0], coords[1]);
            pendingAssetType = null;
            disableMissionTargetMode();
            lblStatus.setText("Actif ajouté à (" + (int) coords[0] + ", " + (int) coords[1] + ")");
            return;
        }

        // 2. Mission Target Mode
        if (mapCanvas.isMissionTargetMode()) {
            if (missionPanelController != null) {
                missionPanelController.setMissionTarget(coords[0], coords[1]);
            }
            lblStatus.setText("Cible définie: (" + (int) coords[0] + ", " + (int) coords[1] + ")");
            return;
        }

        // 3. Click-to-Move (Instant Navigation & Swarm)
        List<ActifMobile> selected = mapCanvas.getSelectedAssets();
        if (!selected.isEmpty()) {
            // Swarm Logic: Calculate Centroid
            double sumX = 0, sumY = 0;
            boolean has3D = false;
            double defaultZ = 0;

            for (ActifMobile a : selected) {
                sumX += a.getX();
                sumY += a.getY();
                if (a instanceof ActifAerien || a instanceof VehiculeSousMarin) {
                    has3D = true;
                    defaultZ = a.getZ(); // Use last 3D asset's Z as default
                }
            }
            double centroidX = sumX / selected.size();
            double centroidY = sumY / selected.size();

            // Prompt for Z if applicable
            double finalZ = defaultZ;
            boolean zUpdated = false;
            if (has3D) {
                Optional<Double> zInput = promptForZ("Déplacement de Groupe",
                        "Entrez l'altitude/profondeur cible (ou Annuler pour garder actuel):",
                        defaultZ);
                if (zInput.isPresent()) {
                    finalZ = zInput.get();
                    zUpdated = true;
                } else {
                    // If cancelled, do we cancel move or just keep Z?
                    // User might want to move X/Y but keep Z.
                    // Let's assume Cancel means "Cancel Move" to be safe/consistent with single
                    // move.
                    mapCanvas.deselectAll();
                    return;
                }
            }

            // Dispatch Missions
            for (ActifMobile asset : selected) {
                double offsetX = asset.getX() - centroidX;
                double offsetY = asset.getY() - centroidY;
                double targetX = coords[0] + offsetX;
                double targetY = coords[1] + offsetY;
                double targetZ = zUpdated ? finalZ : asset.getZ();

                Mission navMission = new MissionLogistique("Déplacement Rapide");
                navMission.setTarget(targetX, targetY, targetZ);
                gestionnaire.demarrerMission(navMission, List.of(asset));
            }

            lblStatus.setText("🚀 Déplacement de l'essaim (" + selected.size() + " actifs) vers (" + (int) coords[0]
                    + ", " + (int) coords[1] + ")");
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
        mapCanvas.selectAll(gestionnaire.getFlotte());
        lblStatus.setText("Tous les actifs sélectionnés");
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
        if (!SwarmValidator.isPlacementValid(x, y, gestionnaire.getFlotte())) {
            String msg = "Placement Curseur refusé (Prox.)";
            if (sidebarController != null)
                sidebarController.addAlert(msg);
            showAlert("Placement Invalide",
                    "Trop proche d'un autre actif !\nDistance min requise: " + SimConfig.MIN_DISTANCE);
            return;
        }
        // --------------------

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
            gestionnaire.ajouterActif(new NavirePatrouille(id, x, y));
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
            if (!SwarmValidator.isPlacementValid(x, y, gestionnaire.getFlotte())) {
                String msg = "Placement refusé (Prox.): Drone vs Essaim";
                if (sidebarController != null)
                    sidebarController.addAlert(msg);
                showAlert("Placement Invalide",
                        "Trop proche d'un autre actif !\nDistance min requise: " + SimConfig.MIN_DISTANCE + "m");
                return;
            }
            // --------------------

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
            if (!SwarmValidator.isPlacementValid(x, y, gestionnaire.getFlotte())) {
                if (sidebarController != null)
                    sidebarController.addAlert("Placement Navire refusé (Prox.)");
                showAlert("Placement Invalide", "Trop proche d'un autre actif !");
                return;
            }
            String id = String.format("Navire %03d", countBoat++);
            gestionnaire.ajouterActif(new NavirePatrouille(id, x, y));
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
            if (!SwarmValidator.isPlacementValid(x, y, gestionnaire.getFlotte())) {
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
                    System.out.println("DEBUG: Manual Input Recu: " + x + ", " + y + ", " + z);
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

    @FXML
    private void handleStartMission() {
        // --- FINAL SWARM VALIDATION ---
        // Check if any drone is too close to another (in case drag moved them, or
        // initial placement)
        List<ActifMobile> fleet = gestionnaire.getFlotte();
        for (ActifMobile a1 : fleet) {
            if (!SwarmValidator.isPlacementValid(a1.getX(), a1.getY(),
                    fleet.stream().filter(a -> a != a1).collect(Collectors.toList()))) {
                showAlert("Impossible de lancer",
                        "L'essaim n'est pas sécurisé !\nCertains drones sont trop proches.\nVérifiez les zones rouges.");
                return;
            }
        }
        // ------------------------------

        // Generic Start/Assign Mission Logic
        if (missionPanelController != null) {
            Mission selectedMission = missionPanelController.getSelectedMission();
            List<ActifMobile> selectedAssets = mapCanvas.getSelectedAssets();

            if (selectedMission != null && !selectedAssets.isEmpty()) {

                // STRICT COMPATIBILITY CHECK
                for (ActifMobile asset : selectedAssets) {
                    boolean compatible = false;
                    String title = selectedMission.getTitre();

                    if (asset instanceof DroneReconnaissance && title.contains("Surveillance"))
                        compatible = true;
                    else if (asset instanceof DroneLogistique && title.contains("Logistique"))
                        compatible = true;
                    else if (asset instanceof VehiculeSurface && title.contains("Surface"))
                        compatible = true;
                    else if (asset instanceof VehiculeSousMarin && title.contains("Underwater"))
                        compatible = true;
                    // Fallback for generic movement "Déplacement" which any can do? Use
                    // "Déplacement" for explicit move commands
                    else if (title.contains("Déplacement"))
                        compatible = true;

                    if (!compatible) {
                        showAlert("Erreur de Compatibilité",
                                "L'actif " + asset.getId() + " (" + asset.getClass().getSimpleName() +
                                        ") ne peut pas effectuer la mission '" + title + "'.\n" +
                                        "Types requis: Surveillance->Drone, Logistique->Cargo, Surface->Boat.");
                        return; // Abort whole operation
                    }
                }

                if (selectedAssets.size() == 1) {
                    // Single Asset
                    Mission missionToStart = selectedMission;
                    if (selectedMission.getStatut() != Mission.StatutMission.PLANIFIEE) {
                        missionToStart = selectedMission.copy();
                        missionPanelController.addMission(missionToStart);
                    }
                    gestionnaire.demarrerMission(missionToStart, selectedAssets);
                    lblStatus.setText("Mission '" + missionToStart.getTitre() + "' démarrée.");
                } else {
                    // Multi Asset - Formation
                    double centerX = selectedMission.getTargetX();
                    double centerY = selectedMission.getTargetY();
                    double centerZ = selectedMission.getTargetZ();
                    double radius = 30.0 * Math.max(1, selectedAssets.size() / 2.0);
                    double angleStep = 2 * Math.PI / selectedAssets.size();

                    for (int i = 0; i < selectedAssets.size(); i++) {
                        ActifMobile asset = selectedAssets.get(i);
                        double angle = i * angleStep;
                        double offsetX = radius * Math.cos(angle);
                        double offsetY = radius * Math.sin(angle);

                        Mission missionClone = selectedMission.copy();
                        missionClone.setTarget(centerX + offsetX, centerY + offsetY, centerZ);
                        gestionnaire.demarrerMission(missionClone, List.of(asset));
                    }
                    lblStatus.setText("Mission de groupe démarrée pour " + selectedAssets.size() + " actifs.");
                }
                missionPanelController.refresh();
            } else {
                showAlert("Attention", "Sélectionnez une mission dans la liste ET un actif sur la carte.");
            }
        }
    }

    @FXML
    private void handlePlayPause() {
        if (isPaused) {
            simulationService.startSimulation();
            if (btnPlayPause != null)
                btnPlayPause.setText("Pause");
            isPaused = false;
        } else {
            simulationService.stopSimulation();
            if (btnPlayPause != null)
                btnPlayPause.setText("Play");
            isPaused = true;
        }
    }

    public void enableMissionTargetMode() {
        mapCanvas.setMissionTargetMode(true);
    }

    public void disableMissionTargetMode() {
        mapCanvas.setMissionTargetMode(false);
    }

    public void assignMissionToSelected(Mission mission) {
        List<ActifMobile> selected = mapCanvas.getSelectedAssets();
        if (!selected.isEmpty()) {
            gestionnaire.demarrerMission(mission, selected);
            lblStatus.setText("Mission assignée à " + selected.size() + " actifs");
        } else {
            showAlert("Attention", "Aucun actif sélectionné pour la mission.");
        }
    }

    public List<ActifMobile> getSelectedAssets() {
        return mapCanvas.getSelectedAssets();
    }
}
