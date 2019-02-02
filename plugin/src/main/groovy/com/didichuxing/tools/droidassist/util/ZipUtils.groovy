package com.didichuxing.tools.droidassist.util

import org.apache.commons.io.FilenameUtils
import org.apache.tools.ant.NoBannerLogger
import org.apache.tools.ant.Project
import org.apache.tools.ant.ProjectHelper

import java.nio.file.Files
import java.util.stream.Collectors
import java.util.stream.Stream
import java.util.zip.ZipFile

class ZipUtils {

    static AntBuilder ant
    public static final String DOT_CLASS = '.class'

    static {
        def project = new Project()
        def helper = ProjectHelper.getProjectHelper()
        project.addReference(ProjectHelper.PROJECTHELPER_REFERENCE, helper)
        helper.getImportStack().addElement("AntBuilder") // checks that stack is not empty
        def logger = new NoBannerLogger()
        logger.setMessageOutputLevel(Project.MSG_ERR)
        logger.setOutputPrintStream(System.out)
        logger.setErrorPrintStream(System.err)
        project.addBuildListener(logger)
        project.init()
        ant = new AntBuilder(project)
    }

    static Stream<String> collectAllClassesFromJar(File jar) {
        return new ZipFile(jar)
                .stream()
                .parallel()
                .filter { !it.isDirectory() }
                .map { it.name }
                .filter { it.endsWith(DOT_CLASS) }
                .map { it.replace(File.separator, ".") }
                .map { FilenameUtils.removeExtension(it) }
    }

    static void zip(String baseDir, String destFile) {
        ant.zip(destfile: destFile, basedir: baseDir)
    }

    static void zipAppend(File srcFile, File destFile, File dir) {
        ant.zip(destfile: destFile.absolutePath) {
            zipfileset(dir: dir.absolutePath)
            def basedirPath = dir.toPath()
            def excludes = Files.walk(dir.toPath())
                    .parallel()
                    .map { basedirPath.relativize(it).toString() }
                    .collect(Collectors.joining(","))
            zipfileset(src: srcFile.absolutePath, excludes: excludes)
        }
    }
}