package com.drumge.easy.plugin

import com.android.annotations.NonNull
import com.android.annotations.Nullable
import com.android.build.api.transform.*
import com.android.ide.common.internal.WaitableExecutor
import com.drumge.easy.cache.JarContentCache
import com.drumge.easy.plugin.api.IEasyPluginContainer
import com.drumge.easy.plugin.api.IEasyTransform
import com.drumge.easy.plugin.api.IEasyTransformSupport
import com.drumge.easy.plugin.extend.EasyExtend
import com.drumge.easy.plugin.extend.EasyPluginContainer
import com.drumge.easy.plugin.utils.JarZipUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

import java.util.concurrent.Callable

/**
 * https://www.jianshu.com/p/facbca0576a6
 * https://blog.csdn.net/weixin_38754349/article/details/97077999
 */
class EasyTransform extends Transform implements IEasyTransformSupport {

    private final Project project
    private EasyPluginContainer extend
    private boolean isEnable = true
    private final List<IEasyTransform> transformList = new ArrayList<>()
    private final Map<String, String> allUnzipJars = new HashMap<>()
    // 暂存解压并被修改的 jar 文件，在最后在压缩
    private final Map<String, String> unzipJars = new HashMap<>()
    private WaitableExecutor waitableExecutor
    private List<Exception> exceptionList
    private JarContentCache jarContentCache

    EasyTransform(Project project) {
        this.project = project
        extend = project[IEasyPluginContainer.EASY_PLUGIN_TAG]
        if (project.hasProperty(IEasyPluginContainer.EASY_PLUGIN_TAG)) {
            project.afterEvaluate {
                extend = project[IEasyPluginContainer.EASY_PLUGIN_TAG]
                isEnable = extend.enable && extend.plugins != null
                if (!isEnable) {
                    return
                }
                extend.plugins.each { EasyExtend plugin ->
                    println("[EasyTransform] plugin name: ${plugin.name}")
                    if (plugin.transform != null) {
                        transformList.add(plugin.transform)
                    }

                }
            }
        }
    }

