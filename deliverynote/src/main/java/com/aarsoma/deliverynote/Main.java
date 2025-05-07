package com.aarsoma.deliverynote;

import com.aarsoma.deliverynote.config.DBConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Initialize database
        DBConfig.initDatabase();

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aarsoma/deliverynote/view/splash.fxml"));
        Parent root = loader.load();

        stage.setTitle("AARSOMA GRAPHICS DELIVERY NOTE SYSTEM");
        stage.setScene(new Scene(root, 800, 600));
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        DBConfig.closeConnections();
    }

    public static void main(String[] args) {
        launch();
    }
}
