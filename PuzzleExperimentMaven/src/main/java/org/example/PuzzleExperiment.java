package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

// Main class to handle the experiment
public class PuzzleExperiment {
    private static final int SIZE = 3; // given
    private static final int STATE_COUNT = 100;

    public static void main(String[] args) {
        // total nodes and time
        int totalNodesHamming = 0, totalNodesManhattan = 0;
        long totalTimeHamming = 0, totalTimeManhattan = 0;

        // total memory usage
        List<Long> memoryUsageHamming = new ArrayList<>();
        List<Long> memoryUsageManhattan = new ArrayList<>();

        // time for each experiment
        List<Long> experimentTimeHamming = new ArrayList<>();
        List<Long> experimentTimeManhatan = new ArrayList<>();

        // Track nodes expanded for each heuristic
        List<Integer> nodesExpandedHamming = new ArrayList<>();
        List<Integer> nodesExpandedManhattan = new ArrayList<>();

        // Data for Excel export
        List<Map<String, Object>> data = new ArrayList<>();

        // generates goal state
        int[][] goalState = Puzzle.generateGoalState(SIZE);
        // loop for excercise goal of 100 random states
        for (int i = 0; i < STATE_COUNT; i++) {
            // Random puzzle states to solve
            int[][] randomState = generateRandomState();
            Puzzle puzzle = new Puzzle(randomState);
            // We need to be able to skip and retry
            if (!puzzle.isSolvable()) {
                i--; // Skip unsolvable states
                continue;
            }
            //Prints state´s number and constellation
            System.out.println("Experiment #" + (i + 1));
            System.out.println("Initial State:");
            puzzle.printState();


            Runtime runtime = Runtime.getRuntime();
            runtime.gc(); // not sure if totally the right aproach, but intention is to have more accurate memory usage callculated

            //Hamming
            long memoryBeforeHamming = runtime.totalMemory() - runtime.freeMemory();//We measure the memory usage before starting solving with Hamming
            System.out.println("Solving using Hamming Heuristic...");
            long startHamming = System.nanoTime(); //timiug in nano due hw power >> problem
            AStarSearch solverHamming = new AStarSearch(); //A* instance
            AStarSearch.PuzzleNode hammingGoalNode = solverHamming.solveStepByStep(puzzle, goalState, "Hamming");
            long endHamming = System.nanoTime(); //end timing
            double hammingTimeSecs = (endHamming - startHamming) / 1_000_000_000.0; //convert time to ms
            double hammingTimeMs = hammingTimeSecs * 1000;
            experimentTimeHamming.add((long) (hammingTimeMs)); //Add time per experiment durchführung
            //calc. memory usage
            long memoryAfterHamming = (runtime.totalMemory() - runtime.freeMemory());
            memoryUsageHamming.add(memoryAfterHamming - memoryBeforeHamming);
            //calc. nodes and time statistics
            totalNodesHamming += solverHamming.getExpandedNodes();
            nodesExpandedHamming.add(solverHamming.getExpandedNodes()); // Store expanded nodes for statistics
            totalTimeHamming += (long) hammingTimeMs;
            //Show solution steps
            List<Puzzle> hammingSolution = solverHamming.reconstructPath(hammingGoalNode);
            int hammingSolutionDepth = hammingSolution.size() - 1;
            printSolutionSteps(hammingSolution);
            // Calculate Effective Branching Factor (EBF) for Hamming
            double hammingEBF = (hammingSolutionDepth > 0) ? Math.pow(solverHamming.getExpandedNodes(), 1.0 / hammingSolutionDepth) : 0;
            System.out.println("Hamming Heuristic - Solution Depth (d): " + hammingSolutionDepth);
            System.out.println("Hamming Heuristic - EBF: " + hammingEBF);



            runtime.gc();

            //Manhattan
            long memoryBeforeManhattan = runtime.totalMemory() - runtime.freeMemory(); //mem.before Man.
            System.out.println("Solving using Manhattan Heuristic...");
            long startManhattan = System.nanoTime(); //start time
            AStarSearch solverManhattan = new AStarSearch();
            AStarSearch.PuzzleNode manhattanGoalNode = solverManhattan.solveStepByStep(puzzle, goalState, "Manhattan");
            long endManhattan = System.nanoTime(); //end time
            double manhattanTimeMs = (endManhattan - startManhattan)/ 1_000_000_000.0;
            experimentTimeManhatan.add((long) (manhattanTimeMs * 1000));
            //mem.usage
            long memoryAfterManhattan = runtime.totalMemory() - runtime.freeMemory();
            memoryUsageManhattan.add(memoryAfterManhattan - memoryBeforeManhattan);
            //calc. nodes and time statistics
            totalNodesManhattan += solverManhattan.getExpandedNodes();
            nodesExpandedManhattan.add(solverManhattan.getExpandedNodes());
            totalTimeManhattan += (long) manhattanTimeMs;
            //show solution steps
            List<Puzzle> manhattanSolution = solverManhattan.reconstructPath(manhattanGoalNode);
            int manhattanSolutionDepth = manhattanSolution.size() - 1;
            printSolutionSteps(manhattanSolution);
            // Calculate Effective Branching Factor (EBF) for Manhattan
            double manhattanEBF = (manhattanSolutionDepth > 0) ? Math.pow(solverManhattan.getExpandedNodes(), 1.0 / manhattanSolutionDepth) : 0;
            System.out.println("Manhattan Heuristic - Solution Depth (d): " + manhattanSolutionDepth);
            System.out.println("Manhattan Heuristic - EBF: " + manhattanEBF);

            //pretty formatting
            System.out.println("======================================");
            // After Hamming calculations for Excel Spreadsheet
            Map<String, Object> hammingData = Map.of(
                    "Heuristic", "Hamming",
                    "ExecutionTime (ms)", String.format("%.10f", hammingTimeMs),
                    "MeanMemory (bytes)", memoryAfterHamming - memoryBeforeHamming,
                    "NodesExpanded", solverHamming.getExpandedNodes(),
                    "SolutionDepth", hammingSolutionDepth,
                    "EBF", hammingEBF
            );
            data.add(hammingData);

            // For Excel Manhattan calculations
            Map<String, Object> manhattanData = Map.of(
                    "Heuristic", "Manhattan",
                    "ExecutionTime (ms)", String.format("%.10f", manhattanTimeMs),
                    "MeanMemory (bytes)", memoryAfterManhattan - memoryBeforeManhattan,
                    "NodesExpanded", solverManhattan.getExpandedNodes(),
                    "SolutionDepth", manhattanSolutionDepth,
                    "EBF", manhattanEBF
            );
            data.add(manhattanData);
            exportToExcel(data);
        }

        // Findings for our 100 experiment attempts
        System.out.println("Hamming Heuristic:");
        System.out.println("Total Execution Time (ms): " + totalTimeHamming);
        printStatistics("Memory Usage (bytes)", memoryUsageHamming);
        //System.out.println("Average Nodes Expanded: " + (totalNodesHamming / (double) STATE_COUNT));
        printStatistics("Execution Time (ms)", experimentTimeHamming);
        System.out.println("Nodes Expanded Statistics for Hamming:");
        printStatistics("Nodes Expanded", nodesExpandedHamming);
        System.out.println("__________________________________________");
        System.out.println("Manhattan Heuristic:");
        System.out.println("Total Execution Time (ms): " + totalTimeManhattan);
        printStatistics("Memory Usage (bytes)", memoryUsageManhattan);
        //System.out.println("Average Nodes Expanded: " + (totalNodesManhattan / (double) STATE_COUNT));
        printStatistics("Execution Time (ms)", experimentTimeManhatan);
        printStatistics("Nodes Expanded", nodesExpandedManhattan);

    }
    // Export data to Excel
    private static void exportToExcel(List<Map<String, Object>> data) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Puzzle Experiment20");

