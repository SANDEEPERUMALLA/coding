package com.sandeep.leetcode.solutions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MergeIntervals {

    public int[][] merge(int[][] intervals) {

        Arrays.sort(intervals, Comparator.comparingInt(a -> a[0]));
        List<int[]> mergedIntervals = new ArrayList<>();

        int len = intervals.length;
        int i = 0;
        while (i < len) {
            int j = i + 1;
            int end = intervals[i][1];
            while (j < len && intervals[j][0] <= end) {
                end = Math.max(end, intervals[j][1]);
                j++;
            }
            mergedIntervals.add(new int[]{intervals[i][0], end});
            i = j;
        }
        return mergedIntervals.toArray(new int[][]{new int[0]});
    }

    public static void main(String[] args) {
        MergeIntervals mergeIntervals = new MergeIntervals();
        int[][] mergedIntervals = mergeIntervals.merge(new int[][]{{2,3},{4,5},{6,7},{8,9},{1,10}});
        Arrays.stream(mergedIntervals).forEach(ele -> {
            System.out.print("[" + ele[0] + "," + ele[1] + "],");
        });
    }
}
