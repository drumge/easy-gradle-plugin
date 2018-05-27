package com.drumge.plugn.example.java;

import com.drumge.easy.plugin.api.BaseEasyTransform;
import com.drumge.easy.plugin.api.IEasyPluginContainer;
import com.drumge.easy.plugin.api.IExtend;

import org.gradle.api.Action;
import org.gradle.api.Project;

import java.util.Iterator;

/**
 * Created by chenrenzhan on 2018/5/28.
 */

public class JavaTransform extends BaseEasyTransform {

    public JavaTransform(Project project) {
        super(project);
        System.out.println();
        System.out.println("===========  JavaTransform   ================");
        System.out.println();

        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                Object easyPlugin = project.property("easy_plugin");
                if (easyPlugin instanceof IEasyPluginContainer) {
                    IExtend extend = ((IEasyPluginContainer) easyPlugin).getPlugins().getByName("java_example").getExtend();
                    if (extend instanceof JavaExtend) {
                        Iterator<JavaInfo> it = ((JavaExtend) extend).infos.iterator();
                        while (it.hasNext()) {
                            JavaInfo info = it.next();
                            System.out.println(info.getName() + ", " + info.infoVersion);
                        }
                    }
                }
            }
        });
    }
}
