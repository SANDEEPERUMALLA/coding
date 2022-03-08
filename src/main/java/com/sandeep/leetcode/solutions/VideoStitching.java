package com.sandeep.leetcode.solutions;

import java.util.Arrays;
import java.util.Comparator;

public class VideoStitching {
    public int videoStitching(int[][] clips, int T) {
        // Arrays.sort(clips, (a, b) -> a[0] - b[0]);
        // 我也想用 Lambda表达式 来写...奈何每次跑出来的成绩实在太难看...
        Arrays.sort(clips, new Comparator<int[]>() {
            public int compare(int[] a, int[] b) {
                return a[0] - b[0];
            }
        });
        int currEnd = 0, ans = 0;
        for (int i = 0; i < clips.length; ) {
            // 如果当前 clip 的左端点 > 当前覆盖区域的右端点，则说明无法完成一个连续区间的覆盖，返回-1
            if (clips[i][0] > currEnd) {
                return -1;
            }
            int newEnd = currEnd;
            // 只要当前 clips 的左端点能够被 reach 到，当前区间的右端点便可以被不断地更新。
            while (i < clips.length && clips[i][0] <= currEnd) {
                newEnd = Math.max(newEnd, clips[i++][1]);
            }

            // cen
            ans++;
            currEnd = newEnd;
            if (currEnd >= T) {
                return ans;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        VideoStitching videoStitching = new VideoStitching();
        videoStitching.videoStitching(new int[][]{{0,2},{4,6},{8,10},{1,9},{1,5},{5,9}}, 10);
    }
}
