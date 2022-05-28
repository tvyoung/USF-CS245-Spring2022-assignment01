package Lab02.Nonogram;
//@name Vicki Young
//@date/version 2022.3.10
//CS245 Lab02: Nonogram

import java.util.*;

public class Lab02 {
    //startBoard method creates empty nonogram board filled with false values
    //@param columns = column length of nonogram board
    //@param rows = row length of nonogram board
    //@returns board = double boolean array representing nonogram board
    public static boolean[][] startBoard(int columns, int rows) {
        boolean[][] board = new boolean[rows][columns];
        for (boolean[] row : board) {
            Arrays.fill(row, false);
        }
        return board;
    }

    //solveNonogram method solves the puzzle with the given two parameters
    //@param double array of integers that represent the columns
    //@param double array of integers that represent the rows
    //@returns double boolean array of solution
    public static boolean[][] solveNonogram(int[][] columns, int[][] rows) {
        //call method to create nonogram board from given block length entries
        boolean[][] solutionBoard = startBoard(columns.length, rows.length);
        //starting block index = the first block length entry of the first row
        int[] startingBlockIndex = {0, 0};
        //call solve method to solve the nonogram board
        solve(solutionBoard, columns, rows, startingBlockIndex);

        return solutionBoard;
    }

    //solve method solves recursively calls itself to solve the given nonogram board.
    //method will loop through the columns in the given row and check if from the current block, the row block length entry  is safe to paint
    //method recursively calls itself after every block is painted (rather than after every row is completed)
    //@param board = double boolean array for given nonogram board
    //@param columns = double array of integers that represent the column block entries
    //@param rows = double array of integers that represent the row block entries
    //@param blockIndex = array of two integers that represent the current row block entry (blockIndex[0] = row, blockIndex[1] = first or second block entry)
    //@returns boolean = represents a successful paint attempt, or false for a failed paint attempt
    public static boolean solve(boolean[][] board, int[][] columns, int[][] rows, int[] blockIndex) {
        //base case; if the last row has been checked, entire board has been solved without error, return true
        if (blockIndex[0] == rows.length) {
            return true;
        }

        //r = current row in nonogram board
        int r = blockIndex[0];

        //get the current row's current block length entry
        int rowLength = rows[r][blockIndex[1]];
        //check how many block entries there are for current row; if blockIndex(r, 0) = 0, then the only block length entry is at blockIndex(r, 1)
        if (rowLength == 0) {
            blockIndex[1] = 1;
            rowLength = rows[r][blockIndex[1]];
        }
        //entry = current row block entry
        int entry = blockIndex[1];

        //loop through columns of current row
        for (int c = 0; c < columns.length; c++) {
            //find first square in current row that is not painted in
            if (board[r][c]) {
                continue;
            }
            //check if it is safe to paint in the current column of current row with given row block length
            int[] currentBlock = {r, c};
            if (isSafeToPaint(board, columns, currentBlock, rowLength)) {
                //if safe, fill in squares along row for given row block length entry
                for (int i = 0; i < rowLength; i++) {
                    board[r][c + i] = true;
                }
                //BACKTRACKING ALGORITHM
                //if all the block entries in the current row have been checked, verify board is correct
                if (blockIndex[1] == 1) {
                    //if board is verified correct, continue; check the next row's entries
                    if (verifyBoard(board, columns, r)) {
                        blockIndex[0]++;
                        blockIndex[1] = 0;
                    //else: board is not correct, so backtrack and continue (skip the below solve() recursive call)
                    } else {
                        for (int i = 0; i < rowLength; i++) {
                            board[r][c + i] = false;
                        }
                        continue;
                    }
                //else: not all block entries have been checked, so check the current row's next block entry
                } else {
                    blockIndex[1] = 1;
                }
                //recursive call to check the next row block entry
                if (solve(board, columns, rows, blockIndex)) {
                    return true;
                } //else: backtrack
                for (int i = 0; i < rowLength; i++) {
                    board[r][c + i] = false;
                }
                //additional backtracking to maintain the current row + current row block entry
                blockIndex[0] = r;
                blockIndex[1] = entry;
            }
        }
        //reached end of row and nowhere is safe to paint a block; return false
        return false;
    }


