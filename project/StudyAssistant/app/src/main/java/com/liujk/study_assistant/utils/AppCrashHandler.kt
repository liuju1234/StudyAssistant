package com.liujk.study_assistant.utils

import android.content.Context
import android.os.Process
import android.util.Log
import com.liujk.study_assistant.BaseActivity
import com.liujk.study_assistant.ExceptionActivity
import com.liujk.study_assistant.TAG

class AppCrashHandler(val context: Context) : Thread.UncaughtExceptionHandler {
    private val mDefaultHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(
        thread: Thread,
        ex: Throwable
    ) {
        Log.e(TAG, Utils.getThrowableInfo(ex))
        BaseActivity.finishAll()
        ExceptionActivity.display(context, ex)
        Process.killProcess(Process.myPid())
    }
}
