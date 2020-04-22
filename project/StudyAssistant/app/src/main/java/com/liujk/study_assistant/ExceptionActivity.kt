package com.liujk.study_assistant

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.liujk.study_assistant.utils.Utils

class ExceptionActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exception)

        val throwableInfo = intent.getStringExtra(DATA_THROWABLE) ?: ""
        val infoView: TextView = findViewById(R.id.message_info)
        infoView.text = commonInfo() + throwableInfo
    }

    fun commonInfo(): String {
        return "SDK Version: " + Build.VERSION.SDK_INT + "\n"
    }

    companion object{
        const val DATA_THROWABLE = "MY_DATA_THROWABLE"
        fun display(context: Context, throwable: Throwable) {
            try {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.component =
                    ComponentName(context.packageName, context.packageName + ".ExceptionActivity")
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra(DATA_THROWABLE, Utils.getThrowableInfo(throwable))
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "error:" + e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}