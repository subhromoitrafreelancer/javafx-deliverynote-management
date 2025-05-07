package com.aarsoma.deliverynote.controller;

import com.aarsoma.deliverynote.model.Customer;
import com.aarsoma.deliverynote.model.DeliveryNote;
import com.aarsoma.deliverynote.service.CustomerService;
import com.aarsoma.deliverynote.service.DeliveryNoteService;
import com.aarsoma.deliverynote.service.PrintService;
import com.aarsoma.deliverynote.util.AlertUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class DeliveryHistoryController implements Initializable {

    @FXML private ComboBox<Customer> customerComboBox;
    @FXML private DatePicker singleDatePicker;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TableView<DeliveryNote> deliveryNotesTable;
    @FXML private TableColumn<DeliveryNote, String> noteNumberColumn;
    @FXML private TableColumn<DeliveryNote, String> issueDateColumn;
    @FXML private TableColumn<DeliveryNote, String> customerNameColumn;
    @FXML private TableColumn<DeliveryNote, Integer> itemCountColumn;
    @FXML private VBox detailsPane;
    @FXML private Label selectedNoteNumberLabel;
    @FXML private Label selectedCustomerLabel;
    @FXML private Label selectedDateLabel;
    @FXML private TableView<DeliveryItemViewModel> itemsTable;
    @FXML private TableColumn<DeliveryItemViewModel, Integer> itemSrNoColumn;
    @FXML private TableColumn<DeliveryItemViewModel, String> itemNameColumn;
    @FXML private TableColumn<DeliveryItemViewModel, Integer> orderedQtyColumn;
    @FXML private TableColumn<DeliveryItemViewModel, Integer> deliveredQtyColumn;
    @FXML private TableColumn<DeliveryItemViewModel, Integer> balanceQtyColumn;

    private final DeliveryNoteService deliveryNoteService = new DeliveryNoteService();
    private final CustomerService customerService = new CustomerService();
    private final PrintService printService = new PrintService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize date pickers with current date
        LocalDate today = LocalDate.now();
        singleDatePicker.setValue(today);
        startDatePicker.setValue(today.minusWeeks(1));
        endDatePicker.setValue(today);

        // Load customers
        loadCustomers();

        // Configure delivery notes table
        configureDeliveryNotesTable();

        // Configure items table
        configureItemsTable();

        // Hide details pane initially
        detailsPane.setVisible(false);

        // Load all delivery notes initially
        loadAllDeliveryNotes();
    }

    private void loadCustomers() {
        try {
            List<Customer> customers = customerService.getAllCustomers();
            customerComboBox.setItems(FXCollections.observableArrayList(customers));
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showErrorAlert("Error", "Could not load customers", e.getMessage());
        }
    }

    private void configureDeliveryNotesTable() {
        noteNumberColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNoteNumber()));

        issueDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getIssueDate().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));

        customerNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCustomer().getName()));

        itemCountColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getItems().size()));

        deliveryNotesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        showDeliveryNoteDetails(newSelection);
                    } else {
                        detailsPane.setVisible(false);
                    }
                }
        );
    }

    private void configureItemsTable() {
        itemSrNoColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getSrNo()));

        itemNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getItemName()));

        orderedQtyColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getOrderedQty()));

        deliveredQtyColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getDeliveredQty()));

        balanceQtyColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getBalanceQty()));
    }

    @FXML
    private void handleSearchByCustomer() {
        Customer selectedCustomer = customerComboBox.getValue();
        if (selectedCustomer == null) {
            AlertUtil.showWarningAlert("Warning", "No Customer Selected",
                    "Please select a customer to search.");
            return;
        }

        try {
            List<DeliveryNote> notes = deliveryNoteService.getDeliveryNotesByCustomer(
                    selectedCustomer.getId());

            if (notes.isEmpty()) {
                AlertUtil.showInformationAlert("No Results", "No Delivery Notes Found",
                        "No delivery notes found for the selected customer.");
                deliveryNotesTable.setItems(FXCollections.observableArrayList());
                detailsPane.setVisible(false);
            } else {
                deliveryNotesTable.setItems(FXCollections.observableArrayList(notes));
                deliveryNotesTable.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showErrorAlert("Error", "Search Failed", e.getMessage());
        }
    }

    @FXML
    private void handleSearchByDate() {
        LocalDate date = singleDatePicker.getValue();
        if (date == null) {
            AlertUtil.showWarningAlert("Warning", "No Date Selected",
                    "Please select a date to search.");
            return;
        }

        try {
            List<DeliveryNote> notes = deliveryNoteService.getDeliveryNotesByDate(date);

            if (notes.isEmpty()) {
                AlertUtil.showInformationAlert("No Results", "No Delivery Notes Found",
                        "No delivery notes found for the selected date.");
                deliveryNotesTable.setItems(FXCollections.observableArrayList());
                detailsPane.setVisible(false);
            } else {
                deliveryNotesTable.setItems(FXCollections.observableArrayList(notes));
                deliveryNotesTable.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showErrorAlert("Error", "Search Failed", e.getMessage());
        }
    }

    @FXML
    private void handleSearchByDateRange() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            AlertUtil.showWarningAlert("Warning", "Date Range Incomplete",
                    "Please select both start and end dates.");
            return;
        }

        if (startDate.isAfter(endDate)) {
            AlertUtil.showWarningAlert("Warning", "Invalid Date Range",
                    "Start date must be before or equal to end date.");
            return;
        }

        try {
            List<DeliveryNote> notes = deliveryNoteService.getDeliveryNotesByDateRange(
                    startDate, endDate);

            if (notes.isEmpty()) {
                AlertUtil.showInformationAlert("No Results", "No Delivery Notes Found",
                        "No delivery notes found for the selected date range.");
                deliveryNotesTable.setItems(FXCollections.observableArrayList());
                detailsPane.setVisible(false);
            } else {
                deliveryNotesTable.setItems(FXCollections.observableArrayList(notes));
                deliveryNotesTable.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showErrorAlert("Error", "Search Failed", e.getMessage());
        }
    }

    @FXML
    private void handleShowAll() {
        loadAllDeliveryNotes();
    }

    private void loadAllDeliveryNotes() {
        try {
            List<DeliveryNote> notes = deliveryNoteService.getAllDeliveryNotes();

            if (notes.isEmpty()) {
                AlertUtil.showInformationAlert("No Data", "No Delivery Notes",
                        "There are no delivery notes in the system.");
                deliveryNotesTable.setItems(FXCollections.observableArrayList());
                detailsPane.setVisible(false);
            } else {
                deliveryNotesTable.setItems(FXCollections.observableArrayList(notes));
                deliveryNotesTable.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showErrorAlert("Error", "Load Failed", e.getMessage());
        }
    }

    @FXML
    private void handlePrint() {
        DeliveryNote selectedNote = deliveryNotesTable.getSelectionModel().getSelectedItem();
        if (selectedNote == null) {
            AlertUtil.showWarningAlert("Warning", "No Delivery Note Selected",
                    "Please select a delivery note to print.");
            return;
        }

        printService.printDeliveryNote(selectedNote);
    }

    private void showDeliveryNoteDetails(DeliveryNote note) {
        // Show details pane
        detailsPane.setVisible(true);

        // Set labels
        selectedNoteNumberLabel.setText(note.getNoteNumber());
        selectedCustomerLabel.setText(note.getCustomer().getName());
        selectedDateLabel.setText(note.getIssueDate().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // Populate items table
        List<DeliveryItemViewModel> itemViewModels = note.getItems().stream()
                .map(item -> new DeliveryItemViewModel(
                        note.getItems().indexOf(item) + 1,
                        item.getItemName(),
                        item.getOrderedQty(),
                        item.getDeliveredQty(),
                        item.getBalanceQty()))
                .collect(java.util.stream.Collectors.toList());

        itemsTable.setItems(FXCollections.observableArrayList(itemViewModels));
    }

    // Inner class for item table view
    public static class DeliveryItemViewModel {
        private final int srNo;
        private final String itemName;
        private final int orderedQty;
        private final int deliveredQty;
        private final int balanceQty;

        public DeliveryItemViewModel(int srNo, String itemName, int orderedQty, int deliveredQty, int balanceQty) {
            this.srNo = srNo;
            this.itemName = itemName;
            this.orderedQty = orderedQty;
            this.deliveredQty = deliveredQty;
            this.balanceQty = balanceQty;
        }

        public int getSrNo() {
            return srNo;
        }

        public String getItemName() {
            return itemName;
        }

        public int getOrderedQty() {
            return orderedQty;
        }

        public int getDeliveredQty() {
            return deliveredQty;
        }

        public int getBalanceQty() {
            return balanceQty;
        }
    }
}
