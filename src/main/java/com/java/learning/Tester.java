package com.java.learning;

import javax.annotation.Nonnull;

public class Tester {

    public Integer add(@Nonnull Integer a, @Nonnull Integer b) {
        return a + b;
    }

    public static void main(String[] args) {
        Tester tester = new Tester();
        int a = 4;
        assert 1 == 1 & a != 4 : "Validation failed";
        //System.out.println(tester.add(null, 10));
    }
}
