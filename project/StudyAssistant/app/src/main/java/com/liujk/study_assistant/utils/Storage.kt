package com.liujk.study_assistant.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import com.liujk.study_assistant.TAG
import java.io.*
import java.lang.Exception

class Storage {
    companion object {
        fun getDriverDirs(context: Context): ArrayList<File> {
            var hashDirs: HashSet<String> = hashSetOf()
            var driverDirs: ArrayList<File> = arrayListOf()
            val subdir = buildPath( "Android", "data", context.packageName, "files",
                Environment.DIRECTORY_MOVIES)
            val dirs = context.getExternalFilesDirs(Environment.DIRECTORY_MOVIES)
            for (dir in dirs) {
                var path = dir.path
                var length = path.length - subdir.length - 1
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

        fun findForDir(context: Context, rootDir: String): List<String> {
            var rootDirs: ArrayList<String> = arrayListOf()

            for (driverDir in getDriverDirs(context)) {
                val file = File(driverDir, rootDir)
                if (file.isDirectory) {
                    Log.v(TAG, "add path '${file.path}' to root dirs")
                    rootDirs.add(file.path)
                }
            }

            return rootDirs
        }

        fun buildPath(base: String, vararg segments: String) : String {
            var cur:File = File(base)
            for (segment in segments) {
                if (cur == null) {
                    cur = File(segment)
                } else {
                    cur = File(cur, segment);
                }
            }
            return cur.path
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

            var lines: ArrayList<String> = arrayListOf()
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
                Log.d(TAG, "The File doesn't not exist.", e)
            } catch (e: IOException) {
                Log.d(TAG, "read file error", e)
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
}