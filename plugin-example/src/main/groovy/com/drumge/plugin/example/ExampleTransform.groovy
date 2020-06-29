package com.drumge.plugin.example

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.drumge.easy.plugin.api.BaseEasyTransform
import javassist.CtClass
import javassist.CtConstructor
import javassist.CtField
import javassist.CtMethod
import javassist.Modifier
import org.gradle.api.Project

class ExampleTransform extends BaseEasyTransform {

    private static final String TAG = "ExampleTransform"

    private static final String GLIDE_ARTIFACT = 'com.github.bumptech.glide:glide'
    private static final String FACEBOOK_CORE_ARTIFACT = 'com.facebook.android:facebook-core:'

    private String fbUnzip = ""

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
        log(TAG, " isNeedUnzipJar lalalalala  artifact " + artifact)
        if (artifact.startsWith(GLIDE_ARTIFACT)) {
            return true
        } else if (artifact.startsWith(FACEBOOK_CORE_ARTIFACT)) {
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
//            handleGlide(unzipPath)
            return true
        } else if (artifact.startsWith(FACEBOOK_CORE_ARTIFACT)) {
            fbUnzip = unzipPath
            return true
        }
        return false
    }

    @Override
    void onAfterDirectory() {
        super.onAfterDirectory()
//        handler(fbUnzip)
    }

    private static log(String tag, String format, Object... argvs) {
        println("[${tag}]\t${String.format(format, argvs)}")
    }


    private void handler(String unzipPath) {
        def newMethod = '''{
        if ($1 == null || $1.isEmpty()) {
            return;
        }

        // Kill events if kill-switch is enabled
        if (com.facebook.internal.FetchedAppGateKeepersManager.getGateKeeperForKey(
                APP_EVENTS_KILLSWITCH,
                com.facebook.FacebookSdk.getApplicationId(),
                false)) {
            com.facebook.internal.Logger.log(com.facebook.LoggingBehavior.APP_EVENTS, "AppEvents",
                    "KillSwitch is enabled and fail to log app event: " + $1);
            return;
        }
        
        Runnable runnable = new com.facebook.appevents.AppEventRunnable(
                                    this, 
                                    this.contextName,
                                    $1,
                                    $2,
                                    $3,
                                    $4,
                                    com.facebook.appevents.internal.ActivityLifecycleTracker.isInBackground(),
                                    $5,
                                    this.accessTokenAppId);
                                    
        if (com.drumge.easy.plugin.UserInfo.isEventInject) {
            $0.backgroundExecutor.execute(runnable);
        } else {
            runnable.run();
        }
        }'''

        CtClass cls1 = pool.get('com.facebook.internal.Logger')
        log("FbAnrHandler", "cls1 %s", cls1)
        CtClass cls = pool.get('com.facebook.appevents.AppEventsLoggerImpl')
        if (cls.isFrozen()) {
            cls.defrost()
        }
        log("FbAnrHandler", "cls %s", cls)

        CtMethod method
        cls.getDeclaredMethods('logEvent').each {
            it.setModifiers(Modifier.PROTECTED)
            if (it.parameterTypes.size() == 5) {
                method = it
            }
        }
        createEventRunnable(unzipPath)

        method.setBody(newMethod)
        cls.writeFile(unzipPath)
        log("FbAnrHandler", " finish cls %s", cls)
    }

    private void createEventRunnable(String unzipPath) {
        CtClass newClass = pool.makeClass("com.facebook.appevents.AppEventRunnable")
        //设置父类
        newClass.setSuperclass(pool.get("java.lang.Runnable"))
        def imp = pool.get("com.facebook.appevents.AppEventsLoggerImpl")
        def uuid = pool.get("java.util.UUID")
        def pair = pool.get("com.facebook.appevents.AccessTokenAppIdPair")
        def bundle = pool.get("android.os.Bundle")
        def string = pool.get("java.lang.String")
        def dt = pool.get("java.lang.Double")
        def bt = CtClass.booleanType
        newClass.addField(new CtField(imp, "imp", newClass))
        newClass.addField(new CtField(string, "contextName", newClass))
        newClass.addField(new CtField(string, "eventName", newClass))
        newClass.addField(new CtField(dt, "valueToSum", newClass))
        newClass.addField(new CtField(bundle, "parameters", newClass))
        newClass.addField(new CtField(bt, "isImplicitlyLogged", newClass))
        newClass.addField(new CtField(bt, "isInBackground", newClass))
        newClass.addField(new CtField(uuid, "currentSessionId", newClass))
        newClass.addField(new CtField(pair, "accessTokenAppId", newClass))

        CtConstructor constructor = new CtConstructor([imp, string, string, dt, bundle,
                                                       bt, bt, uuid, pair
        ] as CtClass[], newClass)
        constructor.setBody('''{
            this.imp = imp;
            this.contextName = contextName;
            this.eventName = eventName;
            this.valueToSum = valueToSum;
            this.parameters = parameters;
            this.isImplicitlyLogged = isImplicitlyLogged;
            this.isInBackground = isInBackground;
            this.currentSessionId = currentSessionId;
            this.accessTokenAppId  = accessTokenAppId;
            }''')
        newClass.addConstructor(constructor)
        CtMethod run = CtMethod.make('''
        public void run() {
            try {
                com.facebook.appevents.AppEvent event =
                        new com.facebook.appevents.AppEvent(
                                this.contextName,
                                this.eventName,
                                this.valueToSum,
                                this.parameters,
                                this.isImplicitlyLogged,
                                this.isInBackground,
                                this.currentSessionId);
                imp.logEvent(event, this.accessTokenAppId);
            } catch (org.json.JSONException jsonException) {
                // If any of the above failed, just consider this an illegal event.
                com.facebook.internal.Logger.log(com.facebook.LoggingBehavior.APP_EVENTS,
                        "AppEvents",
                        "JSON encoding for app event failed: " + jsonException.toString());

            } catch (com.facebook.FacebookException e) {
                // If any of the above failed, just consider this an illegal event.
                com.facebook.internal.Logger.log(com.facebook.LoggingBehavior.APP_EVENTS,
                        "AppEvents", "Invalid app event: " + e.toString());
            }
        }
        ''', newClass)
        newClass.addMethod(run)
        newClass.writeFile(unzipPath)
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

        println("hhhhhffffdddddddd")
        CtClass test = pool.get("com.bumptech.glide.load.resource.file.Test")
        println(test)
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