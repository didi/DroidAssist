package com.didichuxing.tools.droidassist

import com.android.build.api.transform.*
import com.didichuxing.tools.droidassist.tasks.DirInputTask
import com.didichuxing.tools.droidassist.tasks.InputTask
import com.didichuxing.tools.droidassist.tasks.JarInputTask
import com.didichuxing.tools.droidassist.util.GradleUtils
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.commons.io.FileUtils

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Stream

/**
 * process {@link DirectoryInput} and {@link JarInput} parallel
 */
class DroidAssistExecutor {

    static class BuildContext {
        def totalCounter = new AtomicInteger(0)
        def affectedCounter = new AtomicInteger(0)
        File temporaryDir
    }

    TransformOutputProvider outputProvider
    boolean incremental
    DroidAssistContext context
    BuildContext buildContext
    File destCacheFile
    def destCacheMapping = new ConcurrentHashMap<String, String>()

    DroidAssistExecutor(
            DroidAssistContext context,
            TransformOutputProvider outputProvider,
            boolean incremental) {
        this.outputProvider = outputProvider
        this.incremental = incremental
        this.context = context

        def temporaryDir = context.context.temporaryDir
        buildContext = new BuildContext(temporaryDir: temporaryDir)

        def buildDir = context.project.buildDir
        def variant = context.context.variantName
        destCacheFile =
                new File("$buildDir/intermediates/droidAssist/$variant/dest-cache.json")

        if (destCacheFile.exists()) {
            if (incremental) {
                destCacheMapping.putAll(new JsonSlurper().parse(destCacheFile))
            } else {
                FileUtils.forceDelete(destCacheFile)
            }
        }
    }

    void execute(Collection<TransformInput> inputs) {
        def dirStream = inputs.stream()
                .flatMap { it.directoryInputs.stream() }

        def jarStream = inputs.stream()
                .flatMap { it.jarInputs.stream() }

        Stream.concat(dirStream, jarStream)
                .parallel()
                .map { createTask(it) }
                .filter { it != null }
                .forEach { it.run() }

        FileUtils.forceMkdir(destCacheFile.parentFile)
        destCacheFile.write(JsonOutput.toJson(destCacheMapping))
    }

    InputTask createTask(QualifiedContent content) {
        def taskInput =
                new InputTask.TaskInput(
                        input: content,
                        dest: getDestFile(content),
                        incremental: incremental)
        if (content instanceof JarInput) {
            return new JarInputTask(context, buildContext, taskInput)
        }
        if (content instanceof DirectoryInput) {
            return new DirInputTask(context, buildContext, taskInput)
        }
        return null
    }

    File getDestFile(QualifiedContent content) {
        def path = destCacheMapping.get(content.name)
        def buildDir = context.project.buildDir
        File dest = path == null ? null : new File(buildDir, path)
        if (dest == null || !dest.exists()) {
            dest = GradleUtils.getTransformOutputLocation(outputProvider, content)
            destCacheMapping.put(content.name, buildDir.toPath().relativize(dest.toPath()).toString())
        }
        return dest
    }

    int getAffectedCount() {
        return buildContext.affectedCounter.get()
    }

    int getClassCount() {
        return buildContext.totalCounter.get()
    }

}