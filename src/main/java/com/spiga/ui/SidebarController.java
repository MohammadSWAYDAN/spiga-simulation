package com.spiga.ui;

import com.spiga.core.ActifMobile;
import com.spiga.management.GestionnaireEssaim;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SidebarController - OPTIMIZED
 * Fixes selection flickering and updates details efficiently.
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
    private Label lblMission;
    @FXML
    private Label lblSpeed;

    private GestionnaireEssaim gestionnaire;
    private ActifMobile selectedAsset;
    private MainController mainController;

    public void setGestionnaire(GestionnaireEssaim gestionnaire) {
        this.gestionnaire = gestionnaire;
        refreshList(); // Initial populate
    }

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    @FXML
    public void initialize() {
        if (listAssets != null) {
            listAssets.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && gestionnaire != null) {
                    for (ActifMobile asset : gestionnaire.getFlotte()) {
                        if (asset.getId().equals(newVal)) {
                            // Update local details
                            selectedAsset = asset;
                            updateDetails(asset);
                            // Notify MainController to highlight on map
                            if (mainController != null) {
                                mainController.onSidebarSelection(asset);
                            }
                            break;
                        }
                    }
                }
            });
        }
    }

    public void refresh() {
        if (gestionnaire == null)
            return;

        // 1. Update List ONLY if changed
        // Simple check: size difference or ID mismatch
        // For production, we should use ObservableList in Gestionnaire, but here we
        // patch.
        List<String> currentIds = gestionnaire.getFlotte().stream().map(ActifMobile::getId)
                .collect(Collectors.toList());
        if (!currentIds.equals(listAssets.getItems())) {
            String selected = listAssets.getSelectionModel().getSelectedItem();
            listAssets.getItems().setAll(currentIds);
            if (selected != null && currentIds.contains(selected)) {
                listAssets.getSelectionModel().select(selected);
            }
        }

        // 2. Update Details (Always, as battery/pos changes)
        if (selectedAsset != null) {
            updateDetails(selectedAsset);
        }
    }

    public void selectAsset(ActifMobile asset) {
        this.selectedAsset = asset;
        updateDetails(asset);

        if (listAssets != null && asset != null) {
            if (!asset.getId().equals(listAssets.getSelectionModel().getSelectedItem())) {
                listAssets.getSelectionModel().select(asset.getId());
            }
        } else if (listAssets != null && asset == null) {
            listAssets.getSelectionModel().clearSelection();
        }
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
            if (lblId != null)
                lblId.setText("ID: " + asset.getId());
            if (lblType != null)
                lblType.setText("Type: " + asset.getClass().getSimpleName());

            if (lblState != null) {
                lblState.setText("State: " + asset.getState());
                switch (asset.getState()) {
                    case STOPPED:
                        lblState.setTextFill(Color.RED);
                        break;
                    case LOW_BATTERY:
                        lblState.setTextFill(Color.ORANGE);
                        break;
                    case EXECUTING_MISSION:
                        lblState.setTextFill(Color.GREEN);
                        break;
                    default:
                        lblState.setTextFill(Color.BLACK);
                }
            }

            if (progressBattery != null) {
                double pct = asset.getBatteryPercent();
                progressBattery.setProgress(pct);
                if (pct < 0.2)
                    progressBattery.setStyle("-fx-accent: red;");
                else if (pct < 0.5)
                    progressBattery.setStyle("-fx-accent: orange;");
                else
                    progressBattery.setStyle("-fx-accent: green;");
            }

            if (lblBatteryPercent != null) {
                lblBatteryPercent.setText(String.format("%.0f%%", asset.getBatteryPercent() * 100));
            }

            if (lblPosition != null) {
                lblPosition
                        .setText(String.format("Coords: (%.0f, %.0f, %.0f)", asset.getX(), asset.getY(), asset.getZ()));
            }

            if (lblMission != null) {
                if (asset.getCurrentMission() != null) {
                    lblMission.setText("Mission: " + asset.getCurrentMission().getTitre());
                } else {
                    lblMission.setText("Mission: None");
                }
            }

            if (lblSpeed != null) {
                lblSpeed.setText(String.format("%.1f km/h", asset.getCurrentSpeed()));
            }
        });
    }

    private void clearDetails() {
        Platform.runLater(() -> {
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
            if (lblMission != null)
                lblMission.setText("Mission: -");
        });
    }
}
