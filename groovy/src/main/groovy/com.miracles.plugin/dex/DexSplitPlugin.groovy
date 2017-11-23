package com.miracles.plugin.dex

import com.android.build.gradle.api.ApplicationVariant
import org.codehaus.groovy.runtime.StringGroovyMethods
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * 如果用keep的话请自行保证格式，该插件不会检查keep的格式，只会进行简单的读取操作。
 * 基本的格式要求是空格分开描述性词语
 */
class DexSplitPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.afterEvaluate {
            for (variant in project.android.applicationVariants) {
                def split = check(project, variant)
                if (split) {
                    split.split()
                }
            }
        }
    }

    static BaseSplit check(Project project, ApplicationVariant variant) {
        def match = isTaskMatchVariant(project, variant)
        if (!match) {
            return null
        }
        def plugin = project.plugins.findPlugin("com.android.application")
        if (!plugin) {
            return null
        }
        if (!isMultiDexEnabled(variant)) {
            project.logger.error("DexSplit: plugin isMultiDexEnabled=false!")
            return null
        }
        try {
            String creator = plugin.androidBuilder.getCreatedBy()
            def replace = creator.replaceAll("[^\\d]", "")
            if (Integer.valueOf(replace) >= 300) {
                return new Split300(project, variant)
            }
        } catch (Exception ex) {
            project.logger.error("DexSplit: plugin get split tools failed!check it...", ex)
            return null
        }
    }

    static boolean isMultiDexEnabled(variant) {
        def is = variant.buildType.multiDexEnabled
        if (is != null) {
            return is
        }
        is = variant.mergedFlavor.multiDexEnabled
        if (is != null) {
            return is
        }
        return false
    }

    static boolean isTaskMatchVariant(Project project, ApplicationVariant variant) {
        Gradle gradle = project.getGradle()
        String tskReqStr = gradle.getStartParameter().getTaskRequests().toString()
        Pattern pattern;
        if (tskReqStr.contains("assemble")) {
            println tskReqStr
            pattern = Pattern.compile("assemble(\\w*)(Release|Debug)")
        } else {
            pattern = Pattern.compile("generate(\\w*)(Release|Debug)")
        }
        Matcher matcher = pattern.matcher(tskReqStr)
        if (matcher.find()) {
            String task = matcher.group(0)
            project.logger.error("DexSplit: task is $task, variant Name is ${variant.name}")
            def flavors = matcher.group(1)
            def cName = StringGroovyMethods.capitalize(variant.name)
            def variantType = cName
            def equalsFlavor = true
            if (flavors) {
                if (flavors.length() < cName.length()) {
                    equalsFlavor = flavors == cName.substring(0, flavors.length())
                    variantType = cName.substring(flavors.length())
                }
            }
            def result = equalsFlavor && variantType == matcher.group(2)
            project.logger.error("DexSplit: task ${result ? "will be started if at time." : "will not be started,for ${equalsFlavor ? "variantType" : "flavors"} is not matched!"}")
            return result
        } else {
            return false
        }
    }
}