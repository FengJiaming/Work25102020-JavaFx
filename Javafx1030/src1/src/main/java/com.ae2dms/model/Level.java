package com.ae2dms.model;

import java.awt.*;
import java.util.Iterator;
import java.util.List;

/**
 * Level handles the creation of the game level parsing a {@link List} of {@link String}s and putting the right
 * {@link GameObject} in a 2D array. The object is created matching a char with the corresponding {@link GameObject}.
 *
 * @version 2.0
 */
public final class Level implements Iterable<GameObject>, Cloneable{ /*Add cloneable*/
    /**
     * The array containing the objectsGrid
     */
    public final GameGrid objectsGrid;
    /**
     * The object containing the diamondsGrid.
     */
    public final GameGrid diamondsGrid;
    /**
     * The level name
     */
    private final String name;
    /**
     * The level index
     */
    private final int index;
    /**
     * The total number of diamondsGrid in this level
     */
    private int numberOfDiamonds = 0;
    /**
     * The current warehouse keeper position
     */
    private Point keeperPosition = new Point(0, 0);
    /**
     * The number of rows of the level
     */
    public int levelRow;
    /**
     * The number of columns of the level
     */
    public int levelColumn;
    /**
     * This constructor is used to clone the level object
     *
     * @param levelName  the name of the level
     * @param levelIndex the number used as index for the levels
     * @param rows  the number of rows
     * @param columns the number of columns
     * @param numberOfDiamonds  the total number of diamondsGrid in this level
     */
    public Level(String levelName, int levelIndex, int rows , int columns, int numberOfDiamonds) {
        name = levelName;
        index = levelIndex;
        objectsGrid = new GameGrid(rows, columns);
        diamondsGrid = new GameGrid(rows, columns);
        levelRow = rows;
        levelColumn = columns;
        this.numberOfDiamonds = numberOfDiamonds;
    }

    /**
     * Creates a level using the first parameter as the level name and the second parameter as {@link List} of
     * {@link String}, each one containing the characters corresponding to a specific game object
     *
     * @param levelName  the name of the level
     * @param levelIndex the number used as index for the levels
     * @param raw_level  the raw data of the level
     */
    public Level(String levelName, int levelIndex, List<String> raw_level) {
        if (GameEngine.isDebugActive()) {
            System.out.printf("[ADDING LEVEL] LEVEL [%d]: %s\n", levelIndex, levelName);
        }

        name = levelName;
        index = levelIndex;

        int rows = raw_level.size();
        int columns = raw_level.get(0).trim().length();

        levelRow = rows;
        levelColumn = columns;

        objectsGrid = new GameGrid(rows, columns);
        diamondsGrid = new GameGrid(rows, columns);

        for (int row = 0; row < raw_level.size(); row++) {

            for (int col = 0; col < raw_level.get(row).length(); col++) {
                GameObject curTile = GameObject.fromChar(raw_level.get(row).charAt(col));

                if (curTile == GameObject.DIAMOND) {
                    numberOfDiamonds++;
                    diamondsGrid.putGameObjectAt(curTile, row, col);
                    curTile = GameObject.FLOOR;
                } else if (curTile == GameObject.KEEPER) {
                    keeperPosition = new Point(row, col);
                }
                objectsGrid.putGameObjectAt(curTile, row, col);
                curTile = null;
            }
        }
    }
    /**
     * Determine whether the level is complete
     *
     * @return {@code true} if all diamonds are eliminated
     */
    boolean isComplete() {
        int cratedDiamondsCount = 0;
        for (int row = 0; row < objectsGrid.ROWS; row++) {
            for (int col = 0; col < objectsGrid.COLUMNS; col++) {
                if (objectsGrid.getGameObjectAt(col, row) == GameObject.CRATE && diamondsGrid.getGameObjectAt(col, row) == GameObject.DIAMOND) {
                    cratedDiamondsCount++;
                }
            }
        }
        return cratedDiamondsCount >= numberOfDiamonds;
    }

    /**
     * Returns the name of this level
     *
     * @return the name of this level
     */
    public String getName() {
        return name;
    }
    /**
     * Returns the level index
     *
     * @return the level index
     */
    public int getIndex() {
        return index;
    }
    /**
     * Returns the warehouse keeper position
     *
     * @return the warehouse keeper position
     */
    Point getKeeperPosition() {
        return keeperPosition;
    }
    /**
     * Returns the object at distance delta from source
     *
     * @param source the source point
     * @param delta  the distance from the source point
     * @return the object at distance delta from source
     */
    GameObject getTargetObject(Point source, Point delta) {
        return objectsGrid.getTargetFromSource(source, delta);
    }

    /**
     * Returns located at point p of the objects grid.
     *
     * @param point the point where the object is located
     * @return {@link GameObject} the game object located at point p.
     */
    GameObject getObjectAt(Point point) {
        return objectsGrid.getGameObjectAt(point);
    }

    /**
     * Moves a {@link GameObject} to the target destination.
     * It removes the object from its original position and places it into the new one.
     *
     * @param object the {@link GameObject} to be moved
     * @param source the position of the object to be moved
     * @param destination The destination of the object
     */
    private void moveGameObjectTo(GameObject object, Point source, Point destination) {
        objectsGrid.putGameObjectAt(getObjectAt(destination), source);
        objectsGrid.putGameObjectAt(object, destination);
    }

    /**
     * Intermediate function that calls translatePoint function
     *
     * @param object Object to move
     * @param source initial position
     * @param delta Moving direction
     */
    void moveGameObjectBy(GameObject object, Point source, Point delta) {
        moveGameObjectTo(object, source, GameGrid.translatePoint(source, delta));
    }
    @Override
    public String toString() {
        return objectsGrid.toString();
    }
    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<GameObject> iterator() {
        return new LevelIterator();
    }

    /* Modified:Added clone function */
    /**
     * Provide a clone function for the {@link Level} class
     * @return Cloned {@link Level} object
     */
    @Override
    public Level clone() {
        Level level = new Level(name, index, objectsGrid.COLUMNS, objectsGrid.ROWS, numberOfDiamonds);
        if (objectsGrid != null) {
            level.objectsGrid.setGameObjects(objectsGrid.cloneGameObjects());
        }
        if (diamondsGrid != null) {
            level.diamondsGrid.setGameObjects(diamondsGrid.cloneGameObjects());
        }
        if (keeperPosition != null) {
            level.keeperPosition = (Point) keeperPosition.clone();
        }
        return level;
    }
    /**
     * LevelIterator provides the interface to iterate through the {@link GameGrid}
     * containing the {@link GameObject}s for the current {@link Level}.
     *
     * @see Iterator
     */
    public class LevelIterator implements Iterator<GameObject> {

        int column = 0;
        int row = 0;
        @Override
        public boolean hasNext() {
            return !(row == objectsGrid.ROWS - 1 && column == objectsGrid.COLUMNS);
        }
        @Override
        public GameObject next() { if (column >= objectsGrid.COLUMNS) {
            column = 0;
            row++;
        }
        GameObject object = objectsGrid.getGameObjectAt(column, row);
        GameObject diamond = diamondsGrid.getGameObjectAt(column, row);
        GameObject retObj = object;
        column++;
            if (diamond == GameObject.DIAMOND) {
                if (object == GameObject.CRATE) {
                    retObj = GameObject.CRATE_ON_DIAMOND;
                } else if (object == GameObject.FLOOR) {
                    retObj = diamond;
                } else {
                    retObj = object;
                }
            }
            return retObj;
        }
        public Point getCurrentPosition() {
            return new Point(column, row);
        }
    }

}