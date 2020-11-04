package com.ae2dms.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.effect.Effect;
import javafx.scene.effect.MotionBlur;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.ae2dms.model.GameEngine;
import com.ae2dms.model.GameObject;
import com.ae2dms.model.GraphicObject;
import com.ae2dms.model.Level;
import javafx.stage.WindowEvent;

import java.awt.*;
import java.io.*;

/**
 * The main interface controller of the game, responsible for handling menu item button operations
 *
 * @version 2.0
 */
public class MainController {

    private Stage primaryStage;
    @FXML
    public MenuBar menu;
    @FXML
    private MenuItem undoItem;
    @FXML
    public GridPane gameGrid;
    /**
     * Game Engine
     */
    private GameEngine gameEngine;
    private File saveFile;

    /**
     * Loads the default game file.
     *
     * @param primaryStage the primary stage that will display the game
     */
    public void loadDefaultSaveFile(Stage primaryStage) {
        this.primaryStage = primaryStage;
        InputStream in = getClass().getResourceAsStream("/level/SampleGame.skb");
        initializeGame(in, 1);

        setEventFilter();
    }

    /**
     * Load custom game level files
     *
     * @param primaryStage The stage of the main interface
     * @param in File stream of level files
     * @param levelIndex Index of the level to be loaded
     * @throws FileNotFoundException
     */
    public void loadLevelsFile(Stage primaryStage,InputStream in, int levelIndex) throws FileNotFoundException {
        this.primaryStage = primaryStage;
        initializeGame(in, levelIndex);
    }
    /**
     * Initializes the game using the provided game file.
     *
     * @param input the game file to be loaded
     */
    public void initializeGame(InputStream input, int levelIndex) {
        gameEngine = new GameEngine(input, true);
        gameEngine.setCurrentLevel(levelIndex);
        reloadGrid();
    }

