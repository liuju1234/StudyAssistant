package com.liujk.study_assistant.data

import android.content.Context
import android.content.Intent
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.liujk.study_assistant.utils.Storage
import com.liujk.study_assistant.utils.Utils
import java.io.File
import java.lang.StringBuilder

class Config(var rootJsons: List<JsonObject>) {
    lateinit var weekDays: ArrayList<WeekDayInfo>
    lateinit var displayDays: ArrayList<WeekDayInfo>
    lateinit var displayIndexs: ArrayList<Int>
    lateinit var processesTable: ArrayList<ProcessInfo>
    fun loadWeekDays() {
        val displayDaysCfg = getFromConfigs<JsonArray<JsonObject>>("displayDays")
        val weekDaysCfg = getFromConfigs<JsonObject>("weekDays")

        weekDays = ArrayList(weekDaysCfg.size)
        val weekDaysMap = hashMapOf<String, WeekDayInfo>()
        for ((key, value) in weekDaysCfg) {
            val weekDayInfo = WeekDayInfo(value as JsonObject, false, 0)
            weekDays.add(weekDayInfo)
            weekDaysMap[key] = weekDayInfo
        }
        displayDays = ArrayList(displayDaysCfg.size)
        displayIndexs = ArrayList(displayDaysCfg.size)
        for (displayDay in displayDaysCfg) {
            val displayDayStr: String = displayDay.string("day") ?: ""
            val dataIdx = displayDay.int("index") ?: 0
            val weekDayInfo = weekDaysMap[displayDayStr] as WeekDayInfo
            weekDayInfo.display = true
            weekDayInfo.dataIdx = dataIdx
            displayDays.add(weekDayInfo)
            displayIndexs.add(weekDays.indexOf(weekDayInfo))
        }
    }

    fun loadProcessesTable() {
        val processContentMap = HashMap<String, ProcessContent>()
        val processContentsCfg = getFromConfigs<JsonObject>("processContents")
        for ((key, value) in processContentsCfg) {
            processContentMap[key] = ProcessContent(value as JsonObject)
        }

        processesTable = arrayListOf()
        val processesTableCfg = getFromConfigs<JsonArray<JsonObject>>("processesTable")
        for (processesRowCfg in processesTableCfg) {
            processesTable.add(objToProcessInfo(processesRowCfg, processContentMap))
        }
    }

    private fun objToProcessInfo(cfg: JsonObject, processContentMap: HashMap<String, ProcessContent>) : ProcessInfo {
        val processesArrayCfg = cfg.array<String>("processes") ?: JsonArray<String>()
        val processesArray = arrayListOf<ProcessContent>()
        for (processKey in processesArrayCfg) {
            processContentMap[processKey]?.let { processesArray.add(it) }
        }
        return ProcessInfo(MyTime(cfg["time"] as JsonObject), MyDuration(cfg.int("duration")?:0),
            processesArray.toTypedArray(), weekDays, displayDays, displayIndexs,
            cfg.boolean("mergeCell")?: false
        )
    }

    fun getIntentFromName(context: Context, appName: String): Intent? {
        var intent: Intent? = null
        if (appName != "") {
            val allIntentsCfg = getFromConfigs<JsonObject>("intents")
            val intentCfg = allIntentsCfg[appName] as JsonObject
            val action = intentCfg["action"] as String?
            val componentNameStr = intentCfg["component"] as String ?: ""
            val packageName = intentCfg["package"] as String?

            intent = if (packageName != null) {
                Utils.intentFromPackageName(context, packageName)
            } else {
                Utils.intentFromComponentName(componentNameStr, action)
            }
        }
        return intent
    }

    fun getAppNameForProcess(processName: String, day: String): String {
        return getInfoForProcess("apps", processName, day, true)
    }

    fun getUrlForProcess(processName: String, day: String): String {
        return getInfoForProcess("urls", processName, day)
    }

    fun getNotesForProcess(processName: String, day: String): String {
        return getInfoForProcess("notes", processName, day)
    }

    private fun getInfoForProcess(infoName:String, processName: String, day: String, justOne: Boolean = false): String {
        val infoCfg = getFromConfigs<JsonObject>(infoName)
        val processInfoCfg = infoCfg[processName] as JsonObject?
        val commonInfo = processInfoCfg?.get("common") as String? ?: ""
        val dayInfo = (processInfoCfg?.get("days") as JsonObject?)?.get(day) as String? ?: ""
        return if (justOne) {
            if (commonInfo != "") commonInfo
            else dayInfo
        } else {
            if (commonInfo != "" && dayInfo != "") {
                commonInfo + '\n' + dayInfo
            } else {
                commonInfo + dayInfo
            }
        }
    }

    private inline fun <reified T> getFromConfigs(fieldName: String) : T {
        for (rootJson in rootJsons) {
            val field = rootJson[fieldName]
            if (field != null && field is T) {
                return field
            }
        }
        return T::class.java.newInstance()
    }

    companion object {
        val parser = Parser.default()
        const val ROOT_DIR = "网课"
        const val CFG_FILE_NAME = "配置.txt"
        var initConfig: Config? = null

        private fun getBuildInCfgJson() : JsonObject {
            return parser.parse(StringBuilder(BuildInData.configRawString)) as JsonObject
        }

        fun loadConfig(context: Context): Boolean {
            val cfgJsonList = arrayListOf<JsonObject>()
            var noConfigFile = true
            val rootDirs = Storage.findForDir(context, ROOT_DIR)
            for(rootDir in rootDirs) {
                val cfgFile = File(rootDir, CFG_FILE_NAME)
                if (cfgFile.isFile && cfgFile.canRead()) {
                    try {
                        val cfgJson = parser.parse(cfgFile.path) as JsonObject
                        noConfigFile = false
                        cfgJsonList.add(cfgJson)
                    } catch (e: Throwable) {
                        // ignore
                    }
                }
            }
            cfgJsonList.add(getBuildInCfgJson())

            initConfig = Config(cfgJsonList)
            return noConfigFile
        }

        fun getConfig(): Config {
            if (initConfig != null) {
                return initConfig as Config
            } else {
                return getBuildInConfig()
            }
        }

        fun writeBuildInConfig(context: Context) {
            val fileToWrite = File(Storage.buildPath(getNeedWriteDriverPath(context).path, ROOT_DIR, CFG_FILE_NAME))
            Storage.writeStringToFile(context, buildInConfigRawString, fileToWrite)
        }

        fun getNeedWriteDriverPath(context: Context): File {
            val rootDirs = Storage.findForDir(context, ROOT_DIR)
            var driverDirs = arrayListOf<File>()
            for (rootDir in rootDirs) {
                val parentDir = rootDir.parent
                if (parentDir != null) {
                    driverDirs.add(File(parentDir))
                }
            }
            if (driverDirs.isEmpty()) {
                driverDirs = Storage.getDriverDirs(context)
            }
            if (driverDirs.isEmpty()) {
                return Storage.getDefaultStorage()
            }
            return driverDirs[0]
        }

        fun getBuildInConfig(): Config {
            return Config(listOf(getBuildInCfgJson()))
        }
    }
}