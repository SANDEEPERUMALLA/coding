package com.kyro.testing;

public class AdvancedOption extends Option{

    public AdvancedOption(String name) {
        super(name);
    }

    @Override
    public String retrieve() {
        return "ADVANCED";
    }

    @Override
    public String toString() {
        return "AdvancedOption{}" + super.toString();
    }
}
