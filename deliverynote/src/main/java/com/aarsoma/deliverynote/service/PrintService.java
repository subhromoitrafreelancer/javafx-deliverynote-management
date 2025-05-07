package com.aarsoma.deliverynote.service;

import com.aarsoma.deliverynote.model.DeliveryNote;
/*
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;
*/

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class PrintService {

    public void printDeliveryNote(DeliveryNote deliveryNote) {
       /* try {
            // Load report template
            InputStream reportTemplate = getClass().getResourceAsStream("/com/aarsoma/deliverynote/reports/DeliveryNote.jasper");

            // Prepare parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("NOTE_NUMBER", deliveryNote.getNoteNumber());
            parameters.put("CUSTOMER_NAME", deliveryNote.getCustomer().getName());
            parameters.put("CUSTOMER_ADDRESS", deliveryNote.getCustomer().getAddress());
            parameters.put("ISSUE_DATE", deliveryNote.getIssueDate());
            parameters.put("FOOTER_TEXT", "This is a system generated Delivery Note & does not require a physical signature");

            // Create data source from delivery items
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(deliveryNote.getItems());

            // Fill report
            JasperPrint jasperPrint = JasperFillManager.fillReport(reportTemplate, parameters, dataSource);

            // Show print preview
            JasperViewer viewer = new JasperViewer(jasperPrint, false);
            viewer.setTitle("Print Preview - Delivery Note #" + deliveryNote.getNoteNumber());
            viewer.setVisible(true);
        } catch (JRException e) {
            e.printStackTrace();
            // Show error dialog
        }*/
    }
}
