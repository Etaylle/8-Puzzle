package org.example;

import java.util.*;


// Code structure and implementation  https://www.baeldung.com/java-a-star-pathfinding
public class AStarSearch {
    private int expandedNodes;

    // Takes the initial puzzle, its goal state (array), heuristic name (String)
    // Returns the node with the lowest f=g+h value (the node to explore next)
    public PuzzleNode solveStepByStep(Puzzle puzzle, int[][] goalState, String heuristicType) {
        // Open list, represented by Priority queue, natural ordering, of nodes to explore
        // manages states to explore based on f=g+h
        PriorityQueue<PuzzleNode> nodesToExplore = new PriorityQueue<>();
        Set<String> exploredNodes = new HashSet<>(); // Closed list: set of visited, fully explored nodes
        expandedNodes = 0;
        //create start node
        PuzzleNode startNode = new PuzzleNode(puzzle.getState(), 0, calculateHeuristic(puzzle.getState(), goalState, heuristicType), null);
        nodesToExplore.add(startNode); // add to "nodesToExplore"

        while (!nodesToExplore.isEmpty()) {
            PuzzleNode current = nodesToExplore.poll(); // takes the node with lowest F value
            exploredNodes.add(Arrays.deepToString(current.getState())); // marks as visited

            // If goal state reached, return node
            if (Arrays.deepEquals(current.getState(), goalState)) {
                return current;
            }
            // expand neighbours
            for (PuzzleNode neighbor : createNeighbours(current, goalState, heuristicType)) {
                if (!exploredNodes.contains(Arrays.deepToString(neighbor.getState()))) {
                    nodesToExplore.add(neighbor);
                }
            }

            expandedNodes++;
        }

        return null; // if no solution
    }


    // To show solution steps, by traversing from the goal node to start
    // Takes PuzzleNode representing the goal state
    // Gives the list for the solution path
    public List<Puzzle> reconstructPath(PuzzleNode goalNode) {
        List<Puzzle> path = new ArrayList<>();
        PuzzleNode current = goalNode;

        while (current != null) {
            path.add(new Puzzle(current.getState()));
            current = current.getParent();
        }

        Collections.reverse(path); // Reverse the path to start-to-goal order
        return path;
    }

    // Get the number of expanded nodes
    public int getExpandedNodes() {
        return expandedNodes;
    }

    // Generate all valid neighbor states for the current node
    // takes current PuzzleNode, goal state, heuristic
    // Returns a list of PuzzleNodes representing the neighbours of the current node,
    // by moving the 0 (empty spot on board)
    private List<PuzzleNode> createNeighbours(PuzzleNode current, int[][] goalState, String heuristicType) {
        List<PuzzleNode> neighbors = new ArrayList<>();
        int[][] currentPosition = current.getState();
        int size = currentPosition.length;

        // Free space on board, for understanding lets just call it 0, gets located with the function
        int emptySpotX = -1, emptySpotY = -1;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (currentPosition[i][j] == 0) {
                    emptySpotX = i;
                    emptySpotY = j;
                }
            }
        }

        // Possible moves for 0
        int[][] moves = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // Up, Down, Left, Right
        for (int[] move : moves) {
            int nextX = emptySpotX + move[0]; // x set of values for possible moves
            int nextY = emptySpotY + move[1]; // y set of values for moves

            // Check if the move is legit, whether it is inside of the playfield or not
            if (nextX >= 0 && nextY >= 0 && nextX < size && nextY < size) {
                int[][] nextState = copyState(currentPosition);

                // Swap  0 and adjecent positions
                nextState[emptySpotX][emptySpotY] = nextState[nextX][nextY];
                nextState[nextX][nextY] = 0;

                // Calculate the heuristic and create a new node
                int g = current.getG() + 1;
                int h = calculateHeuristic(nextState, goalState, heuristicType);
                neighbors.add(new PuzzleNode(nextState, g, h, current));
            }
        }

        return neighbors;
    }

    // Calculate the heuristic value based on the chosen type
    // Needs the current puzzle state, goal and heuristic
    // gives the value for either Hamming or Manhattan
    // e.g. goal state is ofc 1,2,3,...,8 and if the current state has 5,4,1 on their spots,
    // for Hamming it will return 5, while Manhattan will return the sum of the distance for
    // each tile to its goal position
    private int calculateHeuristic(int[][] state, int[][] goalState, String heuristicType) {
        int heuristic = 0;
        int size = state.length;

        if ("Hamming".equals(heuristicType)) { // Hamming algorithm, counts misplaced tiles
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (state[i][j] != 0 && state[i][j] != goalState[i][j]) {
                        heuristic++;
                    }
                }
            }
        } else if ("Manhattan".equals(heuristicType)) { // Manhattan = Sum of distances of each tile to its goal position
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (state[i][j] != 0) {
                        int value = state[i][j];
                        int goalX = (value - 1) / size;
                        int goalY = (value - 1) % size;
                        heuristic += Math.abs(i - goalX) + Math.abs(j - goalY);
                    }
                }
            }
        }

        return heuristic;
    }

    // Helper to deep copy a 2D array
    private int[][] copyState(int[][] state) {
        int[][] clone = new int[state.length][];
        for (int i = 0; i < state.length; i++) {
            clone[i] = Arrays.copyOf(state[i], state[i].length);
        }
        return clone;
    }

    // Node class to represent a state in the search tree; Parent Mapping
    static class PuzzleNode implements Comparable<PuzzleNode> {
        private final int[][] state; //current puzzle configuration
        private final int g; // Cost from start to this node in the search tree = actual distance so far
        private final int h; // estimated distance remaining (->goal), heuristic value (in our case Hamming or Manhattan)
        private final PuzzleNode parent;

        public PuzzleNode(int[][] state, int g, int h, PuzzleNode parent) {
            this.state = state;
            this.g = g;
            this.h = h;
            this.parent = parent;
        }

        public int[][] getState() {
            return state;
        }

        public int getG() {
            return g;
        }

        public PuzzleNode getParent() {
            return parent;
        }

        public int getF() { // A*-search (formula) combines uniform cost search adn greedy search, by sum their eval.
            return g + h;
        }

        @Override
        public int compareTo(PuzzleNode other) {
            return Integer.compare(this.getF(), other.getF()); // diff. f value comparison
        }
    }
}

