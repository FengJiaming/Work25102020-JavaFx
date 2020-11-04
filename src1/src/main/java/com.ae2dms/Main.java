package com.ae2dms;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.ae2dms.model.GameEngine;
import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Javafx program entry
     * @param stage
     * @throws IOException
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("view/startPage.fxml"));
        Parent root = loader.load();
        stage.setTitle(GameEngine.GAME_NAME);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.show();
    }

}
