package com.liujk.study_assistant.data

import android.content.Context
import android.util.Log
import com.bin.david.form.data.CellRange
import com.bin.david.form.data.Column
import com.bin.david.form.data.table.TableData
import com.liujk.study_assistant.TAG
import com.liujk.study_assistant.action.ProcessNotify
import com.liujk.study_assistant.action.ProgressAction
import com.liujk.study_assistant.view.MySmartTable

val MILLIS_MINUTE: Long = 60 * 1000
val MILLIS_HOURE: Long = 60 * MILLIS_MINUTE
val MILLIS_DAY: Long = 24 * MILLIS_HOURE

const val STR_YU_WEN = "语文"
val YU_WEN = ProcessContent(STR_YU_WEN, ProgressType.CLASS)
const val STR_SHU_XUE = "数学"
val SHU_XUE = ProcessContent(STR_SHU_XUE, ProgressType.CLASS)
const val STR_ENGLISH = "英语"
val ENGLISH = ProcessContent(STR_ENGLISH, ProgressType.CLASS)
const val STR_MEETING = "班会"
val MEETING = ProcessContent(STR_MEETING, ProgressType.CLASS)
const val STR_EYE = "眼保健操"
val EYE = ProcessContent(STR_EYE, ProgressType.ACTION)
const val STR_BIG_PLAY = "大课间"
val BIG_PLAY = ProcessContent(STR_BIG_PLAY, ProgressType.REST)
const val STR_GUO_QI = "线上升国旗"
val GUO_QI = ProcessContent(STR_GUO_QI, ProgressType.REST)
const val STR_TI_YU = "体育"
val TI_YU = ProcessContent(STR_TI_YU, ProgressType.REST)
const val STR_ARRANGE = "自由梳理"
val ARRANGE = ProcessContent(STR_ARRANGE, ProgressType.ACTION)
const val STR_NOON = "午间休息"
const val INFO_NOON = "/家务劳动"
val NOON = ProcessContent(STR_NOON, ProgressType.REST, info=INFO_NOON)
//const val STR_BY_ONE_SELF = "自主安排"
//const val INFO_BY_ONE_SELF = "：道德法制、全科阅读、综合实践、艺术鉴赏、体育锻炼"
//val BY_ONE_SELF = ProcessContent(STR_BY_ONE_SELF, ProgressType.ACTION, info= INFO_BY_ONE_SELF)

const val STR_MEISHU = "美术"
val MEISHU = ProcessContent(STR_MEISHU, ProgressType.CLASS)
const val STR_KEXUE = "科学"
val KEXUE = ProcessContent(STR_KEXUE, ProgressType.CLASS, alias = "科技")
const val STR_DAOFA = "道法"
val DAOFA = ProcessContent(STR_DAOFA, ProgressType.CLASS, alias = "道德法制")
const val STR_MUSIC = "音乐"
val MUSIC = ProcessContent(STR_MUSIC, ProgressType.CLASS)
const val STR_QITA = "其它"
val QITA = ProcessContent(STR_QITA, ProgressType.CLASS)

val weekDays = arrayListOf<WeekDayInfo>(
    WeekDayInfo("星期日", "sunday", listOf("周日", "Sun"), true, 6),
    WeekDayInfo("星期一", "monday", listOf("周一", "Mon"), true, 0),
    WeekDayInfo("星期二", "tuesday", listOf("周二", "Tus"), true,1),
    WeekDayInfo("星期三", "wednesday", listOf("周三", "Wed"), true, 2),
    WeekDayInfo("星期四", "thursday", listOf("周四", "Thu"), true, 3),
    WeekDayInfo("星期五", "friday", listOf("周五", "Fri"), false,4),
    WeekDayInfo("星期六", "saturday", listOf("周六", "Sat"), false, 5)
)

class WeekDayInfo(val displayStr: String, val field: String, initAlias: List<String>, val display: Boolean, val dataIdx: Int) {
    var alias: ArrayList<String> = arrayListOf()
    init {
        alias.add(displayStr)
        alias.add(field)
        for(path in initAlias) {
            alias.add(path)
        }
    }
}

class ProgressData(val context: Context, dataTable: MySmartTable<ProgressInfo>) {
    var columnList: ArrayList<Column<Any>> = arrayListOf()

