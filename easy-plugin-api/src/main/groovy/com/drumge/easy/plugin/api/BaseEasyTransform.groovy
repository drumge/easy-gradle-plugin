package com.drumge.easy.plugin.api

import com.android.annotations.NonNull
import com.android.annotations.Nullable
import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformOutputProvider
import javassist.ClassPath
import javassist.ClassPool
import org.gradle.api.Project


class BaseEasyTransform implements IEasyTransform {
    protected Project project
    protected IEasyTransformSupport support

    protected ClassPool pool = new ClassPool(true)
    private final List<ClassPath> classPathList = new ArrayList<>()

    BaseEasyTransform(@NonNull Project project){
        this.project = project

    }

    /**
     * 操作 transform 回调的接口
     * @param support
     */
    void onTransformSupport(IEasyTransformSupport support) {
        this.support = support
    }

    @Override
    void onBeforeTransform(@NonNull Context context,
                           @Nullable TransformOutputProvider outputProvider,
                           boolean isIncremental){

    }

    /**
     * 处理jar包之前回调
     */
    @Override
    void onBeforeJar() {

    }

    /**
     * 判断是否需要加压 jar
     * @param jarInput
     * @param outputFile
     * @return 默认false不解压，如需加压请子类重写并返回true
     */
    @Override
    boolean isNeedUnzipJar(JarInput jarInput, File outputFile){
        return false
    }

    /**
     * 解压之后的jar目录
     * @param jarInput
     * @param unzipPath
     * @param outputFile
     * @return 是否处理过了解压后的jar 文件
     */
    @Override
    boolean onUnzipJarFile(JarInput jarInput, String unzipPath, File outputFile){
        return false
    }

    /**
     * 处理当个jar(library)文件流
     * @param jarInput
     */
    @Override
    void onEachJarOutput(JarInput jarInput, File outputs){
    }

    /**
     * 处理jar包之后回调
     */
    @Override
    void onAfterJar() {

    }

    /**
     * 处理Directory包之前回调
     */
    @Override
    void onBeforeDirectory() {

    }

    /**
     * 处理代码目录，一般指工程中的Java代码文件
     * @param directoryInput
     * @return 是否继续遍历，true--继续，false--跳出循环
     */
    @Override
    void onEachDirectoryOutput(DirectoryInput directoryInput, File outputs){

    }

    /**
     * 处理Directory包之后回调
     */
    @Override
    void onAfterDirectory() {

    }

    /**
     * transform 处理结束
     */
    @Override
    void onAfterTransform(){
    }

    @Override
    void onFinally() {
        removeAllPoolClass()
    }

    @Override
    boolean onException(Exception exception) {
        return false
    }

    /**
     * Appends a directory or a jar (or zip) file to the end of the
     * search path.
     * @param path
     */
    protected final void appendClassPath(String path){
        ClassPath cp = pool.appendClassPath(path)
        classPathList.add(cp)
    }

    /**
     * 在执行结束之后调用该方法释放掉ClassPath，不然有可能java.exe进程不能结束
     */
    private final void removeAllPoolClass(){
        classPathList.each { ClassPath cp ->
            pool.removeClassPath(cp)
        }
        pool.clearImportedPackages()
        classPathList.clear()
    }

    protected final void appendDirClass(List<String> inputs){
        inputs.each { String input ->
            appendClassPath(input)
        }
    }
}