    @Override
    String getName() {
        return "EasyTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
//        return Sets.immutableEnumSet(QualifiedContent.DefaultContentType.CLASSES)
        return extend.inputTypes
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
//        return TransformManager.SCOPE_FULL_PROJECT
        return extend.scopes
        // Transforms with scopes '[SUB_PROJECTS, EXTERNAL_LIBRARIES]' cannot be applied to library projects.
//        return Sets.immutableEnumSet(QualifiedContent.Scope.PROJECT)
//        return Sets.immutableEnumSet(
//                QualifiedContent.Scope.PROJECT,
//                QualifiedContent.Scope.SUB_PROJECTS,
//                QualifiedContent.Scope.EXTERNAL_LIBRARIES)
    }

    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        if (!isEnable) {
            return
        }
        exceptionList = new ArrayList<>()
        waitableExecutor = WaitableExecutor.useGlobalSharedThreadPool()
        try {
            doTransform(transformInvocation)
        } catch (Exception exception) {
            if (!onException(exception)) { // 不处理异常，则终止编译并输出异常堆栈
                throwException(exception)
            }
            exception.printStackTrace()
        } finally {
            onFinally()
        }
    }

    private void throwException(Exception exception) {
        if (exception instanceof RuntimeException) {
            throw ((RuntimeException) exception)
        }
        throw new RuntimeException("some error happened when building, look up more detail ahead", exception)
    }

    @Override
    void rezipUnzipJarFile(String unzipPath) {
        if (unzipJars.containsKey(unzipPath)) {
            return
        }
        String outPath = allUnzipJars.get(unzipPath)
        if (outPath != null && outPath.size() > 0) {
            unzipJars.put(unzipPath, outPath)
        }
    }

    @Override
    <V> void execute(Callable<V> callable) {
        waitableExecutor.execute(new ExecutorRunnable(callable, { e ->
            happenException(e)
            checkThrowException()
        }))
    }

    @Override
    void waitForTasks() {
        waitableExecutor.waitForTasksWithQuickFail(true)
        checkThrowException()
    }

    private void doTransform(TransformInvocation transformInvocation) {
        Context context = transformInvocation.getContext()
        Collection<TransformInput> inputs = transformInvocation.getInputs()
        Collection<TransformInput> referencedInputs = transformInvocation.getReferencedInputs()
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider()
        boolean isIncremental = transformInvocation.isIncremental()

        println("isIncremental " + isIncremental)

        setSupport(this)
        jarContentCache = new JarContentCache(context.temporaryDir.absolutePath)

        /** 为避免多次遍历，解压，压缩等，如需添加transform处理,请继承 BaseEasyTransform，并重写相关的方法进行 */
        doBeforeTransform(context, outputProvider, isIncremental)

        doBeforeJar()
        transformJar(context, inputs, outputProvider, isIncremental)
        doAfterJar()

        doBeforeDirectory()
        transformSrc(inputs, outputProvider, isIncremental)
        doAfterDirectory()

        doAfterTransform()
        zipTmpJar()
    }

    private void transformJar(Context context, Collection<TransformInput> inputs,
                              TransformOutputProvider outputProvider, boolean isIncremental) {
        String tmpDirPath = context.temporaryDir.absolutePath
        boolean isJarIncremental = isIncremental && jarContentCache.exits()
        println("transformJar isJarIncremental " + isJarIncremental)
        if (!isJarIncremental) {
            outputProvider.deleteAll()
            jarContentCache.deleteAll()
        }

        inputs.each { TransformInput input ->
            input.jarInputs.each { JarInput jarInput ->
//                println("jarInput " + jarInput)
                execute {
                    File output = outputProvider.getContentLocation(jarInput.name,
                            jarInput.contentTypes, jarInput.scopes, Format.JAR)
                    if (isJarIncremental) {
                        handleIncrementJar(jarInput, output, tmpDirPath)
                    } else {
                        handleNoIncrementJar(output, jarInput, tmpDirPath)
                    }

                    if (jarInput.status != Status.REMOVED) {
                        jarContentCache.addJar(jarInput, output.absolutePath)
                    }
                }
            }
        }
        waitForTasks()
    }

    private void handleIncrementJar(JarInput jarInput, File output,
                                    String tmpDirPath) {
        if (jarInput.status == Status.ADDED
                || jarInput.status == Status.CHANGED) {
            handleNoIncrementJar(output, jarInput, tmpDirPath)
        } else if (jarInput.status == Status.NOTCHANGED) {
            //记录当前哪些jar参与了编译
//            File output = outputProvider.getContentLocation(jarInput.name,
//                    jarInput.contentTypes, jarInput.scopes, Format.JAR)
            doNoChangeJar(jarInput, output)
        } else if (jarInput.status == Status.REMOVED) {
            //Status.REMOVED,其实一般删除一个jar，实测并不会传入进来,所以什么都不干
        }
    }

    private void handleNoIncrementJar(File output, JarInput jarInput,
                                      String tmpDirPath) {
        handleEachJarInput(output, jarInput, tmpDirPath)
        doEachJarOutput(jarInput, output)
    }

    private void transformSrc(Collection<TransformInput> inputs, TransformOutputProvider
            outputProvider, boolean isIncremental) {
        boolean isDirIncremental = isIncremental && jarContentCache.exits()
//        println("transformSrc isDirIncremental " + isDirIncremental)
        inputs.each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput directoryInput ->
                println("${System.currentTimeMillis()} directoryInput " + directoryInput)
                execute {
                    File outputDirFile = outputProvider.getContentLocation(directoryInput.name,
                            directoryInput.contentTypes, directoryInput.scopes,
                            Format.DIRECTORY)
                    println("${System.currentTimeMillis()} outputDirFile " + outputDirFile)
                    doEachDirectoryOutput(directoryInput, outputDirFile)
                    def inputFilePath = directoryInput.file.absolutePath
                    if (isDirIncremental) {
                        handleIncrementChangeFile(directoryInput, inputFilePath, outputDirFile)
                    } else {
                        File output = handleEachDirInput(outputProvider, directoryInput)
                        project.fileTree(output).findAll { !it.directory }.each {
                            doEachChangeFile(directoryInput, outputDirFile, it)
                        }
                    }
                }
            }
        }
        waitForTasks()
    }

    private List handleIncrementChangeFile(DirectoryInput directoryInput,
                                           inputFilePath, File outputDirFile) {
        def outputDir = outputDirFile.absolutePath
        for (def entry : directoryInput.changedFiles.entrySet()) {
            File file = entry.key
            Status status = entry.value
            def outputFullPath = file.absolutePath.replace(inputFilePath, outputDir)
            def outputFile = new File(outputFullPath)
            if (!outputFile.parentFile.exists()) {
                outputFile.parentFile.mkdirs()
            }
            if ((status == Status.CHANGED || status == Status.ADDED)
                    && !file.isDirectory()) {
                FileUtils.copyFile(file, outputFile)
                doEachChangeFile(directoryInput, outputDirFile, outputFile)
            } else if (status == Status.REMOVED) {
                outputFile.delete()
            }
        }
    }

    private File handleEachDirInput(TransformOutputProvider outputProvider, DirectoryInput directoryInput) {
        File output = outputProvider.getContentLocation(directoryInput.name,
                directoryInput.contentTypes, directoryInput.scopes,
                Format.DIRECTORY)
        FileUtils.copyDirectory(directoryInput.file, output)
        return output
    }

    private void handleEachJarInput(File output, JarInput jarInput, String tmpDirPath) {
        if (isNeedUnzipJar(jarInput, output)) { // 需要解压 jar
            String tmpPath = "${tmpDirPath}${File.separator}${jarInput.name.replace(':', '')}${File.separator}"
            JarZipUtils.unzipJar(jarInput.file.absolutePath, tmpPath)
            allUnzipJars.put(tmpPath, output.absolutePath)
            if (doUnzipJarFile(jarInput, tmpPath, output)) { // 需要压缩
                unzipJars.put(tmpPath, output.absolutePath)
            }
        }
        FileUtils.copyFile(jarInput.file, output)
    }

    private void zipTmpJar() {
//        WaitableExecutor waitableExecutor = WaitableExecutor.useGlobalSharedThreadPool()
        unzipJars.each { String tmp, String out ->
            if (out != '') {
                execute {
                    JarZipUtils.zipJar(tmp, out)
                }
            }

        }
        execute {
            jarContentCache.checkAndSave()
        }
        waitForTasks()
    }

    private void setSupport(IEasyTransformSupport support) {
        transformList.each { IEasyTransform easyTransform ->
            easyTransform.onTransformSupport(support)
        }
    }

    /**
     *
     * @param context
     * @param referencedInputs
     * @param outputProvider
     * @param isIncremental
     */
    private void doBeforeTransform(@NonNull Context context,
                                   @Nullable TransformOutputProvider outputProvider,
                                   boolean isIncremental) {
        transformList.each { IEasyTransform easyTransform ->
            easyTransform.onBeforeTransform(context, outputProvider, isIncremental)
        }
    }

    /**
     * 处理jar包之前回调
     */
    private void doBeforeJar() {
        transformList.each { IEasyTransform easyTransform ->
            easyTransform.onBeforeJar()
        }
    }


    /**
     * 增量有缓存，无需处理
     * @param outputs 输出目标文件
     */
    private void doNoChangeJar(JarInput jarInput, File outputs) {
        transformList.each { IEasyTransform easyTransform ->
            easyTransform.onNoChangeJar(jarInput, outputs)
        }
    }

    /**
     * 判断是否需要加压 jar, 有一个需要解压就要解压
     * @param jarInput
     * @param outputFile
     * @return
     */
    private boolean isNeedUnzipJar(JarInput jarInput, File outputFile) {
        for (IEasyTransform easyTransform : transformList) {
            if (easyTransform.isNeedUnzipJar(jarInput, outputFile)) {
                return true
            }
        }
        return false
    }

    /**
     * {@link #isNeedUnzipJar} 返回 true, 解压之后的jar目录
     * @param jarInput
     * @param unzipPath
     * @param outputFile
     * @return 是否处理过了解压后的jar 文件
     */
    private boolean doUnzipJarFile(JarInput jarInput, String unzipPath, File outputFile) {
        boolean result = false
        transformList.each { IEasyTransform easyTransform ->
            if (easyTransform.onUnzipJarFile(jarInput, unzipPath, outputFile)) {
                result = true
            }
        }
        return result
    }

    /**
     * jar文件输出目录，不要在 outputs 的输出目录进行文件操作，如需要解压jar包修改文件，可以参考{@link #doUnzipJarFile}
     * @param outputs 输出目标文件
     */
    private void doEachJarOutput(JarInput jarInput, File outputs) {
        transformList.each { IEasyTransform easyTransform ->
            easyTransform.onEachJarOutput(jarInput, outputs)
        }
    }

    /**
     * 处理jar包之后回调
     */
    private void doAfterJar() {
        transformList.each { IEasyTransform easyTransform ->
            easyTransform.onAfterJar()
        }
    }

    /**
     * 处理Directory包之前回调
     */
    private void doBeforeDirectory() {
        transformList.each { IEasyTransform easyTransform ->
            easyTransform.onBeforeDirectory()
        }
    }

    private void doEachDirectoryOutput(DirectoryInput directoryInput, File outputDirFile) {
        transformList.each { IEasyTransform easyTransform ->
            easyTransform.onEachDirectoryOutput(directoryInput, outputDirFile)
        }
    }

    private void doEachChangeFile(DirectoryInput directoryInput, File outputDirFile,
                                      File outputFile) {
        transformList.each { IEasyTransform easyTransform ->
            easyTransform.onChangeFile(directoryInput, outputDirFile, outputFile)
        }
    }

    /**
     * 处理Directory包之后回调
     */
    private void doAfterDirectory() {
        transformList.each { IEasyTransform easyTransform ->
            easyTransform.onAfterDirectory()
        }
    }

    /**
     * transform 处理结束，可在此方法中remove classPool 中的 classpath
     */
    private void doAfterTransform() {
        transformList.each { IEasyTransform easyTransform ->
            easyTransform.onAfterTransform()
        }
    }

    /**
     * 发生异常时回调，只要有一个返回false，即不处理异常，都会终止编译并输出异常堆栈
     * @param exception
     * @return 是否拦截处理异常
     */
    private boolean onException(Exception exception) {
        boolean interception = true
        transformList.each { IEasyTransform easyTransform ->
            if (!easyTransform.onException(exception)) {
                interception = false
            }
        }
        return interception
    }


    private void checkThrowException() {
        Exception e = null
        exceptionList.each {
            e = it
            e.printStackTrace()
        }
        if (e != null) {
            throwException(e)
        }
        waitableExecutor.cancelAllTasks()
    }

    private void happenException(Exception e) {
        if (!onException(e)) {
            exceptionList.add(e)
        }
    }

    /**
     * 结束回调此方法，无论正常结束或者发生异常都会调到该方法，释放掉ClassPath
     */
    private void onFinally() {
        transformList.each { IEasyTransform easyTransform ->
            easyTransform.onFinally()
        }
    }
}

class ExecutorRunnable<V> implements Callable<V> {

    Callable<V> callable
    Closure exception

    ExecutorRunnable(Callable<V> callable, Closure exception) {
        this.callable = callable
        this.exception = exception
    }

    @Override
    V call() throws Exception {
        try {
            return callable.call()
        } catch(Exception e) {
            exception.call(e)
        }
        return null
    }
}