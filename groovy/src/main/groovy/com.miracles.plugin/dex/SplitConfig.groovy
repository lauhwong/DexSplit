package com.miracles.plugin.dex

import org.gradle.api.file.RelativePath
import org.gradle.api.specs.Spec

/**
 * Created by lxw
 */
class SplitConfig {
    Set<String> splits
    Set<String> options
    Set<String> keeps

    Spec<RelativePath> getAsSplitSpec() {
        return Path.create(splits, true, true)
    }
}
