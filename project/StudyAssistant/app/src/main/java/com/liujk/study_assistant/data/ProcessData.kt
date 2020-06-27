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
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

const val MILLIS_MINUTE: Long = 60 * 1000
const val MILLIS_HOUR: Long = 60 * MILLIS_MINUTE
const val MILLIS_DAY: Long = 24 * MILLIS_HOUR

class WeekDayInfo(val displayStr: String, val field: String, initAlias: Array<String>, var display: Boolean, var dataIdx: Int) {
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
        for (displayDay in config.displayDays) {
            columnList.add(Column<Any>(displayDay.displayStr, displayDay.field))
        }

        for (oneColumn in columnList) {
            oneColumn.setOnColumnItemClickListener { _, _, item, _ ->
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
        val config: Config = Config.getConfig()
        var dataMap: HashMap<Context, ProcessData> = hashMapOf()
        fun getInstance(context: Context) : ProcessData {
            return dataMap[context] ?: ProcessData(
                context
            ).also { dataMap[context] = it }
        }
        init {
            config.loadWeekDays()
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
                     val info: String? = null,
                     val noMerge: Boolean = false) {
    constructor(cfg: JsonObject) :
            this(cfg.string("name") ?: "",
                strToProcessType(cfg.string("type")),
                cfg.string("alias"),
                cfg.string("info"),
                cfg.boolean("noMerge")?:false)

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
                  private val dataIndex: Int,
                  var content: ProcessContent,
                  val displayDays: ArrayList<WeekDayInfo>) : MyItem {
    lateinit var action: ProcessAction
    lateinit var daysInfo: String
    var prepareNotify: ProcessNotify? = null
    var notify: ProcessNotify? = null
    val type: ProcessType = content.type
    var tittle: String = content.name

    var endDayIndex: Int = 0
    var displayIndex: Int = 0
        set(value) {
            field = value
            action = ProcessAction(content, field, displayDays)
            daysInfo = displayDays[field].displayStr
            endDayIndex = field
        }

    var range: Int = 0
        set(value) {
            if (value > 0) {
                daysInfo += "~"
                Log.v(TAG, "ItemProcess.setRange(${value})")
                Log.v(TAG, "content is $content")
                Log.v(TAG, "displayIndex is $displayIndex")
                endDayIndex = displayIndex + value
                daysInfo += displayDays[endDayIndex].displayStr
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

    fun cloneMySelf(): ItemProcess {
        return ItemProcess(processInfo, time, dataIndex, content, displayDays)
    }
}

class ProcessInfo(begin: MyTime, duration: MyDuration, private var contentArray: Array<ProcessContent>,
                  weekDays: ArrayList<WeekDayInfo>, displayDays: ArrayList<WeekDayInfo>,
                  displayIndexs: ArrayList<Int>,
                  private val mergeCell:Boolean = false) {

    var cellRowRanges: ArrayList<RowRange> = arrayListOf()
    var time:ItemTime = ItemTime(this, begin, duration)
    private var monday: ItemProcess
    private var tuesday: ItemProcess
    private var wednesday: ItemProcess
    private var thursday: ItemProcess
    private var friday: ItemProcess
    private var saturday: ItemProcess
    private var sunday: ItemProcess

    init {
        if (contentArray.isEmpty()) {
            throw IllegalArgumentException("No content argument given!")
        }
        val weekDayCount = 5
        val weekDaysDataArray: ArrayList<ItemProcess> = ArrayList<ItemProcess>(weekDayCount)
        var lastItemProcess = ItemProcess(this, time, 0, contentArray[0], displayDays)
        for (i in contentArray.indices) {
            lastItemProcess = ItemProcess(this, time, i, contentArray[i], displayDays)
            weekDaysDataArray.add(i, lastItemProcess)
        }
        for (i in contentArray.size until weekDayCount) {
            weekDaysDataArray.add(i, lastItemProcess)
        }

        val weekDaysItems: ArrayList<ItemProcess> = ArrayList<ItemProcess>(weekDays.size)
        for (weekDay in weekDays) {
            weekDaysItems.add(weekDaysDataArray[weekDay.dataIdx].cloneMySelf())
        }

        val displayItems: ArrayList<ItemProcess> = arrayListOf()
        for ((displayIndex, weekIndex) in displayIndexs.withIndex()) {
            val itemProcess = weekDaysItems[weekIndex]
            itemProcess.displayIndex = displayIndex
            displayItems.add(itemProcess)
        }

        if (mergeCell) {
            var recordDisplayItem: ItemProcess? = null
            var recordIndex: Int = -1
            var endIndex: Int = -1
            for ((index, displayItem) in displayItems.withIndex()) {
                if (recordDisplayItem == null) {
                    recordDisplayItem = displayItem
                    recordIndex = index
                } else {
                    if (Objects.equals(recordDisplayItem.content, displayItem.content)) {
                        endIndex = index
                    } else {
                        if (endIndex != -1) {
                            if (endIndex > recordIndex) {
                                if (!recordDisplayItem.content.noMerge) {
                                    recordDisplayItem.range = endIndex - recordIndex
                                    cellRowRanges.add(RowRange(recordIndex + 1, endIndex + 1))
                                }
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
                        if (!recordDisplayItem.content.noMerge) {
                            recordDisplayItem.range = endIndex - recordIndex
                            cellRowRanges.add(RowRange(recordIndex + 1, endIndex + 1))
                        }
                    }
                }
            }
        }
        monday = weekDaysItems[0]
        tuesday = weekDaysItems[1]
        wednesday = weekDaysItems[2]
        thursday = weekDaysItems[3]
        friday = weekDaysItems[4]
        saturday = weekDaysItems[5]
        sunday = weekDaysItems[6]
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