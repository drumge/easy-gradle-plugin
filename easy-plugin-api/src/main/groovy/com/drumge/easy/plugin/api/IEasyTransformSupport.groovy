package com.drumge.easy.plugin.api

import java.util.concurrent.Callable

interface IEasyTransformSupport {
    /**
     * 重新压缩解压操作之后的jar包
     * @param unzipPath
     */
    void rezipUnzipJarFile(String unzipPath)


    /**
     * 异步执行任务
     * @param callable
     */
    def <V> void execute(Callable<V> callable)

    /**
     * 等待正在执行的异步任务全部完成
     */
    void waitForTasks()
}