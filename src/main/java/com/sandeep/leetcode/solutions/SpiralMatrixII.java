package com.sandeep.leetcode.solutions;

public class SpiralMatrixII {
    public int[][] generateMatrix(int n) {

        int[][] spiralMatrix = new int[n][n];
        int rowStart = 0;
        int rowEnd = n - 1;
        int colStart = 0;
        int colEnd = n - 1;
        int number = 1;
        int lastNum = (int) Math.pow(n, 2);

        while (true) {
            for (int j = colStart; j <= colEnd; j++) {
                spiralMatrix[rowStart][j] = number++;
            }
            rowStart++;


            if (isLastNumDone(number, lastNum)) {
                break;
            }

            for (int i = rowStart; i <= rowEnd; i++) {
                spiralMatrix[i][colEnd] = number++;
            }
            colEnd--;


            if (isLastNumDone(number, lastNum)) {
                break;
            }

            for (int j = colEnd; j >= colStart; j--) {
                spiralMatrix[rowEnd][j] = number++;
            }
            rowEnd--;


            if (isLastNumDone(number, lastNum)) {
                break;
            }

            for (int i = rowEnd; i >= rowStart; i--) {
                spiralMatrix[i][colStart] = number++;
            }

            colStart++;

            if (isLastNumDone(number, lastNum)) {
                break;
            }
        }


        return spiralMatrix;
    }

    private boolean isLastNumDone(int number, int lastNum) {
        return number > lastNum;
    }

    public static void main(String[] args) {
        System.out.println("Spiral Matrix");
        SpiralMatrixII spiralMatrixII = new SpiralMatrixII();
        printMatrix(spiralMatrixII.generateMatrix(3));
        printMatrix(spiralMatrixII.generateMatrix(4));
        printMatrix(spiralMatrixII.generateMatrix(6));
    }

    private static void printMatrix(int[][] matrix) {
        int m = matrix.length;
        int n = matrix[0].length;

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }
}
