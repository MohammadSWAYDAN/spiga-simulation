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
 * Contrôleur de Gestion des Missions (Création et Liste).
 * 
 * <h3>CONCEPTS CLÉS :</h3>
 * <ul>
 * <li><b>Logique d'Interaction :</b> Guide l'utilisateur via des dialogues
 * modaux pour définir des coordonnées complexes.</li>
 * <li><b>Validation Métier (Business Logic) :</b> Vérifie la cohérence des
 * ordres avant exécution (ex: {@link #validatePhysicalConstraints}
 * empêche un sous-marin de valider une cible aérienne).</li>
 * </ul>
 *
 * @author Equipe SPIGA
 * @version 1.0
 */
public class MissionController {

    @FXML
    private ListView<Mission> listMissions;
    @FXML
    private TextField txtMissionTitle;
    @FXML
    private Label lblMissionStatus;
    @FXML
    private TextField txtDuration; // Nouveau champ durée
    @FXML
    private Label lblTargetCoords;
    @FXML
    private Button btnSetTarget;
    @FXML
    private Button btnCreateMission;

    private GestionnaireEssaim gestionnaire;
    private MainController mainController;

    // Coordonnées temporaires pour la création
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
        // Configuration de la liste des missions (Affichage personnalisé)
        if (listMissions != null) {
            listMissions.setCellFactory(param -> new ListCell<Mission>() {
                private final javafx.scene.layout.VBox root = new javafx.scene.layout.VBox();
                private final Label lblName = new Label();
                private final Label lblDetails = new Label();

                {
                    root.setSpacing(2);
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
                        // Titre
                        lblName.setText(item.getTitre());

                        // Détails: Nom Actif | Cible
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
            });
        }

        // Écouteur de sélection dans la liste
        listMissions.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        lblMissionStatus.setText("Sélectionné: " + newVal.getTitre());
                    } else {
                        lblMissionStatus.setText("Sélectionné: -");
                    }
                });
        updateTargetLabel();

        // Menu Contextuel (Clic Droit) : Relancer / Annuler
        ContextMenu ctxMenu = new ContextMenu();
        MenuItem restartItem = new MenuItem("Relancer Mission");
        MenuItem cancelItem = new MenuItem("Annuler Mission");

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
        // Logique déplacée au moment de la création
    }

    /**
     * Gère le clic sur "Définir Cible". Ouvre un dialogue pour choisir entre
     * le mode Curseur (clic carte) ou Saisie Manuelle.
     */
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

    /**
     * Affiche un dialogue complexe pour la saisie manuelle des coordonnées A
     * (Départ) et B (Arrivée).
     */
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

    /**
     * Action associée au bouton "Créer Mission".
     * Instancie la mission, l'assigne aux actifs sélectionnés après validation,
     * et l'ajoute au gestionnaire.
     */
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

        // Détection automatique du type de mission selon le premier actif sélectionné
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
            mission = new MissionLogistique(title); // Par défaut
        }

        mission.setTarget(targetX, targetY, targetZ);

        // Parsing Durée
        long duration = 180; // Par défaut 3 min
        if (txtDuration != null && !txtDuration.getText().isEmpty()) {
            try {
                duration = Long.parseLong(txtDuration.getText());
            } catch (NumberFormatException e) {
                // Ignorer, garder défaut
            }
        }
        mission.setPlannedDurationSeconds(duration);

        // Validation & Lancement
        if (!validatePhysicalConstraints(selectedAssets, targetZ))
            return;

        // Démarrage via le Gestionnaire (qui gère l'état initial)
        gestionnaire.demarrerMission(mission, selectedAssets);

        mainController.refreshSidebar(); // Mise à jour UI
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

    /**
     * Vérifie la compatibilité physique entre l'actif et la cible (Z notamment).
     *
     * @param assets  Liste des actifs à vérifier.
     * @param targetZ Altitude/Profondeur cible.
     * @return true si tout est valide, false sinon (avec alerte affichée).
     */
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

        // Pré-remplissage intelligent du Z
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
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
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
