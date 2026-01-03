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
 * Contrôleur de la Barre Latérale (Panneau de Droite).
 * <p>
 * Ce contrôleur gère l'affichage des informations détaillées de l'actif
 * sélectionné,
 * la liste de tous les actifs présents dans l'essaim, ainsi que le journal des
 * alertes
 * et événements importants. Il permet aussi le contrôle direct des missions
 * (Pause/Reprise).
 * </p>
 *
 * @author Equipe SPIGA
 * @version 1.0
 */
public class SidebarController {

    // --- Composants UI (Injectés via FXML) ---

    /** Liste affichant les identifiants de tous les actifs de la flotte. */
    @FXML
    private ListView<String> listAssets;

    @FXML
    private Label lblId;
    @FXML
    private Label lblType;
    @FXML
    private Label lblState;

    /** Barre de progression indiquant le niveau de batterie. */
    @FXML
    private ProgressBar progressBattery;
    @FXML
    private Label lblBatteryPct;

    @FXML
    private Label lblPosition;
    @FXML
    private Label lblSpeed;

    /** Liste déroulante des missions associées à l'actif sélectionné. */
    @FXML
    private ComboBox<com.spiga.management.Mission> cmbMissions;

    /** Liste des alertes et notifications système (log visuel). */
    @FXML
    private ListView<String> listAlerts;

    // --- Boutons d'Action ---
    @FXML
    private Button btnRecharge;
    @FXML
    private Label lblMissionStatusBadge;
    @FXML
    private Button btnStartMission;
    @FXML
    private Button btnStopMission;

    // --- État Interne ---
    private boolean isUpdatingDetails = false;
    private boolean isUpdatingSelection = false;
    private GestionnaireEssaim gestionnaire;
    private ActifMobile selectedAsset;
    private MainController mainController;

    /**
     * Ajoute un message d'alerte ou de notification dans le journal latéral.
     * Le message est ajouté en haut de la liste. La liste conserve les 50 derniers
     * messages.
     *
     * @param message Le texte de l'alerte à afficher.
     */
    public void addAlert(String message) {
        if (listAlerts != null) {
            Platform.runLater(() -> {
                listAlerts.getItems().add(0, message); // Ajout en tête
                if (listAlerts.getItems().size() > 50) {
                    listAlerts.getItems().remove(50); // Garder les 50 derniers
                }
            });
        }
    }

    /**
     * Définit le gestionnaire d'essaim et initialise la liste des actifs.
     *
     * @param gestionnaire L'instance du gestionnaire métier.
     */
    public void setGestionnaire(GestionnaireEssaim gestionnaire) {
        this.gestionnaire = gestionnaire;
        refreshList(); // Premier remplissage
    }