    //isSafeToPaint method checks for validity of painting at the current block and with the given block length entry
    //will also call isSafeColumn method along every placement of block length entry to check validity
    //@param board = double boolean array for given nonogram board
    //@param columns = double array of integers that represent the column block entries
    //@param currentBlock = array of integers representing the current block to be painted in
    //@param length = integer representing the length of the block entry to paint
    //@returns boolean = returns true if it is safe to paint at the current block, false if not
    public static boolean isSafeToPaint(boolean[][] board, int[][] columns, int[] currentBlock, int length) {
        //r = current row in nonogram board, c = current column in nonogram board
        int r = currentBlock[0];
        int c = currentBlock[1];
        //if the current block is not the first block in the row and the block right before the current block is painted in,
        //or length of block to fill in exceeds board row length, return false
        if ((c != 0 && board[r][c - 1]) || c + length > columns.length) {
            return false;
        }
        //check validity of each column along the row if painting with projected block length
        for (int i = 0; i < length; i++) {
            int[] testBlock = {currentBlock[0], currentBlock[1] + i};
            if (!isSafeColumn(board, columns, testBlock)) {
                return false;
            }
        }
        //all checks pass, safe to paint at the current block; return true
        return true;
    }

    //isSafeColumn method checks if painting at the current block is safe by checking validity of the given column
    //will loop through block length entries for the given column and compare it to a count of painted blocks in the column
    //@param board = double boolean array for given nonogram board
    //@param columns = double array of integers that represent the column block entries
    //@param currentBlock = array of integers representing the current block to be painted in
    //@returns boolean = returns true if the current block's column is valid, false if not
    public static boolean isSafeColumn(boolean[][] board, int[][] columns, int[] currentBlock) {
        //r = current row in nonogram board, c = current column in nonogram board
        int r = currentBlock[0];
        int c = currentBlock[1];

        //rowIndex = index tracker for given column, starting from the top
        int rowIndex = 0;
        //entry = each block length entry for given column
        int entry = 0;
        //if the first block length entry for given column = 0, then there is only one block length entry given (at columns[c][1])
        if (columns[c][entry] == 0) {
            entry = 1;
        }

        //totalBlocks = counts total number of squares painted in given column
        int totalBlocks = 0;
        //loops through block length entries for given column
        while (entry < 2 && rowIndex <= r) {
            //if given block is not painted in, increment rowIndex and continue
            if (!board[rowIndex][c]) {
                rowIndex++;
            //else: given block is painted in; count the length of continuous blocks painted in
            } else {
                int columnLength = 0;
                while (board[rowIndex + columnLength][c]) {
                    columnLength++;
                }
                //increment rowIndex and totalBlocks by number of block length counted
                rowIndex += columnLength;
                totalBlocks += columnLength;

                //if current block is continuous to counted length (directly below the painted blocks without a gap),
                //and counted length + 1 (filling in the current block) is greater than block length entry, returns false
                if (rowIndex == r && columnLength + 1 > columns[c][entry]) {
                    return false;
                } //else: check the next block entry
                entry++;
            }
        }
        //last check: return false if the number of squares painted in the given column + 1 (the current square to fill in) > total block entries length
        return !(totalBlocks + 1 > columns[c][0] + columns[c][1]);
    }

