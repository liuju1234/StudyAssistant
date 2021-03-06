package com.liujk.study_assistant.action

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.liujk.study_assistant.BuildConfig
import com.liujk.study_assistant.TAG
import com.liujk.study_assistant.WebActivity
import com.liujk.study_assistant.data.Config
import com.liujk.study_assistant.data.ProcessContent
import com.liujk.study_assistant.data.WeekDayInfo
import com.liujk.study_assistant.utils.Storage
import com.liujk.study_assistant.utils.Utils
import com.liujk.study_assistant.view.ActionList
import java.io.File

enum class ActionTimes {
    DAY, WEEK
}

enum class ActionType {
    URL, VIDEO, APP, OTHER
}

enum class RunStatus {
    IDLE, RUNNING
}

class ProcessAction(var content: ProcessContent, var displayIndex: Int,
                    var displayDays: ArrayList<WeekDayInfo>) {
    var status: RunStatus = RunStatus.IDLE
    private var actions:ArrayList<MyAction> = arrayListOf()
    private var otherActionsMap: HashMap<String, ArrayList<MyAction>> = hashMapOf()
    private var note = ""

    private fun addNote(noteStr: String) {
        if (noteStr != "") {
            if (note != "") {
                note += "\n"
            }
            note += noteStr
        }
    }

    private fun findActionsFromOneDir(actions: ArrayList<MyAction>, dir: File, findSub: Boolean = false) {
        if (dir.isDirectory) {
            val fileNames = dir.list()
            if (fileNames != null) {
                for (fileName in fileNames) {
                    when {
                        fileName == "note.txt" -> {
                            Log.v(
                                TAG,
                                "add note from '${Storage.buildPath(dir.path, fileName)}'"
                            )
                            addNote(Storage.readStringFromFile(dir, fileName))
                        }
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
                        findSub -> {
                            val subFile = File(dir, fileName)
                            if (subFile.isDirectory) {
                                findActionsFromOneDir(actions, subFile)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun addAppAction(appName: String, appIntent: Intent?) {
        if (appIntent != null) {
            actions.add(MyAction(ActionType.APP, appName, appIntent))
        }
    }

    private fun findActionsFromConfig(context: Context) {
        val config = Config.getConfig()
        val urlString = config.getUrlForProcess(content.name, displayDays[displayIndex].displayStr)
        addMultiUrls(actions, urlString)
        val notesString = config.getNotesForProcess(content.name, displayDays[displayIndex].displayStr)
        addNote(notesString)
        val appName = config.getAppNameForProcess(content.name, displayDays[displayIndex].displayStr)
        val appIntent = config.getIntentFromName(context, appName)
        addAppAction(appName, appIntent)
    }

    private fun getActions(context: Context) {
        actions = arrayListOf()
        otherActionsMap = hashMapOf()
        note = ""

        findActionsFromConfig(context)
        val rootDirs = Storage.findForDir(context, Config.ROOT_DIR)
        for (rootDir in rootDirs) {
            val (processRoots, _) = Storage.findDirsByNames(rootDir, content.names)
            for (processRoot in processRoots) {
                findActionsFromOneDir(actions, processRoot)
                val (processDayDirs, otherDirs) = Storage.findDirsByNames(processRoot, displayDays[displayIndex].alias)
                for (processDayDir in processDayDirs) {
                    findActionsFromOneDir(actions, processDayDir, true)
                }
                for (otherDir in otherDirs) {
                    var notDaysDir = true
                    for (day in displayDays) {
                        for (name in day.alias) {
                            if (otherDir.name.contains(name)) {
                                notDaysDir = false
                            }
                        }
                    }
                    if (notDaysDir) {
                        val otherActions = arrayListOf<MyAction>()
                        findActionsFromOneDir(otherActions, otherDir, true)
                        if (otherActions.size > 0) {
                            otherActions.sort()
                            otherActionsMap[otherDir.name] = otherActions
                        }
                    }
                }
            }
        }
        actions.sort()
    }

    private fun addMultiUrls(actions:ArrayList<MyAction>, urls: String) {
        addMultiActions(actions, ActionType.URL, urls.split('\n'))
    }

    private fun addMultiActions(actions:ArrayList<MyAction>, type: ActionType, file: File) {
        addMultiActions(actions, type, Storage.readLinesFromFile(file))
    }
    private fun addMultiActions(actions:ArrayList<MyAction>, type: ActionType, lines: List<String>) {
        for (line in lines) {
            val lineTrim = line.trim()
            if (lineTrim != "" && !lineTrim.startsWith('#')) {
                if (type == ActionType.URL
                    && (lineTrim.startsWith("http://")
                            || lineTrim.startsWith("https://"))) {
                    actions.add(MyAction(ActionType.URL, lineTrim))
                } else if (type == ActionType.APP) {
                    addAppAction(lineTrim, Utils.intentFromComponentName(lineTrim))
                }
            }
        }
    }

    fun run(context: Context) {
        getActions(context)
        if (actions.size > 0 || note != "") {
            if (actions.size == 1 && note == "") {
                actions[0].run(context)
            } else {
                ActionList(context, actions, otherActionsMap).display(note)
            }
        }
    }
}

class MyAction(var type: ActionType, var param: String, var appIntent: Intent? = null) : Comparable<MyAction> {
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
                appIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (context is Activity) {
                    if (appIntent != null) {
                        val intent = appIntent as Intent
                        context.startActivityIfNeeded(intent, 100)
                    }
                } else {
                    context.startActivity(appIntent)
                }
            }
        } catch (e: Exception) {
            Log.v(TAG, "", e)
        }
    }

    override fun toString(): String {
        if (appIntent == null) {
            return "Action{$type, $param}"
        } else {
            return "Action{$type, $param, $appIntent}"
        }
    }

    override fun compareTo(other: MyAction): Int {
        return if (type > other.type) 1
        else if (type == other.type) {
            param.compareTo(other.param)
        } else -1
    }
}

class ProcessNotify {
}
