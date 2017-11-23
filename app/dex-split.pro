#keep的优先级小于split
-keep class **.A {
    *;
}
#优先级高于keep
-split rx.**
#-split android.support.**
#dexOption 空格分开
-option --minimal-main-dex