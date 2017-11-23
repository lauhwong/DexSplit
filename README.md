DexSplit
========
定义multiDex中mainDex的分割规则，统一keep和split策略。

Notice:当前插件只是支持Gradle Build 插件版本不低于3.0.0的编译环境

使用
---

1.Maven仓库的SNAPSHOT版本，在buildscript中加入maven快照仓库，并加入classpath 依赖。
```groovy
repositories {
        maven {
           url 'https://oss.sonatype.org/content/repositories/snapshots/'
        }
    }
dependencies {
        classpath 'com.github.lauhwong:dexsplit:1.0.0-SNAPSHOT'
    }
```
2.在application插件中引入dexSplit插件。
```groovy
apply plugin: 'com.miracles.dexsplit'
```
3.在app(application插件的)文件夹根目录下配置增加名字为：dex-split.pro 的配置文件.如：
```pro
#keep语句等同于multiDexKeepProguard，会将keep的保留到mainDex
-keep class **.A {
    *;
}
#split语句用于分割当前可能处于mainDex中的class文件到后面的dex
-split rx.**
-split android.support.**
#option参数等同于DexOption中的addtionalParameters。
-option --minimal-main-dex

```

License
-------
什么都没有。

最后
---
这个以前项目用的插件[DexKnifePlugin](https://github.com/ceabie/DexKnifePlugin)，升级到3.0.0之后插件不支持，就想着自己修改一下然后pr,最后改完发现
改变太大，不得不重新另做一个新的项目。感谢！最后DexKnifePlugin现在已经支持3.0.0编译环境。
