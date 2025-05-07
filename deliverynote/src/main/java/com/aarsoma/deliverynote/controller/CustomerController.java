package com.aarsoma.deliverynote.controller;

import com.aarsoma.deliverynote.model.Customer;
import com.aarsoma.deliverynote.service.CustomerService;
import com.aarsoma.deliverynote.util.AlertUtil;
import com.aarsoma.deliverynote.util.ValidationUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class CustomerController implements Initializable {

    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> nameColumn;
    @FXML private TableColumn<Customer, String> contactPersonColumn;
    @FXML private TableColumn<Customer, String> phoneColumn;
    @FXML private TableColumn<Customer, String> emailColumn;

    @FXML private TextField nameField;
    @FXML private TextField addressField;
    @FXML private TextField contactPersonField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;

    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Button deleteButton;

    @FXML private VBox formContainer;

    private final CustomerService customerService = new CustomerService();
    private Customer selectedCustomer;
    private Consumer<Customer> onCustomerAddedCallback;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configure table columns
        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));

        contactPersonColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getContactPerson()));

        phoneColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPhone()));

        emailColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEmail()));

        // Load customers
        loadCustomers();

        // Set table selection listener
        customerTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        showCustomerDetails(newSelection);
                        deleteButton.setDisable(false);
                    } else {
                        clearForm();
                        deleteButton.setDisable(true);
                    }
                }
        );

        // Initialize form
        clearForm();
        deleteButton.setDisable(true);
    }

    private void loadCustomers() {
        try {
            List<Customer> customers = customerService.getAllCustomers();
            customerTable.setItems(FXCollections.observableArrayList(customers));
        } catch (SQLException e) {
            AlertUtil.showErrorAlert("Error", "Could not load customers", e.getMessage());
        }
    }

    private void showCustomerDetails(Customer customer) {
        selectedCustomer = customer;

        nameField.setText(customer.getName());
        addressField.setText(customer.getAddress());
        contactPersonField.setText(customer.getContactPerson());
        phoneField.setText(customer.getPhone());
        emailField.setText(customer.getEmail());

        saveButton.setText("Update");
    }

    @FXML
    private void handleSaveButton(ActionEvent event) {
        // Validate form
        if (!validateForm()) {
            return;
        }

        try {
            Customer customer = new Customer();

            // If updating existing customer, set its ID
            if (selectedCustomer != null) {
                customer.setId(selectedCustomer.getId());
            }

            // Set customer data from form
            customer.setName(nameField.getText().trim());
            customer.setAddress(addressField.getText().trim());
            customer.setContactPerson(contactPersonField.getText().trim());
            customer.setPhone(phoneField.getText().trim());
            customer.setEmail(emailField.getText().trim());

            // Save customer
            Customer savedCustomer = customerService.saveCustomer(customer);

            // Show success message
            String message = (selectedCustomer != null)
                    ? "Customer updated successfully."
                    : "Customer added successfully.";
            AlertUtil.showInformationAlert("Success", "Customer Saved", message);

            // Refresh table
            loadCustomers();

            // Select the saved customer
            for (Customer c : customerTable.getItems()) {
                if (c.getId().equals(savedCustomer.getId())) {
                    customerTable.getSelectionModel().select(c);
                    break;
                }
            }

            // Notify callback if new customer added
            if (selectedCustomer == null && onCustomerAddedCallback != null) {
                onCustomerAddedCallback.accept(savedCustomer);
            }

            // If this was opened as a dialog from another form
            if (onCustomerAddedCallback != null && selectedCustomer == null) {
                // Close this window
                Stage stage = (Stage) saveButton.getScene().getWindow();
                stage.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showErrorAlert("Error", "Could not save customer", e.getMessage());
        }
    }

    @FXML
    private void handleClearButton(ActionEvent event) {
        clearForm();
    }

    @FXML
    private void handleDeleteButton(ActionEvent event) {
        if (selectedCustomer == null) {
            return;
        }

        // Confirm deletion
        boolean confirmed = AlertUtil.showConfirmationAlert(
                "Confirm Delete",
                "Delete Customer",
                "Are you sure you want to delete the selected customer?\nThis action cannot be undone."
        );

        if (!confirmed) {
            return;
        }

        try {
            // Check if customer is used in any delivery notes
            boolean isUsed = customerService.isCustomerUsedInDeliveryNotes(selectedCustomer.getId());

            if (isUsed) {
                AlertUtil.showWarningAlert(
                        "Cannot Delete",
                        "Customer In Use",
                        "This customer cannot be deleted because it is used in one or more delivery notes."
                );
                return;
            }

            // Delete customer
            customerService.deleteCustomer(selectedCustomer.getId());

            // Show success message
            AlertUtil.showInformationAlert(
                    "Success",
                    "Customer Deleted",
                    "Customer has been deleted successfully."
            );

            // Refresh table
            loadCustomers();

            // Clear form
            clearForm();

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showErrorAlert("Error", "Could not delete customer", e.getMessage());
        }
    }

    private void clearForm() {
        selectedCustomer = null;

        nameField.clear();
        addressField.clear();
        contactPersonField.clear();
        phoneField.clear();
        emailField.clear();

        saveButton.setText("Add");
        deleteButton.setDisable(true);
        customerTable.getSelectionModel().clearSelection();
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
