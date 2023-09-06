package com.estimate_java_object_size.model;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.Serializable;

public class TestObject implements Serializable {

    private int i1 = 4324;
    private int i2 = 10439987;
    private int i3 = 10994587;
    private int i4 = 4324;
    private int i5 = 139987;
    private int i6 = 10987;
    private String s1  = RandomStringUtils.randomAlphanumeric(200);
    private String s2  = RandomStringUtils.randomAlphanumeric(200);
    private String s3  = RandomStringUtils.randomAlphanumeric(200);
    private String s4  = RandomStringUtils.randomAlphanumeric(200);
    private String s5  = RandomStringUtils.randomAlphanumeric(200);
    private String s6  = RandomStringUtils.randomAlphanumeric(200);
    private Long l1 = 12444L;
    private Long l2 = 434l;
    private Long l3 = 132330l;
    private TestChildObject1 testChildObject1 = new TestChildObject1();
    private TestChildObject2 testChildObject2 = new TestChildObject2();
    private TestChildObject3 testChildObject3 = new TestChildObject3();
}
