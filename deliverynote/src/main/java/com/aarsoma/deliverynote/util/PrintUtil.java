package com.aarsoma.deliverynote.util;

import javafx.collections.ObservableSet;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Window;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PrintUtil {

    public static void printNodeWithDialog(Node node, String jobName) {
        // Get all available printers
        ObservableSet<Printer> printers = Printer.getAllPrinters();

        if (printers.isEmpty()) {
            AlertUtil.showErrorAlert("Print Error", "No Printers Available",
                    "No printers were found on your system.");
            return;
        }

        // Create a printer selection dialog
        List<String> printerNames = printers.stream()
                .map(Printer::getName)
                .collect(Collectors.toList());

        ChoiceDialog<String> dialog = new ChoiceDialog<>(printerNames.get(0), printerNames);
        dialog.setTitle("Select Printer");
        dialog.setHeaderText("Select a printer to use");
        dialog.setContentText("Printer:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String selectedPrinterName = result.get();
            Printer selectedPrinter = printers.stream()
                    .filter(p -> p.getName().equals(selectedPrinterName))
                    .findFirst()
                    .orElse(null);

            if (selectedPrinter != null) {
                // Create print job
                PrinterJob job = PrinterJob.createPrinterJob(selectedPrinter);
                if (job != null) {
                    //job.setJobName(jobName);

                    // Ask for number of copies
                    TextInputDialog copiesDialog = new TextInputDialog("1");
                    copiesDialog.setTitle("Copies");
                    copiesDialog.setHeaderText("Enter number of copies");
                    copiesDialog.setContentText("Copies:");

                    Optional<String> copiesResult = copiesDialog.showAndWait();
                    int copies = 1;
                    if (copiesResult.isPresent()) {
                        try {
                            copies = Integer.parseInt(copiesResult.get());
                            if (copies < 1) copies = 1;
                        } catch (NumberFormatException e) {
                            // Ignore and use default
                        }
                    }

                    // Set job settings
                    PageLayout pageLayout = selectedPrinter.createPageLayout(
                            Paper.A5, PageOrientation.PORTRAIT, Printer.MarginType.DEFAULT);
                    job.getJobSettings().setPageLayout(pageLayout);
                    job.getJobSettings().setCopies(copies);

                    // Show printer settings dialog
                    boolean showDialog = job.showPrintDialog(node.getScene().getWindow());

                    if (showDialog) {
                        // Print the node
                        boolean printed = job.printPage(node);
                        if (printed) {
                            job.endJob();
                            AlertUtil.showInformationAlert("Print", "Print Successful",
                                    "Document has been sent to the printer.");
                        } else {
                            AlertUtil.showErrorAlert("Print Error", "Print Failed",
                                    "Unable to print the document.");
                        }
                    }
                } else {
                    AlertUtil.showErrorAlert("Print Error", "Print Job Creation Failed",
                            "Unable to create print job for the selected printer.");
                }
            }
        }
    }
}
