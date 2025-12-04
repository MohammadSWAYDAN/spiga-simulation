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

    private void updateUI() {
        mapCanvas.draw(gestionnaire.getFlotte(), simulationService.getObstacles());
        if (sideViewCanvas != null) {
            sideViewCanvas.draw(gestionnaire.getFlotte(), simulationService.getObstacles());
        }

        if (sidebarController != null)
            sidebarController.refresh();
        if (missionPanelController != null)
            missionPanelController.refresh();

        updateStatusLabel();
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
        promptForCreationMethod("Ajouter Drone", "DRONE");
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
                if ("DRONE".equals(type))
                    promptForDroneManual();
                else if ("BOAT".equals(type))
                    promptForBoatManual();
                else if ("SUB".equals(type))
                    promptForSubManual();
            }
        });
    }

    private void createAssetAt(String type, double x, double y) {
        if ("DRONE".equals(type)) {
            promptForZ("Altitude Drone", "Entrez l'altitude (Z > 0):", 100.0).ifPresent(z -> {
                if (z <= 0) {
                    showAlert("Erreur", "Altitude > 0 requise.");
                    return;
                }
                gestionnaire.ajouterActif(new DroneReconnaissance("DR-" + System.currentTimeMillis(), x, y, z));
            });
        } else if ("BOAT".equals(type)) {
            gestionnaire.ajouterActif(new NavirePatrouille("NV-" + System.currentTimeMillis(), x, y));
        } else if ("SUB".equals(type)) {
            promptForZ("Profondeur Sous-Marin", "Entrez la profondeur (Z < 0):", -50.0).ifPresent(z -> {
                if (z >= 0) {
                    showAlert("Erreur", "Profondeur < 0 requise.");
                    return;
                }
                gestionnaire.ajouterActif(new SousMarinExploration("SUB-" + System.currentTimeMillis(), x, y, z));
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

    private void promptForDroneManual() {
        promptForCoordinates("Ajouter Drone", "Zone Aérienne (Z > 0)", (x, y, z) -> {
            if (z <= 0) {
                showAlert("Erreur", "Altitude > 0 requise.");
                return;
            }
            gestionnaire.ajouterActif(new DroneReconnaissance("DR-" + System.currentTimeMillis(), x, y, z));
        });
    }

    private void promptForBoatManual() {
        promptForCoordinates("Ajouter Navire", "Surface (Z = 0)", (x, y, z) -> {
            gestionnaire.ajouterActif(new NavirePatrouille("NV-" + System.currentTimeMillis(), x, y));
        });
    }

    private void promptForSubManual() {
        promptForCoordinates("Ajouter Sous-Marin", "Zone Sous-Marine (Z < 0)", (x, y, z) -> {
            if (z >= 0) {
                showAlert("Erreur", "Profondeur < 0 requise.");
                return;
            }
            gestionnaire.ajouterActif(new SousMarinExploration("SUB-" + System.currentTimeMillis(), x, y, z));
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
        // Generic Start/Assign Mission Logic
        if (missionPanelController != null) {
            Mission selectedMission = missionPanelController.getSelectedMission();
            List<ActifMobile> selectedAssets = mapCanvas.getSelectedAssets();

            if (selectedMission != null && !selectedAssets.isEmpty()) {

                if (selectedAssets.size() == 1) {
                    // Single Asset - Direct Assignment (Clone if needed)
                    Mission missionToStart = selectedMission;
                    if (selectedMission.getStatut() != Mission.StatutMission.PLANIFIEE) {
                        missionToStart = selectedMission.copy();
                        missionPanelController.addMission(missionToStart);
                    }
                    gestionnaire.demarrerMission(missionToStart, selectedAssets);
                    lblStatus.setText("Mission '" + missionToStart.getTitre() + "' démarrée.");
                } else {
                    // Multi Asset - Formation Assignment
                    double centerX = selectedMission.getTargetX();
                    double centerY = selectedMission.getTargetY();
                    double centerZ = selectedMission.getTargetZ();
                    double radius = 30.0 * Math.max(1, selectedAssets.size() / 2.0); // Dynamic radius
                    double angleStep = 2 * Math.PI / selectedAssets.size();

                    for (int i = 0; i < selectedAssets.size(); i++) {
                        ActifMobile asset = selectedAssets.get(i);

                        // Calculate formation position
                        double angle = i * angleStep;
                        double offsetX = radius * Math.cos(angle);
                        double offsetY = radius * Math.sin(angle);

                        // Clone mission for this specific asset
                        Mission missionClone = selectedMission.copy();
                        missionClone.setTarget(centerX + offsetX, centerY + offsetY, centerZ);

                        // Add to list if it's a new plan (optional, maybe clutter? Let's add for
                        // visibility)
                        // missionPanelController.addMission(missionClone);
                        // Actually, let's NOT add every single formation clone to the list to avoid
                        // spam.
                        // We just start it.

                        gestionnaire.demarrerMission(missionClone, List.of(asset));
                    }
                    lblStatus.setText(
                            "Mission de groupe démarrée (Formation Cercle) pour " + selectedAssets.size() + " actifs.");
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
