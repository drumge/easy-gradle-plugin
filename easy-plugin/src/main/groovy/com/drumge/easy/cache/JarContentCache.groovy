package com.drumge.easy.cache


import com.android.build.api.transform.JarInput
import com.google.gson.Gson
import org.apache.commons.io.FileUtils

import java.util.concurrent.CopyOnWriteArrayList

/**
 * 记录每次输入的 jar 文件
 * 对比上一次的文件，可区分第一次，以及解决增量重复类的问题
 */
class JarContentCache {
    private String FILE_NAME = "easy_config.json"
    private Gson gson = new Gson()
    private File configFile
    private Config lastConfig
    private Config curConfig
    private boolean isConfigExits = false

    public JarContentCache(String tempDir) {
        configFile = new File(tempDir + File.separator + FILE_NAME)
        println("JarContentCache configFile: " + configFile)
        if (configFile.exists()) {
            isConfigExits = true
            lastConfig = gson.fromJson(FileUtils.readFileToString(configFile), Config.class)
        }
        curConfig = new Config()
        curConfig.jars = new CopyOnWriteArrayList<>()
    }

    boolean exits() {
        return isConfigExits
    }

    void deleteAll() {
        if (configFile.exists()) {
            FileUtils.forceDelete(configFile)
        }
        lastConfig = null
    }

    public void addJar(JarInput jarInput, String output) {
        curConfig.jars.add(new JarInfo(jarInput.name, output))
    }

    public void checkAndSave() {
        curConfig.jars.each {
            lastConfig?.jars?.remove(it)
        }
        lastConfig?.jars?.each {
            println("JarContentCache checkAndSave delete: " + it.filePath)
            FileUtils.forceDelete(it.filePath)
        }

        saveConfig()
    }

    private void saveConfig() {
        String text = gson.toJson(curConfig)
        FileUtils.writeStringToFile(configFile, text)
//        println("JarContentCache saveConfig configFile: " + configFile + " , text: " + text)
    }
}

class Config {
    List<JarInfo> jars
}

class JarInfo {
    String name
    String filePath

    JarInfo(String name, String filePath) {
        this.name = name
        this.filePath = filePath
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        JarInfo jarInfo = (JarInfo) o

        if (name != jarInfo.name) return false

        return true
    }

    int hashCode() {
        return (name != null ? name.hashCode() : 0)
    }
}