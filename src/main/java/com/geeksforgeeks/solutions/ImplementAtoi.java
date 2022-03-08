package com.geeksforgeeks.solutions;

public class ImplementAtoi {

    int atoi(String str) {

        char firstCh = str.charAt(0);
        if(firstCh == '+'){
            return atoiInternal(str.substring(1));
        } else if(firstCh == '-'){
            return -1 * atoiInternal(str.substring(1));
        } else {
            return atoiInternal(str);
        }
    }

    int atoiInternal(String str) {

        int finalNumber = 0;
        int index = 0;
        for (int i = str.length() - 1; i >= 0; i--) {
            char ch = str.charAt(i);
            if (!Character.isDigit(ch)) {
                return -1;
            }
            int num = ch - '0';
            finalNumber += num * (int) Math.pow(10, index) ;
            index++;
        }
        return finalNumber;
    }

    public static void main(String[] args) {
        ImplementAtoi implementAtoi = new ImplementAtoi();
        System.out.println(implementAtoi.atoi("-112"));
    }
}
