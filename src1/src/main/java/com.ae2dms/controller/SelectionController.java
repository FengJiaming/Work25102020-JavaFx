package com.ae2dms.controller;

import com.ae2dms.Main;
import com.ae2dms.model.GameEngine;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Controller of level selection page
 */
public class SelectionController {

    private  Stage primaryStage;
    @FXML
    private Label titleLabel;
    @FXML
    private GridPane newLevelsGrid;
    @FXML
    private GridPane oldLevelsGrid;
    @FXML
    private HBox twoButtonHbox;
    @FXML
    private TextArea textArea;

    private MainController mainController;

    /**
     * Initialize the controller
     *
     * @param mainController Need to call the method of the main controller
     * @param primaryStage the main interface stage to return
     */
    public void init(MainController mainController, Stage primaryStage) {
        this.mainController = mainController;
        this.primaryStage = primaryStage;
    }

    /**
     * Show old level list
     */
    public void showOldLevels() {
        oldLevelsGrid.setVisible(true);
        twoButtonHbox.setVisible(false);
        textArea.setVisible(false);
        titleLabel.setVisible(true);
        titleLabel.setText("Old Levels");

    }

    /**
     * Show a list of customized new levels
     */
    public void showNewLevels() {
        newLevelsGrid.setVisible(true);
        twoButtonHbox.setVisible(false);
        textArea.setVisible(false);
        titleLabel.setVisible(true);
        titleLabel.setText("New Levels");

    }

    /**
     * Button event of old level selection
     * @param event Mouse click event
     */
    @FXML
    void selectOldLevel(ActionEvent event){
        Button button = (Button) event.getSource();
        System.out.println(event.getSource().toString());
        InputStream in = getClass().getResourceAsStream("/level/SampleGame.skb");
        try {
            loadMainView(in, Integer.parseInt(button.getText()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = (Stage) oldLevelsGrid.getScene().getWindow();
        stage.close();
    }

    /**
     * Button event of new level selection
     * @param event Mouse click event
     */
    @FXML
    void selectNewLevel(ActionEvent event){
        Button button = (Button) event.getSource();
        InputStream in = getClass().getResourceAsStream("/level/MyLevels/MyGame.skb");
        try {
            loadMainView(in, Integer.parseInt(button.getText()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = (Stage) newLevelsGrid.getScene().getWindow();
        stage.close();
    }

    /**
     * Loading main interface
     * @param in
     * @param levelIndex
     * @throws IOException
     */
    private void loadMainView(InputStream in, int levelIndex) throws IOException {
        mainController.loadLevelsFile(primaryStage,in,levelIndex);
    }
}
