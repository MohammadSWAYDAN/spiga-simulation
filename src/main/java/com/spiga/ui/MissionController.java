package com.spiga.ui;

import com.spiga.core.ActifMobile;
import com.spiga.core.ActifAerien;
import com.spiga.core.DroneReconnaissance;
import com.spiga.core.DroneLogistique;
import com.spiga.core.VehiculeSurface;
import com.spiga.core.VehiculeSousMarin;

import com.spiga.management.GestionnaireEssaim;
import com.spiga.management.Mission;
import com.spiga.management.MissionLogistique;
import com.spiga.management.MissionSurveillanceMaritime;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.util.List;
import java.util.Optional;

/**
 * Controleur de Creation de Missions
 * 
 * CONCEPTS CLES :
 * 
 * 1. Logique d'Interaction :
 * - C'est quoi ? Guider l'utilisateur.
 * - Exemple : La methode handleSetTarget ouvre une boite de dialogue (Dialog)
 * pour demander des choix complexes.
 * 
 * 2. Validation Metier (Business Logic) :
 * - C'est quoi ? Empecher l'utilisateur de faire des betises.
 * - Ou ? validatePhysicalConstraints verifie qu'on n'envoie pas un sous-marin
 * dans les nuages !
 * C'est une protection essentielle dans tout logiciel professionnel.
 */
public class MissionController {

    @FXML
    private ListView<Mission> listMissions;
    @FXML
    private TextField txtMissionTitle;
    // ComboBox removed
    @FXML
    private Label lblMissionStatus;
    @FXML
    private TextField txtDuration; // New Duration Field
    @FXML
    private Label lblTargetCoords;
    @FXML
    private Button btnSetTarget;
    @FXML
    private Button btnCreateMission;

    private GestionnaireEssaim gestionnaire;
    private MainController mainController;
    private double startX = 0, startY = 0, startZ = 0;
    private double targetX = 500, targetY = 500, targetZ = 0;
    private boolean useCurrentPosition = true;

    public void setGestionnaire(GestionnaireEssaim gestionnaire) {
        this.gestionnaire = gestionnaire;
    }

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    @FXML
    public void initialize() {
        if (listMissions != null) {
            listMissions.setCellFactory(param -> new ListCell<Mission>() {
                private final javafx.scene.layout.VBox root = new javafx.scene.layout.VBox();
                private final Label lblName = new Label();
                private final Label lblDetails = new Label();

                {
                    root.setSpacing(2); // Tight stacking
                    root.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    root.setPadding(new Insets(5));

                    lblName.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");
                    lblDetails.setStyle("-fx-text-fill: #666; -fx-font-size: 10px;");

                    root.getChildren().addAll(lblName, lblDetails);
                }

                @Override
                protected void updateItem(Mission item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        // Title
                        lblName.setText(item.getTitre());

                        // Details: Drone Name(s) | Target
                        String droneNames = "Aucun";
                        if (item.getAssignedAssets() != null && !item.getAssignedAssets().isEmpty()) {
                            droneNames = item.getAssignedAssets().stream()
                                    .map(ActifMobile::getId)
                                    .collect(java.util.stream.Collectors.joining(", "));
                        }

                        String targetStr = String.format("(%.0f, %.0f, %.0f)",
                                item.getTargetX(), item.getTargetY(), item.getTargetZ());

                        lblDetails.setText("Drone: " + droneNames + " | Cible: " + targetStr);

                        setGraphic(root);
                        setText(null);
                    }
                }
            }); // Close anonymous class and method call
        } // Close if (listMissions != null)