    val initProgresses = arrayListOf<ProgressInfo>(
        ProgressInfo(MyTime(8), MyDuration(25),
            arrayOf<ProcessContent>(SHU_XUE)),
        ProgressInfo(MyTime(8,25), MyDuration(5),
            arrayOf<ProcessContent>(EYE), true),
        ProgressInfo(MyTime(8,30), MyDuration(30),
            arrayOf<ProcessContent>(GUO_QI, BIG_PLAY), true),
        ProgressInfo(MyTime(9), MyDuration(25),
            arrayOf<ProcessContent>(YU_WEN)),
        ProgressInfo(MyTime(9,25), MyDuration(5),
            arrayOf<ProcessContent>(EYE), true),
        ProgressInfo(MyTime(9,30), MyDuration(30),
            arrayOf<ProcessContent>(TI_YU), true),
        ProgressInfo(MyTime(10), MyDuration(25),
            arrayOf<ProcessContent>(ENGLISH, ENGLISH, ENGLISH, ENGLISH, MEETING, ENGLISH)),
        ProgressInfo(MyTime(10,25), MyDuration(5),
            arrayOf<ProcessContent>(EYE), true),
        ProgressInfo(MyTime(10,30), MyDuration(60),
            arrayOf<ProcessContent>(ARRANGE), true),
        ProgressInfo(MyTime(11,30), MyDuration(150),
            arrayOf<ProcessContent>(NOON), true),
        ProgressInfo(MyTime(14), MyDuration(90),
            arrayOf<ProcessContent>(MEISHU, KEXUE, DAOFA, MUSIC, QITA))
    )

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
                    if (item is ItemProgress) {
                        item.action.run(context, dataTable)
                    }
                }
            }
        }
    }

    fun toTableData(): TableData<ProgressInfo> {
        val tableData = TableData("课程表", initProgresses, columnList)
        val ranges: ArrayList<CellRange> = arrayListOf()
        for (index in 0..initProgresses.lastIndex) {
            for (cellRowRange in initProgresses[index].cellRowRanges) {
                ranges.add(cellRowRange.toCellRange(index))
            }
        }
        if (ranges.size > 0) {
            tableData.userCellRange = ranges
        }
        //tableData.sortColumn = columnList[0]
        return tableData
    }

    companion object {
        var dataMap: HashMap<Context, ProgressData> = hashMapOf()
        fun getInstance(context: Context, dataTable: MySmartTable<ProgressInfo>) : ProgressData {
            return dataMap[context] ?: ProgressData(context, dataTable).also { dataMap[context] = it }
        }

        val displayDataIndexArray: ArrayList<Int> = arrayListOf()
        val displayDayIndexArray: ArrayList<Int> = arrayListOf()
        val dayIndexArray: Array<Int> = Array(weekDays.size) {0}
        val data2displayIndexArray: Array<Int> = Array(weekDays.size) {0}

        init {
            var displayIndex = 0
            for ((index, weekDay) in weekDays.withIndex()) {
                if (weekDay.display) {
                    displayDataIndexArray.add(weekDay.dataIdx)
                    displayDayIndexArray.add(index)
                    data2displayIndexArray[weekDay.dataIdx] = displayIndex++
                }
                dayIndexArray[weekDay.dataIdx] = index
            }
        }
    }
}

class RowRange(private val firstCol: Int, private val lastCol: Int) {
    fun toCellRange(row: Int): CellRange {
        return CellRange(row, row, firstCol, lastCol)
    }
}

enum class ProgressType{
    CLASS, REST, ACTION
}

class ProcessContent(val name: String, val type:ProgressType,
                     private val alias: String? = null,
                     val info: String? = null) {
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

class ItemTime(private var progressInfo: ProgressInfo,
               private var begin: MyTime, duration: MyDuration) : MyItem {
    private var end: MyTime = begin + duration

    override fun dump(): String {
        return "Time: $this"
    }

    override fun toString(): String {
        return "${this.begin}-${this.end}"
    }
}

class ItemProgress(private var progressInfo: ProgressInfo, var time: ItemTime,
                   private val dataIndex: Int, range: Int, var content: ProcessContent) : MyItem {
    private val dayIndex = ProgressData.dayIndexArray[dataIndex]
    var action: ProgressAction = ProgressAction(content, dayIndex)
    var prepareNotify: ProcessNotify? = null
    var notify: ProcessNotify? = null
    val type: ProgressType = content.type
    var daysInfo: String = weekDays[dayIndex].displayStr
    var tittle: String = content.name

    var range: Int = range
        set(value) {
            if (value > 0) {
                daysInfo += "~"
                val endDayIndex = ProgressData.displayDayIndexArray[ProgressData.data2displayIndexArray[dataIndex] + value]
                daysInfo += weekDays[endDayIndex].displayStr
            }
            field = value
        }

    init {
        if (type == ProgressType.CLASS) {
            prepareNotify = ProcessNotify()
        }
    }

    override fun dump(): String {
        return "Progress[$type]: $content in $daysInfo $time"
    }

    override fun toString(): String {
        return content.toString()
    }
}

class ProgressInfo(begin: MyTime, duration: MyDuration, private var contentArray: Array<ProcessContent>, private val mergeCell:Boolean = false) {

    var cellRowRanges: ArrayList<RowRange> = arrayListOf()
    var time:ItemTime = ItemTime(this, begin, duration)
    private var sunday: ItemProgress
    private var monday: ItemProgress
    private var tuesday: ItemProgress
    private var wednesday: ItemProgress
    private var thursday: ItemProgress
    private var friday: ItemProgress
    private var saturday: ItemProgress

    init {
        val weekItemArray: ArrayList<ItemProgress> = arrayListOf()
        for (i in 0..contentArray.lastIndex) {
            weekItemArray.add(i, ItemProgress(this, time, i, 0, contentArray[i]))
        }
        for (i in contentArray.lastIndex until WEEK_DAYS) {
            weekItemArray.add(i, ItemProgress(this, time, i, 0, contentArray[contentArray.lastIndex]))
        }

        if (mergeCell) {
            val displayItems: ArrayList<ItemProgress> = arrayListOf()
            for (index in ProgressData.displayDataIndexArray) {
                displayItems.add(weekItemArray[index])
            }

            var recordDisplayItem: ItemProgress? = null
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

    companion object{
        const val WEEK_DAYS = 7
    }
}

open class MyDuration(minutes: Int) : Comparable<MyDuration> {
    open var mMinutes: Int = minutes
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