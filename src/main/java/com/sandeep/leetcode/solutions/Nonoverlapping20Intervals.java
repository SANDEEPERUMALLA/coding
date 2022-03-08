package com.sandeep.leetcode.solutions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Nonoverlapping20Intervals {

    /**
     * @param intervals: a collection of intervals
     * @return: the minimum number of intervals you need to remove
     */
    public int eraseOverlapIntervals(List<Interval> intervals) {
        Collections.sort(intervals, (a, b) -> a.end - b.end == 0 ?
                b.start - a.start : a.end - b.end);

        int preEnd = intervals.get(0).end;
        int nonOverlap = 1;
        for (int i = 1; i < intervals.size(); i++) {
            if (intervals.get(i).start >= preEnd) {
                preEnd = intervals.get(i).end;
                nonOverlap++;
            }
        }
        return intervals.size() - nonOverlap;
    }

    public static void main(String[] args) {
        Nonoverlapping20Intervals nonoverlapping20Intervals = new Nonoverlapping20Intervals();
        List<Interval> intervals = new ArrayList<>();
        intervals.add(new Interval(1, 2));
        intervals.add(new Interval(2, 3));
        intervals.add(new Interval(3, 4));
        intervals.add(new Interval(1, 3));
        nonoverlapping20Intervals.eraseOverlapIntervals(intervals);
    }


    static public class Interval {
        int start, end;

        public Interval(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

}
