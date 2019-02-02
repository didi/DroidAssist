package com.didichuxing.tools.droidassist.util

import org.apache.commons.io.FileUtils

class IOUtils {

    public static final String USER_DIRECTORY_PATH = FileUtils.getUserDirectoryPath()

    static String getPath(File file) {
        return file.path.replace(USER_DIRECTORY_PATH, "~")
    }
}
