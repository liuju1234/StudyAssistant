package com.liujk.study_assistant.data

import android.content.Context
import android.util.Log
import com.beust.klaxon.JsonObject
import com.bin.david.form.data.CellRange
import com.bin.david.form.data.Column
import com.bin.david.form.data.table.TableData
import com.liujk.study_assistant.TAG
import com.liujk.study_assistant.action.ProcessNotify
import com.liujk.study_assistant.action.ProcessAction

const val MILLIS_MINUTE: Long = 60 * 1000
const val MILLIS_HOUR: Long = 60 * MILLIS_MINUTE
const val MILLIS_DAY: Long = 24 * MILLIS_HOUR

class WeekDayInfo(val displayStr: String, val field: String, initAlias: Array<String>, val display: Boolean, val dataIdx: Int) {
    constructor(cfg: JsonObject, display: Boolean, dataIdx: Int) :
            this(cfg.string("displayStr")?:"", cfg.string("field")?:"",
                cfg.array<String>("alias")?.toTypedArray() ?: arrayOf(""),
                display, dataIdx
            )
    var alias: ArrayList<String> = arrayListOf()
    init {
        alias.add(displayStr)
        alias.add(field)
        for(alias in initAlias) {
            this.alias.add(alias)
        }
    }

    override fun toString(): String {
        return "WeekDayInfo{$displayStr,$field,$alias,$display,$dataIdx}"
    }
}

class ProcessData(val context: Context) {
    var columnList: ArrayList<Column<Any>> = arrayListOf()
    init {
        columnList.add(Column<Any>("时间", "time"))
        for (weekDay in weekDays) {
            if (weekDay.display) {
                columnList.add(Column<Any>(weekDay.displayStr, weekDay.field))
            }
        }

        for (oneColumn in columnList) {
            oneColumn.setOnColumnItemClickListener { column, s, item, i ->
                if (item is MyItem) {
                    Log.v(TAG, "onClick item ${item.dump()}")
                    if (item is ItemProcess) {
                        item.action.run(context)
                    }
                }
            }
        }
    }

    fun toTableData(): TableData<ProcessInfo> {
        config.loadProcessesTable()
        val processesTable: ArrayList<ProcessInfo> = config.processesTable
        val tableData = TableData("课程表", processesTable, columnList)
        val ranges: ArrayList<CellRange> = arrayListOf()
        for (index in processesTable.indices) {
            for (cellRowRange in processesTable[index].cellRowRanges) {
                ranges.add(cellRowRange.toCellRange(index))
            }
        }
        if (ranges.size > 0) {
            tableData.userCellRange = ranges
        }
        return tableData
    }

    companion object {
        var dataMap: HashMap<Context, ProcessData> = hashMapOf()
        lateinit var weekDays: ArrayList<WeekDayInfo>
        lateinit var config: Config
        fun getInstance(context: Context) : ProcessData {
            initFromConfig()
            return dataMap[context] ?: ProcessData(
                context
            ).also { dataMap[context] = it }
        }

        val displayDataIndexArray: ArrayList<Int> = arrayListOf()
        val displayDayIndexArray: ArrayList<Int> = arrayListOf()
        val displayDataIndexSet: HashSet<Int> = hashSetOf()
        lateinit var dayIndexArray: Array<Int>
        lateinit var data2displayIndexArray: Array<Int>

        fun initFromConfig() {
            config = Config.getConfig()
            config.loadWeekDays()
            weekDays = config.weekDays
            dayIndexArray = Array(weekDays.size) {-1}
            data2displayIndexArray = Array(weekDays.size) {-1}
            var displayIndex = 0
            for ((index, weekDay) in weekDays.withIndex()) {
                if (weekDay.display) {
                    displayDataIndexArray.add(weekDay.dataIdx)
                    displayDayIndexArray.add(index)
                    displayDataIndexSet.add(weekDay.dataIdx)
                    data2displayIndexArray[weekDay.dataIdx] = displayIndex++
                }
                if (dayIndexArray[weekDay.dataIdx] == -1) {
                    dayIndexArray[weekDay.dataIdx] = index
                }
            }
        }
    }
}

class RowRange(private val firstCol: Int, private val lastCol: Int) {
    fun toCellRange(row: Int): CellRange {
        return CellRange(row, row, firstCol, lastCol)
    }
}

enum class ProcessType{
    NONE, CLASS, REST, ACTION
}

fun strToProcessType(str: String?) : ProcessType {
    when (str) {
        "class" -> { return ProcessType.CLASS}
        "rest" -> {return ProcessType.REST}
        "action" -> {return ProcessType.ACTION}
    }
    return ProcessType.NONE
}

class ProcessContent(val name: String, val type:ProcessType,
                     private val alias: String? = null,
                     val info: String? = null) {
    constructor(cfg: JsonObject) :
            this(cfg.string("name") ?: "",
                strToProcessType(cfg.string("type")),
                cfg.string("alias"),
                cfg.string("info"))

    val names = arrayListOf<String>()

    init {
        names.add(name)
        if (alias != null) {
            names.add(alias)
        }
    }

    override fun toString(): String {
        return name + if (info != null) info else ""
    }
}

interface MyItem {
    fun dump(): String
}

class ItemTime(private var processInfo: ProcessInfo,
               private var begin: MyTime, duration: MyDuration) : MyItem {
    private var end: MyTime = begin + duration

    override fun dump(): String {
        return "Time: $this"
    }

    override fun toString(): String {
        return "${this.begin}-${this.end}"
    }
}

