package com.drumge.easy.plugin.extend

import com.drumge.easy.plugin.api.IEasyPluginContainer
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil

class EasyPluginContainer implements IEasyPluginContainer {

    boolean enable = true
    NamedDomainObjectContainer<EasyExtend> plugins

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
}