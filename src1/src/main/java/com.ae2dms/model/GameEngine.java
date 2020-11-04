package com.ae2dms.model;

import javafx.scene.input.KeyCode;
import javax.sound.sampled.LineUnavailableException;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/**
 * The game engine is the core component of the program and handles all the game mechanics.
 *
 * @version 2.0
 */
public class GameEngine {
    /** Title of the main game window */
    public static final String GAME_NAME = "MySokobanFX";

    /** Game log */
    public static GameLogger logger;

    /** The number of moves */
    public int movesCount = 0;

    /** The name of the level read from the skb file. */
    public String mapSetName;

    /** Debug mode flag */
    private static boolean debug = false;

    /** The level currently loaded in the game  */
    private Level currentLevel;

    /** All levels loaded from skb files */
    private List<Level> levels;

    /** The flag that records whether the game is complete */
    private boolean gameComplete = false;

    /** Background music player */
    private MediaPlayer musicPlayer;

    /** Game start time */
    private long startTime;

    /** Game end time */
    private long endTime;

    /** Game time */
    public double gameTime;

    public List<Double> listTimeScore = new ArrayList<Double>();

    public List<Integer> listMoveScore = new ArrayList<Integer>();

    private Level savedLevel;

    private boolean keeperMoved = false;
    /**
     * {@code GameEngine} constructor
     * Load the game file and initialize the levels
     *
     * @param input the file containing the game levels.
     * @param production {@code true} if using the engine in live mode, {@code false}
     *                   only for testing mode.
     */
    public GameEngine(InputStream input, boolean production) {
        try {
            logger = new GameLogger();
            levels = loadGameFile(input);
            currentLevel = getNextLevel();
            musicPlayer = createPlayer();

            startTime = System.currentTimeMillis(); //Get game start time

        } catch (IOException x) {
            System.out.println("Cannot create logger.");
        } catch (NoSuchElementException e) {
            logger.warning("Cannot load the default save file: " + e.getStackTrace());
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the debug mode is active.
     *
     * @return {@code true} if the debug mode is active.
     */
    public static boolean isDebugActive() {
        return debug;
    }

    public int getMovesCount() {
        return movesCount;
    }
    /**
     * Handle keyboard key click events
     *
     * @param code the keyboard key code.
     */
    public void handleKey(KeyCode code) {
        switch (code) {
            case UP:
                move(new Point(-1, 0));
                break;

            case RIGHT:
                move(new Point(0, 1));
                break;

            case DOWN:
                move(new Point(1, 0));
                break;

            case LEFT:
                move(new Point(0, -1));
                break;

            case SPACE:
                resetCurrentLevel();
                break;

            default:
                // TODO: implement something funny.
        }

        if (isDebugActive()) {
            System.out.println(code);
        }
    }

    /**
     * Realize the operation of character movement
     *
     * @param delta moving direction
     */
    private void move(Point delta) {
        savedLevel = currentLevel.clone();
        // The character does not move if the game is complete
        if (isGameComplete()) {
            return;
        }

        // Instantiate the character and the object in the moving direction including their coordinates
        Point keeperPosition = currentLevel.getKeeperPosition();
        Point targetObjectPoint = GameGrid.translatePoint(keeperPosition, delta);

        /* Added new method for class Level and improved encapsulation */
        GameObject keeper = currentLevel.getObjectAt(keeperPosition);
        GameObject keeperTarget = currentLevel.getObjectAt(targetObjectPoint);

        // Print information if it is in debug mode
        if (GameEngine.isDebugActive()) {
            System.out.println("Current level state:");
            System.out.println(currentLevel.toString());
            System.out.println("Keeper pos: " + keeperPosition);
            System.out.println("Movement source obj: " + keeper);
            System.out.printf("Target object: %s at [%s]", keeperTarget, targetObjectPoint);
        }

        keeperMoved = false;

        switch (keeperTarget) {

            case WALL:
                break;

            case CRATE:
                // Get the object located at delta from this crate
                GameObject crateTarget = currentLevel.getTargetObject(targetObjectPoint, delta);

                // If the crate target is not FLOOR, the crate cannot be moved
                /* Simplified the code logic and used new methods to improve encapsulation, making the code clearer and simpler */
                if (crateTarget == GameObject.FLOOR) {
                    currentLevel.moveGameObjectBy(keeperTarget, targetObjectPoint, delta);
                    currentLevel.moveGameObjectBy(keeper, keeperPosition, delta);
                    keeperMoved = true;
                }
                break;

            case FLOOR:
                /* Use new method moveGameObjectBy to improve encapsulation */
                currentLevel.moveGameObjectBy(keeper, keeperPosition, delta);
                keeperMoved = true;
                break;

            default:
                logger.severe("The object to be moved was not a recognised GameObject.");
                throw new AssertionError("This should not have happened. Report this problem to the developer.");
        }

        if (keeperMoved) {
            keeperPosition.translate((int) delta.getX(), (int) delta.getY());
            movesCount++;
            if (currentLevel.isComplete()) {
                if (isDebugActive()) {
                    System.out.println("Level complete!");
                }

                currentLevel = getNextLevel();
            }
        }
    }

    /**
     * Loads a game file creating a {@Code List} of {@link Level}s.
     *
     * @param input - the file containing the levels
     * @return the list containing the levels
     */
    private List<Level> loadGameFile(InputStream input) {
        List<Level> levels = new ArrayList<>(5);
        int levelIndex = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            boolean parsedFirstLevel = false;
            List<String> rawLevel = new ArrayList<>();
            String levelName = "";

            while (true) {
                String line = reader.readLine();

                if (line == null) {
                    if (rawLevel.size() != 0) {
                        Level parsedLevel = new Level(levelName, ++levelIndex, rawLevel);
                        levels.add(parsedLevel);
                    }
                    break;
                }

                if (line.contains("MapSetName")) {
                    mapSetName = line.replace("MapSetName: ", "");
                    continue;
                }

                if (line.contains("LevelName")) {
                    if (parsedFirstLevel) {
                        Level parsedLevel = new Level(levelName, ++levelIndex, rawLevel);
                        levels.add(parsedLevel);
                        rawLevel.clear();
                    } else {
                        parsedFirstLevel = true;
                    }

                    levelName = line.replace("LevelName: ", "");
                    continue;
                }

                line = line.trim();
                line = line.toUpperCase();
                if (line.matches(".*W.*W.*")) {
                    rawLevel.add(line);
                }
            }

        } catch (IOException e) {
            logger.severe("Error trying to load the game file: " + e);
        } catch (NullPointerException e) {
            logger.severe("Cannot open the requested file: " + e);
        }

        return levels;
    }

    /**
     * Returns {@code true} if the game is complete.
     *
     * @return {@code true} if the game is complete, {@code false} otherwise
     */
    public boolean isGameComplete() {
        return gameComplete;
    }

    public boolean isKeeperMoved() {
        return keeperMoved;
    }
    /* Added: New method to create a media player */
    /**
     * Creates the player object loading the music file.
     *
     * @throws LineUnavailableException if the file is not available.
     */
    private MediaPlayer createPlayer() throws LineUnavailableException {
        File filePath = new File(getClass().getClassLoader().getResource("music/puzzle_theme.wav").toString());
        Media music = new Media(filePath.toString().replaceAll("\\\\","/"));
        MediaPlayer player = new MediaPlayer(music);
        player.setOnEndOfMedia(() -> player.seek(Duration.ZERO));
        player.setVolume(0.1);
        return player;
    }

    /**
     * Starts playing music.
     */
    public void playMusic() {
        musicPlayer.play();
    }

    /**
     * Stops playing music.
     */
    public void stopMusic() {
        musicPlayer.stop();
    }


    /**
     * Returns {@code true} if the player is playing music.
     *
     * @return {@code true} if playing music, {@code false} otherwise.
     */
    public boolean isPlayingMusic() {
        return musicPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }

    /**
     * Returns the next level in the list of levels.
     *
     * @return the next level loaded from the save file.
     */
    public Level getNextLevel() {
        if (currentLevel == null) {
            return levels.get(0).clone(); ////Modified: Use the clone method
        }
        int currentLevelIndex = currentLevel.getIndex();
        if (currentLevelIndex < levels.size()) {
//            return levels.get(currentLevelIndex + 1); ////Modified: A bug fixed here， the stored index is 1,2,3,4,5, but the number of the list  is 0,1,2,3,4, so that only levels with index 1,3,5 are loaded.
            return levels.get(currentLevelIndex).clone(); ////Modified: Use the clone method
        }
        gameComplete = true;
        /* Added: Calculate game time */
        endTime = System.currentTimeMillis();
        gameTime = (endTime - startTime)/1000d;
        return null;
    }

    /* Added: New method to reset level */
    /**
     * Reset level.
     */
    public void  resetCurrentLevel() {
        int currentLevelIndex = currentLevel.getIndex();
        currentLevel = levels.get(currentLevelIndex - 1 ).clone();
    }

    /**
     * Returns the current level.
     *
     * @return the current level.
     */
    public Level getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int index) {
        currentLevel = levels.get(index - 1).clone();
    }
    /**
     * Toggles the debug mode.
     */
    public void toggleDebug() {
        debug = !debug;
    }

    /**
     * Button to undo one step
     */
    public void undo() {
        currentLevel = savedLevel;
    }

    /**
     * Record the score after the round
     */
    public void recordScore() {
        String filePath = "D:/score.txt";
        try{
            File file = new File(filePath);
            FileOutputStream fos = null;
            if(!file.exists()){
                file.createNewFile();
                fos = new FileOutputStream(file);
            }else{
                fos = new FileOutputStream(file,true);
            }

            OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");

            out.write(String.valueOf(gameTime));
            out.write(',');
            out.write(String.valueOf(movesCount));
            out.write("\r\n");
            out.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load list of scores from file
     */
    public void loadScoreFile() {
        File file = new File("D:/score.txt");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            String[] tempStringArray = null;
            while ((tempString = reader.readLine()) != null) {
                listTimeScore.add(Double.parseDouble(tempString.split(",")[0]));
                listMoveScore.add(Integer.parseInt(tempString.split(",")[1]));
            }
            Collections.sort(listTimeScore);
            Collections.sort(listMoveScore);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * A function that returns a list of levels
     *
     * @return the list of levels
     */
    public List<Level> getLevels() {
        return levels;
    }
}