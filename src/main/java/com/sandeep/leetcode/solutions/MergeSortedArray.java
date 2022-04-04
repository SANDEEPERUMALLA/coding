package com.sandeep.leetcode.solutions;

import java.util.Arrays;

public class MergeSortedArray {

    public void merge(int[] nums1, int m, int[] nums2, int n) {

        int lastElementIndex = nums1.length - n - 1;
        int currentStartIndex = 0;
        for (int currNum : nums2) {
            int j = currentStartIndex;
            while ((currNum >= nums1[j] && nums1[j] != 0) || ((currNum >= nums1[j] && nums1[j] == 0) && j < lastElementIndex)) {
                j++;
            }
            if (lastElementIndex + 1 - j >= 0) System.arraycopy(nums1, j, nums1, j + 1, lastElementIndex + 1 - j);
            nums1[j] = currNum;
            currentStartIndex = j + 1;
            lastElementIndex++;
        }
    }

    public static void main(String[] args) {
        MergeSortedArray mergeSortedArray = new MergeSortedArray();
        // Test Cases
        int[] m1 = {1, 2, 3, 0, 0, 0};
        mergeSortedArray.merge(m1, 3, new int[]{2, 5, 6}, 3);
        System.out.println(Arrays.toString(m1));
        m1 = new int[]{1};
        mergeSortedArray.merge(m1, 1, new int[]{}, 0);
        System.out.println(Arrays.toString(m1));
        m1 = new int[]{0};
        mergeSortedArray.merge(m1, 0, new int[]{1}, 1);
        System.out.println(Arrays.toString(m1));
        m1 = new int[]{1, 2, 3, 4, 0, 0, 0, 0};
        mergeSortedArray.merge(m1, 4, new int[]{4, 5, 6, 8}, 4);
        System.out.println(Arrays.toString(m1));
        m1 = new int[]{1, 2, 3, 4, 0, 0, 0, 0};
        mergeSortedArray.merge(m1, 4, new int[]{5, 6, 7, 8}, 4);
        System.out.println(Arrays.toString(m1));
        m1 = new int[]{4, 5, 7, 8, 0, 0, 0, 0};
        mergeSortedArray.merge(m1, 4, new int[]{1, 2, 3, 4}, 4);
        System.out.println(Arrays.toString(m1));
        m1 = new int[]{5, 6, 7, 8, 0, 0, 0, 0};
        mergeSortedArray.merge(m1, 4, new int[]{1, 2, 3, 4}, 4);
        System.out.println(Arrays.toString(m1));
        m1 = new int[]{-1, 0, 0, 3, 3, 3, 0, 0, 0};
        mergeSortedArray.merge(m1, 6, new int[]{1, 2, 2}, 3);
        System.out.println(Arrays.toString(m1));
        m1 = new int[]{-1, 0, 0, 0, 0, 0, 3, 3, 3, 0, 0, 0};
        mergeSortedArray.merge(m1, 9, new int[]{1, 2, 2}, 3);
        System.out.println(Arrays.toString(m1));
    }
}
