package com.sandeep.leetcode.solutions;

public class ContainerWithMostWater {

    public int maxArea(int[] height) {
        int left = 0, right = height.length - 1;
        int maxArea = 0;
        while (left < right) {
            int w = right - left;
            int h = Math.min(height[left], height[right]);
            maxArea = Math.max(maxArea, w * h);
            if (height[left] < height[right]) {
                left++;
            } else {
                right--;
            }
        }
        return maxArea;
    }


    public static void main(String[] args) {
        ContainerWithMostWater containerWithMostWater = new ContainerWithMostWater();
        //int[] height = new int[]{1, 8, 6, 2, 5, 4, 8, 3, 7};
        // int[] height = new int[]{2, 3, 4, 5, 18, 17, 6};
        int[] height = new int[]{1, 1};
        int maxArea = containerWithMostWater.maxArea(height);
        System.out.println(maxArea);
    }


    private static class Pair<T> {

        private final T first;
        private final T second;

        Pair(T first, T second) {
            this.first = first;
            this.second = second;

        }

        public T getFirst() {
            return this.first;
        }

        public T getSecond() {
            return this.second;
        }
    }
}
