package org.divini.smartwaste_g3.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.divini.smartwaste_g3.database.CassonettoDAO;
import org.divini.smartwaste_g3.database.ConnessioneDatabase;
import org.divini.smartwaste_g3.model.Cassonetto;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalTime;
import java.time.LocalDate;

public class VisualizzaController {

    @FXML private TableView<Cassonetto> tabellaCassonetti;

    @FXML private TableColumn<Cassonetto, Integer> colCodice;
    @FXML private TableColumn<Cassonetto, Double> colLat;
    @FXML private TableColumn<Cassonetto, Double> colLon;
    @FXML private TableColumn<Cassonetto, String> colTipo;

    @FXML private TableColumn<Cassonetto, LocalDate> colDataInst;

    @FXML private TableColumn<Cassonetto, LocalTime> colOraInst;

    @FXML private TableColumn<Cassonetto, LocalDate> colDataSvuot;

    @FXML private TableColumn<Cassonetto, LocalTime> colOraSvuot;
    @FXML private TableColumn<Cassonetto, Double> colValore;
    @FXML private TableColumn<Cassonetto, Double> colCapacita;
    @FXML private TableColumn<Cassonetto, Double> colPercentuale;
    @FXML private Button btnElimina;
    @FXML private Button btnModifica;
    @FXML private Button btnHome;
    @FXML private Button btnSvuota;
    @FXML private TableView<Cassonetto> tabella;
    @FXML private TextField txtSearch;
    @FXML private Button btnSearch;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private ComboBox<String> cmbPercentuale;
    private CassonettoDAO dao;

    @FXML
    public void initialize() {

        dao = new CassonettoDAO(new ConnessioneDatabase());

        // Colonne
        colCodice.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getCodice()).asObject());
        colLat.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getLatitudine()).asObject());
        colLon.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getLongitudine()).asObject());
        colDataInst.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getDataInstallazione()));
        colOraInst.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getOraInstallazione()));
        colDataSvuot.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getDataSvuotamento()));
        colOraSvuot.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getOraSvuotamento()));
        colTipo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTipologia().name()));

        colValore.setCellValueFactory(c ->
                new javafx.beans.property.SimpleDoubleProperty(
                        c.getValue().getPercentualeRiempimento() * c.getValue().getCapacita() / 100
                ).asObject()
        );
        colCapacita.setCellValueFactory(c ->
                new javafx.beans.property.SimpleDoubleProperty(
                        c.getValue().getCapacita()
                ).asObject()
        );

        colPercentuale.setCellValueFactory(c ->
                new javafx.beans.property.SimpleDoubleProperty(
                        c.getValue().getPercentualeRiempimento()
                ).asObject()
        );
        colPercentuale.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setAlignment(javafx.geometry.Pos.CENTER);
                setText(String.format("%.1f%%", value));

                if (value > 90) {
                    setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white;"); // rojo
                } else if (value >= 50) {
                    setStyle("-fx-background-color: #ffcc00; -fx-text-fill: black;"); // amarillo
                } else {
                    setStyle("-fx-background-color: #66cc66; -fx-text-fill: black;"); // verde
                }
            }
        });

        // Carica dati
        caricaTabella();

        // Torna alla home
        btnHome.setOnAction(e -> tornaHome());
        //buttoni
        btnElimina.setOnAction(e -> eliminaCassonetto());
        btnModifica.setOnAction(e -> apriModifica());
        btnSearch.setOnAction(e -> filtraTabella());
        btnSvuota.setOnAction(e -> apriSvuota());

        cmbTipo.getItems().addAll("Tutti", "Organico", "Vetro", "Carta", "Plastica", "Indifferenziata");
        cmbTipo.setValue("Tutti");

        cmbPercentuale.getItems().addAll("Tutti", "> 90%", "50% - 90%", "< 50%");
        cmbPercentuale.setValue("Tutti");

        cmbTipo.setOnAction(e -> applicaFiltri());
        cmbPercentuale.setOnAction(e -> applicaFiltri());

    }

    private void caricaTabella() {
        ObservableList<Cassonetto> lista = FXCollections.observableArrayList(dao.getTutti());
        tabellaCassonetti.setItems(lista);
    }

    private void filtraTabella() {
        String filtro = txtSearch.getText().toLowerCase();

        ObservableList<Cassonetto> lista = FXCollections.observableArrayList(dao.getTutti());

        ObservableList<Cassonetto> filtrata = lista.filtered(c ->
                String.valueOf(c.getCodice()).contains(filtro) ||
                        c.getTipologia().name().toLowerCase().contains(filtro)
        );

        tabellaCassonetti.setItems(filtrata);
    }

    private void applicaFiltri() {
        ObservableList<Cassonetto> lista = FXCollections.observableArrayList(dao.getTutti());

        String tipo = cmbTipo.getValue();
        String perc = cmbPercentuale.getValue();

        ObservableList<Cassonetto> filtrata = lista.filtered(c -> {

            boolean okTipo = tipo.equals("Tutti") || c.getTipologia().name().equals(tipo);

            double p = c.getPercentualeRiempimento();
            boolean okPerc = switch (perc) {
                case "> 90%" -> p > 90;
                case "50% - 90%" -> p >= 50 && p <= 90;
                case "< 50%" -> p < 50;
                default -> true;
            };

            return okTipo && okPerc;
        });

        tabellaCassonetti.setItems(filtrata);
    }
    private void tornaHome() {
        try {
            Stage stage = (Stage) btnHome.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/home.fxml")));
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void eliminaCassonetto() {
        Cassonetto selezionato = tabellaCassonetti.getSelectionModel().getSelectedItem();

        if (selezionato == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText("Seleziona un cassonetto dalla tabella.");
            alert.showAndWait();
            return;
        }

        CassonettoDAO dao = new CassonettoDAO(new ConnessioneDatabase());
        boolean ok = dao.elimina(selezionato.getCodice());

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);

        if (ok) {
            alert.setContentText("Cassonetto eliminato con successo!");
            tabellaCassonetti.getItems().remove(selezionato); // aggiorna la tabella
        } else {
            alert.setContentText("Errore: cassonetto non trovato.");
        }

        alert.showAndWait();
    }
    private void apriModifica() {
        Cassonetto selezionato = tabellaCassonetti.getSelectionModel().getSelectedItem();

        if (selezionato == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText("Seleziona un cassonetto dalla tabella.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modifica.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));

            ModificaController controller = loader.getController();
            controller.setCassonetto(selezionato);
            controller.setVisualizzaController(this);

            stage.setTitle("Modifica Cassonetto");
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void apriSvuota() {
        try {
            Stage stage = (Stage) btnSvuota.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/svuota.fxml")));
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void aggiornaTabella() {
        tabellaCassonetti.setItems(
                FXCollections.observableArrayList(dao.getTutti())
        );
    }
}