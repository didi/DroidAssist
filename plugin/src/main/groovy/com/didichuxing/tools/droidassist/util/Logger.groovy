package com.didichuxing.tools.droidassist.util

import java.text.SimpleDateFormat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.logging.ConsoleHandler
import java.util.logging.FileHandler
import java.util.logging.Level
import java.util.logging.LogRecord

/**
 * Plugin logger
 */
class Logger {
    public static final String TAG = "DroidAssist: "
    public static final String LOG_FILE_PREFIX = "droidassist-"

    private static java.util.logging.Logger sLogger =
            java.util.logging.Logger.getLogger("DroidAssist")
    private static ExecutorService sExecutor

    public static final int LEVEL_CONSOLE = 1
    public static final int LEVEL_FILE = 2
    public static final int LEVEL_ALL = 3

    static {
        sLogger.handlers.each {
            sLogger.removeHandler(it)
        }
    }

    static void init(int level, File dir) {
        sLogger.handlers.each {
            sLogger.removeHandler(it)
        }
        sExecutor = Executors.newSingleThreadExecutor()

        def consoleOutput = false
        def fileOutput = false
        if (level == LEVEL_CONSOLE) {
            consoleOutput = true
        }
        if (level == LEVEL_FILE) {
            fileOutput = true
        }
        if (level >= LEVEL_ALL) {
            consoleOutput = true
            fileOutput = true
        }

        if (consoleOutput) {
            def consoleHandler = new ConsoleHandler().with {
                setLevel(Level.ALL)
                setFormatter({ record ->
                    def thrown = getThrownString(record)
                    return record.message + thrown + "\n"
                })
                return it
            }
            sLogger.addHandler(consoleHandler)
        }

        if (fileOutput) {
            dir.mkdirs()
            def format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
            def ts = format.format(Calendar.getInstance().getTime())
            def file = new File(dir, LOG_FILE_PREFIX + ts + ".log")
            def fileHandler = new FileHandler(file.absolutePath).with {
                setLevel(Level.ALL)
                return it
            }
            fileHandler.setFormatter {
                record ->
                    def logTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    def time = logTimeFormatter.format(Calendar.getInstance().getTime())
                    def message = record.getMessage()
                    message = message.substring(18, message.length() - 3)
                    def thrown = getThrownString(record)
                    return time + " " + message + thrown + "\n"
            }
            sLogger.addHandler(fileHandler)
        }
    }

    static String getThrownString(LogRecord record) {
        def thrown = ""
        if (record.getThrown() != null) {
            StringWriter stringWriter = new StringWriter()
            PrintWriter printWriter = new PrintWriter(stringWriter)
            record.getThrown().printStackTrace(printWriter)
            printWriter.close()
            thrown = stringWriter.toString()
        }
        return thrown
    }

    static void close() {
        sExecutor.shutdown()
        sExecutor.awaitTermination(60, TimeUnit.SECONDS)
        sLogger.handlers.each { it.close() }
    }

    static void debug(String msg) {
        sExecutor.execute { sLogger.log(Level.INFO, "\033[36m" + TAG + msg + " \033[0m") }
    }

    static void info(String msg) {
        sExecutor.execute { sLogger.log(Level.INFO, "\033[34m" + TAG + msg + " \033[0m") }
    }

    static void warning(String msg) {
        sExecutor.execute { sLogger.log(Level.INFO, "\033[33m" + TAG + msg + " \033[0m") }
    }

    static void error(String msg, Throwable err) {
        sLogger.log(Level.INFO, "\033[31m" + TAG + msg + " \033[0m", err)
    }

    static void debug(String tag, String msg) {
        sExecutor.execute {
            sLogger.log(Level.INFO, "\033[36m" + TAG + tag + ": " + msg + " \033[0m")
        }
    }

    static void info(String tag, String msg) {
        sExecutor.execute {
            sLogger.log(Level.INFO, "\033[34m" + TAG + tag + ": " + msg + " \033[0m")
        }
    }

    static void warning(String tag, String msg) {
        sExecutor.execute {
            sLogger.log(Level.INFO, "\033[33m" + TAG + tag + ": " + msg + " \033[0m")
        }
    }

    static void warning(String tag, String msg, Throwable err) {
        sLogger.log(Level.INFO, "\033[33m" + TAG + tag + ": " + msg + " \033[0m", err)
    }

    static void warning(String msg, Throwable err) {
        sLogger.log(Level.INFO, "\033[33m" + TAG + msg + " \033[0m", err)
    }

    static void error(String tag, String msg) {
        sExecutor.execute {
            sLogger.log(Level.INFO, "\033[31m" + TAG + tag + ": " + msg + " \033[0m")
        }
    }

    static void error(String tag, String msg, Throwable err) {

        sLogger.log(Level.INFO, "\033[31m" + TAG + tag + ": " + msg + " \033[0m", err)

    }
}
