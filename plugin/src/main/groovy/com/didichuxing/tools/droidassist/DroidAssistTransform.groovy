package com.didichuxing.tools.droidassist

import com.android.annotations.NonNull
import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.didichuxing.tools.droidassist.util.GradleUtils
import com.didichuxing.tools.droidassist.util.Logger
import com.didichuxing.tools.droidassist.util.TimingLogger
import com.google.common.collect.Lists
import com.google.common.collect.Sets
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

import java.util.stream.Stream

/**
 * DroidAssist Transformer
 */
class DroidAssistTransform extends Transform {

    Project project
    boolean application
    DroidAssistExtension gradleExtension

    DroidAssistTransform(Project project, boolean application) {
        this.project = project
        this.application = application
        gradleExtension = this.project.droidAssistOptions
    }

    @Override
    String getName() {
        return "droidAssist"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS //all classes
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return application ?
                TransformManager.SCOPE_FULL_PROJECT
                : Sets.immutableEnumSet(QualifiedContent.Scope.PROJECT)
    }

    /**
     * classpath needed
     */
    @Override
    Set<? super QualifiedContent.Scope> getReferencedScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return gradleExtension.incremental
    }

    /**
     * Magic here, Changes to the plugin config file will trigger a non incremental execution
     */
    @Override
    Collection<SecondaryFile> getSecondaryFiles() {
        Objects.requireNonNull(gradleExtension.config)
        return Lists.newArrayList(
                SecondaryFile.nonIncremental(project.files(gradleExtension.config)))
    }

    void transform(@NonNull TransformInvocation invocation)
            throws TransformException, InterruptedException, IOException {
        try {
            def logLevel = gradleExtension.logLevel
            Logger.init(
                    logLevel < 0 ? Logger.LEVEL_CONSOLE : logLevel,
                    gradleExtension.logDir ?: project.file("${project.buildDir}/outputs/logs/"))

            onTransform(
                    invocation.getContext(),
                    invocation.getInputs(),
                    invocation.getReferencedInputs(),
                    invocation.getOutputProvider(),
                    invocation.isIncremental())
        } catch (Throwable e) {
            Logger.error("Build failed with an exception: ${e.cause?.message}", e)
            e.fillInStackTrace()
            throw e
        } finally {
            Logger.close()
        }
    }

    /**
     * When droidAssist is enable, process files and write them to an output folder
     *
     * <p> {@link DroidAssistExecutor#execute} process files specifically
     */
    void onTransform(
            Context gradleContext,
            Collection<TransformInput> inputs,
            Collection<TransformInput> referencedInputs,
            TransformOutputProvider outputProvider,
            boolean isIncremental)
            throws IOException, TransformException, InterruptedException {

        Logger.info("Transform start, " +
                "enable:${gradleExtension.enable}, " +
                "incremental:${isIncremental}")

        // If droidAssist is disable, just copy the input folder to the output folder
        if (!gradleExtension.enable) {
            outputProvider.deleteAll()
            def dirStream = inputs
                    .parallelStream()
                    .flatMap { it.directoryInputs.parallelStream() }
                    .filter { it.file.exists() }

            def jarStream = inputs
                    .parallelStream()
                    .flatMap { it.jarInputs.parallelStream() }
                    .filter { it.file.exists() }

            Stream.concat(dirStream, jarStream).forEach {
                def copy = it.file.isFile() ? "copyFile" : "copyDirectory"
                FileUtils."$copy"(
                        it.file,
                        GradleUtils.getTransformOutputLocation(outputProvider, it))
            }
            return
        }

        def start = System.currentTimeMillis()
        Logger.info("DroidAssist options: ${gradleExtension}")
        def timingLogger = new TimingLogger("Timing", "execute")

        //Delete output folder and reprocess files, when it is not incremental
        if (!isIncremental) {
            outputProvider.deleteAll()
            timingLogger.addSplit("delete output")
        }

        def context =
                new DroidAssistContext(
                        gradleContext,
                        project,
                        gradleExtension,
                        referencedInputs)
        context.configure()
        timingLogger.addSplit("configure context")

        def executor =
                new DroidAssistExecutor(
                        context,
                        outputProvider,
                        isIncremental)
        timingLogger.addSplit("create executor")

        //Execute all input classed with byte code operation transformers
        executor.execute(inputs)
        timingLogger.addSplit("execute inputs")

        timingLogger.dumpToLog()
        Logger.info("Transform end, " +
                "input classes count:${executor.classCount}, " +
                "affected classes:${executor.affectedCount}, " +
                "time use:${System.currentTimeMillis() - start} ms")
    }
}