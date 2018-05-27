package com.drumge.plugn.example.java;

import com.drumge.easy.plugin.api.IExtend;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.util.ConfigureUtil;

import groovy.lang.Closure;

/**
 * Created by chenrenzhan on 2018/5/28.
 */

public class JavaExtend implements IExtend {

    public boolean enable = true;
    public NamedDomainObjectContainer<JavaInfo> infos;

    public JavaExtend(Project project) {

        System.out.println();
        System.out.println("===========  JavaExtend   ================");
        System.out.println();

        infos = project.container(JavaInfo.class);

    }


    public static JavaExtend createExtend(Project project, Action<JavaExtend> configuration) {
        JavaExtend extend = new JavaExtend(project);
        configuration.execute(extend);
        return extend;
    }

    //infos，允许我们通过配置传入闭包，来给plugins容器添加对象
    public void infos(Closure configureClosure){
        if (!enable) {
            return;
        }
        ConfigureUtil.configure(configureClosure, infos);
    }
}
