package org.divini.smartwaste_g3.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.divini.smartwaste_g3.database.CassonettoDAO;
import org.divini.smartwaste_g3.database.ConnessioneDatabase;
import org.divini.smartwaste_g3.model.Cassonetto;

public class ModificaController {

    @FXML private TextField txtLat;
    @FXML private TextField txtLon;
    @FXML private TextField txtValore;
    @FXML private Button btnSalva;
    @FXML private Button btnAnnulla;

    private Cassonetto cassonetto;
    private VisualizzaController visualizzaController;

    public void setCassonetto(Cassonetto c) {
        this.cassonetto = c;

        txtLat.setText(String.valueOf(c.getLatitudine()));
        txtLon.setText(String.valueOf(c.getLongitudine()));

        // valore reale = percentuale * capacita / 100
        double valoreReale = c.getPercentualeRiempimento() * c.getCapacita() / 100;
        txtValore.setText(String.valueOf(valoreReale));
    }

    @FXML
    public void initialize() {

        btnSalva.setOnAction(e -> salvaModifiche());
        btnAnnulla.setOnAction(e -> chiudi());
        txtLat.textProperty().addListener((obs, oldVal, newVal) -> validaNumero(txtLat));
        txtLon.textProperty().addListener((obs, oldVal, newVal) -> validaNumero(txtLon));
        txtValore.textProperty().addListener((obs, oldVal, newVal) -> validaNumero(txtValore));

    }

    private void salvaModifiche() {
        if (!validaNumero(txtLat) || !validaNumero(txtLon) || !validaNumero(txtValore)) {
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setHeaderText("Errore");
            err.setContentText("Correggi i campi evidenziati in rosso.");
            err.showAndWait();
            return;
        }

        try {
            double lat = Double.parseDouble(txtLat.getText());
            double lon = Double.parseDouble(txtLon.getText());
            double valore = Double.parseDouble(txtValore.getText());

            cassonetto.setLatitudine(lat);
            cassonetto.setLongitudine(lon);

            cassonetto.aggiorna(valore);

            if (valore == 0) {
                cassonetto.svuota();
            }

            CassonettoDAO dao = new CassonettoDAO(new ConnessioneDatabase());
            dao.aggiorna(cassonetto);

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setHeaderText(null);
            ok.setContentText("Modifica salvata con successo!");
            ok.showAndWait();

            visualizzaController.aggiornaTabella();
            chiudi();

        } catch (Exception ex) {
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setHeaderText("Errore");
            err.setContentText("Controlla i valori inseriti.");
            err.showAndWait();
        }
    }

    public void setVisualizzaController(VisualizzaController controller) {
        this.visualizzaController = controller;
    }

    private boolean validaNumero(TextField campo) {
        try {
            double v = Double.parseDouble(campo.getText());
            if (v < 0) throw new Exception();

            campo.getStyleClass().removeAll("input-error");
            if (!campo.getStyleClass().contains("input-ok"))
                campo.getStyleClass().add("input-ok");

            return true;

        } catch (Exception e) {
            campo.getStyleClass().removeAll("input-ok");
            if (!campo.getStyleClass().contains("input-error"))
                campo.getStyleClass().add("input-error");

            return false;
        }
    }

    private void chiudi() {
        Stage stage = (Stage) btnAnnulla.getScene().getWindow();
        stage.close();
        if (visualizzaController != null) {
            visualizzaController.aggiornaTabella();
        }

    }
}
