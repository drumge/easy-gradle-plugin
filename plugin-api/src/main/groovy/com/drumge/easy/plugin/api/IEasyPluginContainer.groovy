package com.drumge.easy.plugin.api

import com.android.build.api.transform.QualifiedContent
import org.gradle.api.NamedDomainObjectContainer

interface IEasyPluginContainer {
    final static String EASY_PLUGIN_TAG = 'easy_plugin'

    boolean isEnable()

    NamedDomainObjectContainer<IEasyExtend> getPlugins()

    void transformInputTypes(Set<QualifiedContent.ContentType> inputTypes)

    void transformScopes(Set<? super QualifiedContent.Scope> scope)
}