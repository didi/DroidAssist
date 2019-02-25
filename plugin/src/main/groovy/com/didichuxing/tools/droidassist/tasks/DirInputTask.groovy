package com.didichuxing.tools.droidassist.tasks

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Status
import com.didichuxing.tools.droidassist.DroidAssistContext
import com.didichuxing.tools.droidassist.DroidAssistExecutor.BuildContext
import com.didichuxing.tools.droidassist.util.WorkerExecutor
import com.google.common.collect.Lists
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

class DirInputTask extends InputTask<DirectoryInput> {

    DirInputTask(
            DroidAssistContext context,
            BuildContext buildContext,
            TaskInput<DirectoryInput> taskInput) {
        super(context, buildContext, taskInput)
    }

    @Override
    String getInputType() {
        return "dir"
    }

    /**
     * process DirectoryInput
     */
    void execute() {
        DirectoryInput input = taskInput.input
        def inputDir = input.file
        def executor = new WorkerExecutor(1)
        List<File> files = Lists.newArrayList()

        if (taskInput.incremental) {
            //process changedFiles in incremental mode.
            //if file is changed or removed, delete corresponding cache.
            //if file is changed or added, add file to pending collections.
            input.changedFiles.each {
                file, status ->
                    if (status == Status.CHANGED || status == Status.REMOVED) {
                        def cache = getDestFileMapping(file, inputDir, taskInput.dest)
                        if (cache != null) {
                            FileUtils.deleteQuietly(cache)
                        }
                    }
                    if (status == Status.CHANGED || status == Status.ADDED) {
                        files << file
                    }
            }
        } else {
            //process every class file in Non-incremental mode
            executor.execute {
                FileUtils.copyDirectory(inputDir, taskInput.dest)
            }

            def fileList = Files.walk(inputDir.toPath())
                    .parallel()
                    .map { it.toFile() }//Path to file
                    .filter { it.isFile() }
                    .filter { it.name.endsWith(DOT_CLASS) } //Filter class file
                    .collect(Collectors.toList())

            files.addAll(fileList)
        }

        files.stream()
                .filter { it.isFile() }//Path to file
                .filter { it.name.endsWith(DOT_CLASS) }//Filter class file
                .forEach { executeClass(it, inputDir, temporaryDir) }

        executor.execute {
            FileUtils.copyDirectory(temporaryDir, taskInput.dest)
        }

        executor.finish()
    }

    void executeClass(File classFile, File inputDir, File tempDir) {
        def className =
                FilenameUtils.
                        removeExtension(
                                inputDir.toPath()
                                        .relativize(classFile.toPath())
                                        .toString())
                        .replace(File.separator, '.')
        executeClass(className, tempDir)
    }

    static File getDestFileMapping(File file, File baseDir, File destDir) {
        if (file == null || !file.exists() || destDir == null || !destDir.exists()) {
            return null
        }
        Path relativePath = baseDir.toPath().relativize(file.toPath())
        Paths.get(destDir.absolutePath, relativePath.toString()).toFile()
    }
}