        // Listen for selection
        listMissions.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        lblMissionStatus.setText("Sélectionné: " + newVal.getTitre());
                        // Target marker removed as requested
                    } else {
                        lblMissionStatus.setText("Sélectionné: -");
                        // Target marker removed as requested
                    }
                });
        updateTargetLabel();

        // Context Menu for Restart/Cancel
        ContextMenu ctxMenu = new ContextMenu();
        MenuItem restartItem = new MenuItem("Relancer Mission");
        MenuItem cancelItem = new MenuItem("Annuler Mission");
        MenuItem deleteItem = new MenuItem("Supprimer Mission"); // Optional

        restartItem.setOnAction(e -> {
            Mission m = listMissions.getSelectionModel().getSelectedItem();
            if (m != null) {
                m.restart(System.currentTimeMillis() / 1000);
                listMissions.refresh();
                if (mainController != null)
                    mainController.refreshSidebar();
            }
        });

        cancelItem.setOnAction(e -> {
            Mission m = listMissions.getSelectionModel().getSelectedItem();
            if (m != null) {
                m.cancel();
                listMissions.refresh();
            }
        });

        ctxMenu.getItems().addAll(restartItem, cancelItem);
        listMissions.setContextMenu(ctxMenu);
    }

    public void onAssetSelected(ActifMobile asset) {
        // Logic moved to Creation time
    }

    @FXML
    private void handleSetTarget() {
        if (mainController != null) {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Définir Coordonnées de Mission");
            dialog.setHeaderText("Choisissez le mode de saisie");

            ButtonType cursorBtn = new ButtonType("Curseur (Carte)", ButtonBar.ButtonData.OK_DONE);
            ButtonType manualBtn = new ButtonType("Manuel", ButtonBar.ButtonData.APPLY);
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
                    mainController.enableMissionTargetMode();
                    lblMissionStatus.setText("Cliquez sur la carte pour définir la cible");
                } else if ("MANUAL".equals(mode)) {
                    promptForManualCoordinates();
                }
            });
        }
    }

    private void promptForManualCoordinates() {
        Dialog<double[]> dialog = new Dialog<>();
        dialog.setTitle("Coordonnées de Mission");
        dialog.setHeaderText("Entrez les coordonnées Point A (départ) et Point B (arrivée)");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        CheckBox chkUseCurrent = new CheckBox("Utiliser position actuelle pour Point A");
        chkUseCurrent.setSelected(true);

        TextField txtStartX = new TextField("0");
        TextField txtStartY = new TextField("0");
        TextField txtStartZ = new TextField("0");
        TextField txtEndX = new TextField("500");
        TextField txtEndY = new TextField("500");
        TextField txtEndZ = new TextField("50");

        txtStartX.setDisable(true);
        txtStartY.setDisable(true);
        txtStartZ.setDisable(true);

        chkUseCurrent.selectedProperty().addListener((obs, old, val) -> {
            txtStartX.setDisable(val);
            txtStartY.setDisable(val);
            txtStartZ.setDisable(val);
        });

        grid.add(chkUseCurrent, 0, 0, 2, 1);
        grid.add(new Label("Point A - X:"), 0, 1);
        grid.add(txtStartX, 1, 1);
        grid.add(new Label("Point A - Y:"), 0, 2);
        grid.add(txtStartY, 1, 2);
        grid.add(new Label("Point A - Z:"), 0, 3);
        grid.add(txtStartZ, 1, 3);
        grid.add(new Separator(), 0, 4, 2, 1);
        grid.add(new Label("Point B - X:"), 0, 5);
        grid.add(txtEndX, 1, 5);
        grid.add(new Label("Point B - Y:"), 0, 6);
        grid.add(txtEndY, 1, 6);
        grid.add(new Label("Point B - Z:"), 0, 7);
        grid.add(txtEndZ, 1, 7);

        dialog.getDialogPane().setContent(grid);
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == okButton) {
                try {
                    double sx = chkUseCurrent.isSelected() ? 0 : Double.parseDouble(txtStartX.getText());
                    double sy = chkUseCurrent.isSelected() ? 0 : Double.parseDouble(txtStartY.getText());
                    double sz = chkUseCurrent.isSelected() ? 0 : Double.parseDouble(txtStartZ.getText());
                    double ex = Double.parseDouble(txtEndX.getText());
                    double ey = Double.parseDouble(txtEndY.getText());
                    double ez = Double.parseDouble(txtEndZ.getText());
                    return new double[] { sx, sy, sz, ex, ey, ez };
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<double[]> result = dialog.showAndWait();
        result.ifPresent(coords -> {
            useCurrentPosition = chkUseCurrent.isSelected();
            if (!useCurrentPosition) {
                startX = coords[0];
                startY = coords[1];
                startZ = coords[2];
            }
            targetX = coords[3];
            targetY = coords[4];
            targetZ = coords[5];
            updateTargetLabel();
            lblMissionStatus.setText("✅ Coordonnées définies");
        });
    }

    @FXML
    private void handleCreateMission() {
        if (gestionnaire == null || mainController == null)
            return;

        String title = txtMissionTitle.getText();
        List<ActifMobile> selectedAssets = mainController.getSelectedAssets();

        if (title == null || title.isEmpty()) {
            lblMissionStatus.setText("❌ Titre requis");
            return;
        }

        if (selectedAssets.isEmpty()) {
            lblMissionStatus.setText("❌ Sélectionnez au moins un actif");
            return;
        }

        // Auto-detect type based on FIRST selected asset
        ActifMobile firstAsset = selectedAssets.get(0);
        Mission mission = null;

        if (firstAsset instanceof DroneReconnaissance) {
            mission = new MissionSurveillanceMaritime(title);
        } else if (firstAsset instanceof DroneLogistique) {
            mission = new MissionLogistique(title);
        } else if (firstAsset instanceof VehiculeSurface) {
            mission = new MissionSurveillanceMaritime(title + " (Surface)");
        } else if (firstAsset instanceof VehiculeSousMarin) {
            mission = new MissionLogistique(title + " (Sous-marin)");
        } else {
            mission = new MissionLogistique(title); // Fallback
        }

        mission.setTarget(targetX, targetY, targetZ);

        // Parse Duration
        long duration = 180; // Default
        if (txtDuration != null && !txtDuration.getText().isEmpty()) {
            try {
                duration = Long.parseLong(txtDuration.getText());
            } catch (NumberFormatException e) {
                // Ignore, use default
            }
        }
        mission.setPlannedDurationSeconds(duration);

        // Immediate Assignment & Start
        if (!validatePhysicalConstraints(selectedAssets, targetZ))
            return;

        // Use Manager to start properly
        gestionnaire.demarrerMission(mission, selectedAssets);

        mainController.refreshSidebar(); // Update UI
        if (listMissions != null)
            listMissions.getItems().add(mission);

        lblMissionStatus.setText(
                "✅ Mission '" + mission.getClass().getSimpleName() + "' créée (" + selectedAssets.size() + " actifs)");

        txtMissionTitle.clear();
        if (txtDuration != null)
            txtDuration.setText("180");

        if (mainController != null)
            mainController.disableMissionTargetMode();
    }

    private boolean validatePhysicalConstraints(List<ActifMobile> assets, double targetZ) {
        for (ActifMobile asset : assets) {
            if (asset instanceof ActifAerien) {
                if (targetZ < 0) {
                    showAlert("Erreur Physique", "L'actif aérien " + asset.getId() + " ne peut pas aller sous l'eau.");
                    return false;
                }
            } else if (asset instanceof VehiculeSurface) {
                if (Math.abs(targetZ) > 5) {
                    showAlert("Erreur Physique", "Le navire " + asset.getId() + " doit rester près de la surface.");
                    return false;
                }
            } else if (asset instanceof VehiculeSousMarin) {
                if (targetZ > 0) {
                    showAlert("Erreur Physique", "Le sous-marin " + asset.getId() + " ne peut pas voler.");
                    return false;
                }
            }
        }
        return true;
    }

    public void setMissionTarget(double x, double y) {
        this.targetX = x;
        this.targetY = y;

        List<ActifMobile> selected = mainController.getSelectedAssets();
        String defaultZ = "0";
        if (!selected.isEmpty()) {
            ActifMobile first = selected.get(0);
            if (first instanceof ActifAerien)
                defaultZ = "100";
            else if (first instanceof VehiculeSousMarin)
                defaultZ = "-50";
        }

        TextInputDialog dialog = new TextInputDialog(defaultZ);
        dialog.setTitle("Altitude/Profondeur Cible");
        dialog.setHeaderText("Définir Z pour Point B (Arrivée)");
        dialog.setContentText("Z:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(zStr -> {
            try {
                this.targetZ = Double.parseDouble(zStr);
            } catch (NumberFormatException e) {
                this.targetZ = 0;
            }
        });

        updateTargetLabel();
        if (mainController != null)
            mainController.disableMissionTargetMode();
        lblMissionStatus.setText("✅ Cible définie");
    }

    public void setTargetCoordinates(double x, double y, double z) {
        if (lblTargetCoords != null) {
            lblTargetCoords.setText(String.format("%.0f, %.0f", x, y));
        }
        // Store for mission creation using existing fields
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
    }

    // Removed redundant currentTargetX/Y/Z fields

    private void updateTargetLabel() {
        if (lblTargetCoords != null) {
            String start = useCurrentPosition ? "Position actuelle"
                    : String.format("(%.0f, %.0f, %.0f)", startX, startY, startZ);
            lblTargetCoords.setText(String.format("A: %s → B: (%.0f, %.0f, %.0f)", start, targetX, targetY, targetZ));
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void refresh() {
        if (listMissions != null)
            listMissions.refresh();
    }

    public void addMission(Mission mission) {
        if (listMissions != null) {
            listMissions.getItems().add(mission);
        }
    }

    public Mission getSelectedMission() {
        if (listMissions != null)
            return listMissions.getSelectionModel().getSelectedItem();
        return null;
    }
}