    //verifyBoard method checks at the end of a row if all constraints are met
    //will check each column of the board and ensure validity of the current state
    //@param board = double boolean array for given nonogram board
    //@param columns = double array of integers that represent the column block entries
    //@param currentRow = integer representing the current row that has just been completed
    //@returns boolean = returns true if each column of the board is valid, false if not
    public static boolean verifyBoard(boolean[][] board, int[][] columns, int currentRow) {
        //check along each column of the board, verify they are all valid
        for (int c = 0; c < columns.length; c++) {
            //rowIndex = index tracker for given column, starting from the top
            int rowIndex = 0;
            //entry = each block length entry for given column
            int entry = 0;
            //if the first block length entry for given column = 0, then there is only one block length entry given (at columns[c][1])
            if (columns[c][entry] == 0) {
                entry = 1;
            }
            //loops through block length entries for given column
            while (entry < 2 && rowIndex <= currentRow) {
                //if given block is not painted in, increment rowIndex and continue
                if (!board[rowIndex][c]) {
                    rowIndex++;
                //else: given block is painted in; count the length of continuous blocks painted in
                } else {
                    int columnLength = 0;
                    //will continue to count continuous blocks painted in while the count is <= current row,
                    //and the index of the given block does not exceed the board's column length,
                    //and the given block is painted in
                    while (columnLength <= currentRow && (rowIndex + columnLength) < board.length && board[rowIndex + columnLength][c]) {
                        columnLength++;
                    }
                    //increment rowIndex by number of block length counted
                    rowIndex += columnLength;
                    //rowIndex - 1 to get bottomSquare = the bottommost (last) painted square in the column block
                    int bottomSquare = rowIndex - 1;
                    //if counted length is not equal to current block length entry, AND the bottommost square is NOT part of the current row, return false
                    if (columnLength != columns[c][entry] && bottomSquare != currentRow) {
                        return false;
                    } //else: check the next block entry
                    entry++;
                }
            }
        }
        //validity of all columns have been checked; return true
        return true;
    }

    //printBoard method prints out nonogram board boolean values (FOR TESTING)
    //@param board = given nonogram board
    public static void printBoard(boolean[][] board) {
        for (boolean[] innerArray : board) {
            for (boolean value : innerArray) {
                char c;
                if (value) {
                    c = 'T';
                } else {
                    c = 'F';
                }
                System.out.print(c + " ");
            }
            System.out.println();
        }
    }

    /*public static void main(String[] args) {
        //TEST CASE 0
        //int[][] columns = {{0, 2}};
        //int[][] rows = {{0,1}, {0,1}};
        //TEST CASE 1
        //int[][] columns = {{0,1}, {0,1}, {0,1}, {0,1}, {0,1}};
        //int[][] rows = {{0,5}};
        //TEST CASE 2
        //int[][] columns = {{0,2}, {0,2}, {0,2}, {0,1}, {0,1}, {0,1}, {0,2}, {0,2}, {0,1}};
        //int[][] rows = {{4,3}, {3,4}};
        //TEST CASE 3
        //int[][] columns = {{0,2}, {2,1}, {0,4}, {0,3}, {0,1}};
        //int[][] rows = {{0,4}, {0,4}, {0,3}, {0,1}, {0,1}};
        //TEST CASE 4
        //int[][] columns = {{1,1}, {2,1}, {0,2}, {1,1}, {1,1}};
        //int[][] rows = {{1,1}, {0,1}, {0,5}, {0,1}, {1,1}};
        //TEST CASE 5
        //int[][] columns = {{0,3}, {1,1}, {1,1}, {0,3}, {0,3}};
        //int[][] rows = {{0,3}, {0,2}, {2,2}, {0,1}, {0,3}};
        //TEST CASE 6
        //int[][] columns = {{1,3}, {0,1}, {0,2}, {0,2}, {0,4}};
        //int[][] rows = {{1,3}, {0,3}, {1,1}, {1,1}, {0,2}};
        //TEST CASE 7
        //int[][] columns = {{2, 2}, {0, 2}, {0, 3}, {0, 1}, {0, 3}};
        //int[][] rows = {{1, 1}, {1, 3}, {1, 1}, {0, 3}, {0, 2}};
        //TEST CASE 8 - NO SOLUTION
        //int[][] columns = {{0,2}, {0,2}, {0,2}, {3,1}, {0,4}};
        //int[][] rows = {{0,2}, {0,2}, {0,2}, {0,3}, {0,4}};
        //TEST CASE 9 - NO SOLUTION
        //int[][] columns = {{0,0}, {0,0}, {0,0}, {0,0}, {0,0}};
        //int[][] rows = {{0,2}, {0,2}, {0,2}, {0,3}, {0,4}};
        //TEST CASE 10 - NO ENTRIES
        //int[][] columns = {{0,0}, {0,0}, {0,0}, {0,0}, {0,0}};
        //int[][] rows = {{0,0}, {0,0}, {0,0}, {0,0}, {0,0}};

        boolean[][] board = solveNonogram(columns, rows);
        printBoard(board);
    }*/
}
