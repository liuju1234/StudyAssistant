package com.liujk.study_assistant.data

import android.content.Context
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.liujk.study_assistant.utils.Storage
import java.io.File
import java.lang.StringBuilder

const val buildInConfigRawString: String = """
{
    "weekDays": {
        "星期一": {"displayStr": "星期一", "field": "monday", "alias": ["周一", "Mon"]},
        "星期二": {"displayStr": "星期二", "field": "tuesday", "alias": ["周二", "Tus"]},
        "星期三": {"displayStr": "星期三", "field": "wednesday", "alias": ["周三", "Wed"]},
        "星期四": {"displayStr": "星期四", "field": "thursday", "alias": ["周四", "Thu"]},
        "星期五": {"displayStr": "星期五", "field": "friday", "alias": ["周五", "Fri"]},
        "星期六": {"displayStr": "星期六", "field": "saturday", "alias": ["周六", "Sat"]},
        "星期日": {"displayStr": "星期日", "field": "sunday", "alias": ["周日", "Sun"]},
    },
    "processContents": {
        "语文": {"name": "语文", "type": "class"},
        "数学": {"name": "数学", "type": "class"},
        "英语": {"name": "英语", "type": "class"},
        "班会": {"name": "班会", "type": "class"},
        "眼保健操": {"name": "眼保健操", "type": "action"},
        "大课间": {"name": "大课间", "type": "rest"},
        "线上升国旗": {"name": "线上升国旗", "type": "action"},
        "体育": {"name": "体育", "type": "action"},
        "自由梳理": {"name": "自由梳理", "type": "action"},
        "午间休息": {"name": "午间休息", "type": "rest", "info": "/家务劳动"},
        "美术": {"name": "美术", "type": "class"},
        "科学": {"name": "科学", "type": "class", "alias": "科技"},
        "道法": {"name": "道法", "type": "class", "alias": "道德法制"},
        "音乐": {"name": "音乐", "type": "class"},
        "其它": {"name": "其它", "type": "class"},
    },
    "processesTable": [
        {"time": {"hour":8, "minute":0}, "duration": 25,
            "processes": ["数学"]},
        {"time": {"hour":8, "minute":25}, "duration": 5,
            "processes": ["眼保健操"], "mergeCell": true},
        {"time": {"hour":8, "minute":30}, "duration": 30,
            "processes": ["线上升国旗", "大课间"], "mergeCell": true},
        {"time": {"hour":9, "minute":0}, "duration": 25,
            "processes": ["语文"]},
        {"time": {"hour":9, "minute":25}, "duration": 5,
            "processes": ["眼保健操"], "mergeCell": true},
        {"time": {"hour":9, "minute":30}, "duration": 30,
            "processes": ["体育"], "mergeCell": true},
        {"time": {"hour":10, "minute":0}, "duration": 25,
            "processes": ["英语", "英语", "英语", "英语", "班会", "英语"]},
        {"time": {"hour":10, "minute":25}, "duration": 5,
            "processes": ["眼保健操"], "mergeCell": true},
        {"time": {"hour":10, "minute":30}, "duration": 60,
            "processes": ["自由梳理"], "mergeCell": true},
        {"time": {"hour":11, "minute":30}, "duration": 150,
            "processes": ["午间休息"], "mergeCell": true},
        {"time": {"hour":14, "minute":0}, "duration": 90,
            "processes": ["美术", "科学", "道法", "音乐", "其它"], "mergeCell": true},
    ],
    "displayDays": [
        {"day": "星期一", "index": 0},
        {"day": "星期二", "index": 1},
        {"day": "星期三", "index": 2},
        {"day": "星期四", "index": 3},
        {"day": "星期五", "index": 4},
    ],
    "notes": {
        "语文":
            {"common": "",
            "days": {
                "星期一": "",
                "星期二": "",
                "星期三": "",
                "星期四": "",
                "星期五": "",
                }
            },
        "数学":
            {"common": "",
            "days": {
                "星期一": "",
                "星期二": "",
                "星期三": "",
                "星期四": "",
                "星期五": "",
                }
            },
        "英语":
            {"common": "",
            "days": {
                "星期一": "",
                "星期二": "",
                "星期三": "",
                "星期四": "",
                "星期五": "",
                }
            },
    },
    "urls": {
        "语文":
            {"common": "https://v.campus.qq.com/gkk/3cumat9#/course",
            "days": {
                "星期一": "",
                "星期二": "",
                "星期三": "",
                "星期四": "",
                "星期五": "",
                }
            },
        "英语":
            {"common": "",
            "days": {
                "星期一": "",
                "星期二": "",
                "星期三": "",
                "星期四": "",
                "星期五": "",
                }
            },
    },
}
"""

class Config(var rootJsons: List<JsonObject>) {
    lateinit var weekDays: ArrayList<WeekDayInfo>
    lateinit var processesTable: ArrayList<ProcessInfo>
    fun loadWeekDays() {
        val displayDays = getFromConfigs<JsonArray<JsonObject>>("displayDays")
        val weekDaysCfg = getFromConfigs<JsonObject>("weekDays")

        weekDays = ArrayList(weekDaysCfg.size)
        val displayDaysSet = hashSetOf<String>()
        for (displayDay in displayDays) {
            val displayDayStr: String = displayDay.string("day") ?: ""
            displayDaysSet.add(displayDayStr)
            val weekDayCfg = weekDaysCfg[displayDayStr] as JsonObject
            val weekDayInfo = WeekDayInfo(weekDayCfg, true, displayDay.int("index") ?: 0)
            //Log.v(TAG, "add display Day $weekDayInfo")
            weekDays.add(weekDayInfo)
        }
        for ((key, value) in weekDaysCfg) {
            if (!displayDaysSet.contains(key)) {
                val weekDayInfo = WeekDayInfo(value as JsonObject, false, 0)
                //Log.v(TAG, "add no display Day $weekDayInfo")
                weekDays.add(weekDayInfo)
            }
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
            processesArray.toTypedArray(), weekDays,
            cfg.boolean("mergeCell")?: false
        )
    }

    fun getUrlForProcess(processName: String, day: String): String {
        return getInfoForProcess("urls", processName, day)
    }

    fun getNotesForProcess(processName: String, day: String): String {
        return getInfoForProcess("notes", processName, day)
    }

    private fun getInfoForProcess(infoName:String, processName: String, day: String): String {
        val infoCfg = getFromConfigs<JsonObject>(infoName)
        val processInfoCfg = infoCfg[processName] as JsonObject
        val commonInfo = processInfoCfg["common"] as String
        val dayInfo = (processInfoCfg["days"] as JsonObject)[day] as String
        return if (commonInfo != "" && dayInfo != "") {
            commonInfo + '\n' + dayInfo
        } else {
            commonInfo + dayInfo
        }
    }

    private inline fun <reified T> getFromConfigs(fieldName: String) : T {
        for (rootJson in rootJsons) {
            val field = rootJson[fieldName]
            if (field != null && field is T) {
                return field as T
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
            return parser.parse(StringBuilder(buildInConfigRawString)) as JsonObject
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
                driverDirs.add(File(rootDir.parent))
            }
            if (driverDirs.isEmpty()) {
                driverDirs = Storage.getDriverDirs(context)
            }
            if (driverDirs.isEmpty()) {
                return File("/sdcard").absoluteFile.canonicalFile
            }
            return driverDirs[0]
        }

        fun getBuildInConfig(): Config {
            return Config(listOf(getBuildInCfgJson()))
        }
    }
}