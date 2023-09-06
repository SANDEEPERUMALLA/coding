package com.estimate_java_object_size.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class P1 implements Serializable {
    private String name1;
    private String name2;
    private String name3;
    private String name4;
    private String name5;
    private String name6;
    private String name7;
    private String name8;
    private String name9;
    private String name10;
    Map<String, E1> employeeMap1 = new HashMap<>();
    List<E1> employeeList;

    public P1(String name1, String name2, String name3, String name4, String name5, String name6, String name7,
              String name8, String name9, String name10, List<E1> employeeList) {
        this.name1 = name1;
        this.name2 = name2;
        this.name3 = name3;
        this.name4 = name4;
        this.name5 = name5;
        this.name6 = name6;
        this.name7 = name7;
        this.name8 = name8;
        this.name9 = name9;
        this.name10 = name10;
        this.employeeList = employeeList;
    }

    public P1(String name1, String name2, String name3, String name4, String name5, String name6, String name7,
              String name8, String name9, String name10, List<E1> employeeList, Map<String, E1> employeeMap1) {
        this.name1 = name1;
        this.name2 = name2;
        this.name3 = name3;
        this.name4 = name4;
        this.name5 = name5;
        this.name6 = name6;
        this.name7 = name7;
        this.name8 = name8;
        this.name9 = name9;
        this.name10 = name10;
        this.employeeList = employeeList;
        this.employeeMap1 = employeeMap1;
    }

    @Override public String toString() {
        return "Person{" + "name1='" + name1 + '\'' + ", name2='" + name2 + '\'' + ", name3='" + name3 + '\'' + ", name4='" + name4 + '\'' + ", name5='" + name5 + '\'' + ", name6='" + name6 + '\'' + ", name7='" + name7 + '\'' + ", name8='" + name8 + '\'' + ", name9='" + name9 + '\'' + ", name10='" + name10 + '\'' + ", employeeList=" + employeeList + '}';
    }
}
