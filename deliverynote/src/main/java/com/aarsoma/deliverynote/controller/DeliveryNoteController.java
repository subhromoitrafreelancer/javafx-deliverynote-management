package com.aarsoma.deliverynote.controller;

import com.aarsoma.deliverynote.model.Customer;
import com.aarsoma.deliverynote.model.DeliveryItem;
import com.aarsoma.deliverynote.model.DeliveryNote;
import com.aarsoma.deliverynote.model.FinancialYear;
import com.aarsoma.deliverynote.service.CustomerService;
import com.aarsoma.deliverynote.service.DeliveryNoteService;
import com.aarsoma.deliverynote.service.PrintService;
import com.aarsoma.deliverynote.util.AlertUtil;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DeliveryNoteController implements Initializable {

    @FXML private TextField noteNumberField;
    @FXML private TextField dateField;
    @FXML private ComboBox<Customer> customerComboBox;

    @FXML private TableView<DeliveryItem> itemsTable;
    @FXML private TableColumn<DeliveryItem, Integer> srNoColumn;
    @FXML private TableColumn<DeliveryItem, String> itemNameColumn;
    @FXML private TableColumn<DeliveryItem, Integer> orderedQtyColumn;
    @FXML private TableColumn<DeliveryItem, Integer> deliveredQtyColumn;
    @FXML private TableColumn<DeliveryItem, Integer> balanceQtyColumn;

    private final DeliveryNoteService deliveryNoteService = new DeliveryNoteService();
    private final CustomerService customerService = new CustomerService();
    private final PrintService printService = new PrintService();

    private final ObservableList<DeliveryItem> itemsList = FXCollections.observableArrayList();
    private LocalDateTime currentDateTime;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set current date and time
        currentDateTime = LocalDateTime.now();
        dateField.setText(currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // Generate next delivery note number
        try {
            String nextNumber = deliveryNoteService.generateNextDeliveryNoteNumber();
            noteNumberField.setText(nextNumber);
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showErrorAlert("Error", "Could not generate delivery note number", e.getMessage());
        }

        // Load customers
        try {
            List<Customer> customers = customerService.getAllCustomers();
            customerComboBox.setItems(FXCollections.observableArrayList(customers));
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showErrorAlert("Error", "Could not load customers", e.getMessage());
        }

        // Configure table columns
        configureItemTable();

        // Set table items
        itemsTable.setItems(itemsList);
    }

    private void configureItemTable() {
        // Serial number column
        srNoColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(itemsTable.getItems().indexOf(cellData.getValue()) + 1));

        // Item name column
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        itemNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        itemNameColumn.setOnEditCommit(event -> {
            DeliveryItem item = event.getRowValue();
            item.setItemName(event.getNewValue());
        });

        // Ordered quantity column
        orderedQtyColumn.setCellValueFactory(new PropertyValueFactory<>("orderedQty"));
        orderedQtyColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        orderedQtyColumn.setOnEditCommit(event -> {
            DeliveryItem item = event.getRowValue();
            item.setOrderedQty(event.getNewValue());
            updateBalanceQty(item);
        });

        // Delivered quantity column
        deliveredQtyColumn.setCellValueFactory(new PropertyValueFactory<>("deliveredQty"));
        deliveredQtyColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        deliveredQtyColumn.setOnEditCommit(event -> {
            DeliveryItem item = event.getRowValue();
            item.setDeliveredQty(event.getNewValue());
            updateBalanceQty(item);
        });

        // Balance quantity column
        balanceQtyColumn.setCellValueFactory(new PropertyValueFactory<>("balanceQty"));

        // Make table editable
        itemsTable.setEditable(true);
    }

    private void updateBalanceQty(DeliveryItem item) {
        int balance = item.getOrderedQty() - item.getDeliveredQty();
        item.setBalanceQty(balance);
        itemsTable.refresh();
    }

    @FXML
    private void handleAddItem() {
        DeliveryItem newItem = new DeliveryItem();
        newItem.setItemName("");
        newItem.setOrderedQty(0);
        newItem.setDeliveredQty(0);
        newItem.setBalanceQty(0);

        itemsList.add(newItem);

        // Start editing the new item
        int row = itemsList.size() - 1;
        itemsTable.getSelectionModel().select(row);
        itemsTable.edit(row, itemNameColumn);
    }

    @FXML
    private void handleRemoveItem() {
        DeliveryItem selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            itemsList.remove(selectedItem);
        } else {
            AlertUtil.showWarningAlert("Warning", "No Item Selected", "Please select an item to remove.");
        }
    }

    @FXML
    private void handleSave() {
        // Validate input
        if (customerComboBox.getValue() == null) {
            AlertUtil.showWarningAlert("Warning", "Customer Required", "Please select a customer.");
            return;
        }

        if (itemsList.isEmpty()) {
            AlertUtil.showWarningAlert("Warning", "No Items", "Please add at least one item.");
            return;
        }

        // Check for empty items
        List<DeliveryItem> emptyItems = itemsList.stream()
                .filter(item -> item.getItemName() == null || item.getItemName().trim().isEmpty())
                .collect(Collectors.toList());

        if (!emptyItems.isEmpty()) {
            AlertUtil.showWarningAlert("Warning", "Empty Items", "Please provide names for all items.");
            return;
        }

        try {
            // Create delivery note
            DeliveryNote deliveryNote = new DeliveryNote();
            deliveryNote.setNoteNumber(noteNumberField.getText());
            deliveryNote.setCustomerId(customerComboBox.getValue().getId());
            deliveryNote.setCustomer(customerComboBox.getValue());
            deliveryNote.setIssueDate(currentDateTime);
            deliveryNote.setFinancialYear(FinancialYear.getCurrentFinancialYear());
            deliveryNote.setItems(itemsList);

            // Save delivery note
            DeliveryNote savedNote = deliveryNoteService.saveDeliveryNote(deliveryNote);

            AlertUtil.showInformationAlert("Success", "Delivery Note Saved",
                    "Delivery Note #" + savedNote.getNoteNumber() + " has been saved successfully.");

            // Ask if user wants to print
            boolean printNow = AlertUtil.showConfirmationAlert("Print", "Print Delivery Note",
                    "Do you want to print this delivery note now?");

            if (printNow) {
                handlePrintPreview();
            }

            // Clear form for new entry
            clearForm();

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showErrorAlert("Error", "Could not save delivery note", e.getMessage());
        }
    }

    @FXML
    private void handlePrintPreview() {
        if (customerComboBox.getValue() == null || itemsList.isEmpty()) {
            AlertUtil.showWarningAlert("Warning", "Incomplete Data",
                    "Please fill in all required fields before preview.");
            return;
        }

        DeliveryNote deliveryNote = new DeliveryNote();
        deliveryNote.setNoteNumber(noteNumberField.getText());
        deliveryNote.setCustomer(customerComboBox.getValue());
        deliveryNote.setIssueDate(currentDateTime);
        deliveryNote.setItems(itemsList);

        printService.printDeliveryNote(deliveryNote);
    }
    @FXML
    private void handleAddCustomer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aarsoma/deliverynote/view/customer_form.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add New Customer");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            // Set the controller to notify when a new customer is added
            QuickAddCustomerController controller = loader.getController();
            controller.setOnCustomerAddedCallback(customer -> {
                refreshCustomers();

                // Select the newly added customer
                customerComboBox.getSelectionModel().select(customer);
            });

            stage.showAndWait();
        } catch (IOException e) {
            AlertUtil.showErrorAlert("Error", "Could not open add customer form", e.getMessage());
        }
    }

    private void refreshCustomers() {
        try {
            List<Customer> customers = customerService.getAllCustomers();
            customerComboBox.setItems(FXCollections.observableArrayList(customers));
            // Select the last added customer
            if (!customers.isEmpty()) {
                customerComboBox.getSelectionModel().select(customers.size() - 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showErrorAlert("Error", "Could not refresh customers", e.getMessage());
        }
    }

    private void clearForm() {
        try {
            // Generate next delivery note number
            String nextNumber = deliveryNoteService.generateNextDeliveryNoteNumber();
            noteNumberField.setText(nextNumber);

            // Update current date and time
            currentDateTime = LocalDateTime.now();
            dateField.setText(currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // Clear customer selection
            customerComboBox.getSelectionModel().clearSelection();

            // Clear items
            itemsList.clear();
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showErrorAlert("Error", "Could not generate new delivery note number", e.getMessage());
        }
    }
}
