package com.didichuxing.tools.droidassist.util

import com.android.build.api.transform.*

class GradleUtils {

    static File getTransformOutputLocation(
            TransformOutputProvider provider,
            QualifiedContent content) {

        if (content instanceof JarInput) {
            return provider.
                    getContentLocation(
                            content.name,
                            content.contentTypes,
                            content.scopes,
                            Format.JAR)
        }
        if (content instanceof DirectoryInput) {
            return provider.
                    getContentLocation(
                            content.name,
                            content.contentTypes,
                            content.scopes,
                            Format.DIRECTORY)
        }
        return null
    }
}