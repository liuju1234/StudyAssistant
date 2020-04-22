package com.liujk.study_assistant.utils

class Utils {
    companion object{

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

    }
}