            // Create header row
            Row header = sheet.createRow(0);
            String[] columns = {"Heuristic", "ExecutionTime (ms)", "MeanMemory (bytes)", "NodesExpanded", "SolutionDepth", "EBF"};
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }

            // Fill in data
            int rowNum = 1;
            for (Map<String, Object> rowData : data) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue((String) rowData.get("Heuristic"));
                row.createCell(1).setCellValue((String) rowData.get("ExecutionTime (ms)"));
                row.createCell(2).setCellValue((Long) rowData.get("MeanMemory (bytes)"));
                row.createCell(3).setCellValue((Integer) rowData.get("NodesExpanded"));
                row.createCell(4).setCellValue((Integer) rowData.get("SolutionDepth"));
                row.createCell(5).setCellValue((Double) rowData.get("EBF"));
            }

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream("PuzzleExperimentResults.xlsx")) {
                workbook.write(fileOut);
            }

            System.out.println("Data exported to PuzzleExperimentResults.xlsx");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Helper function to print each step of the solution
    private static void printSolutionSteps(List<Puzzle> solution) {
        // In case no solution
        if (solution == null || solution.isEmpty()) {
            System.out.println("No solution found.");
            return;
        }
        // loop for each solution step
        System.out.println("\nSolution Steps:\n");
        for (int i = 0; i < solution.size(); i++) {
            System.out.println("Step " + i + ":"); // iterates step numbers
            solution.get(i).printState();
            if (i < solution.size() - 1) { // no arrow after goal state
                System.out.println("↓");
            }
        }
        System.out.println("Total steps: " + (solution.size() - 1)); // how many steps
    }

    // Helper to calculate and print mean and standard deviation
    private static <T extends Number> void printStatistics(String statistics, List<T> values) {
        // list conv. into values for stream, maps and calc.s avarage for longs in stream
        double mean = values.stream()
                .mapToDouble(Number::doubleValue)
                .average().
                orElse(0.0);
        // =/=, mean gets substracted from values and the difference gets squared
        double variance = values.stream()
                .mapToDouble(value -> Math.pow(value.doubleValue() - mean, 2))
                .average() // mean of the squared difference (variance)
                .orElse(0.0);
        double standDeviation = Math.sqrt(variance); // root to get standard deviation

        System.out.println(statistics + ":");
        System.out.println("  Mean: " + mean);
        System.out.println("  Standard Deviation: " + standDeviation);
    }

    //For random board states, it gives a random integer 2d-array as output
    private static int[][] generateRandomState() {
        // make(flatten) the puzzle into an array for shuffling
        int[] puzzleArray = new int[PuzzleExperiment.SIZE * PuzzleExperiment.SIZE];
        for (int i = 0; i < puzzleArray.length; i++) {
            puzzleArray[i] = i;
        }
        // Fisher-Yates Shuffle Algorithm:
        // https://www.geeksforgeeks.org/shuffle-a-given-array-using-fisher-yates-shuffle-algorithm/
        Random x = new Random();
        for (int i = puzzleArray.length - 1; i > 0; i--) {
            int j = x.nextInt(i + 1);
            int temp = puzzleArray[i];
            puzzleArray[i] = puzzleArray[j];
            puzzleArray[j] = temp;
        }
        // makes the shuffled array into an 2d array for PuzzleExper
        int[][] board = new int[PuzzleExperiment.SIZE][PuzzleExperiment.SIZE];
        for (int i = 0; i < PuzzleExperiment.SIZE; i++) {
            System.arraycopy(puzzleArray, i * PuzzleExperiment.SIZE, board[i], 0, PuzzleExperiment.SIZE);
        }
        return board;
    }
}
