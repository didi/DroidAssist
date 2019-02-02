package com.didichuxing.tools.droidassist

import com.android.build.api.transform.Context
import com.android.build.api.transform.TransformInput
import com.didichuxing.tools.droidassist.util.IOUtils
import com.didichuxing.tools.droidassist.util.Logger
import javassist.ClassPool
import org.gradle.api.Project

import java.util.stream.Stream

/**
 * Context for plugin build env
 */
class DroidAssistContext {

    Context context
    Project project
    DroidAssistConfiguration configuration
    ClassPool classPool
    DroidAssistExtension extension
    Collection<TransformInput> referencedInputs

    DroidAssistContext(
            Context context,
            Project project,
            DroidAssistExtension extension,
            Collection<TransformInput> referencedInputs) {
        this.context = context
        this.project = project
        this.extension = extension
        this.referencedInputs = referencedInputs

        configuration = new DroidAssistConfiguration(project)
    }

    def configure() {
        try {
            createClassPool()
        } catch (Throwable e) {
            Logger.error("Create class pool error", e)
            throw e
        }

        try {
            loadConfiguration()
        } catch (Throwable e) {
            Logger.error("Load configuration error", e)
            throw e
        }
    }

    def loadConfiguration() {
        Logger.info "Dump transformers:"
        configuration.parserFrom(extension.config).each {
            transformer ->
                transformer.classFilterSpec.addIncludes(extension.includes)
                transformer.classFilterSpec.addExcludes(extension.excludes)
                transformer.setClassPool(classPool)
                transformer.setAbortOnUndefinedClass(extension.abortOnUndefinedClass)
                transformer.check()
                Logger.info "transformer: ${transformer}"
        }
    }

    def createClassPool() {
        classPool = new DroidAssistClassPool()
        classPool.appendBootClasspath(project.android.bootClasspath)

        def dirStream = referencedInputs
                .parallelStream()
                .flatMap { it.directoryInputs.parallelStream() }
                .filter { it.file.exists() }

        def jarStream = referencedInputs
                .parallelStream()
                .flatMap { it.jarInputs.parallelStream() }
                .filter { it.file.exists() }

        Stream.concat(dirStream, jarStream).forEach {
            Logger.info("Append classpath: ${IOUtils.getPath(it.file)}")
            classPool.appendClassPath(it.file)
        }
    }

    class DroidAssistClassPool extends ClassPool {
        DroidAssistClassPool() {
            super(true)
            childFirstLookup = true
        }

        void appendBootClasspath(Collection<File> paths) {
            paths.stream().parallel().forEach {
                appendClassPath(it)
                Logger.info "Append boot classpath: ${IOUtils.getPath(it)}"
            }
        }

        void appendClassPath(File path) {
            appendClassPath(path.absolutePath)
        }
    }
}