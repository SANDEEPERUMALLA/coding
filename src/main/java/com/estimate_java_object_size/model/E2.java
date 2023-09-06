package com.estimate_java_object_size.model;

import java.io.Serializable;

public class E2 implements Serializable {

    private Integer name1;
    private Integer name2;
    private Integer name3;
    private Integer name4;
    private Integer name5;
    private Integer name6;
    private Integer name7;
    private Integer name8;
    private Integer name9;
    private Integer name10;

    public E2(Integer name1, Integer name2, Integer name3, Integer name4, Integer name5, Integer name6, Integer name7,
              Integer name8, Integer name9, Integer name10) {
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
    }

    @Override public String toString() {
        return "Employee{" + "name1='" + name1 + '\'' + ", name2='" + name2 + '\'' + ", name3='" + name3 + '\'' + ", name4='" + name4 + '\'' + ", name5='" + name5 + '\'' + ", name6='" + name6 + '\'' + ", name7='" + name7 + '\'' + ", name8='" + name8 + '\'' + ", name9='" + name9 + '\'' + ", name10='" + name10 + '\'' + '}';
    }
}
