// Board.java
package edu.stanford.cs108.tetris;

import java.util.*;

/**
 Tetris Board.
 Represents a Tetris board -- essentially a 2-d grid
 of booleans. Supports tetris pieces and row clearing.
 Has an "undo" feature that allows clients to add and remove pieces efficiently.
 */
public class Board	{
    // Some ivars are stubbed out for you:
    private int width;
    private int height;
    private boolean[][] grid;
    private boolean DEBUG = true;
    boolean committed;

    private int maxHeight;
    private int[] widths;
    private int[] heights;

    private boolean[][] xGrid;
    private int xMaxHeight;
    private int[] xWidths;
    private int[] xHeights;

    // Here a few trivial methods are provided:

    /**
     Creates an empty board of the given width and height
     measured in blocks.
     */
    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        grid = new boolean[width][height];
        xGrid = new boolean[width][height];
        committed = true;

        // YOUR CODE HERE
        maxHeight = 0;
        // initialize grid
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                grid[i][j] = false;
            }
        }
        // initialize widths and heights
        widths = new int[height];
        heights = new int[width];
        xWidths = new int[height];
        xHeights = new int[width];
        for (int i = 0; i < height; i++) {
            widths[i] = 0;
        }
        for (int i = 0; i < width; i++) {
            heights[i] = 0;
        }
    }


    /**
     Returns the width of the board in blocks.
     */
    public int getWidth() {
        return width;
    }


    /**
     Returns the height of the board in blocks.
     */
    public int getHeight() {
        return height;
    }


    /**
     Returns the max column height present in the board.
     For an empty board this is 0.
     */
    public int getMaxHeight() {
        return maxHeight; // YOUR CODE HERE
    }


    /**
     Checks the board for internal consistency -- used
     for debugging.
     */
    public void sanityCheck() {

        if (DEBUG) {
            // YOUR CODE HERE
            int[] countRowWidth = new int[height];
            int[] countColumnHeight = new int[width];
            int check_max_height = 0;
            for (int i = 0; i < width; i++) {
                countColumnHeight[i] = 0;
            }
            for (int i = 0; i < height; i++) {
                countRowWidth[i] = 0;
            }
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (grid[i][j] == true) {
                        countRowWidth[j]++;
                        if (j + 1 > countColumnHeight[i]) {
                            countColumnHeight[i] = j + 1;
                        }
                    }
                }
                // check heights[]
                if (countColumnHeight[i] != heights[i]) {
                    throw new RuntimeException("Sanity check failed: heights");
                }
                check_max_height = Math.max(check_max_height, heights[i]);
            }

            // check widths[]
            if (!Arrays.equals(countRowWidth,widths)) {
                throw new RuntimeException("Sanity check failed: widths");
            }

            if (check_max_height != maxHeight) {
                throw new RuntimeException("Sanity check failed: maxHeight");
            }
        }
    }

    /**
     Given a piece and an x, returns the y
     value where the piece would come to rest
     if it were dropped straight down at that x.

     <p>
     Implementation: use the skirt and the col heights
     to compute this fast -- O(skirt length).
     */
    public int dropHeight(Piece piece, int x) {
        int drop = 0;
        for(int i = 0; i < piece.getWidth(); i++) {
            drop = Math.max(drop, heights[i+x] - piece.getSkirt()[i]);
        }
        return drop; // YOUR CODE HERE
    }


    /**
     Returns the height of the given column --
     i.e. the y value of the highest block + 1.
     The height is 0 if the column contains no blocks.
     */
    public int getColumnHeight(int x) {
        return heights[x]; // YOUR CODE HERE
    }


    /**
     Returns the number of filled blocks in
     the given row.
     */
    public int getRowWidth(int y) {
        return widths[y]; // YOUR CODE HERE
    }


    /**
     Returns true if the given block is filled in the board.
     Blocks outside of the valid width/height area
     always return true.
     */
    public boolean getGrid(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return true;
        }
        return grid[x][y]; // YOUR CODE HERE
    }


    public static final int PLACE_OK = 0;
    public static final int PLACE_ROW_FILLED = 1;
    public static final int PLACE_OUT_BOUNDS = 2;
    public static final int PLACE_BAD = 3;

    /**
     Attempts to add the body of a piece to the board.
     Copies the piece blocks into the board grid.
     Returns PLACE_OK for a regular placement, or PLACE_ROW_FILLED
     for a regular placement that causes at least one row to be filled.

     <p>Error cases:
     A placement may fail in two ways. First, if part of the piece may falls out
     of bounds of the board, PLACE_OUT_BOUNDS is returned.
     Or the placement may collide with existing blocks in the grid
     in which case PLACE_BAD is returned.
     In both error cases, the board may be left in an invalid
     state. The client can use undo(), to recover the valid, pre-place state.
     */
    public int place(Piece piece, int x, int y) {

        // flag !committed problem
        if (!committed) throw new RuntimeException("place commit problem");

        backup();
        committed = false;
        int result = PLACE_OK;

        // YOUR CODE HERE
        for (int i = 0; i < piece.getBody().length; i++) {
            int x_position = x + piece.getBody()[i].x;
            int y_position = y + piece.getBody()[i].y;
            if (x_position >= width || x_position < 0 || y_position >= height || y_position < 0) {
                result = PLACE_OUT_BOUNDS;
                return result;
            }
            if (grid[x_position][y_position]) {
                result = PLACE_BAD;
                return PLACE_BAD;
            }
            grid[x_position][y_position] = true;
            // update widths[] and heights[]
            widths[y_position]++;
            if (widths[y_position] == width) {
                result = PLACE_ROW_FILLED;
            }
            heights[x_position] = Math.max(y_position + 1, heights[x_position]);
            maxHeight = Math.max(maxHeight,heights[x_position]);
        }
        //System.out.println("mark");
        sanityCheck();
        return result;
    }


    /**
     Deletes rows that are filled all the way across, moving
     things above down. Returns the number of rows cleared.
     */
    public int clearRows() {

        if(committed) {
            backup();
            committed = false;
        }

        int rowsCleared = 0;
        // copy the rows that are not full from bottom to top
        int from = -1;
        for (int j = 0; j < maxHeight - rowsCleared; j++) {
            from++;
            while (widths[from] == width) {
                from++;
                rowsCleared++;
            }
            for (int i = 0; i < width; i++) {
                grid[i][j] = grid[i][from];
                widths[j] = widths[from];
            }
        }

        // fill the upper empty rows
        for (int j = maxHeight - rowsCleared; j < maxHeight ; j++) {
            for (int i = 0; i < width; i++) {
                grid[i][j] = false;
                widths[j] = 0;
            }
        }

        // update heights[]
        maxHeight = 0;
        for (int i = 0; i < width; i++) {
            heights[i] = 0;
            for (int j = 0; j < height; j++) {
                if (grid[i][j] == true) {
                    heights[i] = Math.max(j + 1, heights[i]);
                }
            }
            maxHeight = Math.max(maxHeight, heights[i]);
        }
        // YOUR CODE HERE
        sanityCheck();
        return rowsCleared;
    }



    /**
     Reverts the board to its state before up to one place
     and one clearRows();
     If the conditions for undo() are not met, such as
     calling undo() twice in a row, then the second undo() does nothing.
     See the overview docs.
     */
    public void undo() {
        if (!committed) {

            maxHeight = xMaxHeight;
            System.arraycopy(xWidths, 0, widths, 0, height);
            System.arraycopy(xHeights, 0, heights, 0, width);
            for (int i = 0; i < width; i++) {
                System.arraycopy(xGrid[i], 0, grid[i], 0, height);
            }
            committed = true;
            sanityCheck();
        }
        // YOUR CODE HERE
    }

    private void backup() {
        xMaxHeight = maxHeight;
        System.arraycopy(widths, 0, xWidths, 0, height);
        System.arraycopy(heights, 0, xHeights, 0, width);
        for (int i = 0; i < width; i++) {
            System.arraycopy(grid[i], 0, xGrid[i], 0, height);
        }
    }


    /**
     Puts the board in the committed state.
     */
    public void commit() {
        committed = true;
    }



    /*
     Renders the board state as a big String, suitable for printing.
     This is the sort of print-obj-state utility that can help see complex
     state change over time.
     (provided debugging utility)
     */
    public String toString() {
        StringBuilder buff = new StringBuilder();
        for (int y = height-1; y>=0; y--) {
            buff.append('|');
            for (int x=0; x<width; x++) {
                if (getGrid(x,y)) buff.append('+');
                else buff.append(' ');
            }
            buff.append("|\n");
        }
        for (int x=0; x<width+2; x++) buff.append('-');
        return(buff.toString());
    }
}


