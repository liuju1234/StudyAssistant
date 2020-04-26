package com.liujk.study_assistant.view

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_BACK
import android.webkit.WebSettings
import android.webkit.WebView

class MyWebView : WebView {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {}

    var webSettings: WebSettings = this.settings
    init {

        webSettings.setLoadWithOverviewMode(true)
        webSettings.setJavaScriptEnabled(true)
        //webSettings.setUseWideViewPort(true)
        webSettings.setSupportZoom(true)
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true)
        //webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setGeolocationEnabled(true)
        webSettings.setDomStorageEnabled(true)
        webSettings.setDatabaseEnabled(true)
        webSettings.setAllowFileAccess(true)
        webSettings.setSupportZoom(true)
        //webSettings.setUseWideViewPort(true)
        webSettings.setBuiltInZoomControls(true);
        //webSettings.setLoadWithOverviewMode(true)
        //webSettings.setPluginState(WebSettings.PluginState.ON)
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSettings.setPluginState(WebSettings.PluginState.ON_DEMAND)
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE)
        webSettings.setAllowUniversalAccessFromFileURLs(true)
        webSettings.setLoadsImagesAutomatically(true)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }
        } catch (e: Throwable) {
            // ignore
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KEYCODE_BACK && canGoBack()) {
            goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}