package com.drumge.easy.plugin

import com.android.annotations.NonNull
import com.android.annotations.Nullable
import com.android.build.api.transform.*
import com.android.ide.common.internal.WaitableExecutor
import com.drumge.easy.plugin.api.IEasyPluginContainer
import com.drumge.easy.plugin.api.IEasyTransform
import com.drumge.easy.plugin.api.IEasyTransformSupport
import com.drumge.easy.plugin.extend.EasyExtend
import com.drumge.easy.plugin.extend.EasyPluginContainer
import com.drumge.easy.plugin.utils.JarZipUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

class EasyTransform extends Transform implements IEasyTransformSupport {

    private final Project project
    private EasyPluginContainer extend
    private boolean isEnable = true
    private final List<IEasyTransform> transformList = new ArrayList<>()
    private final Map<String, String> allUnzipJars = new HashMap<>()
    // 暂存解压并被修改的 jar 文件，在最后在压缩
    private final Map<String, String> unzipJars = new HashMap<>()

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
        try {
            doTransform(transformInvocation)
        } catch (Exception exception) {
            if (!onException(exception)) { // 不处理异常，则终止编译并输出异常堆栈
//                exception.printStackTrace()
                throw new RuntimeException("some error happened when building, look up more detail ahead", exception)
            }
            exception.printStackTrace()
        } finally {
            onFinally()
        }
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

    private void doTransform(TransformInvocation transformInvocation) {
        Context context = transformInvocation.getContext()
        Collection<TransformInput> inputs = transformInvocation.getInputs()
        Collection<TransformInput> referencedInputs = transformInvocation.getReferencedInputs()
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider()
        boolean isIncremental = transformInvocation.isIncremental()

        setSupport(this)

        /** 为避免多次遍历，解压，压缩等，如需添加transform处理,请继承 BaseEasyTransform，并重写相关的方法进行 */
        doBeforeTransform(context, outputProvider, isIncremental)

        doBeforeJar()
        String tmpDirPath = context.temporaryDir.absolutePath
//        println("context " + context.variantName + " , " + context.temporaryDir + " , " + context.path)
        WaitableExecutor waitableExecutor = WaitableExecutor.useGlobalSharedThreadPool()
        inputs.each { TransformInput input ->
            input.jarInputs.each { JarInput jarInput ->
//                println("jarInput " + jarInput)
                waitableExecutor.execute {
                    File output = handleEachJarInput(outputProvider, jarInput, tmpDirPath)
                    doEachJarOutput(jarInput, output)
                }
            }
        }
        waitableExecutor.waitForTasksWithQuickFail(true)
        doAfterJar()

        doBeforeDirectory()
        inputs.each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput directoryInput ->
//                println("directoryInput " + directoryInput)
                waitableExecutor.execute {
                    File output = handleEachDirInput(outputProvider, directoryInput)
                    doEachDirectoryOutput(directoryInput, output)
                }
            }
        }
        waitableExecutor.waitForTasksWithQuickFail(true)
        doAfterDirectory()

        doAfterTransform()
        zipTmpJar()
    }

    private File handleEachDirInput(TransformOutputProvider outputProvider, DirectoryInput directoryInput) {
        File output = outputProvider.getContentLocation(directoryInput.name,
                directoryInput.contentTypes, directoryInput.scopes,
                Format.DIRECTORY)
        FileUtils.copyDirectory(directoryInput.file, output)
        return output
    }

    private File handleEachJarInput(TransformOutputProvider outputProvider, JarInput jarInput, String tmpDirPath) {
        File output = outputProvider.getContentLocation(jarInput.name,
                jarInput.contentTypes, jarInput.scopes, Format.JAR)
        if (isNeedUnzipJar(jarInput, output)) { // 需要解压 jar
            String tmpPath = "${tmpDirPath}${File.separator}${jarInput.name.replace(':', '')}${File.separator}"
            JarZipUtils.unzipJar(jarInput.file.absolutePath, tmpPath)
            allUnzipJars.put(tmpPath, output.absolutePath)
            if (doUnzipJarFile(jarInput, tmpPath, output)) { // 需要压缩
                unzipJars.put(tmpPath, output.absolutePath)
            }
        }
        FileUtils.copyFile(jarInput.file, output)
        return output
    }

    private void zipTmpJar() {
        WaitableExecutor waitableExecutor = WaitableExecutor.useGlobalSharedThreadPool()
        unzipJars.each { String tmp, String out ->
            if (out != '') {
                waitableExecutor.execute {
                    JarZipUtils.zipJar(tmp, out)
                }
            }

        }
        waitableExecutor.waitForTasksWithQuickFail(true)
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

    /**
     * 处理代码目录，一般指工程中的Java代码文件
     * 可直接在 output 目录中修改文件
     * @param outputs 输出目标文件
     */
    private void doEachDirectoryOutput(DirectoryInput directoryInput, File outputs) {
//        println("doEachDirectoryOutput " + directoryInput + ' , outputs ' + outputs + ' , transformList ' + transformList)
        transformList.each { IEasyTransform easyTransform ->
            easyTransform.onEachDirectoryOutput(directoryInput, outputs)
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

    /**
     * 结束回调此方法，无论正常结束或者发生异常都会调到该方法，释放掉ClassPath
     */
    private void onFinally() {
        transformList.each { IEasyTransform easyTransform ->
            easyTransform.onFinally()
        }
    }
}