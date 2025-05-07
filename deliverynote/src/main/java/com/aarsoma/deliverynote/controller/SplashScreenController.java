package com.aarsoma.deliverynote.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SplashScreenController {

    @FXML
    private void handleNextButton(ActionEvent event) {
        try {
            // Load dashboard screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aarsoma/deliverynote/view/dashboard.fxml"));
            Parent dashboardRoot = loader.load();

            // Get current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set new scene
            Scene scene = new Scene(dashboardRoot, 1024, 768);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setTitle("Dashboard - AARSOMA GRAPHICS DELIVERY NOTE SYSTEM");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Show error dialog
        }
    }
}
