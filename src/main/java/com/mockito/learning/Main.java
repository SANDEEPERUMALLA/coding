package com.mockito.learning;

public class Main {

    public static void main(String[] args) {
        PersonService personService = new PersonService();
        PersonCtlr personCtlr = new PersonCtlr(personService);
        System.out.println(personCtlr.getDetails());
    }
}
