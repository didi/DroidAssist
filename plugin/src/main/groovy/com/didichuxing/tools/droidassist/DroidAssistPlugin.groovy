package com.didichuxing.tools.droidassist

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin entrance
 */
class DroidAssistPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.extensions.create("droidAssistOptions", DroidAssistExtension)

        if (project.plugins.hasPlugin(AppPlugin.class)) {
            AppExtension extension = project.extensions.getByType(AppExtension)
            extension.registerTransform(
                    new DroidAssistTransform(project, true))
        }
        if (project.plugins.hasPlugin(LibraryPlugin.class)) {
            LibraryExtension extension = project.extensions.getByType(LibraryExtension)
            extension.registerTransform(
                    new DroidAssistTransform(project, false))
        }
    }
}
