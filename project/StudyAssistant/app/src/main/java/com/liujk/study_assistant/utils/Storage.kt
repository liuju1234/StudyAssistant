package com.liujk.study_assistant.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import com.liujk.study_assistant.TAG
import com.liujk.study_assistant.utils.DocumentsUtils
import java.io.*
import java.lang.Exception

object Storage {
    fun getDriverDirs(context: Context): ArrayList<File> {
        val hashDirs: HashSet<String> = hashSetOf()
        val driverDirs: ArrayList<File> = arrayListOf()
        val subdir = buildPath( "Android", "data", context.packageName, "files",
            Environment.DIRECTORY_MOVIES)
        val dirs = context.getExternalFilesDirs(Environment.DIRECTORY_MOVIES)
        for (dir in dirs) {
            var path = dir.path
            val length = path.length - subdir.length - 1
            path = path.substring(0, length)
            Log.v(TAG, "add path '$path' to driver dirs")
            hashDirs.add(path)
            driverDirs.add(File(path))
        }
        try {
            val storageDir = File("/storage")
            if (storageDir.isDirectory) {
                val fileList = storageDir.listFiles()
                if (fileList != null) {
                    for (file in fileList) {
                        if (file.name != "emulated" && file.isDirectory) {
                            if (!hashDirs.contains(file.path)) {
                                hashDirs.add(file.path)
                                Log.v(TAG, "add path '${file.path}' to driver dirs")
                                driverDirs.add(file)
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

    fun findDirsByNames(dir: File, names:List<String>): List<File> {
        val resDirs = arrayListOf<File>()
        if (dir.isDirectory) {
            val fileList = dir.listFiles()
            if (fileList != null) {
                for (subPath in fileList) {
                    for (name in names) {
                        if (subPath.path.contains(name, true)) {
                            resDirs.add(subPath)
                        }
                    }
                }
            }
        }
        return resDirs
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