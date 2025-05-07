
// DashboardController.java
package com.aarsoma.deliverynote.controller;

import com.aarsoma.deliverynote.model.FinancialYear;
import com.aarsoma.deliverynote.model.Statistics;
import com.aarsoma.deliverynote.service.StatisticsService;
import com.aarsoma.deliverynote.util.AlertUtil;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private MenuBar menuBar;
    @FXML private MenuItem customerMenuItem;
    @FXML private MenuItem deliveryChallanMenuItem;
    @FXML private MenuItem deliveryHistoryMenuItem;

    @FXML private Label dateTimeLabel;
    @FXML private Label totalDeliveryNotesLabel;
    @FXML private Label fyDeliveryNotesLabel;
    @FXML private Label monthlyDeliveryNotesLabel;
    @FXML private Label weeklyDeliveryNotesLabel;
    @FXML private Label dailyDeliveryNotesLabel;
    @FXML private Label financialYearLabel;

    private final StatisticsService statisticsService = new StatisticsService();
    private Timeline clockTimeline;
    private Timeline statsUpdateTimeline;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize clock
        initializeClock();

        // Set financial year
        String currentFY = FinancialYear.getCurrentFinancialYear();
        financialYearLabel.setText("Financial Year: " + currentFY);

        // Load initial statistics
        loadStatistics();

        // Set up periodic statistics refresh (every 5 minutes)
        statsUpdateTimeline = new Timeline(
                new KeyFrame(Duration.minutes(5), event -> loadStatistics())
        );
        statsUpdateTimeline.setCycleCount(Animation.INDEFINITE);
        statsUpdateTimeline.play();
    }

    private void initializeClock() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        updateClock(formatter);

        clockTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> updateClock(formatter))
        );
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();
    }

    private void updateClock(DateTimeFormatter formatter) {
        LocalDateTime now = LocalDateTime.now();
        dateTimeLabel.setText(now.format(formatter));
    }

    private void loadStatistics() {
        try {
            Statistics stats = statisticsService.getDeliveryNoteStatistics();

            totalDeliveryNotesLabel.setText(String.valueOf(stats.getTotalDeliveryNotes()));
            fyDeliveryNotesLabel.setText(String.valueOf(stats.getFinancialYearDeliveryNotes()));
            monthlyDeliveryNotesLabel.setText(String.valueOf(stats.getMonthlyDeliveryNotes()));
            weeklyDeliveryNotesLabel.setText(String.valueOf(stats.getWeeklyDeliveryNotes()));
            dailyDeliveryNotesLabel.setText(String.valueOf(stats.getDailyDeliveryNotes()));
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showErrorAlert("Error", "Could not load statistics", e.getMessage());
        }
    }

    @FXML
    private void handleCustomerMenuItem(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aarsoma/deliverynote/view/customermanagement.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Customer Management");
            stage.setScene(new Scene(root, 800, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh stats after customer management is closed
            loadStatistics();
        } catch (IOException e) {
            AlertUtil.showErrorAlert("Error", "Could not open customer management", e.getMessage());
        }
    }

    @FXML
    private void handleDeliveryChallanMenuItem(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aarsoma/deliverynote/view/deliverynote.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Create Delivery Note");
            stage.setScene(new Scene(root, 900, 700));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh stats after delivery note creation is closed
            loadStatistics();
        } catch (IOException e) {
            AlertUtil.showErrorAlert("Error", "Could not open delivery note form", e.getMessage());
        }
    }

    @FXML
    private void handleDeliveryHistoryMenuItem(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aarsoma/deliverynote/view/deliveryhistory.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Delivery Note History");
            stage.setScene(new Scene(root, 900, 700));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            AlertUtil.showErrorAlert("Error", "Could not open delivery history", e.getMessage());
        }
    }


    public void stop() {
        // Stop timelines when controller is destroyed
        if (clockTimeline != null) {
            clockTimeline.stop();
        }
        if (statsUpdateTimeline != null) {
            statsUpdateTimeline.stop();
        }
    }
}
