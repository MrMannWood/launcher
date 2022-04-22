package com.mrmannwood.hexlauncher.timber

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.database.sqlite.SQLiteException
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import com.mrmannwood.hexlauncher.file.copyContentsTo
import timber.log.Timber
import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class FileLoggerTree private constructor(context: Context) : Timber.Tree() {

    companion object {
        const val LOGS_DIRECTORY = "logs"
        private const val TAG = "FileLoggerTree"
        private const val MAX_LOG_FILES = 3
        private const val MAX_LOGS_IN_MEMORY = 100
        private const val MAX_LOGS_IN_FILE = 1000
        private const val LOG_FILE_SUFFIX = "txt"
        private const val MESSAGE_GOT_LOG = 1
        private const val MESSAGE_FLUSH = 2
        private const val MESSAGE_COPY_LOGS = 3

        private var INSTANCE: FileLoggerTree? = null

        fun getAndInit(context: Context): FileLoggerTree {
            if (INSTANCE == null) {
                INSTANCE = FileLoggerTree(context)
            }
            return INSTANCE!!
        }

        fun get(): FileLoggerTree {
            return INSTANCE!!
        }
    }

    private var enableDiskFlush = AtomicBoolean(false)

    fun enableDiskFlush() {
        enableDiskFlush.set(true)
    }

    fun disableDiskFlush() {
        enableDiskFlush.set(false)
    }

    fun copyLogsTo(out: File, callback: () -> Unit) {
        flush()
        handler.sendMessage(handler.obtainMessage(MESSAGE_COPY_LOGS, Pair(out, callback)))
    }

    private fun flush() {
        handler.sendMessage(handler.obtainMessage(MESSAGE_FLUSH))
    }

    override fun isLoggable(tag: String?, priority: Int) = true

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        handler.sendMessage(handler.obtainMessage(MESSAGE_GOT_LOG, Log(Date(), priority, tag, message, t)))
    }

    private val handler = object : Handler(HandlerThread("LoggerThread").apply { start() }.looper) {

        private val logs = ArrayList<Log>(MAX_LOGS_IN_MEMORY)

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_GOT_LOG -> log(context, msg.obj as Log)
                MESSAGE_FLUSH -> flush(context)
                MESSAGE_COPY_LOGS -> {
                    val (out: File, callback) = msg.obj as Pair<File, () -> Unit>
                    copyLogs(context, out, callback)
                }
            }
        }

        private fun log(context: Context, log: Log) {
            logs.add(log)
            when {
                isAtLeastWarn(log.priority) -> flush(context)
                logs.size >= MAX_LOGS_IN_MEMORY -> flush(context)
                else -> {
                    sendMessageDelayed(obtainMessage(MESSAGE_FLUSH), 30_000)
                }
            }
        }

        private fun flush(context: Context) {
            removeMessages(MESSAGE_FLUSH)
            if (logs.isEmpty()) {
                return
            }
            if (!enableDiskFlush.get()) {
                logs.clear()
                return
            }

            val logTimeStampFormat = SimpleDateFormat("MM dd 'at' HH:mm:ss:SSS", Locale.US)

            var (linesInFile, out) = openLastFile(context)?.let { file ->
                val lines = countLinesInFile(file)
                if (lines < MAX_LOGS_IN_FILE) {
                    Pair(lines, OutputStreamWriter(FileOutputStream(file, true)))
                } else {
                    Pair(0, openNewFile(context))
                }
            } ?: run {
                Pair(0, openNewFile(context))
            }

            try {
                logs.forEach { log ->
                    if (linesInFile >= MAX_LOGS_IN_FILE) {
                        out.close()
                        out = openNewFile(context)
                        linesInFile = 0
                    }
                    out.append("${logTimeStampFormat.format(log.date)} ${toPriorityString(log.priority)}/${log.tag}: ${log.message}\n")
                    linesInFile++
                }
            } catch (e: IOException) {
                logError("Unable to write to output file", e)
            } finally {
                try {
                    out.close()
                } catch (e: IOException) { /* ignore */ }
            }

            logs.clear()
        }

        private fun copyLogs(context: Context, out: File, callback: () -> Unit) {
            try {
                openLogsFolder(context)?.copyContentsTo(out)
            } catch (e: IOException) {
                logError("Unable to copy logs to ${out.absoluteFile}", e)
            }
            Handler(Looper.getMainLooper()).post {
                callback()
            }
        }

        private fun countLinesInFile(file: File): Int {
            return BufferedReader(InputStreamReader(FileInputStream(file))).use { reader ->
                var count = 0
                while (true) {
                    val line = reader.readLine() ?: break
                    if (!line.startsWith(" ")) {
                        count++
                    }
                }
                count
            }
        }

        private fun openLastFile(context: Context): File? {
            val fileNameFormat = getFileNameDateFormat()
            return openLogsFolder(context)?.listFiles()
                ?.filter { it.name.endsWith(LOG_FILE_SUFFIX) }
                ?.maxByOrNull { fileNameFormat.parse(it.nameWithoutExtension)?.time!! }
        }

        private fun openNewFile(context: Context): OutputStreamWriter {
            val logsDir = openLogsFolder(context) ?: return OutputStreamWriter(NoOpOutputStream)
            deleteOldLogFiles(logsDir)

            return OutputStreamWriter(
                try {
                    FileOutputStream(
                        File(logsDir, "${getFileNameDateFormat().format(Date())}.$LOG_FILE_SUFFIX")
                    )
                } catch (e: FileNotFoundException) {
                    logError("Unable to open output stream", e)
                    NoOpOutputStream
                }
            )
        }

        private fun openLogsFolder(context: Context): File? {
            return try {
                context.getDir(LOGS_DIRECTORY, MODE_PRIVATE)
            } catch (e: SQLiteException) {
                logError("Cannot open logs directory", e)
                null
            }
        }

        private fun deleteOldLogFiles(logsDir: File) {
            val numFiles = logsDir.listFiles()
                ?.filter { it.name.endsWith(LOG_FILE_SUFFIX) }
                ?.size
                ?: 0
            if (numFiles >= MAX_LOG_FILES) {
                val fileNameFormat = getFileNameDateFormat()
                logsDir.listFiles()
                    ?.filter { it.name.endsWith(LOG_FILE_SUFFIX) }
                    ?.sortedBy { fileNameFormat.parse(it.nameWithoutExtension)?.time!! }
                    ?.take(numFiles - MAX_LOG_FILES)
                    ?.forEach { it.delete() }
            }
        }

        private fun getFileNameDateFormat(): DateFormat {
            val df = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US)
            df.timeZone = TimeZone.getTimeZone("UTC")
            return df
        }

        private fun logError(message: String, e: Exception?) {
            android.util.Log.e(TAG, message, e)
        }
    }
}
