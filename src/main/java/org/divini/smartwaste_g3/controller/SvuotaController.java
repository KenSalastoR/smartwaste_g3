package org.divini.smartwaste_g3.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.divini.smartwaste_g3.database.CassonettoDAO;
import org.divini.smartwaste_g3.database.ConnessioneDatabase;
import org.divini.smartwaste_g3.model.Cassonetto;

public class SvuotaController {

    @FXML private TableView<Cassonetto> tabellaSvuota;

    @FXML private TableColumn<Cassonetto, Integer> colCodice;
    @FXML private TableColumn<Cassonetto, String> colTipo;
    @FXML private TableColumn<Cassonetto, Double> colValore;
    @FXML private TableColumn<Cassonetto, Double> colCapacita;
    @FXML private TableColumn<Cassonetto, Double> colPercentuale;

    @FXML private Button btnVisualizza;

    private CassonettoDAO dao;

    @FXML
    public void initialize() {

        dao = new CassonettoDAO(new ConnessioneDatabase());

        // Colonne
        colCodice.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getCodice()).asObject());
        colTipo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTipologia().name()));

        colValore.setCellValueFactory(c ->
                new javafx.beans.property.SimpleDoubleProperty(
                        c.getValue().getPercentualeRiempimento() * c.getValue().getCapacita() / 100
                ).asObject()
        );

        colCapacita.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getCapacita()).asObject());
        colPercentuale.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getPercentualeRiempimento()).asObject());

        // Carica cassonetti > 90%
        caricaTabella();

        // Click su un cassonetto → svuota
        tabellaSvuota.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) { // doppio click
                Cassonetto selezionato = tabellaSvuota.getSelectionModel().getSelectedItem();
                if (selezionato != null) {
                    svuotaCassonetto(selezionato);
                }
            }
        });

        // Torna a visualizza
        btnVisualizza.setOnAction(e -> tornaVisualizza());
    }

    private void caricaTabella() {
        ObservableList<Cassonetto> lista = FXCollections.observableArrayList(dao.getCassonettiDaSvuotare());
        tabellaSvuota.setItems(lista);
    }

    private void svuotaCassonetto(Cassonetto c) {
        try {
            c.svuota();

            c.aggiorna(0);

            dao.aggiorna(c);

            caricaTabella();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("Cassonetto " + c.getCodice() + " svuotato con successo!");
            alert.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void tornaVisualizza() {
        try {
            Stage stage = (Stage) btnVisualizza.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/visualizza.fxml")));
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}