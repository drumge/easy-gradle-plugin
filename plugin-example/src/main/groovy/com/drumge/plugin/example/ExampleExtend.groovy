package com.drumge.plugin.example

import com.drumge.easy.plugin.api.IExtend
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil

class ExampleExtend implements IExtend {
    private Project mProject

    boolean enable = true
    NamedDomainObjectContainer<ExtendInfo> infos


    private ExampleExtend(Project project) {
        mProject = project

        println()
        println("============= ExampleExtend =========")
        println()

        infos = project.container(ExtendInfo)

    }

    static ExampleExtend createExtend(Project project, Action<ExampleExtend> configuration) {
        ExampleExtend extend = new ExampleExtend(project)
        configuration.execute(extend)
        return extend
    }

    //infos，允许我们通过配置传入闭包，来给plugins容器添加对象
    void infos(Closure configureClosure){
        if (!enable) {
            return
        }
        ConfigureUtil.configure(configureClosure, infos)
    }

}