    /**
     * Référence vers le contrôleur principal pour la communication
     * inter-contrôleurs.
     *
     * @param controller L'instance de {@link MainController}.
     */
    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    /**
     * Initialisation du contrôleur (appelé par JavaFX).
     * Configure les écouteurs d'événements, les convertisseurs de cellules
     * (ListCell)
     * et les actions des boutons.
     */
    @FXML
    public void initialize() {
        if (btnRecharge != null) {
            btnRecharge.setOnAction(e -> handleRecharge());
            btnRecharge.setDisable(true);
        }
        if (btnStartMission != null) {
            btnStartMission.setOnAction(e -> handleStartMission());
        }
        if (btnStopMission != null) {
            btnStopMission.setOnAction(e -> handleStopMission());
        }

        // Configuration de la ComboBox des Missions (Affichage du Titre uniquement)
        if (cmbMissions != null) {
            cmbMissions.setConverter(new javafx.util.StringConverter<com.spiga.management.Mission>() {
                @Override
                public String toString(com.spiga.management.Mission m) {
                    return (m == null) ? "Aucune" : m.getTitre();
                }

                @Override
                public com.spiga.management.Mission fromString(String string) {
                    return null; // Non utilisé (Saisie impossible)
                }
            });

            // Écouteur de changement de mission active
            cmbMissions.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (isUpdatingDetails || newVal == null || selectedAsset == null)
                    return;
                // L'utilisateur change la mission prioritaire
                selectedAsset.promoteMission(newVal);
                updateDetails(selectedAsset);
            });
        }

        // Configuration de la Liste des Actifs (Sélection multiple + Bouton Supprimer)
        if (listAssets != null) {
            listAssets.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            // Synchronisation Sélection Liste -> Carte
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

            // Cell Factory Personnalisée (Nom + Bouton 'X' pour suppression)
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
                            alert.setContentText("Confirmer la suppression définitive ?");

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

        // Configuration visuelle de la liste des Alertes
        if (listAlerts != null) {
            listAlerts.setCellFactory(param -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        // Nettoyage des icônes pour affichage propre
                        String cleanText = item.replaceAll("[⚠️⛔ℹ️🚩🔄?]", "").trim();
                        if (cleanText.startsWith(":"))
                            cleanText = cleanText.substring(1).trim();

                        setText(cleanText);
                        setWrapText(true);
                        // Style Critique (Fond Rouge clair)
                        setStyle(
                                "-fx-background-color: #ffcccc; -fx-text-fill: #900000; -fx-padding: 5px; -fx-border-width: 0 0 1 0; -fx-border-color: #ffaaaa;");
                    }
                }
            });
        }
    }

    // --- Gestion des Actions Utilisateur ---

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
                break; // Rien à faire
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
            selectedAsset.recharger(); // Réinitialise l'autonomie et l'état
            updateDetails(selectedAsset); // Rafraîchissement UI immédiat
        }
    }

    @FXML
    public void handleTestCollision() {
        if (mainController != null) {
            mainController.handleDemoCollision();
        }
    }

    /**
     * Rafraîchit l'ensemble du panneau latéral.
     * Met à jour la liste des actifs et les détails de l'actif sélectionné.
     */
    public void refresh() {
        if (gestionnaire == null)
            return;

        // Mise à jour de la liste si changement dans la flotte
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

    /**
     * Sélectionne programmatiquement des actifs dans la liste latérale.
     * Utilisé pour la synchronisation depuis la Carte vers la Sidebar.
     *
     * @param assets La liste des actifs à sélectionner.
     */
    public void selectAssets(List<ActifMobile> assets) {
        if (listAssets == null)
            return;

        isUpdatingSelection = true;
        try {
            List<String> allIds = listAssets.getItems();

            if (assets == null || assets.isEmpty()) {
                if (!listAssets.getSelectionModel().getSelectedItems().isEmpty()) {
                    listAssets.getSelectionModel().clearSelection();
                }
                this.selectedAsset = null;
                clearDetails();
            } else {
                // Trouve les indices correspondants
                int[] indices = assets.stream()
                        .map(ActifMobile::getId)
                        .mapToInt(id -> allIds.indexOf(id))
                        .filter(i -> i >= 0)
                        .toArray();

                if (indices.length > 0) {
                    if (!listAssets.getSelectionModel().getSelectedItems().isEmpty()) {
                        listAssets.getSelectionModel().clearSelection();
                    }

                    if (indices.length == 1) {
                        listAssets.getSelectionModel().select(indices[0]);
                    } else {
                        int first = indices[0];
                        int[] rest = new int[indices.length - 1];
                        System.arraycopy(indices, 1, rest, 0, rest.length);
                        listAssets.getSelectionModel().selectIndices(first, rest);
                    }
                } else {
                    listAssets.getSelectionModel().clearSelection();
                }

                if (assets.size() == 1) {
                    this.selectedAsset = assets.get(0);
                    updateDetails(this.selectedAsset);
                } else {
                    this.selectedAsset = null;
                    clearDetails(assets.size());
                }
            }
        } catch (Exception e) {
            System.err.println("Safely caught UI Selection exception: " + e.getMessage());
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

    /**
     * Met à jour les widgets de détails avec les données de l'actif.
     *
     * @param asset L'actif à afficher.
     */
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

            // Mise à jour État et Couleurs
            if (lblState != null) {
                lblState.setText("Etat: " + asset.getState());
                if (asset.getState() == ActifMobile.AssetState.LOW_BATTERY
                        || asset.getState() == ActifMobile.AssetState.STOPPED) {
                    lblState.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                } else if (asset.getState() == ActifMobile.AssetState.RETURNING_TO_BASE) {
                    lblState.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                } else {
                    lblState.setStyle("-fx-text-fill: black;");
                }
            }

            // Batterie
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
            if (lblBatteryPct != null) {
                lblBatteryPct.setText(String.format("%.0f%%", asset.getBatteryPercent() * 100));
            }

            if (lblPosition != null) {
                lblPosition.setText(String.format("X:%.0f Y:%.0f Z:%.0f", asset.getX(), asset.getY(), asset.getZ()));
            }

            // Combobox Missions
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

            // Badge Statut Mission
            com.spiga.management.Mission m = asset.getCurrentMission();
            if (m != null) {
                if (lblMissionStatusBadge != null) {
                    lblMissionStatusBadge.setText(m.getStatut().toString());
                    String colorStyle = "-fx-background-color: #ddd;"; // Gris
                    switch (m.getStatut()) {
                        case EN_COURS:
                            colorStyle = "-fx-background-color: #3B82F6; -fx-text-fill: white;";
                            break; // Bleu
                        case TERMINEE:
                            colorStyle = "-fx-background-color: #22C55E; -fx-text-fill: white;";
                            break; // Vert
                        case ECHOUEE:
                            colorStyle = "-fx-background-color: #EF4444; -fx-text-fill: white;";
                            break; // Rouge
                        case ANNULEE:
                            colorStyle = "-fx-background-color: #F97316; -fx-text-fill: white;";
                            break; // Orange
                        case PAUSED:
                            colorStyle = "-fx-background-color: #F59E0B; -fx-text-fill: white;";
                            break; // Ambre
                        default:
                            break;
                    }
                    lblMissionStatusBadge.setStyle(
                            colorStyle + " -fx-background-radius: 3; -fx-padding: 2 5; -fx-font-size: 9px;");
                }

                // Boutons Mission
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
                // Pas de mission active
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
                lblSpeed.setText(String.format("%.1f km/h", asset.getCurrentSpeed()));
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
        if (lblBatteryPct != null)
            lblBatteryPct.setText("0%");
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
