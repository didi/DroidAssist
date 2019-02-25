package com.didichuxing.tools.droidassist

import com.android.build.api.transform.*
import com.didichuxing.tools.droidassist.tasks.DirInputTask
import com.didichuxing.tools.droidassist.tasks.InputTask
import com.didichuxing.tools.droidassist.tasks.JarInputTask
import com.didichuxing.tools.droidassist.util.GradleUtils

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

    DroidAssistExecutor(
            DroidAssistContext context,
            TransformOutputProvider outputProvider,
            boolean incremental) {
        this.outputProvider = outputProvider
        this.incremental = incremental
        this.context = context

        def dir = context.context.temporaryDir
        buildContext = new BuildContext(temporaryDir: dir)
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
    }

    InputTask createTask(QualifiedContent content) {
        def taskInput =
                new InputTask.TaskInput(
                        input: content,
                        dest: GradleUtils.getTransformOutputLocation(outputProvider, content),
                        incremental: incremental)
        if (content instanceof JarInput) {
            return new JarInputTask(context, buildContext, taskInput)
        }
        if (content instanceof DirectoryInput) {
            return new DirInputTask(context, buildContext, taskInput)
        }
        return null
    }

    int getAffectedCount() {
        return buildContext.affectedCounter.get()
    }

    int getClassCount() {
        return buildContext.totalCounter.get()
    }
}