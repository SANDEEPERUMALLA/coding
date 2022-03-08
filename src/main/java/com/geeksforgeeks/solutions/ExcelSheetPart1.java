package com.geeksforgeeks.solutions;

public class ExcelSheetPart1 {


    /*
        Excel Sheet | Part - 1
     */
    // A - Z
    public String excelColumn(long N) {

        StringBuilder excelColumn = new StringBuilder();

        while (N != 0) {
            long col = N % 26L;
            if (col == 0) {
                excelColumn.append("Z");
                N = N / 26L - 1L;
            } else {
                excelColumn.append((char) ('A' + col - 1));
                N = N / 26L;
            }
        }

        return excelColumn.reverse().toString();
    }

    public static void main(String[] args) {
        ExcelSheetPart1 excelSheetPart1 = new ExcelSheetPart1();
        System.out.println(excelSheetPart1.excelColumn(703));
    }
}
