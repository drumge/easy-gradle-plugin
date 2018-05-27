package com.drumge.plugin.example

import com.drumge.easy.plugin.api.IPlugin
import org.gradle.api.Project

class ExamplePlugin implements IPlugin {

    ExamplePlugin(Project project) {
        println()
        println('=========== ExamplePlugin =============')
        println()
    }

}