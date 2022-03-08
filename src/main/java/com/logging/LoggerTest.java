package com.logging;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import test.Binder;

public class LoggerTest {

    private static final Logger logger
            = LoggerFactory.getLogger(LoggerTest.class);

    public static void main(String[] args) throws IOException {
        Enumeration<URL> resources = LoggerTest.class.getClassLoader().getResources("test/Binder.class");
        System.out.println(resources);
        while (resources.hasMoreElements()){
            URL url = resources.nextElement();
            System.out.println(url);
        }
        System.out.println(Binder.get());
    }
}
