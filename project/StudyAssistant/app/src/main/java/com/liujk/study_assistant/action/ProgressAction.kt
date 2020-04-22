package com.liujk.study_assistant.action

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import com.liujk.study_assistant.BuildConfig
import com.liujk.study_assistant.TAG
import com.liujk.study_assistant.WebActivity
import com.liujk.study_assistant.data.ProcessContent
import com.liujk.study_assistant.data.weekDays
import com.liujk.study_assistant.utils.Storage
import com.liujk.study_assistant.view.ActionList
import java.io.File

enum class ActionTimes {
    DAY, WEEK
}

enum class ActionType {
    URL, VIDEO, APP
}

enum class RunStatus {
    IDLE, RUNNING
}

class ProgressAction(var content: ProcessContent, var day: Int) {
    val ROOT_DIR = "网课"
    var status: RunStatus = RunStatus.IDLE
    var dirsMap: HashMap<Context, List<String>> = hashMapOf()

    private fun getPaths(): List<String> {
        val paths = arrayListOf<String>()
        if (content.times == ActionTimes.DAY) {
            for (path in weekDays[day].paths) {
                paths.add(Storage.buildPath(content.name, path))
            }
        } else
            paths.add(content.name)

        return paths
    }

    private fun getRootDirs(context: Context): List<String> {
        return dirsMap[context] ?: Storage.findForDir(context, ROOT_DIR).also { dirsMap[context] = it }
    }

    private fun getActions(context: Context): List<MyAction> {
        val actions:ArrayList<MyAction> = arrayListOf()

        val rootDirs = getRootDirs(context)
        for (rootDir in rootDirs) {
            for (path in getPaths()) {
                val dir = File(rootDir, path)
                if (dir.isDirectory) {
                    val fileNames = dir.list()
                    if (fileNames != null) {
                        for (fileName in fileNames) {
                            when {
                                fileName == "url.txt" -> {
                                    Log.v(
                                        TAG,
                                        "add URL Action from '${Storage.buildPath(dir.path, fileName)}'"
                                    )
                                    addMultiActions(actions, ActionType.URL, File(dir, fileName))
                                }
                                fileName == "app.txt" -> {
                                    Log.v(
                                        TAG,
                                        "add APP Action from '${Storage.buildPath(dir.path, fileName)}'"
                                    )
                                    addMultiActions(actions, ActionType.APP, File(dir, fileName))
                                }
                                Storage.isVideo(fileName) -> {
                                    Log.v(
                                        TAG,
                                        "add VIDEO Action for '${Storage.buildPath(
                                            dir.path,
                                            fileName
                                        )}'"
                                    )
                                    actions.add(
                                        MyAction(
                                            ActionType.VIDEO,
                                            Storage.buildPath(dir.path, fileName)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        return actions
    }

    private fun addMultiActions(actions:ArrayList<MyAction>, type: ActionType, file: File) {
        val lines: List<String> = Storage.readLinesFromFile(file)
        for (line in lines) {
            val lineTrim = line.trim()
            if (lineTrim != "" && !lineTrim.startsWith('#')) {
                if (type == ActionType.URL
                    && (lineTrim.startsWith("http://")
                            || lineTrim.startsWith("https://"))) {
                    actions.add(MyAction(ActionType.URL, lineTrim))
                } else if (type == ActionType.APP) {
                    actions.add(MyAction(ActionType.APP, lineTrim))
                }
            }
        }
    }

    fun run(context: Context, view: View) {
        val actions = getActions(context)
        if (actions.lastIndex > 0) {
            ActionList(context, actions).select()
        } else if (actions.lastIndex == 0) {
            actions[0].run(context)
        }
    }
}

class MyAction(var type: ActionType, var param: String) {
    var display = param
    init {
        if (type == ActionType.VIDEO) {
            display = File(param).name
        }
    }
    fun run(context: Context) {
        Log.v(TAG, "run $this")
        try {
            if (type == ActionType.URL) {
                Toast.makeText(context, "即将访问地址：$param", Toast.LENGTH_SHORT).show()
                if (false) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(param)
                    context.startActivity(intent)
                } else {
                    WebActivity.start(context, param)
                }
            } else if (type == ActionType.VIDEO) {
                Toast.makeText(context, "即将播放视频：$param", Toast.LENGTH_SHORT).show()
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val contentUri = FileProvider.getUriForFile(context,
                        BuildConfig.APPLICATION_ID + ".fileProvider",
                        File(param))
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.setDataAndType(contentUri, "video/*")
                } else {
                    intent.setDataAndType(Uri.fromFile(File(param)), "video/*")
                }
                context.startActivity(intent)
            } else if (type == ActionType.APP) {
                Toast.makeText(context, "即将打开应用：$param", Toast.LENGTH_SHORT).show()
                val intent = Intent(Intent.ACTION_MAIN)
                val strings = param.split("/", limit = 2)
                if (strings.lastIndex >= 1) {
                    intent.component = ComponentName(strings[0], strings[1])
                    context.startActivity(intent)
                }
            }
        } catch (e: Exception) {
            Log.v(TAG, "", e)
        }
    }

    override fun toString(): String {
        return "Action{$type, $param}"
    }
}

class ProcessNotify {
}
