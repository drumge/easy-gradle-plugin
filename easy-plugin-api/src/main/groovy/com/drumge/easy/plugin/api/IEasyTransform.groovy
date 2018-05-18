package com.drumge.easy.plugin.api

import com.android.annotations.NonNull
import com.android.annotations.Nullable
import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformOutputProvider

/**
 * 为避免多次遍历，解压，压缩等，如需添加transform处理,请继承 BaseEasyTransform，并重写相关的方法进行
 */

interface IEasyTransform {

    /**
     * 操作 transform 回调的接口
     * @param support
     */
    void onTransformSupport(IEasyTransformSupport support)

    /**
     * 在 transform 开始处理文件之前的回调
     * @param context
     * @param outputProvider
     * @param isIncremental
     */
    void onBeforeTransform(@NonNull Context context,
                           @Nullable TransformOutputProvider outputProvider,
                           boolean isIncremental)

    /**
     * 处理jar包之前回调
     */
    void onBeforeJar()

    /**
     * 判断是否需要解压 jar
     * @param jarInput
     * @param outputFile
     * @return 默认false不解压，如需解压请子类重写并返回true
     */
    boolean isNeedUnzipJar(JarInput jarInput, File outputFile)

    /**
     * 解压之后的jar目录，如果都没有操作（修改）过jar文件，则在执行结束之后不会进行压缩
     * @param jarInput
     * @param unzipPath
     * @param outputFile
     * @return 是否处理过了解压后的jar 文件
     */
    boolean onUnzipJarFile(JarInput jarInput, String unzipPath, File outputFile)

    /**
     * jar文件输出目录，不要在 outputs 的输出目录进行文件操作
     * 如需要解压jar包并修改文件，可以在{@link #doUnzipJarFile}中操作
     * @param jarInput jar 文件输入流
     * @param outputs 输入的jar转到输出的文件
     */
    void onEachJarOutput(JarInput jarInput, File outputs)

    /**
     * 处理jar包之后回调
     */
    void onAfterJar()

    /**
     * 处理Directory包之前回调
     */
    void onBeforeDirectory()

    /**
     * 处理代码目录，一般指工程中的Java代码文件
     * @param directoryInput
     * @return 是否继续遍历，true--继续，false--跳出循环
     */
    void onEachDirectoryOutput(DirectoryInput directoryInput, File outputs)

    /**
     * 处理Directory包之后回调
     */
    void onAfterDirectory()

    /**
     * transform 处理结束
     * 可在这里做一些清除操作，比如释放掉ClassPath，不然有可能java.exe进程不能结束
     */
    void onAfterTransform()

    /**
     * 发生异常时回调，只要有一个返回false，即不处理异常，都会终止编译并输出异常堆栈
     * @param exception
     * @return 是否拦截处理异常
     */
    boolean onException(Exception exception)

    /**
     * 结束回调此方法，无论正常结束或者发生异常都会调到该方法，释放掉ClassPath
     */
    void onFinally()
}