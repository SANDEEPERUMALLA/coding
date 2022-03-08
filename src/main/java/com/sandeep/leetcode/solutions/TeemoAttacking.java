package com.sandeep.leetcode.solutions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class TeemoAttacking {

    public int findPoisonedDuration(int[] timeSeries, int duration) {
        int totalTime = 0;
        int start = timeSeries[0], end = timeSeries[0] + duration - 1;
        for (int i = 1; i < timeSeries.length; i++) {
            if (timeSeries[i] > end) {
                totalTime += end - start + 1;
                start = timeSeries[i];
            }
            end = timeSeries[i] + duration - 1;
        }
        totalTime += end - start + 1;
        return totalTime;
    }

    public static void main(String[] args) {
        TeemoAttacking teemoAttacking = new TeemoAttacking();
        int time = teemoAttacking.findPoisonedDuration(new int[]{1, 2, 3}, 2);
        System.out.println(time);
    }
}
