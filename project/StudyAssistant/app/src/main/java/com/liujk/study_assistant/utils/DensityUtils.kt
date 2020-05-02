package com.liujk.study_assistant.utils

import android.content.Context
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.util.TypedValue.COMPLEX_UNIT_SP

object DensityUtils {
    fun dp2px(context: Context, dpVal: Float): Int {
        return TypedValue.applyDimension(
            COMPLEX_UNIT_DIP,
            dpVal,
            context.resources.displayMetrics
        ).toInt()
    }

    fun sp2px(context: Context, spVal: Float): Int {
        return TypedValue.applyDimension(
            COMPLEX_UNIT_SP,
            spVal,
            context.resources.displayMetrics
        ).toInt()
    }

    fun px2dp(context: Context, pxVal: Float): Float {
        val scale = context.resources.displayMetrics.density
        return pxVal / scale
    }

    fun px2sp(context: Context, pxVal: Float): Float {
        return pxVal / context.resources.displayMetrics.scaledDensity
    }
}
