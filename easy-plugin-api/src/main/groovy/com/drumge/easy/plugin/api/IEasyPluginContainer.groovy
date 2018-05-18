package com.drumge.easy.plugin.api

import org.gradle.api.NamedDomainObjectContainer

interface IEasyPluginContainer {
    final static String EASY_PLUGIN_TAG = 'easy_plugin'

    boolean isEnable()

    NamedDomainObjectContainer<IEasyExtend> getPlugins()
}