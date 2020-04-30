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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.liujk.study_assistant.view.MyWebView

class WebActivity : BaseActivity() {
    lateinit var myWebView: MyWebView
    lateinit var fullScreenVideo: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_web)
        myWebView = findViewById(R.id.my_web_view)
        fullScreenVideo = findViewById(R.id.full_screen_video)

        myWebView.webViewClient = object : WebViewClient() {
            override fun onRenderProcessGone(
                view: WebView?,
                detail: RenderProcessGoneDetail?
            ): Boolean {
                Log.v(TAG, "onRenderProcessGone")
                return super.onRenderProcessGone(view, detail)
            }

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

            override fun getDefaultVideoPoster(): Bitmap? {
                Log.v(TAG, "getDefaultVideoPoster")
                return super.getDefaultVideoPoster()
            }

            override fun getVideoLoadingProgressView(): View? {
                Log.v(TAG, "onShowCustomView")
                return super.getVideoLoadingProgressView()
            }

            var mCallback: CustomViewCallback? = null
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                Log.v(TAG, "onShowCustomView")
                myWebView.setVisibility(View.GONE)
                fullScreenVideo.setVisibility(View.VISIBLE)
                fullScreenVideo.addView(view)
                mCallback = callback
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
                super.onShowCustomView(view, callback)
            }

            override fun onHideCustomView() {
                Log.v(TAG, "onHideCustomView")
                mCallback?.onCustomViewHidden()
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
                builder.setTitle("来自：${url}")
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
            try {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.component =
                    ComponentName(context, WebActivity::class.java)
                intent.putExtra(DATA_URL, urlStr)
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "error:" + e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}