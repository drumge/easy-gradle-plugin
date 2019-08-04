
package com.drumge.easy.plugin.utils

import org.gradle.api.Project
import org.gradle.api.artifacts.*
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency

import java.security.DigestInputStream
import java.security.MessageDigest

/** Class to resolve project dependencies */
final class DependenciesUtils {

    static Set<ResolvedDependency> getAllResolveDependencies(Project project, String config) {
        Configuration configuration
        try {
            configuration = project.configurations[config]
        } catch (UnknownConfigurationException ignored) {
            return null
        }

        return getAllResolveDependencies(configuration)
    }

    static Set<ResolvedDependency> getFirstLevelDependencies(Project project, String config) {
        def configuration = project.configurations[config]
        ResolvedConfiguration resolvedConfiguration = configuration.resolvedConfiguration
        def firstLevelDependencies = resolvedConfiguration.firstLevelModuleDependencies
        return firstLevelDependencies
    }

    static void collectAllDependencies(Project prj, Set<Dependency> allDependencies, String config ) {
        //Defining configuration names from which dependencies will be taken (debugCompile or releaseCompile and compile)
        prj.configurations["${config}"].allDependencies.each { depend ->
            if (allDependencies.find { addedNode -> addedNode.group == depend.group && addedNode.name == depend.name } == null) {
                allDependencies.add(depend)
            }
            if (depend instanceof DefaultProjectDependency) {
                collectAllDependencies(depend.dependencyProject, allDependencies, config)
            }
        }
    }

    static Set<ResolvedDependency> getAllResolveDependencies(Configuration configuration) {
        ResolvedConfiguration resolvedConfiguration = configuration.resolvedConfiguration
        def firstLevelDependencies = resolvedConfiguration.firstLevelModuleDependencies
        Set<ResolvedDependency> allDependencies = new HashSet<>()
        firstLevelDependencies.each {
            collectDependencies(it, allDependencies)
        }
        return allDependencies
    }

    private static void collectDependencies(ResolvedDependency node, Set<ResolvedDependency> out) {
        if (out.find { addedNode -> addedNode.name == node.name } == null) {
            out.add(node)
        }
        // Recursively
        node.children.each { newNode ->
            collectDependencies(newNode, out)
        }
    }

    static void collectAars(File d, Set outAars) {
        if (!d.exists()) return
        d.eachLine { line ->
            def module = line.split(':')
            def N = module.size()
            def aar = [group: module[0], name: module[1], version: (N == 3) ? module[2] : '', jars:null]
            if (!outAars.contains(aar)) {
                outAars.add(aar)
            }
        }
    }

    static String generateMD5(File file) {
        file.withInputStream {
            new DigestInputStream(it, MessageDigest.getInstance('MD5')).withStream {
                it.eachByte {}
                it.messageDigest.digest().encodeHex() as String
            }
        }
    }
}