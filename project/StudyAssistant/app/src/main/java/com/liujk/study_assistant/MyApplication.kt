package com.liujk.study_assistant

import android.app.Application
import com.liujk.study_assistant.utils.AppCrashHandler

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        AppCrashHandler(applicationContext)
    }
}