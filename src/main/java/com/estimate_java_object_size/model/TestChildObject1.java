package com.estimate_java_object_size.model;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.Serializable;

public class TestChildObject1 implements Serializable {
    private String s1  = RandomStringUtils.randomAlphanumeric(200);
    private String s2  = RandomStringUtils.randomAlphanumeric(200);
    private String s3  = RandomStringUtils.randomAlphanumeric(200);

}
