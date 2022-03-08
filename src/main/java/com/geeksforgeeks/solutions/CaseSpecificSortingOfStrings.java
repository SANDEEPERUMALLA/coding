package com.geeksforgeeks.solutions;

import java.util.*;

/*
Case-specific Sorting of Strings
 */
public class CaseSpecificSortingOfStrings {

    public static String caseSort(String str) {
        List<Character> lowerCaseChars = new ArrayList<>();
        List<Character> upperCaseChars = new ArrayList<>();

        for (char ch : str.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                upperCaseChars.add(ch);
            } else {
                lowerCaseChars.add(ch);
            }
        }

        Collections.sort(lowerCaseChars);
        Collections.sort(upperCaseChars);

        char[] finalString = new char[str.length()];
        int lStart = 0;
        int uStart = 0;
        for (int i = 0; i < str.length(); i++) {
            if (Character.isUpperCase(str.charAt(i))) {
                finalString[i] = upperCaseChars.get(uStart);
                uStart++;
            } else {
                finalString[i] = lowerCaseChars.get(lStart);
                lStart++;
            }
        }

        return String.valueOf(finalString);
    }

    public static void main(String[] args) {
        System.out.println(caseSort("defRTSersUXI"));
        System.out.println(caseSort("birDKs"));
        System.out.println(caseSort("aaaa"));
        System.out.println(caseSort("acbBZD"));
        System.out.println(caseSort("aaa"));
        System.out.println(caseSort("ZZZ"));

    }
}
