package org.example;

import java.util.*;

public class Puzzle {
    private int[][] state;
    private int size;

    // Constructor, gives  out an initialized Puzzle object with a given state,
    // after recieveing a board in a specific state
    public Puzzle(int[][] state) {
        this.state = state;
        this.size = state.length;
    }
    // Functionm to create an image (Plato) of the solution for the 2d array it receives
    public static int[][] generateGoalState(int size) {
        int[][] goal = new int[size][size];
        int value = 1; // start at 1
        // loop, to fill our board
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                goal[i][j] = value++; // increments value, so that we can fill the board up to size-1
            }
        }
        goal[size - 1][size - 1] = 0; // lastly, here comes also the empty spot for maneuvers added to the board
        return goal;
    }
    // concept https://www.youtube.com/watch?v=bhmCmbj9VAg
    // https://www.geeksforgeeks.org/count-inversions-in-a-permutation-of-first-n-natural-numbers/
    // https://stackoverflow.com/questions/5932756/finding-the-number-of-permutation-inversions
    // https://jonathan-kuo.medium.com/data-structures-at-play-sliding-block-puzzle-and-permutation-inversions-c2e1a5494d52
    // Function to check if the puzzle is solvable based on number of inversions,
    // takes no inputs as it operates on the object´s state and gives boolean
    public boolean isSolvable() {
        // 2d -> 1d
        int[] flatBoard = Arrays.stream(state).flatMapToInt(Arrays::stream).toArray();
        int inversions = 0;
        // Check number of inversions in 1d array  (each number greater than the number to its right)
        for (int i = 0; i < flatBoard.length; i++) {
            for (int j = i + 1; j < flatBoard.length; j++) {
                if (flatBoard[i] > flatBoard[j] && flatBoard[j] != 0) { //ignore empty space
                    inversions++;
                }
            }
        }
        /*
        Rule for finding solutions:
        Inversions must be even.
        */
        return (inversions % 2 == 0);
    }



    public void printState() {
        int width = size * 3; // Adjust width
        System.out.println("-".repeat(width)); // horizontal border
        for (int[] row : state) {
            for (int cell : row) {
                System.out.printf("|%2s ", (cell == 0 ? " " : cell)); // number or empty
            }
            System.out.println("|"); // End of row
        }
        System.out.println("-".repeat(width)); // horizontal border
    }
    public int[][] getState() {
        return state;
    }
}
