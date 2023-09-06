package com.kyro.testing;

import java.io.Serializable;
import java.util.List;

public class Company implements Serializable {
    private List<Person> personList;

    public Company(List<Person> personList) {
        this.personList = personList;
    }

    public List<Person> getPersonList() {
        return personList;
    }

    @Override
    public String toString() {
        return "Company{" +
                "personList=" + personList +
                '}';
    }
}
