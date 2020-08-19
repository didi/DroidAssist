
# DroidAssist ![license](http://img.shields.io/badge/license-Apache2.0-brightgreen.svg?style=flat) ![Release Version](https://img.shields.io/badge/release-1.1.1-blue.svg)
----------
`DroidAssist` 是一个轻量级的 Android 字节码编辑插件，基于 `Javassist` 对字节码操作，根据 xml 配置处理 class 文件，以达到对 class 文件进行动态修改的效果。和其他 AOP 方案不同，DroidAssist 提供了一种更加轻量，简单易用，无侵入，可配置化的字节码操作方式，你不需要 Java 字节码的相关知识，只需要在 Xml 插件配置中添加简单的 Java 代码即可实现类似 AOP 的功能，同时不需要引入其他额外的依赖。

> [**Javassist**: A Java bytecode engineering toolkit since 1999](http://www.Javassist.org/ "Java bytecode engineering toolkit since 1999")

[English](README_EN.md)

## 功能
- **替换**：把指定位置代码替换为指定代码
- **插入**：在指定位置的前后插入指定代码
- **环绕**：在指定位置环绕插入指定代码
- **增强**：
	- **TryCatch** 对指定代码添加 try catch 代码
	- **Timing** 对指定代码添加耗时统计代码

## 特点
* 灵活的配置化方式，使得一个配置就可以处理项目中所有的 class 文件。
* 丰富的字节码处理功能，针对 Android 移动端的特点提供了例如代码替换，添加try catch，方法耗时等功能。
* 简单易用，只需要依赖一个插件，处理过程以及处理后的代码中也不需要添加额外的依赖。
* 处理速度较快，只占用较少的编译时间。

## 使用指南

DroidAssist 适用于 `Android Studio` 工程 `application model` 或者 `library model`，使用 DroidAssist 需要接入 DroidAssist 插件并编写专有配置文件。

在 root project 的  `build.gradle`  里添加：

```groovy
dependencies {
    classpath "com.didichuxing.tools:droidassist:1.1.1"
}
```

在需要处理的 model project 的 build.gradle 里添加：

```groovy
apply plugin: 'com.didichuxing.tools.droidassist'
droidAssistOptions {
    config file("droidassist.xml"),file("droidassist2.xml") //插件配置文件(必选配置,支持多配置文件)
}
```

其他配置：
* `enable` 如果需要停用 DroidAssist 插件功能，可以添加 `enable false` 以停用插件 (可选配置)
* `logLevel` 日志输出等级：`0` 关闭日志输出，`1` 输出日志到控制台 `2` 输出日志到文件 `3` 输出日志到控制台以及日志 (可选配置)
* `logDir` 日志输出目录，当日志输出到文件时，默认的输出目录是当前 `model` 的 `build/outputs/logs` 目录 (可选配置)

## 示例

下面例子将把项目中所有使用系统 `android.util.Log` 类进行 `DEBUG` 日志输出的代码替换为自定义的日志输出类，以方便对线上日志进行策略化，动态化管理。
```xml
<Replace>
    <MethodCall>
        <Source>
           int android.util.Log.d(java.lang.String,java.lang.String)
        </Source>
        <Target>
            $_=com.didichuxing.tools.test.LogUtils.log($1,$2);
        </Target>
    </MethodCall>
</Replace>
```

处理前的class：

```java
public class MainActivity extends Activity {
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity onCreate");
    }
}
```

处理后的 class：

```java
public class MainActivity extends Activity {
    public static final String TAG = "MainActivity";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String var2 = "MainActivity";
	    String var3 = "MainActivity onCreate";
        int var4 = LogUtils.log(var2, var3); // The target method using custom log method.
    }
}
```

## 完整文档
完整开发文档和配置见 [开发文档wiki](docs/wiki.md)

## 局限

1. 由于 Javassist 的机制，DroidAssist 在处理的过程中将会产生额外的局部变量用以指向参数变量和保存返回值，但处理后有一些局部变量并没有实际作用。
2. DroidAssist 在处理某些代码时可能会新增一些额外的代理方法。
3. DroidAssist 插件用于 `library model`  只能处理 Java 源码产生的 class，不能处理本地依赖中的 jar 。