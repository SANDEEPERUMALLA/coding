package com.geeksforgeeks.solutions;

public class NumberOf1Bits {
    static int setBits(int N) {


        int setBits = 0;
        while (N != 0) {
            int rem = N % 2;
            if (rem == 1) {
                setBits++;
            }
            N = N / 2;
        }

        return setBits;
    }

    public static void main(String[] args) {
        System.out.println(setBits(1));
        System.out.println(setBits(8));
        System.out.println(setBits(6));
        System.out.println(setBits(16));
        System.out.println(setBits(15));
    }
}
