package com.drumge.plugin.example

import com.drumge.easy.plugin.api.BaseEasyTransform
import org.gradle.api.Project

class ExampleTransform extends BaseEasyTransform {

    ExampleTransform(Project project) {
        super(project)
        println()
        println('=========== ExampleTransform =========')
        println()

        project.afterEvaluate {
            ExampleExtend extend = project.easy_plugin.plugins.example.extend
            extend.infos.each {
                println(it.name + ', ' + it.infoVersion)
            }
        }
    }

    @Override
    void onBeforeJar() {
        super.onBeforeJar()
        println('onBeforeJar')
    }

    @Override
    void onAfterTransform() {
        super.onAfterTransform()
        println('onAfterTransform')

    }
}