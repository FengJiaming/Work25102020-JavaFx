package com.ae2dms.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.ae2dms.model.GameEngine;
import java.io.IOException;

/**
 * Controller of the start page
 */
public class StartPageController {

    @FXML
    private Button startButton;

    /**
     * Button event to start the game
     * @throws IOException
     */
    public void startGame() throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("view/main.fxml"));
        Parent root = loader.load();
        Stage primaryStage = new Stage();
        primaryStage.setTitle(GameEngine.GAME_NAME);
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
        MainController mainController = loader.getController();
        mainController.loadDefaultSaveFile(primaryStage);

        Stage stage = (Stage) startButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Button event of game description
     * @throws IOException
     */
    public void showDescription() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("view/description.fxml"));
        Parent root = loader.load();
        Stage descriptionStage = new Stage();
        descriptionStage.initModality(Modality.APPLICATION_MODAL);
        descriptionStage.setTitle("Game Description");
        descriptionStage.setScene(new Scene(root));
        descriptionStage.setResizable(false);
        descriptionStage.show();
    }

}