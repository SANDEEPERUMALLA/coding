package com.geeksforgeeks.solutions;

import java.util.*;
import java.util.stream.Collectors;

/*
    Reverse each word in a given string
 */
public class ReverseEachWordInAGivenString {


    String reverseWords(String S) {

        if (S == null || S.isEmpty()) {
            return S;
        }

        StringBuilder finalStr = new StringBuilder();
        char[] chars = S.toCharArray();
        int start = 0;
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            if (ch == '.') {
                reverseChars(chars, start, i - 1);
                start = i + 1;
            }
        }
        reverseChars(chars, start, S.length() - 1);
        return String.valueOf(chars);
    }

    private void reverseChars(char[] chars, int start, int end) {

        for (int i = start, j = end; i <= j; i++, j--) {
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
    }

    public static void main(String[] args) {

        ReverseEachWordInAGivenString reverseEachWordInAGivenString = new ReverseEachWordInAGivenString();
        System.out.println(reverseEachWordInAGivenString.reverseWords("i.like.this.program.very.much"));
        System.out.println(reverseEachWordInAGivenString.reverseWords("pqr.mno"));
        System.out.println(reverseEachWordInAGivenString.reverseWords("acbdef"));
        System.out.println(reverseEachWordInAGivenString.reverseWords(""));
    }
}
