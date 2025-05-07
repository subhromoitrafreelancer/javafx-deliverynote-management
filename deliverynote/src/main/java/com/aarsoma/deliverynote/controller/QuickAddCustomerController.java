package com.aarsoma.deliverynote.controller;

import com.aarsoma.deliverynote.model.Customer;
import com.aarsoma.deliverynote.service.CustomerService;
import com.aarsoma.deliverynote.util.AlertUtil;
import com.aarsoma.deliverynote.util.ValidationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class QuickAddCustomerController implements Initializable {

    @FXML private TextField nameField;
    @FXML private TextField addressField;
    @FXML private TextField contactPersonField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;

    @FXML private Button saveButton;
    @FXML private Button clearButton;

    private final CustomerService customerService = new CustomerService();
    private Consumer<Customer> onCustomerAddedCallback;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize form - nothing special needed
    }

    @FXML
    private void handleSaveButton(ActionEvent event) {
        // Validate form
        if (!validateForm()) {
            return;
        }

        try {
            Customer customer = new Customer();

            // Set customer data from form
            customer.setName(nameField.getText().trim());
            customer.setAddress(addressField.getText().trim());
            customer.setContactPerson(contactPersonField.getText().trim());
            customer.setPhone(phoneField.getText().trim());
            customer.setEmail(emailField.getText().trim());

            // Save customer
            Customer savedCustomer = customerService.saveCustomer(customer);

            // Show success message
            AlertUtil.showInformationAlert("Success", "Customer Added",
                    "Customer \"" + savedCustomer.getName() + "\" added successfully.");

            // Notify the calling form through callback
            if (onCustomerAddedCallback != null) {
                onCustomerAddedCallback.accept(savedCustomer);
            }

            // Close the dialog
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showErrorAlert("Error", "Could not save customer", e.getMessage());
        }
    }

    @FXML
    private void handleClearButton(ActionEvent event) {
        clearForm();
    }

    private void clearForm() {
        nameField.clear();
        addressField.clear();
        contactPersonField.clear();
        phoneField.clear();
        emailField.clear();
    }

    private boolean validateForm() {
        // Validate required fields
        if (nameField.getText().trim().isEmpty()) {
            AlertUtil.showWarningAlert(
                    "Validation Error",
                    "Name Required",
                    "Please enter a name for the customer."
            );
            nameField.requestFocus();
            return false;
        }

        // Validate email format if provided
        if (!ValidationUtil.isValidEmail(emailField.getText().trim())) {
            AlertUtil.showWarningAlert(
                    "Validation Error",
                    "Invalid Email",
                    "Please enter a valid email address or leave it blank."
            );
            emailField.requestFocus();
            return false;
        }

        // Validate phone format if provided
        if (!ValidationUtil.isValidPhone(phoneField.getText().trim())) {
            AlertUtil.showWarningAlert(
                    "Validation Error",
                    "Invalid Phone",
                    "Please enter a valid phone number or leave it blank."
            );
            phoneField.requestFocus();
            return false;
        }

        return true;
    }

    public void setOnCustomerAddedCallback(Consumer<Customer> callback) {
        this.onCustomerAddedCallback = callback;
    }
}
