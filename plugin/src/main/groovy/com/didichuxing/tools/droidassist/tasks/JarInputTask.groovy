package com.didichuxing.tools.droidassist.tasks

import com.android.build.api.transform.JarInput
import com.android.build.api.transform.Status
import com.didichuxing.tools.droidassist.DroidAssistContext
import com.didichuxing.tools.droidassist.DroidAssistExecutor.BuildContext
import com.didichuxing.tools.droidassist.util.IOUtils
import com.didichuxing.tools.droidassist.util.Logger
import com.didichuxing.tools.droidassist.util.ZipUtils
import org.apache.commons.io.FileUtils

class JarInputTask extends InputTask<JarInput> {

    JarInputTask(
            DroidAssistContext context,
            BuildContext buildContext,
            TaskInput<JarInput> taskInput) {
        super(context, buildContext, taskInput)
    }

    @Override
    String getInputType() {
        return "jar"
    }

    /**
     * If jar is changed, reprocess jar and otherwise skip.
     *
     * <p> See for details {@link InputTask#executeClass}
     */
    void execute() {
        JarInput input = taskInput.input
        File inputJar = input.file
        if (taskInput.incremental) {
            if (input.status != Status.NOTCHANGED) {
                Logger.info("Jar incremental build: \ninput:${ taskInput.input.name} \npath: ${IOUtils.getPath(inputJar)} \ndest:${taskInput.dest} \nstatus:${input.status}")
                FileUtils.deleteQuietly(taskInput.dest)
            } else {
                Logger.info("${IOUtils.getPath(inputJar)} not changed, skip.")
                return
            }
        }

        def written = false
        ZipUtils.collectAllClassesFromJar(inputJar).forEach {
            written = executeClass(it, temporaryDir) || written
        }
        if (written) {
            ZipUtils.zipAppend(inputJar, taskInput.dest, temporaryDir)
        } else {
            FileUtils.copyFile(inputJar, taskInput.dest)
        }
    }
}
