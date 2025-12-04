package com.spiga.ui;

import com.spiga.core.*;
import com.spiga.management.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 * MissionController - FIXED & ENHANCED
 * Adds cursor mode for coordinate picking and fixes validation.
 */
public class MissionController {

    @FXML
    private ListView<Mission> listMissions;
    @FXML
    private TextField txtMissionTitle;
    @FXML
    private ComboBox<String> comboMissionType;
    @FXML
    private Label lblMissionStatus;
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
        // Default items if nothing selected
        comboMissionType.setItems(FXCollections.observableArrayList("Sélectionnez un actif..."));

        if (listMissions != null) {
            listMissions.setCellFactory(param -> new ListCell<Mission>() {
                @Override
                protected void updateItem(Mission item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getTitre() + " [" + item.getStatut() + "]");
                    }
                }
            });

            listMissions.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    lblMissionStatus.setText("Sélectionné: " + newVal.getTitre());
                }
            });
        }

        updateTargetLabel();
    }

    public void onAssetSelected(ActifMobile asset) {
        if (asset == null) {
            comboMissionType.setItems(FXCollections.observableArrayList("Sélectionnez un actif..."));
            return;
        }

        List<String> missions = new ArrayList<>();
        if (asset instanceof ActifAerien) {
            missions.add("Surveillance");
            missions.add("Recherche et Sauvetage");
            missions.add("Logistique");
        } else if (asset instanceof VehiculeSurface) {
            missions.add("Surveillance");
            missions.add("Recherche et Sauvetage");
            missions.add("Navigation");
        } else if (asset instanceof VehiculeSousMarin) {
            missions.add("Navigation");
            missions.add("Surveillance");
        }

        comboMissionType.setItems(FXCollections.observableArrayList(missions));
        if (!missions.isEmpty()) {
            comboMissionType.setValue(missions.get(0));
        }
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
        String type = comboMissionType.getValue();

        if (title == null || title.isEmpty() || type == null) {
            lblMissionStatus.setText("❌ Titre et type requis");
            return;
        }

        Mission mission = createMissionByType(title, type);
        if (mission == null) {
            lblMissionStatus.setText("❌ Type de mission invalide");
            return;
        }
        mission.setTarget(targetX, targetY, targetZ);

        List<ActifMobile> selectedAssets = mainController.getSelectedAssets();

        if (selectedAssets.isEmpty()) {
            // Generic Creation
            mission.setStatut(Mission.StatutMission.PLANIFIEE);
            if (listMissions != null)
                listMissions.getItems().add(mission);
            lblMissionStatus.setText("✅ Mission planifiée (Non assignée)");
        } else {
            // Immediate Assignment
            if (!validatePhysicalConstraints(selectedAssets, targetZ))
                return;

            mainController.assignMissionToSelected(mission);
            if (listMissions != null)
                listMissions.getItems().add(mission);
            lblMissionStatus.setText("✅ Mission créée et assignée");
        }

        txtMissionTitle.clear();
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

    private Mission createMissionByType(String title, String type) {
        switch (type) {
            case "Surveillance":
                return new MissionSurveillanceMaritime(title);
            case "Logistique":
                return new MissionLogistique(title);
            case "Recherche et Sauvetage":
                return new MissionRechercheEtSauvetage(title);
            case "Navigation":
                return new MissionLogistique(title);
            default:
                return null;
        }
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
