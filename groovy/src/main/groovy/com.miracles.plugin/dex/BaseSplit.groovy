package com.miracles.plugin.dex

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.scope.VariantScope
import org.gradle.api.Project
import proguard.FileWordReader

/**
 * Created by lxw
 */
abstract class BaseSplit {
    protected Project mProject
    protected ApplicationVariant mVariant
    protected SplitConfig mSplitConfig

    BaseSplit(Project project, ApplicationVariant variant) {
        this.mProject = project
        this.mVariant = variant
    }
    /**
     * get user configuration split file
     */
    File splitFile() {
        return mProject.file("dex-split.pro")
    }

    protected void println(content) {
        mProject.logger.error("DexSplit: ${content}")
    }
    /**
     * split by configuration
     */
    void split() {
        VariantScope variantScope = mVariant.getVariantData().getScope()
        if (isInInstantRunMode(variantScope)) {
            println("Instant Run mode, DexSplit is auto disabled!")
            return
        }
        if (isInTestingMode(mVariant)) {
            println("Testing mode, DexSplit is auto disabled!")
            return
        }
        def spf = splitFile()
        if (!spf) {
            println("no dex-split.pro find !base split is not work!")
            return
        }
        doSplit()
    }

    protected SplitConfig getSplitConfig() {
        if (mSplitConfig) {
            return mSplitConfig
        }
        println("start parse config for ${mVariant.name}")
        def config = new SplitConfig()
        def parser = new SplitParser(new FileWordReader(splitFile()))
        parser.parse(config)
        println("end of parse config for ${mVariant.name}")
        mSplitConfig = config
        return mSplitConfig
    }

    private static boolean isInInstantRunMode(VariantScope scope) {
        try {
            def instantRunBuildContext = scope.getInstantRunBuildContext()
            return instantRunBuildContext.isInInstantRunMode()
        } catch (Throwable ignored) {
        }
        return false
    }

    private static boolean isInTestingMode(ApplicationVariant variant) {
        return (variant.getVariantData().getType().isForTesting());
    }

    protected abstract void doSplit()
    /**
     * parse predefine split file，rewrite mainList to replace old。
     * @param ideMainList multi-dex default file
     * @param clzMapping release mapping
     * @param splitFile config from user.
     */
    protected void parseAndRewrite(File ideMainList, File clzMapping, SplitConfig config) {
        def includeSpec = config.getAsSplitSpec()
        Set<String> ideList = new HashSet<>()
        //step1:read ide main list
        println("read ide main list")
        readFile(ideMainList) {
            ideList.add(Path.split(it))
        }
        //step2:read mappings
        println("read mappings")
        Map<String, String> proguardMapping = new HashMap<>()
        readFile(clzMapping) {
            if (it.endsWith(":")) {
                String mapping = it.substring(0, it.length() - 1)
                def splits = mapping.split("->")
                if (splits.length == 2) {
                    def guard = Path.split(splits[1].replaceAll(" ", ""))
                    def noGuard = Path.split(splits[0].replaceAll(" ", ""))
                    proguardMapping.put(guard, noGuard)
                }
            }
        }

        Path element
        //step3:filter
        println("filter main list")
        for (int i = ideList.size() - 1; i >= 0; i--) {
            String idePath = ideList.getAt(i)
            String elementPath = idePath;
            if (!proguardMapping.isEmpty()) {
                elementPath = proguardMapping.get(elementPath)
            }
            if (elementPath == null) {
                continue
            }
            if (element == null) {
                element = new Path(elementPath)
            } else {
                element.updatePath(elementPath)
            }
            if (includeSpec.isSatisfiedBy(element.getRelativePath())) {
                ideList.remove(idePath)
            }
        }
        //step4:rewrite file
        println("rewrite main list file")
        writeClassFile(ideMainList, ideList)
        //step5:copy to project's root dir
        println("copy to project's root dir")
        mProject.copy {
            from ideMainList
            into mProject.projectDir
        }
        println("end!!!")
    }

    static void writeClassFile(File file, Set<String> msgList) {
        if (file == null || !file.exists()) return
        def writer = null
        try {
            writer = new BufferedWriter(new FileWriter(file))
            for (String msg : msgList) {
                writer.writeLine(msg + ".class")
            }
            writer.flush()
            close(writer)
        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            close(writer)
        }

    }

    static void readFile(File file, callback) {
        if (file == null || !file.exists()) return
        def reader = null
        try {
            reader = new BufferedReader(new FileReader(file))
            def line
            while ((line = reader.readLine()) != null) {
                callback(line)
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            close(reader)
        }

    }

    static void close(Closeable... closeables) {
        if (closeables == null) return
        for (Closeable closeable : closeables) {
            try {
                if (closeable != null) {
                    closeable.close()
                }
            } catch (Exception ignored) {
            }
        }
    }
}
