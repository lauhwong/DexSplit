package com.miracles.plugin.dex

import proguard.WordReader

/**
 * Created by lxw
 */
class SplitParser {
    private final WordReader mReader
    private String mNextWord
    private boolean mNextReader

    SplitParser(WordReader wordReader) {
        this.mReader = wordReader
        readNextWord()
    }

    private void readNextWord() throws IOException {
        readNextWord(false)
    }

    private void readNextWord(boolean isFileName) throws IOException {
        mNextWord = mReader.nextWord(isFileName);
    }
    /**
     * Note:simple read your config,without any check.
     */
    void parse(SplitConfig splitConfig) {
        Set<String> splits = new HashSet<>()
        Set<String> options = new HashSet<>()
        Set<String> keeps = new HashSet<>()
        while (mNextWord) {
            if ("-split".startsWith(mNextWord)) {
                parseSplit(splits)
            } else if ("-option".startsWith(mNextWord)) {
                parseOptions(options)
            } else if ("-keep".startsWith(mNextWord)) {
                parseKeep(keeps)
            }
            if (mNextWord && !mNextReader) {
                readNextWord()
            }
        }
        mReader.close()
        DefaultConfig.add2Options(options)
        splitConfig.splits = splits
        splitConfig.options = options
        splitConfig.keeps = keeps

    }

    private Set<String> parseSplit(Set<String> splits) {
        return simpleRead(splits, false) {
            Path.split(it)
        }
    }

    private Set<String> parseKeep(Set<String> keeps) {
        return simpleRead(keeps, false, null)
    }

    private Set<String> parseOptions(Set<String> options) {
        return simpleRead(options, true, null)
    }
    /**
     * simple read...
     */
    private Set<String> simpleRead(Set<String> results, boolean split, callback) {
        if (results == null) {
            results = new HashSet<>()
        }
        readNextWord()
        StringBuilder sb = new StringBuilder()
        while (mNextWord) {
            if (mNextWord.startsWith("-split") || mNextWord.startsWith("-keep") || mNextWord.startsWith("-option")) {
                mNextReader = true
                break
            }
            String read = callback != null ? callback(mNextWord) : mNextWord
            if (split) {
                results.add(read)
            } else {
                sb.append(read)
                sb.append(" ")
            }
            readNextWord()
        }
        if (sb.length() > 0) {
            results.add(sb.substring(0, sb.length() - 1))
        }
        return results
    }
}
