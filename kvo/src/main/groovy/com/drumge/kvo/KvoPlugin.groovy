package com.drumge.kvo

import com.drumge.easy.plugin.api.IPlugin
import org.gradle.api.Project

class KvoPlugin implements IPlugin {

    KvoPlugin(Project project) {
        println('================  KvoPlugin  ================')

//        project.rootProject.subprojects.each { Project p ->
//            println("sub project: " + p)
//            project.dependencies.add('annotationProcessor', 'com.github.drumge:kvo-compiler:0.0.1')
//            println("sub annotationProcessor: " + p.configurations.hasProperty('annotationProcessor'))
//
//        }
    }
}