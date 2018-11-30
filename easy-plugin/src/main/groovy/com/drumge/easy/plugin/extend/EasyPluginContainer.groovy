package com.drumge.easy.plugin.extend

import com.android.build.api.transform.QualifiedContent
import com.drumge.easy.plugin.api.IEasyPluginContainer
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil

class EasyPluginContainer implements IEasyPluginContainer {

    boolean enable = true
    NamedDomainObjectContainer<EasyExtend> plugins
    Set<QualifiedContent.ContentType> inputTypes
    Set<? super QualifiedContent.Scope> scopes

    EasyPluginContainer(Project project) {
        plugins = project.container(EasyExtend)
    }

    //plugins，允许我们通过配置传入闭包，来给plugins容器添加对象
    void plugins(Closure configureClosure){
        if (!enable) {
            return
        }
        ConfigureUtil.configure(configureClosure, plugins)
//        plugins.configure(closure)
    }

    @Override
    void transformInputTypes(Set<QualifiedContent.ContentType> set) {
        inputTypes = set
    }

    @Override
    void transformScopes(Set<? super QualifiedContent.Scope> set) {
        scopes = set
    }
}