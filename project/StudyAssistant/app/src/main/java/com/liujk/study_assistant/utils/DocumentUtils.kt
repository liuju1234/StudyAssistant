package com.liujk.study_assistant.utils

import android.annotation.TargetApi
import android.content.Context
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.provider.DocumentsContract
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.liujk.study_assistant.TAG
import java.io.*
import java.util.*
import kotlin.collections.ArrayList


object DocumentsUtils {
    //private val TAG = DocumentsUtils::class.java.simpleName
    //const val OPEN_DOCUMENT_TREE_CODE = 8000
    private val sExtSdCardPaths: MutableList<String> = ArrayList()
    fun cleanCache() {
        sExtSdCardPaths.clear()
    }

    /**
     * Get a list of external SD card paths. (Kitkat or higher.)
     *
     * @return A list of external SD card paths.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun getExtSdCardPaths(context: Context): Array<String> {
        if (sExtSdCardPaths.size > 0) {
            return sExtSdCardPaths.toTypedArray()
        }
        for (file in context.getExternalFilesDirs("external")) {
            if (file != null && !file.equals(context.getExternalFilesDir("external"))) {
                val index: Int = file.getAbsolutePath().lastIndexOf("/Android/data")
                if (index < 0) {
                    Log.w(
                        TAG,
                        "Unexpected external file dir: " + file.getAbsolutePath()
                    )
                } else {
                    var path: String = file.getAbsolutePath().substring(0, index)
                    try {
                        path = File(path).getCanonicalPath()
                    } catch (e: IOException) {
                        // Keep non-canonical path.
                    }
                    sExtSdCardPaths.add(path)
                }
            }
        }
        if (sExtSdCardPaths.isEmpty()) sExtSdCardPaths.add("/storage/sdcard1")
        return sExtSdCardPaths.toTypedArray()
    }

    /**
     * Determine the main folder of the external SD card containing the given file.
     *
     * @param file the file.
     * @return The main folder of the external SD card containing this file, if the file is on an SD
     * card. Otherwise,
     * null is returned.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun getExtSdCardFolder(file: File, context: Context): String? {
        val extSdPaths = getExtSdCardPaths(context)
        try {
            for (i in extSdPaths.indices) {
                if (file.getCanonicalPath().startsWith(extSdPaths[i])) {
                    return extSdPaths[i]
                }
            }
        } catch (e: IOException) {
            return null
        }
        return null
    }

    /**
     * Determine if a file is on external sd card. (Kitkat or higher.)
     *
     * @param file The file.
     * @return true if on external sd card.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun isOnExtSdCard(file: File, c: Context): Boolean {
        return getExtSdCardFolder(file, c) != null
    }

    /**
     * Get a DocumentFile corresponding to the given file (for writing on ExtSdCard on Android 5).
     * If the file is not
     * existing, it is created.
     *
     * @param file        The file.
     * @param isDirectory flag indicating if the file should be a directory.
     * @return The DocumentFile
     */
    fun getDocumentFile(
        file: File, isDirectory: Boolean,
        context: Context
    ): DocumentFile? {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            return DocumentFile.fromFile(file)
        }
        Log.v(TAG, "getDocumentFile for '$file', isDirectory is $isDirectory")
        val baseFolder = getExtSdCardFolder(file, context)
        var originalDirectory = false
        if (baseFolder == null) {
            Log.v(TAG, "return null")
            return null
        }
        var relativePath: String? = null
        try {
            val fullPath: String = file.getCanonicalPath()
            if (baseFolder != fullPath) {
                relativePath = fullPath.substring(baseFolder.length + 1)
            } else {
                originalDirectory = true
            }
        } catch (e: IOException) {
            Log.v(TAG, "return null", e)
            return null
        } catch (f: Exception) {
            originalDirectory = true
            //continue
        }
        val tempAs = PreferenceManager.getDefaultSharedPreferences(context).getString(
            baseFolder,
            null
        )
        var treeUri: Uri? = null
        if (tempAs != null) treeUri = Uri.parse(tempAs)
        if (treeUri == null) {
            return null
        }

