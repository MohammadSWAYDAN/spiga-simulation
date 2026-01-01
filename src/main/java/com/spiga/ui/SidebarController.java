package com.spiga.ui;

import com.spiga.core.ActifMobile;
import com.spiga.management.GestionnaireEssaim;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controleur de la Barre Laterale (Details).
 */
public class SidebarController {

    @FXML
    private ListView<String> listAssets;
    @FXML
    private Label lblId;
    @FXML
    private Label lblType;
    @FXML
    private Label lblState;
    @FXML
    private ProgressBar progressBattery;
    @FXML
    private Label lblBatteryPercent;
    @FXML
    private Label lblPosition;
    @FXML
    private ComboBox<com.spiga.management.Mission> cmbMissions;
    private boolean isUpdatingDetails = false;
    @FXML
    private Label lblSpeed;
    @FXML
    private ListView<String> listAlerts;
    @FXML
    private Button btnRecharge;
    @FXML
    private Label lblMissionStatusBadge;
    @FXML
    private Button btnStartMission;
    @FXML
    private Button btnStopMission;

    private boolean isUpdatingSelection = false;
    private GestionnaireEssaim gestionnaire;
    private ActifMobile selectedAsset;
    private MainController mainController;

    public void addAlert(String message) {
        if (listAlerts != null) {
            Platform.runLater(() -> {
                listAlerts.getItems().add(0, message); // Add to top
                if (listAlerts.getItems().size() > 50) {
                    listAlerts.getItems().remove(50); // Keep last 50
                }
            });
        }
    }

