package com.drumge.easy.plugin.api

import org.gradle.api.Named

interface IEasyExtend<T extends IEasyTransform, P extends IPlugin, E extends IExtend> extends Named {

    T getTransform()

    P getPlugin()

    E getExtend()
}