# DroidAssist

`DroidAssist` 是一个轻量级的 Android 字节码编辑插件，基于 `Javassist` 对字节码操作，根据 xml 配置处理 class 文件，以达到对 class 文件进行动态修改的效果。和其他 AOP 方案不同，DroidAssist 提供了一种更加轻量，简单易用，无侵入，可配置化的字节码操作方式，你不需要 Java 字节码的相关知识，只需要在 Xml 插件配置中添加简单的 Java 代码即可实现类似 AOP 的功能，同时不需要引入其他额外的依赖。

> [ Javassist: A Java bytecode engineering toolkit since 1999](http://www.Javassist.org/ "Java bytecode engineering toolkit since 1999")

## 功能
- **替换**：把指定位置代码替换为指定代码
- **插入**：在指定位置的前后插入指定代码
- **环绕**：在指定位置环绕插入指定代码
- **增强**：
	- **TryCatch** 对指定代码添加 try catch 代码
	- **Timing** 对指定代码添加耗时统计代码

## 特点
* 灵活的配置化方式，使得一个配置就可以处理项目中所有的 class 文件。
* 丰富的字节码处理功能，针对 Android 移动端的特点提供了例如代码替换，添加 try catch，方法耗时等功能。
* 简单易用，只需要依赖一个插件，处理过程以及处理后的代码中也不需要添加额外的依赖。
* 处理速度较快，只占用较少的编译时间。

## 开发文档

### DroidAssist 配置文件

DroidAssist 将扫描工程中的每一个单独的 class 以及 jar 中的 class， 并对 class 与配置文件中的规则进行匹配，如果有规则能够匹配到 class，则根据 DroidAssist 配置对此 class 进行字节码修改。
DroidAssist 配置是一个 xml 文件，根节点是 `DroidAssist` , 根节点下包含  `Global` , `Insert` , `Around` , `Replace` , `Enhance` 代码操作配置，完整的 DroidAssist 配置文件格式如下：
```xml
<?xml version="1.0" encoding="utf-8"?>
<DroidAssist>
    <!--全局配置-->
    <Global>
        ...
    </Global>

    <!--代码插入配置-->
    <Insert>
        ...
    </Insert>

    <!--代码环绕配置-->
    <Around>
        ...
    </Around>

    <!--代码替换配置-->
    <Replace>
        ...
    </Replace>

    <!--代码增强配置-->
    <Enhance>
        ...
    </Enhance>
</DroidAssist>
```
> 为了方便编写配置文件，在 IDE 中能自动提示，请将根目录下 [DTD文件](droidassist.dtd) 拷贝到配置文件第二行。


### 配置分类：

- **Insert**：代码插入类
- **Replace**：代码替换类
- **Around**：代码环绕类
- **Enhance**：代码增强类

### Source 和 Target
Insert、Replace、Around、Enhance 类型代码操作配置中均需要包含 `Source` 和 `Target` 元素：
例:
```xml
<Replace>
    <MethodCall>
        <Source>
            int android.util.Log.d(java.lang.String,java.lang.String)
        </Source>
        <Target>
            $_= com.didichuxing.tools.test.LogUtils.log($$);
        </Target>
    </MethodCall>
</Replace>
```
Source 的值 `int android.util.Log.d(java.lang.String,java.lang.String)` 表示需要匹配方法调用 `android.util.Log.d( )`
Target 的值 `$_= com.didichuxing.tools.test.LogUtils.log($$); `表示将调用 `android.util.Log.d( )` 方法调用的代码替换为 `com.didichuxing.tools.test.LogUtils.log( )`

####  Source
表示需要进行修改的代码位置，用以精确匹配代码位置，Source 按照代码位置类型可以分为方法、构造方法、字段、静态初始化块:
##### 1. 方法
Source 表示方法时，格式为 `returnType className.methodName(argType1,argType2)` ：
```xml
 <Source> int android.util.Log.d(java.lang.String,java.lang.String) </Source>
```

##### 2. 构造方法
Source 表示方法时，格式为 `new className(argType1,argType2)` 或者  `className.new(argType1,argType2)`：
```xml
 <Source> new com.didichuxing.tools.test.ExampleSpec(int) </Source>
```
或
```xml
 <Source> com.didichuxing.tools.test.ExampleSpec.new(int) </Source>
```

##### 3. 字段
Source 表示字段时，格式为 `fieldType className.fieldName` ：
```xml
 <Source> int com.didichuxing.tools.test.ExampleSpec.id </Source>
```

##### 4. 静态初始化块
Source 表示静态初始化块时，格式为 `className` ：
```xml
 <Source> com.didichuxing.tools.test.ExampleSpec </Source>
```
> 注意:
> 1. Source 的范围为本类和在子类有效(构造方法和静态初始化块除外)，方法和字段如果在子类中可见，则也会被匹配，如果不需要匹配子类只匹配当前配置类，可在` <Source>`标签中添加 `extend` 属性:`extend = "false"`
> 2. Source 中所有的 class 均需要配置全限定名。
> 3. Source 中 class 如果是内部类，需要使用分隔符 `$` 和外部类隔开，如 `com.didichuxing.tools.test.ExampleSpec$Inner`。


####  Target
需要修改成的目标代码，该值接受一个 Java 表达式或者以大括号`{}`包围的代码块。如果表达式是一个单独的表达式，需要以分号`；`结尾。

例：
```xml
<Target>java.lang.System.out.println("BeforeMethodCall");</Target>
```
或:
```xml
<Target>{System.out.println("Hello"); System.out.println("World");}</Target>
```

> 注意:
>
> 1. 如果 Source 的表达式中 `returnType` 为非 `void` 类型时, Target 中表达式必须 要包含 `$_=` 以保存返回值,否则可能会出现错误。


#####  扩展变量
基于 Javassist 的支持，可以在 Target 中使用语言扩展，`$` 开头的标识符有特殊的含义：

| 符号      |    含义 | scope|
| :-------- | --------| --- |
|`$0`,`$1`,`$2` .. |`this` 和方法的参数       				|runtime |
|`$args`       |方法参数数组，类型为 `Object[]` 				|runtime |
|`$$`          |所有实参, 例如 m(`$$`) 等价于 m(`$1`,`$2`,...)	|runtime |
|`$proceed`    |表示原始的方法、构造方法、字段调用               	|runtime |
|`$cflow(...)` |cflow 变量            						|runtime |
|`$r`  		   |返回结果的类型，用于强制类型转换   				|runtime |
|`$w`          |包装器类型，用于强制类型转换      				|runtime |
|`$_`          |返回值                        				|runtime |
|`$sig`  |参数类型数组，类型为 `java.lang.Class[]` 			|runtime |
|`$type` |返回值类型，类型为 `java.lang.Class`     			|runtime |
|`$class`|表示当前正在修改的类，类型为 `java.lang.Class`			|compile |
|`$line` |表示当前正在修改的行号，类型为 `int`					|compile |
|`$name` |表示当前正在方法或字段名，类型为 `java.lang.String`	|compile |
|`$file` |表示当前正在修改的文件名，类型为 `java.lang.String`	|compile |

> 1. Target 中对于 `java.lang` 包中的类可以直接使用，不用添加包名。
> 2. Around 类型配置中 `Target` 分解为 `TargetBefore` 和 `TargetAfter`
> 3. Scope 为 `compile` 类型的扩展在编译后将直接替换成结果值，`runtime` 类型的扩展只在运行期有效。


### 类过滤器 Filter
默认情况下 DroidAssist 将扫描工程中的每一个 class 并进行匹配和处理，为了加快处理速度以及排除某些不需要处理的类，可以使用类过滤器 Filter 配置将不需要处理的类排除。Filter 配置中包含：

- **Include**：需要处理的类，支持通配符匹配和精确匹配
- **Exclude**：不需要处理的类，支持通配符匹配和精确匹配

每个 Filter 中可以包含多个 Include、Exclude 配置，支持通配符匹配，class 被匹配的条件是类名可以被 Include 规则匹配但是不能被 Exclude 匹配，即 `(Include & !Exclude)` 。

例:
```xml
<Replace>
    <MethodCall>
        <Source>
            int android.util.Log.d(java.lang.String,java.lang.String)
        </Source>
        <Target>
            com.didichuxing.tools.test.LogUtils.log($$);
        </Target>
    </MethodCall>
    <Filter>
        <Include>*</Include>
        <Exclude>com.didichuxing.tools.test.Utils</Exclude>
        <Exclude>android.*</Exclude>
        <Exclude>com.android.*</Exclude>
    </Filter>
</Replace>
```
该配置中的 Filter 中 有1个 Include 配置，值 `*` 表示将处理所有的 class，有 3 个 Exclude 配置表示将不处理`com.didichuxing.tools.test.Utils` 类，以及类名匹配 `android.*` 和 `com.android.*` 的类。

>1. 每一个代码操作配置规则下都可以添加 Filter 配置(可选)
>2. Global 配置中可以包含 Filter，当 Filter 出现在 Global 配置中时，对所有的代码操作配置都生效，如果需要忽略全局 Filter 配置，可在 Filter 标签中添加 ignoreGlobalIncludes="true" 和 ignoreGlobalExcludes="true"
例:
```xml
<Filter ignoreGlobalIncludes="true" ignoreGlobalExcludes="true">
    <Include>*</Include>
    <Exclude>android.*</Exclude>
    <Exclude>com.android.*</Exclude>
</Filter>
```


### Global 配置
Global 配置可以包含类过滤器 Filter：
```xml
<Global>
    <Filter>
        <Include>*</Include>
        <Exclude>android.*</Exclude>
        <Exclude>com.android.*</Exclude>
    </Filter>
</Global>
```

### Replace 配置
Replace 类型代码操作配置的作用是将指定代码替换成目标代码，包含以下配置:
- **MethodCall** 方法调用
- **MethodExecution** 方法体执行
- **ConstructorCall** 构造方法调用
- **ConstructorExecution** 构造方法体执行
- **InitializerExecution** 静态代码初始化块执行
- **FieldRead** 字段读取
- **FieldWrite** 字段赋值

>Call 表示方法或者构造方法被其他代码调用，Execution 代表方法、构造方法或者静态初始化代码块的方法体本身。

例:
```xml
<Replace>
    <MethodCall>
        <Source>
            int android.util.Log.d(java.lang.String,java.lang.String)
        </Source>
        <Target>
            $_= com.didichuxing.tools.test.LogUtils.log($$);
        </Target>
    </MethodCall>
    <ConstructorCall>
        <Source>new com.didichuxing.tools.test.ExampleSpec(int)</Source>
        <Target>{$_= com.didichuxing.tools.test.ExampleSpec.getInstance();}</Target>
    </ConstructorCall>
</Replace>
```


### Insert 配置
Insert 类型代码操作配置的作用是将指定代码之前或之后插入目标代码，包含以下配置:
- **BeforeMethodCall** 方法调用之前
- **AfterMethodCall** 方法调用之后
- **BeforeMethodExecution** 方法体执行之前
- **AfterMethodExecution** 方法体执行之后
- **BeforeConstructorCall** 构造方法体调用之前
- **AfterConstructorCall** 构造方法体调用之后
- **BeforeConstructorExecution** 构造方法体执行之前
- **AfterConstructorExecution** 构造方法体执行之前
- **BeforeInitializerExecution** 静态代码初始化块执行之前
- **AfterInitializerExecution** 静态代码初始化块执行之前
- **BeforeFieldRead** 字段读取之前
- **AfterFieldRead** 字段读取之后
- **BeforeFieldWrite** 字段赋值之前
- **AfterFieldWrite** 字段赋值之后

例:
```xml
<Insert>
    <BeforeMethodCall>
        <Source>void com.didichuxing.tools.test.ExampleSpec.run()</Source>
        <Target>{java.lang.System.out.println("BeforeMethodCall");}</Target>
    </BeforeMethodCall>

    <AfterConstructorExecution>
        <Source>new com.didichuxing.tools.test.ExampleSpec()</Source>
        <Target>java.lang.System.out.println("AfterConstructorExecution");</Target>
    </AfterConstructorExecution>
</Insert>
```

### Around 配置
Around 类型代码操作配置的作用是将指定代码前后环绕插入目标代码，包含以下配置:
- **MethodCall** 方法调用环绕插入代码
- **MethodExecution** 方法体执行环绕插入代码
- **ConstructorCall** 构造方法调用环绕插入代码
- **ConstructorExecution** 构造方法体执行环绕插入代码
- **InitializerExecution** 静态代码初始化块执行环绕插入代码
- **FieldRead** 字段读取环绕插入代码
- **FieldWrite** 字段赋值环绕插入代码

在 Around 类型配置中 Target 配置分解为 `TargetBefore` 和 `TargetAfter`，分别表示 Source 代码之前和之后插入的代码，在 `TargetBefore` 中声明的变量，在 `TargetAfter` 可以直接使用。

例:
```xml
<Around>
    <MethodCall>
        <Source>
            void com.didichuxing.tools.test.ExampleSpec.call()
        </Source>
        <TargetBefore>
            java.lang.System.out.println("around before MethodCall");
        </TargetBefore>
        <TargetAfter>
            java.lang.System.out.println("around after MethodCall");
        </TargetAfter>
    </MethodCall>
</Around>
```

### Enhance 配置
Enhance 类型代码操作配置的作用是加入增强性代码，可以对 Source 代码添加 `TryCatch` 方法和 `Timing` 耗时统计方法 :

#### TryCatch
TryCatch 类型配置可以对 Source 代码添加 `try{...} catch(...){...}`代码，包含以下配置：
- **TryCatchMethodCall** 方法调用添加 Try Catch 代码
- **TryCatchMethodExecution** 方法体执行添加 Try Catch 代码
- **TryCatchConstructorCall** 构造方法调用添加 Try Catch 代码
- **TryCatchConstructorExecution** 构造方法体执行添加 Try Catch 代码
- **TryCatchInitializerExecution** 静态代码初始化块执行添加 Try Catch 代码

##### Exception
TryCatch 配置默认将捕获 `java.lang.Exception` 类型异常，如果需要捕获其他异常，需要添加 `Exception` 配置，声明需要捕获的异常，在 Target 表达式中可以使用 `$e` 扩展变量接收捕获的异常对象。

例：
```xml
<TryCatchMethodCall>
    <Source>
        void android.content.Context.startActivity(android.content.Intent)
    </Source>
    <Exception>
        android.content.ActivityNotFoundException
    </Exception>
    <Target>
        android.util.Log.d("test", "startActivity error", $e);
    </Target>
</TryCatchMethodCall>
```

#### Timing
Timing 类型配置可以对 Source 代码添加耗时统计代码，包含以下配置：
- **TimingMethodCall** 方法调用添加耗时统计代码
- **TimingMethodExecution** 方法体执行耗时统计代码
- **TimingConstructorCall** 构造方法调用耗时统计代码
- **TimingConstructorExecution** 构造方法体执行耗时统计代码
- **TimingInitializerExecution** 静态代码初始化块执行耗时统计代码

Timing 类型配置会自动在 Source 代码前后添加耗时计算代码，并将耗时毫秒值保存到 `$time` 扩展变量中，可以在 Target 配置中直接使用该扩展变量。
例:
```xml
<TimingMethodExecution>
    <Source>void com.didichuxing.tools.test.ExampleSpec.timing()</Source>
    <Target>
        android.util.Log.d("test", "time cost= "+ $time);
    </Target>
</TimingMethodExecution>
```
> `$time`  扩展变量为 `long` 型，单位为毫秒，如果需要获取耗时的微秒值，可以使用 `$nanotime` 扩展变量。


#### Reparent
Reparent 类型配置可以重新设置制定 class 的父类
- **ReparentClass** 重新设置类的父类型

Reparent 类型配置将指定的类型( Source 中配置的类型)的直接子类的父类型设置到另外一个类型( Target 中配置的类型)。
例:
```xml
<ReparentClass>
    <Source>com.didichuxing.tools.test.Parent</Source>
    <Target>com.didichuxing.tools.test.ReParent</Target>
</ReparentClass>
```
上面例子中 class 'com.didichuxing.tools.test.Parent' 的直接子类在处理后父类型将被设置为 'com.didichuxing.tools.test.ReParent'

> 使用此配置时需要注意 Source 类和 Target 类的 api 兼容性，需要注意子类中构造方法、方法、字段在重新设置到指定的父类后还能否有正确的继承关系。


## Q & A

#### 1. DroidAssist 可以实现什么功能？

DroidAssist 提供了一套轻量级的字节码操作方案，可以轻易实现诸如代码替换，代码插入等功能，滴滴出行APP 目前利用DroidAssist 实现了日志输出替换，系统 SharedPreferences 替换，SharedPreferences commit 替换 apply，Dialog show 保护，获取 deviceId 接口替换，getPackageInfo 接口替换，getSystemService 接口替换，startActivity 保护，匿名线程重命名，线程池创建监控，主线程卡顿监控，文件夹创建监控，Activity 生命周期耗时统计，APP启动耗时统计等功能。

#### 2. DroidAssist 和 AspectJ 有什么区别？

DroidAssist 采用配置化方案，编写相关配置就可以实现 AOP 的功能，可以完全不用修改 Java 代码，DroidAssist 使用比较简单，不需要复杂的注解配置，DroidAssist 可以比较方便的实现 AspectJ 不容易实现的代码替换功能。