    public void setGestionnaire(GestionnaireEssaim gestionnaire) {
        this.gestionnaire = gestionnaire;
        refreshList(); // Initial populate
    }

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    @FXML
    public void initialize() {
        if (btnRecharge != null) {
            btnRecharge.setOnAction(e -> handleRecharge());
            btnRecharge.setDisable(true); // Initially disabled
        }
        if (btnStartMission != null) {
            btnStartMission.setOnAction(e -> handleStartMission());
        }
        if (btnStopMission != null) {
            btnStopMission.setOnAction(e -> handleStopMission());
        }

        if (cmbMissions != null) {
            // Setup Converter
            cmbMissions.setConverter(new javafx.util.StringConverter<com.spiga.management.Mission>() {
                @Override
                public String toString(com.spiga.management.Mission m) {
                    if (m == null)
                        return "Aucune";
                    return m.getTitre();
                }

                @Override
                public com.spiga.management.Mission fromString(String string) {
                    return null; // Not needed
                }
            });

            // Setup Listener
            cmbMissions.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (isUpdatingDetails || newVal == null || selectedAsset == null)
                    return;

                // User Selected a new mission from dropdown
                selectedAsset.promoteMission(newVal);
                updateDetails(selectedAsset); // Refresh to show new Active state
            });
        }

        if (listAssets != null) {
            listAssets.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            // Selection Listener (Sidebar -> Map)
            listAssets.getSelectionModel().getSelectedItems()
                    .addListener((javafx.collections.ListChangeListener.Change<? extends String> c) -> {
                        if (isUpdatingSelection)
                            return;

                        List<String> selectedIds = listAssets.getSelectionModel().getSelectedItems();
                        if (gestionnaire != null && mainController != null) {
                            List<ActifMobile> selectedAssets = gestionnaire.getFlotte().stream()
                                    .filter(a -> selectedIds.contains(a.getId()))
                                    .collect(Collectors.toList());
                            mainController.onSidebarSelection(selectedAssets);
                        }
                    });

            // Custom Cell Factory for Delete Button
            listAssets.setCellFactory(param -> new ListCell<String>() {
                private final HBox content = new HBox();
                private final Label lblName = new Label();
                private final Region spacer = new Region();
                private final Button btnDelete = new Button("X");

                {
                    content.setSpacing(10);
                    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                    btnDelete.setStyle(
                            "-fx-background-color: #ffcccc; -fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 10px;");
                    content.getChildren().addAll(lblName, spacer, btnDelete);

                    btnDelete.setOnAction(e -> {
                        String assetId = getItem();
                        if (assetId != null) {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("Suppression");
                            alert.setHeaderText("Supprimer " + assetId + " ?");
                            alert.setContentText("Confirmer suppression ?");

                            alert.showAndWait().ifPresent(response -> {
                                if (response == ButtonType.OK) {
                                    gestionnaire.supprimerActif(assetId);
                                    getListView().getItems().remove(assetId);
                                    if (selectedAsset != null && selectedAsset.getId().equals(assetId)) {
                                        SidebarController.this.clearDetails();
                                        selectedAsset = null;
                                    }
                                    if (mainController != null)
                                        mainController.onSidebarSelection(java.util.Collections.emptyList());
                                }
                            });
                        }
                    });
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        lblName.setText(item);
                        setGraphic(content);
                        setWrapText(true);
                        setStyle("-fx-font-size: 10px;");
                    }
                }
            });
        }

        if (listAlerts != null) {
            listAlerts.setCellFactory(param -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        // Strip icons
                        String cleanText = item.replaceAll("[⚠️⛔ℹ️🚩🔄?]", "").trim();
                        if (cleanText.startsWith(":"))
                            cleanText = cleanText.substring(1).trim();

                        setText(cleanText);
                        setWrapText(true);
                        // Red Background and Dark Red Text
                        setStyle(
                                "-fx-background-color: #ffcccc; -fx-text-fill: #900000; -fx-padding: 5px; -fx-border-width: 0 0 1 0; -fx-border-color: #ffaaaa;");
                    }
                }
            });
        }
    }

    private void handleStartMission() {
        if (selectedAsset == null)
            return;
        com.spiga.management.Mission m = selectedAsset.getCurrentMission();
        if (m == null) {
            addAlert("Aucune mission assignée.");
            return;
        }

        long simTime = System.currentTimeMillis() / 1000;
        switch (m.getStatut()) {
            case PLANIFIEE:
                m.start(simTime);
                addAlert("Mission lancée: " + m.getTitre());
                break;
            case TERMINEE:
            case ECHOUEE:
            case ANNULEE:
                m.restart(simTime);
                addAlert("Mission relancée: " + m.getTitre());
                break;
            case PAUSED:
                m.resume(simTime);
                addAlert("Mission reprise: " + m.getTitre());
                break;
            case EN_COURS:
                // Already running
                break;
        }
        updateDetails(selectedAsset);
    }

    private void handleStopMission() {
        if (selectedAsset == null)
            return;
        com.spiga.management.Mission m = selectedAsset.getCurrentMission();
        if (m == null)
            return;

        if (m.getStatut() == com.spiga.management.Mission.StatutMission.EN_COURS ||
                m.getStatut() == com.spiga.management.Mission.StatutMission.PAUSED) {
            m.cancel("Arrêt Manuel (Utilisateur)");
            addAlert("Mission arrêtée: " + m.getTitre());
            updateDetails(selectedAsset);
        }
    }

    private void handleRecharge() {
        if (selectedAsset != null) {
            selectedAsset.recharger(); // Resets autonomie & state
            updateDetails(selectedAsset); // Refresh UI
        }
<<<<<<< HEAD
    }

    @FXML
    public void handleTestCollision() {
        if (mainController != null) {
            mainController.handleDemoCollision();
        }
=======
>>>>>>> 2e1c7d997378ffc2a62a0fdc8796641db0ce29fa
    }

    public void refresh() {
        if (gestionnaire == null)
            return;

        List<String> currentIds = gestionnaire.getFlotte().stream().map(ActifMobile::getId)
                .collect(Collectors.toList());
        if (!currentIds.equals(listAssets.getItems())) {
            String selected = listAssets.getSelectionModel().getSelectedItem();
            listAssets.getItems().setAll(currentIds);
            if (selected != null && currentIds.contains(selected)) {
                listAssets.getSelectionModel().select(selected);
            }
        }

        if (selectedAsset != null) {
            updateDetails(selectedAsset);
        }
    }

    public void selectAssets(List<ActifMobile> assets) {
        if (listAssets == null)
            return;

        isUpdatingSelection = true;
        try {
<<<<<<< HEAD
            // Robust selection handling
            List<String> allIds = listAssets.getItems();

            if (assets == null || assets.isEmpty()) {
                if (!listAssets.getSelectionModel().getSelectedItems().isEmpty()) {
                    listAssets.getSelectionModel().clearSelection();
                }
                this.selectedAsset = null;
                clearDetails();
            } else {
                // Determine indices to select
                int[] indices = assets.stream()
                        .map(ActifMobile::getId)
                        .mapToInt(id -> allIds.indexOf(id))
                        .filter(i -> i >= 0)
                        .toArray();

                if (indices.length > 0) {
                    // Use atomic selectIndices if possible
                    if (!listAssets.getSelectionModel().getSelectedItems().isEmpty()) {
                        listAssets.getSelectionModel().clearSelection();
                    }

                    if (indices.length == 1) {
                        listAssets.getSelectionModel().select(indices[0]);
                    } else {
                        // Varargs helper
                        int first = indices[0];
                        int[] rest = new int[indices.length - 1];
                        System.arraycopy(indices, 1, rest, 0, rest.length);
                        listAssets.getSelectionModel().selectIndices(first, rest);
                    }
                } else {
                    listAssets.getSelectionModel().clearSelection();
=======
            listAssets.getSelectionModel().clearSelection();

            if (assets != null && !assets.isEmpty()) {
                for (ActifMobile a : assets) {
                    listAssets.getSelectionModel().select(a.getId());
>>>>>>> 2e1c7d997378ffc2a62a0fdc8796641db0ce29fa
                }

                if (assets.size() == 1) {
                    this.selectedAsset = assets.get(0);
                    updateDetails(this.selectedAsset);
                } else {
                    this.selectedAsset = null;
<<<<<<< HEAD
                    clearDetails(assets.size());
                }
            }
        } catch (Exception e) {
            // Prevent UI Thread crash
            System.err.println("Safely caught UI Selection exception: " + e.getMessage());
=======
                    clearDetails(assets.size()); // Overload or just clear
                }
            } else {
                this.selectedAsset = null;
                clearDetails();
            }
>>>>>>> 2e1c7d997378ffc2a62a0fdc8796641db0ce29fa
        } finally {
            isUpdatingSelection = false;
        }
    }

    public void selectAsset(ActifMobile asset) {
        selectAssets(asset == null ? java.util.Collections.emptyList() : java.util.Collections.singletonList(asset));
    }

    private void refreshList() {
        if (gestionnaire == null || listAssets == null)
            return;
        List<String> currentIds = gestionnaire.getFlotte().stream().map(ActifMobile::getId)
                .collect(Collectors.toList());
        listAssets.getItems().setAll(currentIds);
    }

    private void updateDetails(ActifMobile asset) {
        if (asset == null) {
            clearDetails();
            return;
        }

        Platform.runLater(() -> {
            if (btnRecharge != null)
                btnRecharge.setDisable(false);

            if (lblId != null)
                lblId.setText("ID: " + asset.getId());
            if (lblType != null)
                lblType.setText("Type: " + asset.getClass().getSimpleName());

            if (lblState != null) {
                lblState.setText("State: " + asset.getState());
                if (asset.getState() == ActifMobile.AssetState.LOW_BATTERY
                        || asset.getState() == ActifMobile.AssetState.STOPPED) {
                    lblState.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                } else if (asset.getState() == ActifMobile.AssetState.RETURNING_TO_BASE) {
                    lblState.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                } else {
                    lblState.setStyle("-fx-text-fill: black;");
                }
            }
            if (progressBattery != null) {
                double pct = asset.getBatteryPercent();
                progressBattery.setProgress(pct);

                if (pct > 0.5)
                    progressBattery.setStyle("-fx-accent: green;");
                else if (pct > 0.2)
                    progressBattery.setStyle("-fx-accent: orange;");
                else
                    progressBattery.setStyle("-fx-accent: red;");
            }

            if (lblBatteryPercent != null) {
                double pct = asset.getBatteryPercent();
                lblBatteryPercent.setText(String.format("%.0f%%", pct * 100));
            }

            if (lblPosition != null) {
                lblPosition.setText(String.format("X:%.0f Y:%.0f Z:%.0f", asset.getX(), asset.getY(), asset.getZ()));
            }

            if (cmbMissions != null) {
                isUpdatingDetails = true;
                try {
                    java.util.List<com.spiga.management.Mission> allMissions = new java.util.ArrayList<>();
                    if (asset.getCurrentMission() != null) {
                        allMissions.add(asset.getCurrentMission());
                    }
                    if (asset.getMissionQueue() != null) {
                        allMissions.addAll(asset.getMissionQueue());
                    }

                    cmbMissions.getItems().setAll(allMissions);

                    if (asset.getCurrentMission() != null) {
                        cmbMissions.getSelectionModel().select(asset.getCurrentMission());
                    }
                } finally {
                    isUpdatingDetails = false;
                }
            }

            com.spiga.management.Mission m = asset.getCurrentMission();
            if (m != null) {
                if (lblMissionStatusBadge != null) {
                    lblMissionStatusBadge.setText(m.getStatut().toString());
                    String colorStyle = "-fx-background-color: #ddd;"; // Default Gray
                    switch (m.getStatut()) {
                        case EN_COURS:
                            colorStyle = "-fx-background-color: #3B82F6; -fx-text-fill: white;";
                            break; // Blue
                        case TERMINEE:
                            colorStyle = "-fx-background-color: #22C55E; -fx-text-fill: white;";
                            break; // Green
                        case ECHOUEE:
                            colorStyle = "-fx-background-color: #EF4444; -fx-text-fill: white;";
                            break; // Red
                        case ANNULEE:
                            colorStyle = "-fx-background-color: #F97316; -fx-text-fill: white;";
                            break; // Orange
                        case PAUSED:
                            colorStyle = "-fx-background-color: #F59E0B; -fx-text-fill: white;";
                            break; // Amber
                        default:
                            break;
                    }
                    lblMissionStatusBadge.setStyle(
                            colorStyle + " -fx-background-radius: 3; -fx-padding: 2 5; -fx-font-size: 9px;");
                }

                if (btnStartMission != null) {
                    switch (m.getStatut()) {
                        case PAUSED:
                            btnStartMission.setText("▶ Reprendre");
                            btnStartMission.setDisable(false);
                            break;
                        case TERMINEE:
                        case ECHOUEE:
                        case ANNULEE:
                            btnStartMission.setText("↻ Relancer");
                            btnStartMission.setDisable(false);
                            break;
                        case PLANIFIEE:
                            btnStartMission.setText("▶ Lancer");
                            btnStartMission.setDisable(false);
                            break;
                        case EN_COURS:
                            btnStartMission.setText("▶ En Cours");
                            btnStartMission.setDisable(true);
                            break;
                        default:
                            btnStartMission.setText("▶ Démarrer");
                            btnStartMission.setDisable(true);
                            break;
                    }
                }
                if (btnStopMission != null) {
                    btnStopMission.setDisable(m.getStatut() != com.spiga.management.Mission.StatutMission.EN_COURS
                            && m.getStatut() != com.spiga.management.Mission.StatutMission.PAUSED);
                }

            } else {
                if (cmbMissions != null)
                    cmbMissions.getSelectionModel().clearSelection();
                if (lblMissionStatusBadge != null) {
                    lblMissionStatusBadge.setText("-");
                    lblMissionStatusBadge.setStyle("-fx-background-color: transparent;");
                }
                if (btnStartMission != null) {
                    btnStartMission.setText("▶ Démarrer");
                    btnStartMission.setDisable(true);
                }
                if (btnStopMission != null)
                    btnStopMission.setDisable(true);
            }

            if (lblSpeed != null)
                lblSpeed.setText(String.format("%.1f km/h", asset.getVitesse()));
        });
    }

    public void clearDetails() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::clearDetails);
            return;
        }
        if (lblId != null)
            lblId.setText("ID: -");
        if (lblType != null)
            lblType.setText("Type: -");
        if (lblState != null)
            lblState.setText("State: -");
        if (progressBattery != null)
            progressBattery.setProgress(0);
        if (lblBatteryPercent != null)
            lblBatteryPercent.setText("0%");
        if (lblPosition != null)
            lblPosition.setText("Coords: -");
        if (cmbMissions != null) {
            cmbMissions.getItems().clear();
        }
    }

    public void clearDetails(int count) {
        clearDetails();
        if (lblId != null) {
            Platform.runLater(() -> lblId.setText("Sélection Multiple (" + count + ")"));
        }
    }

    public void clearSelection() {
        if (listAssets != null) {
            listAssets.getSelectionModel().clearSelection();
        }
        clearDetails();
    }
}
