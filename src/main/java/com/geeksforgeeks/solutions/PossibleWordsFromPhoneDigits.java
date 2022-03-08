package com.geeksforgeeks.solutions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
    Possible Words From Phone Digits
 */
public class PossibleWordsFromPhoneDigits {
    static Map<Integer, List<Character>> numberMapping = new HashMap<>();

    static {
        numberMapping.put(1, List.of(' '));
        numberMapping.put(2, List.of('a', 'b', 'c'));
        numberMapping.put(3, List.of('d', 'e', 'f'));
        numberMapping.put(4, List.of('g', 'h', 'i'));
        numberMapping.put(5, List.of('j', 'k', 'l'));
        numberMapping.put(6, List.of('m', 'n', 'o'));
        numberMapping.put(7, List.of('p', 'q', 'r', 's'));
        numberMapping.put(8, List.of('t', 'u', 'v'));
        numberMapping.put(9, List.of('w', 'x', 'y', 'z'));
    }

    static ArrayList<String> possibleWords(int[] a, int N) {
        ArrayList<String> finalWordList = new ArrayList<>();
        possibleWords(a, N, 0, "", finalWordList);
        return finalWordList;
    }

    static void possibleWords(int[] a, int N, int index, String currStr, List<String> finalWordList) {
        if (index == N) {
            finalWordList.add(currStr);
            return;
        }
        List<Character> characters = numberMapping.get(a[index]);
        for (char ch : characters) {
            possibleWords(a, N, index + 1, currStr + ch, finalWordList);
        }
    }

    public static void main(String[] args) {
        System.out.println(possibleWords(new int[]{2, 3, 4}, 3));;
        // possibleWords(new int[]{3, 4, 5}, 3);
    }
}
