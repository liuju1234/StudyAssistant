package com.liujk.study_assistant

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.*
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.liujk.study_assistant.view.MyWebView


class WebActivity : AppCompatActivity() {
    lateinit var myWebView: MyWebView
    lateinit var fullScreenVideo: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_web)
        myWebView = findViewById(R.id.my_web_view)
        fullScreenVideo = findViewById(R.id.full_screen_video)

        myWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                //使用WebView加载显示url
                view.loadUrl(url)
                //返回true
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.v(TAG, "onPageStarted")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.v(TAG, "onPageFinished($url)")
                Log.v(TAG, "BrowserJsInject(${BrowserJsInject.fullScreenByJs(url?:"")})")
                view?.loadUrl(BrowserJsInject.fullScreenByJs(url?:""))
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                Log.v(TAG, "onReceivedError")
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                super.onReceivedSslError(view, handler, error)
                Log.v(TAG, "onReceivedSslError")
            }
        }

        val thisWebActivity = this
        myWebView.webChromeClient = object : WebChromeClient() {

            var mCallback: CustomViewCallback? = null
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                Log.v(TAG, "onShowCustomView")
                myWebView.setVisibility(View.GONE)
                fullScreenVideo.setVisibility(View.VISIBLE)
                fullScreenVideo.addView(view)
                mCallback = callback
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                super.onShowCustomView(view, callback)
            }

            override fun onHideCustomView() {
                Log.v(TAG, "onHideCustomView")
                myWebView.setVisibility(View.VISIBLE)
                fullScreenVideo.setVisibility(View.GONE)
                fullScreenVideo.removeAllViews()
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER)
                super.onHideCustomView()
            }
            override fun onJsAlert(
                view: WebView?, url: String?, message: String?,
                result: JsResult?
            ): Boolean {
                val builder = AlertDialog.Builder(thisWebActivity)
                builder.setTitle("自定义alert事件")
                builder.setMessage(message)
                builder.show()
                return super.onJsAlert(view, url, message, result)
            }
        }

        val urlStr = intent.getStringExtra(DATA_URL) ?: ""

        val cookieManager: CookieManager = CookieManager.getInstance()
        val stringBuffer = StringBuffer()
        stringBuffer.append("android")

        cookieManager.setCookie(urlStr, stringBuffer.toString())
        cookieManager.setAcceptCookie(true)
        if (urlStr != "") {
            myWebView.loadUrl(urlStr)
        }

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (myWebView.onKeyDown(keyCode, event)) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        myWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
        myWebView.clearHistory()
        (myWebView.parent as ViewGroup).removeView(myWebView)
        myWebView.destroy()

        super.onDestroy()
    }

    companion object{
        const val DATA_URL = "MY_DATA_URL"
        fun start(context: Context, urlStr: String) {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.component = ComponentName(context.packageName, context.packageName + ".WebActivity")
            intent.putExtra(DATA_URL, urlStr)
            context.startActivity(intent)
        }
    }
}

internal object BrowserJsInject {
    /**
     * Js注入
     * @param url 加载的网页地址
     * @return 注入的js内容，若不是需要适配的网址则返回空javascript
     */
    fun fullScreenByJs(url: String): String {
        val refer = referParser(url)
        return if (null != refer) {
            val js3 = ("window.onload=function(){document.getElementsByClassName('"
                    + referParser(url) + "')[0].addEventListener('click',function(){alert('120');" +
                    "console.log();" +
                    "alert('110');})}"
                    + ";")
            "javascript:$js3"
        } else {
            "javascript:"
        }
    }

    /**
     * 对不同的视频网站分析相应的全屏控件
     * @param url 加载的网页地址
     * @return 相应网站全屏按钮的class标识
     */
    fun referParser(url: String): String? {
        if (url.contains("letv")) {
            return "hv_ico_screen" //乐视Tv
        } else if (url.contains("youku")) {
            return "x-zoomin" //优酷
        } else if (url.contains("bilibili")) {
            return "icon-widescreen" //bilibili
        } else if (url.contains("qq")) {
            return "tvp_fullscreen_button" //腾讯视频
        }
        return null
    }
}