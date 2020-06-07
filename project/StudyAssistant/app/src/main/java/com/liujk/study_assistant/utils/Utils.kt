package com.liujk.study_assistant.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent

object Utils {
    fun getThrowableInfo(throwable: Throwable) : String {
        val stringBuffer = StringBuffer()
        fillThrowableToStringBuffer(throwable, stringBuffer)
        return stringBuffer.toString()
    }

    fun fillThrowableToStringBuffer(
        throwable: Throwable,
        stringBuffer: StringBuffer
    ) {
        stringBuffer.append(throwable.toString())
        stringBuffer.append('\n')
        for (stackTraceElement in throwable.stackTrace) {
            stringBuffer.append("\tat ")
            stringBuffer.append(stackTraceElement.toString())
            stringBuffer.append('\n')
        }
        val cause = throwable.cause
        if (cause != null) {
            stringBuffer.append("caused by: ")
            fillThrowableToStringBuffer(cause, stringBuffer)
        }
    }

    fun componentNameFromStr(componentNameStr: String) : ComponentName? {
        if (componentNameStr == "") {
            return null
        }
        val componentNameSplit = componentNameStr.split('/', limit = 2)
        if (componentNameSplit.size == 2) {
            return ComponentName(componentNameSplit[0], componentNameSplit[1])
        }
        return null
    }

    fun intentFromPackageName(context: Context, packageName: String) : Intent? {
        return context.packageManager.getLaunchIntentForPackage(packageName)
    }

    fun intentFromComponentName(componentNameStr: String, action: String? = null) : Intent? {
        val componentName = componentNameFromStr(componentNameStr)
        if (componentName != null) {
            var realAction = Intent.ACTION_MAIN
            if (action != null) realAction = action
            val intentPrepare = Intent(realAction)
            intentPrepare.addCategory(Intent.CATEGORY_LAUNCHER)
            intentPrepare.setPackage(componentName.packageName)
            val intent = Intent(intentPrepare)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setClassName(componentName.packageName, componentName.className)
            return intent
        }
        return null
    }
}