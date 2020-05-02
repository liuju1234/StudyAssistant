package com.liujk.study_assistant.utils

import android.content.Context
import android.net.ConnectivityManager

object NetStatusUtil {
    val NETWORK_NONE = -1
    val NETWORK_MOBILE = 0
    val NETWORK_WIFI = 1
    fun getNetWorkState(context: Context): Int {
        // 得到连接管理器对象
        val connectivityManager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager
            .activeNetworkInfo
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
            if (activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI) {
                return NETWORK_WIFI
            } else if (activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE) {
                return NETWORK_MOBILE
            }
        } else {
            return NETWORK_NONE
        }
        return NETWORK_NONE
    }

    fun isConnected(context: Context): Boolean {
        return getNetWorkState(context) != NETWORK_NONE
    }
}