class ItemProcess(private var processInfo: ProcessInfo, var time: ItemTime,
                  private val dataIndex: Int, range: Int,
                  var content: ProcessContent,
                  val weekDays: ArrayList<WeekDayInfo>) : MyItem {
    private val dayIndex = ProcessData.dayIndexArray[dataIndex]
    var action: ProcessAction = ProcessAction(content, dayIndex, weekDays)
    var prepareNotify: ProcessNotify? = null
    var notify: ProcessNotify? = null
    val type: ProcessType = content.type
    var daysInfo: String = weekDays[dayIndex].displayStr
    var tittle: String = content.name

    var range: Int = range
        set(value) {
            if (value > 0) {
                daysInfo += "~"
                val endDayIndex = ProcessData.displayDayIndexArray[ProcessData.data2displayIndexArray[dataIndex] + value]
                daysInfo += weekDays[endDayIndex].displayStr
            }
            field = value
        }

    init {
        if (type == ProcessType.CLASS) {
            prepareNotify = ProcessNotify()
        }
    }

    override fun dump(): String {
        return "Process[$type]: $content in $daysInfo $time"
    }

    override fun toString(): String {
        return content.toString()
    }
}

class ProcessInfo(begin: MyTime, duration: MyDuration, private var contentArray: Array<ProcessContent>,
                  weekDays: ArrayList<WeekDayInfo>,
                  private val mergeCell:Boolean = false) {

    var cellRowRanges: ArrayList<RowRange> = arrayListOf()
    var time:ItemTime = ItemTime(this, begin, duration)
    private var sunday: ItemProcess
    private var monday: ItemProcess
    private var tuesday: ItemProcess
    private var wednesday: ItemProcess
    private var thursday: ItemProcess
    private var friday: ItemProcess
    private var saturday: ItemProcess

    init {
        val weekItemArray: ArrayList<ItemProcess> = ArrayList<ItemProcess>(weekDays.size)
        val fallbackDataIndex = ProcessData.displayDataIndexArray[0]
        val fallbackItemProcess = ItemProcess(this, time, fallbackDataIndex,
            0, contentArray[0], weekDays)
        for (i in contentArray.indices) {
            val itemProcess: ItemProcess
            if (ProcessData.displayDataIndexSet.contains(i)) {
                itemProcess = ItemProcess(
                        this, time, i, 0,
                        contentArray[i], weekDays
                    )
            } else {
                itemProcess = fallbackItemProcess
            }
            weekItemArray.add(i, itemProcess)
        }
        for (i in contentArray.size until weekDays.size) {
            val itemProcess: ItemProcess = if (ProcessData.displayDataIndexSet.contains(i)) {
                ItemProcess(
                    this, time, i, 0,
                    contentArray[contentArray.lastIndex], weekDays
                )
            } else {
                fallbackItemProcess
            }
            weekItemArray.add(i, itemProcess)
        }

        if (mergeCell) {
            val displayItems: ArrayList<ItemProcess> = arrayListOf()
            for (index in ProcessData.displayDataIndexArray) {
                displayItems.add(weekItemArray[index])
            }

            var recordDisplayItem: ItemProcess? = null
            var recordIndex: Int = -1
            var endIndex: Int = -1
            for ((index, displayItem) in displayItems.withIndex()) {
                if (recordDisplayItem == null) {
                    recordDisplayItem = displayItem
                    recordIndex = index
                } else {
                    if (recordDisplayItem.content == displayItem.content) {
                        endIndex = index
                    } else {
                        if (endIndex != -1) {
                            if (endIndex > recordIndex) {
                                recordDisplayItem.range = endIndex - recordIndex
                                cellRowRanges.add(RowRange(recordIndex + 1, endIndex + 1))
                            }
                            endIndex = -1
                        }
                        recordDisplayItem = displayItem
                        recordIndex = index
                    }
                }
            }
            if (endIndex != -1) {
                if (endIndex > recordIndex) {
                    if (recordDisplayItem != null) {
                        recordDisplayItem.range = endIndex - recordIndex
                    }
                    cellRowRanges.add(RowRange(recordIndex + 1, endIndex + 1))
                }
            }
        }
        monday = weekItemArray[0]
        tuesday = weekItemArray[1]
        wednesday = weekItemArray[2]
        thursday = weekItemArray[3]
        friday = weekItemArray[4]
        saturday = weekItemArray[5]
        sunday = weekItemArray[6]
    }
}

open class MyDuration(minutes: Int) : Comparable<MyDuration> {
    var mMinutes: Int = minutes
    override fun compareTo(other: MyDuration): Int {
        return mMinutes - other.mMinutes
    }

    open operator fun plus(other: MyDuration): MyDuration {
        return MyDuration(mMinutes + other.mMinutes)
    }

    open operator fun minus(other: MyDuration): MyDuration {
        return MyDuration(mMinutes - other.mMinutes)
    }
}

class MyTime(hour: Int, minute: Int = 0): MyDuration(hour * 60 + minute) {
    var mHour: Int = hour
    var mMinute: Int = minute

    constructor(cfg: JsonObject): this(cfg.int("hour")?:0, cfg.int("minute")?:0)
    constructor(duration: MyDuration) : this(duration.mMinutes / 60, duration.mMinutes % 60)

    var mMillis: Long = mMinutes * 60 * 1000L
    override fun toString(): String {
        return String.format("%02d:%02d", mHour, mMinute)
    }

    fun toTimeMillis(): Long {
        val currentTime: Long = System.currentTimeMillis()
        val dayBegin: Long = currentTime - (currentTime % MILLIS_DAY)
        return dayBegin + mMillis
    }

    override operator fun plus(other: MyDuration): MyTime {
        val duration:MyDuration = MyDuration(this.mMinutes + other.mMinutes)
        return MyTime(duration)
    }

    override operator fun minus(other: MyDuration): MyTime {
        val duration:MyDuration = MyDuration(this.mMinutes - other.mMinutes)
        return MyTime(duration)
    }
}