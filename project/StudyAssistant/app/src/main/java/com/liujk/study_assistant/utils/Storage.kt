package com.liujk.study_assistant.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import com.liujk.study_assistant.TAG
import java.io.*
import java.lang.Exception
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

object Storage {
    fun getDriverDirs(context: Context): ArrayList<File> {
        val hashDirs: HashSet<File> = hashSetOf()
        val driverDirs: ArrayList<File> = arrayListOf()
        val filesSubDir = buildPath( "Android", "data", context.packageName, "files",
            Environment.DIRECTORY_MOVIES)
        val dirs = context.getExternalFilesDirs(Environment.DIRECTORY_MOVIES)
        val funAddToDirs: (file: File)->Unit = {
            Log.v(TAG, "add path '$it' to driver dirs")
            hashDirs.add(it)
            driverDirs.add(it)
        }
        for (dir in dirs) {
            var path = dir.path
            val length = path.length - filesSubDir.length - 1
            path = path.substring(0, length)
            val formatPathFile = File(path).absoluteFile.canonicalFile
            funAddToDirs(formatPathFile)
        }
        try {
            val storageDir = File("/storage")
            if (storageDir.isDirectory) {
                val fileList = storageDir.listFiles()
                if (fileList != null) {
                    for (file in fileList) {
                        if (file.name != "emulated" && file.isDirectory) {
                            val formatPathFile = file.absoluteFile.canonicalFile
                            if (!hashDirs.contains(formatPathFile)) {
                                funAddToDirs(formatPathFile)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "error:", e)
        }
        return driverDirs
    }

    fun getDefaultStorage(): File {
        return File("/sdcard").absoluteFile.canonicalFile
    }

    fun findForDir(context: Context, rootDir: String): List<File> {
        val rootDirs: ArrayList<File> = arrayListOf()

        for (driverDir in getDriverDirs(context)) {
            val file = File(driverDir, rootDir)
            if (file.isDirectory) {
                Log.v(TAG, "add path '${file.path}' to root dirs")
                rootDirs.add(file)
            }
        }

        return rootDirs
    }

    fun findDirsByNames(dir: File, names:List<String>): Pair<List<File>, List<File>> {
        val resDirs = arrayListOf<File>()
        val otherDirs = arrayListOf<File>()
        if (dir.isDirectory) {
            val fileList = dir.listFiles()
            if (fileList != null) {
                for (subPath in fileList) {
                    var isOther = true
                    for (name in names) {
                        if (subPath.path.contains(name, true)) {
                            resDirs.add(subPath)
                            isOther = false
                        }
                    }
                    if (isOther) {
                        otherDirs.add(subPath)
                    }
                }
            }
        }
        return Pair(resDirs, otherDirs)
    }

    fun buildPath(base: String, vararg segments: String) : String {
        var cur:File = File(base)
        for (segment in segments) {
            cur = File(cur, segment);
        }
        return cur.path
    }

    fun writeStringToFile(context: Context, str: String, file: File) {
        try {
            DocumentsUtils.mkdirs(context, file.parentFile ?: File("/"))
            val outputStream = DocumentsUtils.getOutputStream(context, file)
            if (outputStream != null) {
                Log.d(TAG, "Get output stream for file '$file' ok!")
                val outputWriter = OutputStreamWriter(outputStream)
                outputWriter.write(str)
                outputWriter.flush()
                outputWriter.close()
                outputStream.close()
            } else {
                Log.d(TAG, "Get output stream for file '$file' failed!")
            }
        } catch (e: Throwable) {
            Log.d(TAG, "write file '$file' error", e)
        }
    }

    fun writeStringToFile(str: String, file: File) {
        try {
            if (!file.exists()) {
                file.createNewFile()
            }
            val outputStream: OutputStream = FileOutputStream(file)
            val outputWriter = OutputStreamWriter(outputStream)
            outputWriter.write(str)
            outputWriter.flush()
            outputWriter.close()
            outputStream.close()
        } catch (e: Throwable) {
            Log.d(TAG, "write file '$file' error", e)
        }
    }

    fun readStringFromFile(dir: File, fileName: String = ""): String {
        val stringList = readLinesFromFile(dir, fileName)
        var fileStr = ""
        for (str in stringList) {
            fileStr += str + "\n"
        }
        return fileStr.trim()
    }

    fun readLinesFromFile(dir: File, fileName: String = ""): List<String> {
        var file: File = dir
        if (fileName != "") {
            file = File(dir, fileName)
        }

        val lines: ArrayList<String> = arrayListOf()
        try {
            val inputStream: InputStream = FileInputStream(file)
            val inputReader = InputStreamReader(inputStream)
            val bufReader = BufferedReader(inputReader)
            var line: String = ""
            while (bufReader.readLine()?.also { line = it } != null) {
                lines.add(line)
            }
            inputStream.close()
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "File '$file' doesn't not exist.", e)
        } catch (e: IOException) {
            Log.d(TAG, "read file '$file' error", e)
        }
        return lines
    }

    fun isVideo(fileName: String): Boolean {
        return fileName.endsWith(".mp4") ||
                fileName.endsWith(".avi") ||
                fileName.endsWith(".rmvb") ||
                fileName.endsWith(".flv")
    }
}