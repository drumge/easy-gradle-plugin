package com.drumge.easy.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.drumge.easy.plugin.api.IEasyPluginContainer
import com.drumge.easy.plugin.extend.EasyPluginContainer
import org.gradle.api.Plugin
import org.gradle.api.Project

class EasyPlugin implements Plugin<Project> {


    @Override
    void apply(Project project) {
        println()
        println("================  EasyPlugin  ================")
        println()

        createExtend(project)

        def isApplication = project.plugins.hasPlugin(AppPlugin)
        def isAndroidLibrary = project.plugins.hasPlugin(LibraryPlugin)

        if(isApplication || isAndroidLibrary){
            project.android.registerTransform(new EasyTransform(project))
        }
    }

    private void createExtend(Project project){
        project.extensions.create(IEasyPluginContainer.EASY_PLUGIN_TAG, EasyPluginContainer, project)
    }
}