        // start with root of SD card and then parse through document tree.
        var document = DocumentFile.fromTreeUri(context, treeUri)
        if (originalDirectory) return document
        val parts = relativePath!!.split("/").toTypedArray()
        for (i in parts.indices) {
            var nextDocument = document!!.findFile(parts[i])
            if (nextDocument == null) {
                nextDocument = if (i < parts.size - 1 || isDirectory) {
                    document.createDirectory(parts[i])
                } else {
                    document.createFile("image", parts[i])
                }
            }
            document = nextDocument
        }
        Log.v(TAG, "return document: $document")
        return document
    }

    fun mkdirs(context: Context, dir: File): Boolean {
        var res: Boolean = dir.mkdirs()
        if (!res) {
            if (isOnExtSdCard(dir, context)) {
                val documentFile = getDocumentFile(dir, true, context)
                res = documentFile != null && documentFile.canWrite()
            }
        }
        return res
    }

    fun delete(context: Context, file: File): Boolean {
        var ret: Boolean = file.delete()
        if (!ret && isOnExtSdCard(file, context)) {
            val f = getDocumentFile(file, false, context)
            if (f != null) {
                ret = f.delete()
            }
        }
        return ret
    }

    fun canWrite(file: File): Boolean {
        var res = file.exists() && file.canWrite()
        if (!res && !file.exists()) {
            try {
                res = if (!file.isDirectory()) {
                    file.createNewFile() && file.delete()
                } else {
                    file.mkdirs() && file.delete()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return res
    }

    fun canWrite(context: Context, file: File): Boolean {
        return pathCanWrite(context, file, false)
    }
    fun dirCanWrite(context: Context, file: File): Boolean {
        return pathCanWrite(context, file, true)
    }
    private fun pathCanWrite(context: Context, file: File, isDirectory: Boolean): Boolean {
        var res = canWrite(file)
        if (!res && isOnExtSdCard(file, context)) {
            val documentFile = getDocumentFile(file, isDirectory, context)
            res = documentFile != null && documentFile.canWrite()
        }
        return res
    }

    fun renameTo(context: Context, src: File, dest: File): Boolean {
        var res: Boolean = src.renameTo(dest)
        if (!res && isOnExtSdCard(dest, context)) {
            val srcDoc: DocumentFile?
            srcDoc = if (isOnExtSdCard(src, context)) {
                getDocumentFile(src, false, context)
            } else {
                DocumentFile.fromFile(src)
            }
            val destDoc =
                getDocumentFile(dest.getParentFile() ?: File("/"), true, context)
            if (srcDoc != null && destDoc != null) {
                try {
                    if (Objects.equals(src.getParent(), dest.getParent())) {
                        res = srcDoc.renameTo(dest.getName())
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        res = DocumentsContract.moveDocument(
                            context.getContentResolver(),
                            srcDoc.uri,
                            srcDoc.parentFile!!.uri,
                            destDoc.uri
                        ) != null
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return res
    }

    fun getInputStream(context: Context, destFile: File): InputStream? {
        var inputStream: InputStream? = null
        try {
            if (!canWrite(destFile) && isOnExtSdCard(
                    destFile,
                    context
                )
            ) {
                val file = getDocumentFile(destFile, false, context)
                if (file != null && file.canWrite()) {
                    inputStream = context.getContentResolver().openInputStream(file.uri)
                }
            } else {
                inputStream = FileInputStream(destFile)
            }
        } catch (e: FileNotFoundException) {
            Log.w(TAG, "getInputStream err:", e)
        }
        return inputStream
    }

    fun getOutputStream(context: Context, destFile: File): OutputStream? {
        var out: OutputStream? = null
        try {
            if (!canWrite(destFile) && isOnExtSdCard(
                    destFile,
                    context
                )
            ) {
                val file = getDocumentFile(destFile, false, context)
                if (file != null && file.canWrite()) {
                    out = context.getContentResolver().openOutputStream(file.uri)
                }
            } else {
                out = FileOutputStream(destFile)
            }
        } catch (e: FileNotFoundException) {
            Log.w(TAG, "getOutputStream err:", e)
        }
        return out
    }

    fun saveTreeUri(context: Context, rootPath: String, uri: Uri): Boolean {
        val file = DocumentFile.fromTreeUri(context, uri)
        Log.v(TAG, "saveTreeUri rootPath is $rootPath")
        Log.v(TAG, "saveTreeUri file is $file")
        if (file != null) Log.v(TAG, "saveTreeUri canWrite is ${file.canWrite()}")
        if (file != null && file.canWrite()) {
            val perf = PreferenceManager.getDefaultSharedPreferences(context)
            perf.edit().putString(rootPath, uri.toString()).apply()
            return true
        } else {
            Log.e(TAG, "no write permission: $rootPath")
        }
        return false
    }

    fun checkWritableRootPath(context: Context, rootPath: String): Boolean {
        val root = File(rootPath)
        return if (!root.canWrite()) {
            if (isOnExtSdCard(root, context)) {
                val documentFile =
                    getDocumentFile(root, true, context)
                documentFile == null || !documentFile.canWrite()
            } else {
                val perf = PreferenceManager.getDefaultSharedPreferences(context)
                val documentUri = perf.getString(rootPath, "")
                if (documentUri == null || documentUri.isEmpty()) {
                    true
                } else {
                    val file =
                        DocumentFile.fromTreeUri(context, Uri.parse(documentUri))
                    !(file != null && file.canWrite())
                }
            }
        } else false
    }
}