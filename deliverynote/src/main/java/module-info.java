module com.aarsoma.deliverynote {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires java.sql;
    requires static lombok;
    //requires eu.hansolo.tilesfx;

    opens com.aarsoma.deliverynote to javafx.fxml;
    //opens com.aarsoma.deliverynote.controller to javafx.fxml;
    //opens com.aarsoma.deliverynote.model to javafx.base;

    //exports com.aarsoma.deliverynote.controller;
    exports com.aarsoma.deliverynote;
}