    /**
     * Adds the event filter to handle {@link KeyEvent}s passing them to {@link GameEngine}.
     */
    private void setEventFilter() {
        primaryStage.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            gameEngine.handleKey(event.getCode());
            reloadGrid();
        });
    }

    /**
     * Opens the load game window
     */
    private void loadGameFile() throws FileNotFoundException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Save File");
        fileChooser.setInitialDirectory(new File(getClass().getResource("/").getPath()));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Sokoban save file", "*.skb"));
        saveFile = fileChooser.showOpenDialog(primaryStage);

        if (saveFile != null) {
            if (GameEngine.isDebugActive()) {
                GameEngine.logger.info("Loading save file: " + saveFile.getName());
            }
            initializeGame(new FileInputStream(saveFile), 1);
        }
    }

    /**
     *  Save the current game to a file
     */
    private void saveGameFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        fileChooser.setInitialDirectory(new File(getClass().getResource("/").getPath()));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Sokoban save file", "*.skb"));
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try {
                FileOutputStream fos = new FileOutputStream(file);
                OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
                out.write("MapSetName: " + gameEngine.mapSetName);
                out.write("\r\n");
                int currentLevelIndex = gameEngine.getCurrentLevel().getIndex();
                for (int i = currentLevelIndex - 1; i < gameEngine.getLevels().size();i++) {
                    Level level = null;
                    if (i + 1 == currentLevelIndex) {
                        level = gameEngine.getCurrentLevel();
                    } else {
                        level = gameEngine.getLevels().get(i);
                    }
                    out.write("LevelName: " + level.getName());
                    out.write("\r\n");
                    for (int col = 0; col < level.levelRow; col++) {
                        for (int row = 0; row < level.levelColumn; row++) {

                            if (level.diamondsGrid.getGameObjectAt(col,row) != null) {
                                out.write(level.diamondsGrid.getGameObjectAt(col,row).getCharSymbol());
                            } else {
                                out.write(level.objectsGrid.getGameObjectAt(col,row).getCharSymbol());
                            }
                        }
                        out.write("\r\n");
                    }
                    out.write("\r\n");
                }
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            ;
        }

    }
    /**
     * Reloads the grid using the {@link Level} iterator.
     */
    private void reloadGrid() {
        if (gameEngine.isGameComplete()) {

            gameEngine.recordScore();
            showHighScore();
            showVictoryMessage();

            return;
        }
        if (gameEngine.isKeeperMoved()) {
            undoItem.setDisable(false);
        } else {
            undoItem.setDisable(true);
        }
        Level currentLevel = gameEngine.getCurrentLevel();
        Level.LevelIterator levelGridIterator = (Level.LevelIterator) currentLevel.iterator();
        gameGrid.getChildren().clear();
        while (levelGridIterator.hasNext()) {
            addObjectToGrid(levelGridIterator.next(), levelGridIterator.getCurrentPosition());
        }
        gameGrid.autosize();
        primaryStage.sizeToScene();
    }

    /**
     * Pop-up message after the game is won
     */
    private void showVictoryMessage()  {

        String dialogTitle = "Game Over!";
        String dialogMessage = "You completed " + gameEngine.mapSetName + " in " + gameEngine.movesCount + " moves!\nTime used " + gameEngine.gameTime + "s";
        MotionBlur mb = new MotionBlur(2, 3);

        newDialog(dialogTitle, dialogMessage, mb);

    }

    /**
     * Show score list
     */
    private void showHighScore() {
        Stage scorePage = new Stage();
        scorePage.initOwner(primaryStage);
        scorePage.initModality(Modality.APPLICATION_MODAL);
        scorePage.setTitle("Score");
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("view/scorePage.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        scorePage.setScene(new Scene(root));
        scorePage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    showSelectLevelPage();
                }
            });
        scorePage.show();
        gameEngine.loadScoreFile();
        ScorePageController scorePageController = loader.getController();
        scorePageController.initScorePage(gameEngine.listTimeScore, gameEngine.listMoveScore);
    }

    /**
     * Show select level page
     */
    private void showSelectLevelPage() {
        Stage selectionPage = new Stage();
        selectionPage.initOwner(primaryStage);
        selectionPage.initModality(Modality.APPLICATION_MODAL);
        selectionPage.setTitle("Victory!");
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("view/selectionPage.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        selectionPage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.exit(0);
            }
        });
        selectionPage.setScene(new Scene(root));
        selectionPage.show();
        SelectionController selectionController = loader.getController();
        selectionController.init(this,primaryStage);
    }

    /**
     * Create dialog window
     *
     * @param dialogTitle The title of the dialog
     * @param dialogMessage The message of the dialog
     * @param dialogMessageEffect Message effects
     */
    private void newDialog(String dialogTitle, String dialogMessage, Effect dialogMessageEffect) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setResizable(false);
        dialog.setTitle(dialogTitle);

        Text text1 = new Text(dialogMessage);
        text1.setTextAlignment(TextAlignment.CENTER);
        text1.setFont(javafx.scene.text.Font.font(14));

        if (dialogMessageEffect != null) {
            text1.setEffect(dialogMessageEffect);
        }

        VBox dialogVbox = new VBox(20);
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.setBackground(Background.EMPTY);
        dialogVbox.getChildren().add(text1);
        Scene dialogScene = new Scene(dialogVbox, 350, 150);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    /**
     * Adds an object to the specified grid position.
     * It first converts a {@link GameObject} into a {@link javafx.scene.shape.Rectangle},
     * then adds the new rectangle into the specified location.
     *
     * @param gameObject the game object to be added into the grid
     * @param location   the location where the game object will be added
     */
    private void addObjectToGrid(GameObject gameObject, Point location) {
        GraphicObject graphicObject = new GraphicObject(gameObject);
        gameGrid.add(graphicObject, location.y, location.x);
    }

    /**
     * Close game when click exit
     */
    public void closeGame() {
        System.exit(0);
    }

    /**
     * Menuitem:Save game
     */
    public void saveGame() {
        saveGameFile();
    }

    /**
     * Menuitem:load game
     */
    public void loadGame() {
        try {
            loadGameFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Menuitem:undo
     */
    public void undo() {
        gameEngine.undo();
        reloadGrid();
        undoItem.setDisable(true);
    }

    /**
     * Reset the level to the initial state of the current level
     */
    public void resetLevel() {
        gameEngine.resetCurrentLevel();
        reloadGrid();
    }

    /**
     * Display "About the Game" window
     */
    public void showAbout() {
        String title = "About this game";
        String message = "Game created by XXX\n";

        newDialog(title, message, null);
    }

    /**
     * Enable background music when selected
     */
    public void toggleMusic() {
        if (!gameEngine.isPlayingMusic()) {
            gameEngine.playMusic();
        } else {
            gameEngine.stopMusic();
        }
    }

    /**
     * Enter debugging mode after selection
     */
    public void toggleDebug() {
        gameEngine.toggleDebug();
        reloadGrid();
    }
}
