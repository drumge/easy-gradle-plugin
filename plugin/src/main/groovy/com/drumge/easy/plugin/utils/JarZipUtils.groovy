package com.drumge.easy.plugin.utils

import java.util.zip.ZipFile

class JarZipUtils {
    /**
     * 压缩 dirPath 到 zipFilePath
     */
    def static zipJar(String dirPath, String zipFilePath) {
        new AntBuilder().zip(destfile: zipFilePath, basedir: dirPath)
    }

    /**
     * 解压 zipFilePath 到 目录 dirPath
     */
    static boolean unzipJar(String zipFilePath, String dirPath) {
        if (isZipEmpty(zipFilePath)) {
            println ">>> Zip file is empty! Ignore";
            return false
        }
        new AntBuilder().unzip(src: zipFilePath, dest: dirPath, overwrite: 'true')
        return true
    }

    static boolean isZipEmpty(String zipFilePath) {
        ZipFile z
        try {
            z = new ZipFile(zipFilePath)
            return z.size() == 0
        } finally {
            z.close()
        }
    }
}
