package com.geeksforgeeks.solutions;

import java.util.*;

/*
    Print Anagrams Together
 */
public class PrintAnagramsTogether {
    public List<List<String>> Anagrams(String[] string_list) {

        List<List<String>> anagramsResult = new ArrayList<>();
        Map<String, List<String>> anagramsMap = new LinkedHashMap<>();
        for (String s : string_list) {
            char[] chars = s.toCharArray();
            Arrays.sort(chars);
            String sortedStr = String.valueOf(chars);
            anagramsMap.computeIfAbsent(sortedStr, (k) -> new ArrayList<>()).add(s);
        }

        anagramsMap.forEach((k, v) -> {
            anagramsResult.add(v);
        });

        return anagramsResult;
    }

    public static void main(String[] args) {
        PrintAnagramsTogether printAnagramsTogether = new PrintAnagramsTogether();
        System.out.println(printAnagramsTogether.Anagrams(new String[]{"act","god","cat","dog","tac"}));
        System.out.println(printAnagramsTogether.Anagrams(new String[]{"on","no","is",}));
    }
}
