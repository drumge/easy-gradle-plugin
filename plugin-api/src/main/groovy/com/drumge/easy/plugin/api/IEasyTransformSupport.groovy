package com.drumge.easy.plugin.api

interface IEasyTransformSupport {
    /**
     * 重新压缩解压操作之后的jar包
     * @param unzipPath
     */
    void rezipUnzipJarFile(String unzipPath)
}