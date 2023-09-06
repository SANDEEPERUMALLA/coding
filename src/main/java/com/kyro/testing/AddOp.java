package com.kyro.testing;

public class AddOp implements Op{
    private String op = "ADD";

    @Override
    public String getOp() {
        return "Add";
    }

    @Override
    public String toString() {
        return "AddOp{" +
                "op='" + op + '\'' +
                '}';
    }
}
