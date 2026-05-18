package org.divini.smartwaste_g3.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HomeController {

    @FXML private Button btnVisualizza;
    @FXML private Button btnAggiungi;

    @FXML
    public void initialize() {
        btnVisualizza.setOnAction(e -> apri("visualizza.fxml"));
        btnAggiungi.setOnAction(e -> apri("aggiungi.fxml"));
    }

    private void apri(String nomeFXML) {
        try {
            Stage stage = (Stage) btnVisualizza.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/" + nomeFXML)));
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}