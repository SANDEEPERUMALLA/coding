package com.mockito.learning;

public class PersonCtlr {


    private PersonService personService;
    public PersonCtlr(PersonService personService) {
        this.personService = personService;
    }


    public String getDetails() {
        personService.setRole("Role123");
        personService.setRole("Role789");
        return personService.getName() + " : " + personService.getAge();
    }
}
