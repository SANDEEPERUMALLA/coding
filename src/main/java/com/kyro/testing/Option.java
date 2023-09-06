package com.kyro.testing;

import java.io.Serializable;

public abstract class Option implements Serializable {
    private String name;

    public Option(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract String retrieve();

    @Override
    public String toString() {
        return "Option{" +
                "name='" + name + '\'' +
                '}';
    }
}
