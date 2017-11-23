package com.miracles.plugin.dex

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.pipeline.TransformTask
import org.gradle.api.Project

/**
 * Created by lxw
 */
class Split300 extends BaseSplit {

    Split300(Project project, ApplicationVariant variant) {
        super(project, variant)
    }

    @Override
    protected void doSplit() {
        def tasks = mProject.tasks
        //dexOptions
        tasks.matching {
            (it instanceof TransformTask) && "dex".equalsIgnoreCase(it.transform.name)
        }.each {
            it.doFirst {
                def dexOptions = transform.hasProperty("dexOptions")
                if (dexOptions) {
                    def option = dexOptions.getProperty(transform)
                    def additionalParameters = option.getAdditionalParameters()
                    if (additionalParameters == null) {
                        additionalParameters = new ArrayList<>()
                    }
                    getSplitConfig().options.each {
                        additionalParameters.add(it)
                    }
                    option.setAdditionalParameters(additionalParameters)
                } else {
                    println(" no dexOptions got from dexTransform !")
                }
            }
        }
        //multi-dexList
        tasks.matching {
            (it instanceof TransformTask) && "multidexlist".equalsIgnoreCase(it.transform.name)
        }.each {
            //add keep info
            it.doFirst {
                try {
                    def transform = it.transform
                    def methods = transform.respondsTo("keep", String.class)
                    if (!methods.isEmpty()) {
                        if (getSplitConfig().keeps == null || getSplitConfig().keeps.isEmpty()) {
                            println("no keep for multidexlist transform!")
                        } else {
                            println("ready to add keep rules....")
                            StringBuilder sb = new StringBuilder()
                            getSplitConfig().keeps.each {
                                methods[0].invoke(transform, it)
                                sb.append(it)
                                sb.append("\r\n")
                            }
                            if (sb.length() > 0) {
                                println("rules is : ${sb.toString()}")
                            }
                            println("keep rules add success!!!")

                        }
                    } else {
                        println("can't get keep method from multidexlist transform!")
                    }
                } catch (Exception ignored) {
                    println("before multidexlist add -keep exception!")
                }
            }
            //add split info
            it.doLast {
                def ideMainList = (it.transform).mainDexListFile
                parseAndRewrite(ideMainList, mVariant.getMappingFile(), getSplitConfig())
            }
        }

    }
}
