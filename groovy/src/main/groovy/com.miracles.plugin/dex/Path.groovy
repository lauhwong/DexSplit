package com.miracles.plugin.dex

import org.gradle.api.file.RelativePath
import org.gradle.api.internal.file.pattern.PatternMatcherFactory
import org.gradle.api.specs.Spec
import org.gradle.api.specs.Specs

/**
 * Created by lxw
 */
class Path {
    private RelativePath mRelativePath
    /**
     * is always has no .class symbol..(no other sideEffects)
     * a.b.c.class=>a/b/c
     */
    static String split(String relativePath) {
        def clzIndex = relativePath.indexOf(".class")
        if (clzIndex >= 0) {
            relativePath = relativePath.subSequence(0, clzIndex)
        }
        return relativePath.replaceAll("\\.", "/")
    }

    Path(String relativePath) {
        updatePath(relativePath)
    }

    void updatePath(String relativePath) {
        def path = split(relativePath)
        mRelativePath = RelativePath.parse(true, path)
    }

    RelativePath getRelativePath() {
        return mRelativePath
    }

    static Spec<RelativePath> create(Collection<String> patterns, boolean include, boolean caseSensitive) {
        if (patterns == null || patterns.isEmpty()) {
            return include ? Specs.satisfyAll() : Specs.satisfyNone()
        } else {
            List<Spec<RelativePath>> matchers = new ArrayList(patterns.size())
            patterns.forEach {
                Spec<RelativePath> patternMatcher = PatternMatcherFactory.getPatternMatcher(include, caseSensitive, it)
                matchers.add(patternMatcher)
            }
            def unions = Specs.union(matchers)
            return new Spec<RelativePath>() {
                @Override
                boolean isSatisfiedBy(RelativePath relativePath) {
                    return unions.isSatisfiedBy(relativePath)
                }
            }
        }
    }
}
