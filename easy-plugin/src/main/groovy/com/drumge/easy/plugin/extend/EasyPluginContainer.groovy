package com.drumge.easy.plugin.extend

import com.drumge.easy.plugin.api.IEasyPluginContainer
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

class EasyPluginContainer implements IEasyPluginContainer {

    boolean enable
    NamedDomainObjectContainer<EasyExtend> plugins

    EasyPluginContainer(Project project) {
        plugins = project.container(EasyExtend)
    }

    //plugins，允许我们通过配置传入闭包，来给plugins容器添加对象
    void plugins(Closure closure){
        plugins.configure(closure)
    }
}