package com.drumge.plugn.example.java;

import com.drumge.easy.plugin.api.IPlugin;

import org.gradle.api.Project;

/**
 * Created by chenrenzhan on 2018/5/28.
 */

public class JavaPlugin implements IPlugin {

    public JavaPlugin(Project project) {
        System.out.println();
        System.out.println("===========  JavaPlugin   ================");
        System.out.println();

    }
}
