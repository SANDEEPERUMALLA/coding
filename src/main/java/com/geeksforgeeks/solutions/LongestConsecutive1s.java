package com.geeksforgeeks.solutions;

/*
    Longest Consecutive 1's
 */
public class LongestConsecutive1s {

    public static int maxConsecutiveOnes(int N) {

        if (N == 1 || N == 2) {
            return 1;
        }

        int maxConsecutiveOnes = 0;
        int currCount = 0;
        while (N != 0) {
            int rem = N % 2;
            if (rem == 0) {
                if (currCount > maxConsecutiveOnes) {
                    maxConsecutiveOnes = currCount;
                }
                currCount = 0;
            } else {
                currCount++;
            }
            N = N / 2;
        }

        if (currCount > maxConsecutiveOnes) {
            maxConsecutiveOnes = currCount;
        }
        return maxConsecutiveOnes;
    }

    public static void main(String[] args) {
        System.out.println(maxConsecutiveOnes(9));
        System.out.println(maxConsecutiveOnes(15));
    }
}
