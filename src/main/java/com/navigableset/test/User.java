package com.navigableset.test;

import java.util.List;

public interface User extends Runnable {
    long getNoOfOps();

    List<Long> getLatencies();

    String getName();

}
