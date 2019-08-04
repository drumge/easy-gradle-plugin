# easy-gradle-plugin

* github 项目地址 [https://github.com/drumge/easy-gradle-plugin](https://github.com/drumge/easy-gradle-plugin)
* 这是一款让你更容易使用 android gradle 自定义插件的插件。可以帮助开发者省去了解 gradle 脚本，groovy 学习成本，只关注需求的实现逻辑，完全可以使用 Java 语言来实现自己的 gradle 插件。

* 使用 easy-gradle-plugin 可以很方便的自定义 transform 不用关心任何发布等相关繁琐的操作，直接继承 BaseEasyTransform 类并配置插件 transform = new ExampleTransform(project)  即可。

* 当项目中有多个自定义的 gradle 插件时，每个插件自定义的 transform 操作 jar 库，可能存在每个 transform 都会解压 jar 包并压缩 jar 包等，这些操作是很耗时的，有多个插件时，编译速度变慢是硬伤。使用 easy-gradle-plugin 可以配置多个插件，每个插件可以对应一个 transform， 但是不需要多次解压、压缩的过程，只会有一次。

* 对于不了解 gradle 的开发者来说开发自定义 gradle 插件还是需要了解一些相关的知识的，至少得对 gradle 脚本编译有初步的了解，并且了解 groovy 语言的使用，因为 gradle 脚本和自定义插件都是使用 groovy 来开发的。这个时候 easy-gradle-plugin 可以帮开发者更容易的实现自己的 gradle 插件。  

* 大部分情况下 android 应用开发者并不需要了解 gradle 的使用及原理，平时使用最多的可能就是 dependencies 添加一个依赖库了。如果需要对项目做 AOP 切面编程或者一些特殊的需求，可能就需要使用到 gradle 自定义插件了。例如比较受开发者深受喜欢的EventBus, Butterknife 都使用了自定义 gradle 插件来在编译期间生成一些辅助的类以及插入代码实现相关的功能，可以避免在运行期间使用反射。一般来说框架中使用了反射都可以考虑使用 AOP 思想来在编译期间生成代码来避免反射来实现相同的功能，并且使用更加便捷。

## gradle 配置

* 在 android 项目根目录的 build.gralde 中配置
```groovy
 buildscript {

    repositories {
        maven{ url uri('https://oss.sonatype.org/content/groups/staging')}
    }
    dependencies {
        // 添加 easy-gradle-plugin 插件, 主要是为了 apply plugin: 'com.drumge.easy.plugin'
        classpath "com.github.drumge:easy-plugin:0.0.2"
    }
 }

 allprojects {
    repositories {
        maven{ url uri('https://oss.sonatype.org/content/groups/staging')}
    }
 }
```
* 在 application 所在的 build.gradle 中配置
```groovy
apply plugin: 'com.drumge.easy.plugin'
easy_plugin {
    enable = true

    plugins{
        // 包含多个插件，每个插件名字可以自定义，只是插件内部可能会关系名字，比如 example
    }
}
```
以上就是所有使用 easy-gradle-plugin 的全部配置。实现自己的插件之后，并配置以上的两个地方就可以愉快的使用自定义 gradle 插件了。

## 示例演示说明

* 创建本地插件
 1. 在项目中创建一个 Java Library 类型 module，可参考 plugin-example 和 java-plugin-example。  
 2. 在创建 module 下的 build.gradle dependencies 中加入依赖 `` implementation "com.github.drumge:easy-plugin-api:0.3.0" ``。
 3. 在 build.gradle 最后加入 ``apply from: "${rootDir.absolutePath}/build_scrip/plugin_build.gradle" `` 使用 plugin_build.gradle 脚本中自定义的 task， 包含 buildPlugin 和 cleanPlugin，分别编译本地插件和清除插件。
 4. 根据需要创建类，可分别实现 easy-plugin-api 中的接口类，或者继承抽象类并实现需要的接口。
 5. 执行 buildPlugin 生成插件 jar 并默认拷贝到 rootDir/plugin_libs 目录，生成目录可在 plugin_build.gradle 脚本中手动修改。
 6. 在根目录 build.gradle 添加本地插件依赖的 classpath
 ```groovy
    buildscript {
        dependencies {
            // 添加本地插件依赖， 其中 plugin_libs 目录可以在 plugin_build.gradle 中手动修改
            classpath fileTree(dir: 'plugin_libs', include: ['*.jar'])
        }
    }
 ```
 7. 在 application module 下的 build.gradle 中添加插件配置，以项目中的 example-plugin 和 java-example-plugin 为例
 ```groovy
     apply plugin: 'com.drumge.easy.plugin'

     import com.drumge.plugin.example.ExampleExtend
     import com.drumge.plugin.example.ExamplePlugin
     import com.drumge.plugin.example.ExampleTransform
     import com.drumge.plugn.example.java.JavaPlugin
     import com.drumge.plugn.example.java.JavaExtend
     import com.drumge.plugn.example.java.JavaTransform

     easy_plugin {
         enable = true
         plugins{
             example { // 使用 groovy 语言实现的插件，详细请看 plugin-example module
                 plugin = new ExamplePlugin(project)
                 // 可自定义功能丰富的 extend
                 extend = ExampleExtend.createExtend(project) { ExampleExtend extend ->
                     extend.enable = true
                     extend.infos {
                         info1 {
                             infoVersion = '0.0.1'
                         }
                         info2 {
                             infoVersion = '0.0.2'
                         }
                         info3 {
                             infoVersion = '0.0.3'
                         }
                     }
                 }
                 // 自定义 transform
                 transform = new ExampleTransform(project)
             }

             java_example { // 使用 Java 语言实现的插件，详细请看 java-plugin-example module
                 plugin = new JavaPlugin(project)
                 // 可自定义功能丰富的 extend
                 extend = JavaExtend.createExtend(project) { JavaExtend extend ->
                     extend.enable = true
                     extend.infos {
                         java_info1 {
                             infoVersion = '0.0.1'
                         }
                         java_info2 {
                             infoVersion = '0.0.2'
                         }
                         java_info3 {
                             infoVersion = '0.0.3'
                         }
                     }
                 }
                 transform = new JavaTransform(project)
             }
         }
     }
 ```

* 创建远程插件

 1. 创建远程插件是指，本地生成插件之后上传到 maven 远程仓库，并通过 dependencies 依赖远程仓库的方式导入 classpath。  
 2. 创建步骤跟本地插件是一样的，不同的地方在于，不是使用 buildPlugin 来生成插件，需要通过发布 maven 仓库相关的步骤。这里就不展开讲，需者可自行搜索，资料还是挺多的。  
 3. 发布到 maven 仓库之后，需要在根目录下的 build.gradle ``buildscript { repositories { }} `` 中添加 maven 库的地址。接着像其他的插件一样添加 classpath 即可，如 `` classpath "com.github.drumge:kvo-plugin:0.2.4" ``。
 4. application module 下的 build.gradle 配置同本地插件是一样的
 ```groovy
    apply plugin: 'com.drumge.easy.plugin'

    import com.drumge.kvo.plugin.KvoPlugin
    import com.drumge.kvo.plugin.KvoTransform
    easy_plugin {
        enable = true
        plugins{
            kvo { // 使用了 easy-gradle-plugin 的实践项目，详情可跳转 https://github.com/drumge/kvo.git 了解
                plugin = new KvoPlugin(project)
                transform = new KvoTransform(project)
            }
        }
    }
 ```

## 接口说明

* 接口的使用可以参考 example-plugin 和 java-example-plugin, 或者 [kvo库](https://github.com/drumge/kvo.git)  

* 下边简单举例说明一下
```groovy
easy_plugin {
    plugins {
        exmple { // 名字可自定义
            // 以下的 plugin， extend， transform 名字不能改，并且分别对应着指定的类型， 这些参数的接口类定义在 easy-plugin-api 中。plugin -> IPlugin， extend -> IExtend, transform -> IEasyTransform
            // plugin， extend， ransform 三者并不是必须的，可以只实现其中的任何一个
            plugin = new ExamplePlugin(project) // class ExamplePlugin implements IPlugin
            extend = new ExampleExtend() // class ExampleExtend implements IExtend
            transform = new ExampleTransform(project) // class ExampleTransform extends BaseEasyTransform
        }
    }
}
```

## 实战项目
* Android KVO 参考了 iOS 中的思想使用在 Android 系统上实现一套属性改变自动通知的框架，结合预编译手段实现了简单使用注解就可以很方便的使用 KVO。
github 项目地址 [https://github.com/drumge/kvo](https://github.com/drumge/kvo)
