package com.drumge.plugin.example

import com.android.build.api.transform.JarInput
import com.drumge.easy.plugin.api.BaseEasyTransform
import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project

class ExampleTransform extends BaseEasyTransform {

    private static final String TAG = "ExampleTransform"

    private static final String GLIDE_ARTIFACT = 'com.github.bumptech.glide:glide'


    ExampleTransform(Project project) {
        super(project)
        println()
        println('=========== ExampleTransform =========')
        println()

        project.afterEvaluate {
            ExampleExtend extend = project.easy_plugin.plugins.example.extend
            extend.infos.each {
                println(it.name + ', ' + it.infoVersion)
            }
        }
        appendClassPath(project.android.bootClasspath[0].toString())
    }

    @Override
    void onBeforeJar() {
        super.onBeforeJar()
        println('onBeforeJar')
    }

    @Override
    void onAfterTransform() {
        super.onAfterTransform()
        println('onAfterTransform')

    }

    @Override
    boolean isNeedUnzipJar(JarInput jarInput, File outputFile) {
        String artifact = jarInput.name
        if (artifact.startsWith(GLIDE_ARTIFACT)) {
            log(TAG, " isNeedUnzipJar lalalalala  artifact " + artifact)
            return true
        }
        return false
    }

    @Override
    boolean onUnzipJarFile(JarInput jarInput, String unzipPath, File outputFile) {
        appendClassPath(unzipPath)
        String artifact = jarInput.name
        if (artifact.startsWith(GLIDE_ARTIFACT)) {
            handleGlide(unzipPath)
            return true
        }
        return false
    }

    private static log(String tag, String format, Object... argvs) {
        println("[${tag}]\t${String.format(format, argvs)}")
    }

    private void handleGlide(String unzipPath) {
        CtClass cls = pool.get("com.bumptech.glide.load.resource.file.FileToStreamDecoder")
        if (cls.isFrozen()) {
            cls.defrost()
        }

        CtMethod _decode = createDecodeMethod(cls)
        cls.addMethod(_decode)

        CtMethod decode = cls.getDeclaredMethod("decode")
        String body ='''return $0._decode($0.fileOpener, $0.streamDecoder, $1, $2, $3);'''
        decode.setBody(body)
        cls.writeFile(unzipPath)
    }

    private CtMethod createDecodeMethod(CtClass cls) {
        pool.importPackage('com.bumptech.glide.load.ResourceDecoder')
        pool.importPackage('com.bumptech.glide.load.engine.Resource')
        pool.importPackage('java.io.File')
        pool.importPackage('java.io.FileInputStream')
        pool.importPackage('java.io.FileNotFoundException')
        pool.importPackage('java.io.IOException')
        pool.importPackage('java.io.InputStream')

        pool.importPackage('com.bumptech.glide.load.resource.bitmap.BitmapResource')
        pool.importPackage('android.graphics.BitmapFactory')
        pool.importPackage('java.lang.IllegalArgumentException')
        pool.importPackage('android.graphics.Bitmap')
        pool.importPackage('android.graphics.Bitmap.Config')
        pool.importPackage('com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool')
        pool.importPackage('com.bumptech.glide.load.engine.bitmap_recycle.BitmapPoolAdapter')

        CtMethod decode = CtMethod.make('''
        private Resource _decode(com.bumptech.glide.load.resource.file.FileToStreamDecoder.FileOpener fileOpener, ResourceDecoder streamDecoder, File source, int width, int height) throws IOException {
            InputStream is = null;
            Resource result = null;
            try {
                is = fileOpener.open(source);
                try {
                    result = streamDecoder.decode(is, width, height);
                } catch (java.lang.IllegalArgumentException e) {
                    String errorInfo = e != null ? e.toString() : "";
                    if (source != null && errorInfo != null && errorInfo.contains("Problem decoding into existing bitmap")) {
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException closeE) {
                                // Do nothing.
                            }
                        }
    
                        is = fileOpener.open(source);
                        android.graphics.BitmapFactory.Options opts = new android.graphics.BitmapFactory.Options();
                        opts.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(source.getAbsolutePath(), opts);
                        Bitmap bitmap = null;
                        if (opts.outWidth <= 0 || opts.outHeight <= 0) {
                            bitmap = BitmapFactory.decodeStream(is);
                        } else {
                            opts.inJustDecodeBounds = false;
                            if (width < opts.outWidth || height < opts.outHeight) {
                                if (width == 0 || height == 0) {
                                    opts.inSampleSize = 5;
                                } else if (width < opts.outWidth){
                                    opts.inSampleSize = opts.outWidth / width;
                                } else {
                                    opts.inSampleSize = opts.outHeight / height;
                                }
                            } else {
                                opts.inSampleSize = 1;
                            }
    
                            bitmap = BitmapFactory.decodeStream(is, null, opts);
                        }
    
                        if (bitmap == null) {
                            throw new IOException("Bitmap empty!");
                        }
                        
                        BitmapResource resource = new BitmapResource(bitmap, new BitmapPoolAdapter());
                        return resource;
                    } else {
                        throw e;
                    }
                }
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // Do nothing.
                    }
                }
            }
            return result;
        }''', cls)
        return decode
    }
}