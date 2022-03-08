package com.geeksforgeeks.solutions;

import java.util.Arrays;

public class RedORGreen {

    static int RedOrGreen(int N, String S) {

        if(S.isEmpty()){
            return 0;
        }

        int redCount = 0;
        int greenCount = 0;
        for (char ch : S.toCharArray()) {
            if (ch == 'R')
                redCount++;
            else
                greenCount++;
        }
        return Math.min(redCount, greenCount);
    }

    public static void main(String[] args) {
        System.out.println(RedOrGreen(5, "RGRGR"));
        System.out.println(RedOrGreen(7, "GGGGGGR"));
        System.out.println(RedOrGreen(7, "RRRRR"));
        System.out.println(RedOrGreen(7, "GGGG"));
        System.out.println(RedOrGreen(7, ""));
    }
}
