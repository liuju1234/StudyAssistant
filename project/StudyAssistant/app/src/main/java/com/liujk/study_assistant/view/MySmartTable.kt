package com.liujk.study_assistant.view

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.bin.david.form.core.SmartTable
import com.bin.david.form.data.style.FontStyle
import com.bin.david.form.data.style.LineStyle
import com.liujk.study_assistant.R

class MySmartTable<T> : SmartTable<T> {
    constructor(context: Context?) : super(context) {
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    init {
        config.isShowTableTitle = true
        config.horizontalPadding = resources.getDimensionPixelSize(R.dimen.table_h_padding)
        config.isShowXSequence = false
        config.isShowYSequence = false


        config.columnTitleBackgroundColor =
            ContextCompat.getColor(context, R.color.table_tittle_bg_color)
        val fontStyle = FontStyle(
            resources.getDimensionPixelSize(R.dimen.table_text_size),
            ContextCompat.getColor(context, R.color.table_text_color))
        config.tableTitleStyle = fontStyle
        config.columnTitleStyle = fontStyle
        config.contentStyle = fontStyle
        val gridStyle = LineStyle(
            resources.getDimension(R.dimen.table_line_width),
            ContextCompat.getColor(context, R.color.table_line_color))
        config.gridStyle = gridStyle
        config.columnTitleGridStyle = gridStyle

    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) { // get calculate mode of width and height
        var modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        var modeHeight = MeasureSpec.getMode(heightMeasureSpec)
        // get recommend width and height
        var sizeWidth = MeasureSpec.getSize(widthMeasureSpec)
        var sizeHeight = MeasureSpec.getSize(heightMeasureSpec)
        if (modeWidth == MeasureSpec.AT_MOST) { // wrap_content
            sizeWidth -= resources.getDimensionPixelSize(R.dimen.table_left_padding)
            modeWidth = MeasureSpec.EXACTLY
        }
        if (modeHeight == MeasureSpec.AT_MOST) { // wrap_content
            sizeHeight -= resources.getDimensionPixelSize(R.dimen.table_bottom_padding)
            modeHeight = MeasureSpec.EXACTLY
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(sizeWidth, modeWidth),
            MeasureSpec.makeMeasureSpec(sizeHeight, modeHeight))
    }
}