package com.sandeep.leetcode.solutions;

import java.util.Objects;
import java.util.Stack;

public class BasicCalculator {

    public int calculate(String s) {

        Stack<String> stack = new Stack<>();
        String num = "";
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (i > 0 && !Character.isDigit(s.charAt(i)) && Character.isDigit(s.charAt(i - 1))) {
                if (!stack.empty()) {
                    if (isOp(stack.peek())) {
                        String op = stack.pop();
                        String num2 = stack.pop();
                        String res = eval(num2, num, op);
                        stack.push(res);
                    } else {
                        stack.push(num);
                    }
                } else {
                    stack.push(num);
                }
                num = "";
            }

            if (ch == '+' || ch == '-') {
                stack.push(String.valueOf(ch));
            } else if (ch == '(') {
                stack.push(String.valueOf(ch));
            } else if (Character.isDigit(s.charAt(i))) {
                num = num + ch;
            } else if (ch == ')') {
                String n = stack.pop();
                stack.pop();
                stack.push(n);
            }

            System.out.println("Index: " + i);
            System.out.println(stack);
        }

        if (num.length() > 0) {
            stack.push(num);
            processStack(stack);
        } else if (stack.size() > 1) {
            processStack(stack);
        }

        return Integer.parseInt(stack.peek());
    }

    private void processStack(Stack<String> stack) {

        while (stack.size() != 1) {
            String num1 = stack.pop();
            String op = stack.pop();
            String num2 = stack.pop();
            String res = eval(num2, num1, op);
            stack.push(res);
        }
    }

    private String eval(String num1, String num2, String op) {
        Integer val = null;
        if ("+".equals(op)) {
            val = Integer.parseInt(num1) + Integer.parseInt(num2);
        } else if ("-".equals(op)) {
            val = Integer.parseInt(num1) - Integer.parseInt(num2);
        }
        return String.valueOf(val);
    }

    private boolean isOp(String ele) {
        return Objects.equals(ele, "+") || Objects.equals(ele, "-");
    }

    public static void main(String[] args) {
        BasicCalculator basicCalculator = new BasicCalculator();
        System.out.println(basicCalculator.calculate("(1+(4+5+2)-3)+(6+8)"));
    }
}
