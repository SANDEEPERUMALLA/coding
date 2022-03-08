package com.sandeep.leetcode.solutions;

public class CanPlaceFlowers {

    public boolean canPlaceFlowers(int[] flowerbed, int n) {
        if (flowerbed.length == 1 && flowerbed[0] == 0) {
            return true;
        }

        boolean firstList = true;
        int currentConsecutiveZeroLength = 0;
        int noOfFlowers = 0;
        for (int i : flowerbed) {
            if (i == 0) {
                currentConsecutiveZeroLength++;
            } else {
                if (firstList) {
                    noOfFlowers += currentConsecutiveZeroLength / 2;
                    firstList = false;
                } else {
                    noOfFlowers += (currentConsecutiveZeroLength - 1) / 2;
                }
                if (noOfFlowers >= n) {
                    return true;
                }

                currentConsecutiveZeroLength = 0;
            }
        }

        if (currentConsecutiveZeroLength == flowerbed.length) {
            noOfFlowers = (flowerbed.length + 1) / 2;
        } else if (currentConsecutiveZeroLength != 0 && flowerbed[flowerbed.length - 1] == 0) {
            noOfFlowers += (currentConsecutiveZeroLength / 2);
        }

        return noOfFlowers >= n;
    }

    public static void main(String[] args) {
        CanPlaceFlowers canPlaceFlowers = new CanPlaceFlowers();
        System.out.println(canPlaceFlowers.canPlaceFlowers(new int[]{0, 0, 0, 0, 1}, 3));
    }
}
