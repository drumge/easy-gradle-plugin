package com.drumge.plugin.example

import org.gradle.api.Named

class ExtendInfo implements Named {
    private final String name
    String infoVersion

    ExtendInfo(String name) {
        this.name = name
    }

    @Override
    String getName() {
        return name
    }
}