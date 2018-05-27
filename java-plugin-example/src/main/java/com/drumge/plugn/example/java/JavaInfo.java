package com.drumge.plugn.example.java;

import org.gradle.api.Named;

/**
 * Created by chenrenzhan on 2018/5/28.
 */

public class JavaInfo implements Named {
    private final String name;
    public String infoVersion;

    public JavaInfo(String name) {
        this.name = name;

    }

    @Override
    public String getName() {
        return name;
    }

}
