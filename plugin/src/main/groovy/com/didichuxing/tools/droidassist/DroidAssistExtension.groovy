package com.didichuxing.tools.droidassist

import com.google.common.collect.Lists

/**
 * Options container for plugin
 */
class DroidAssistExtension {
    boolean enable = true
    int logLevel = -1
    List<File> configFiles = Lists.newArrayList()
    File logDir
    boolean abortOnUndefinedClass = false
    boolean incremental = true

    List<String> includes = Lists.newArrayList()
    List<String> excludes = Lists.newArrayList()

    void exclude(String... filter) {
        excludes.addAll(filter)
    }

    void include(String... filter) {
        includes.addAll(filter)
    }

    void config(File... file) {
        configFiles.addAll(file)
    }

    List<File> getConfig() {
        return configFiles
    }

    @Override
    String toString() {
        return "\n{" +
                "\n    enable=" + enable +
                "\n    logLevel=" + logLevel +
                "\n    config=" + configFiles +
                "\n    logDir=" + logDir +
                "\n    includes=" + includes +
                "\n    excludes=" + excludes +
                '\n}'
    }
}
