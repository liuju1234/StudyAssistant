package com.liujk.study_assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings;
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.liujk.study_assistant.data.ProgressData
import com.liujk.study_assistant.data.ProgressInfo
import com.liujk.study_assistant.view.MySmartTable

const val TAG = "liujk_log"
private const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 100

abstract class BaseActivity: AppCompatActivity() {
    var canFinish: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        canFinish = true
        activitySet.add(this)
    }

    override fun finish() {
        super.finish()
        canFinish = false
    }

    override fun onDestroy() {
        super.onDestroy()
        activitySet.remove(this)
    }

    companion object {
        val activitySet = HashSet<BaseActivity>()
        fun finishAll() {
            for (activity in activitySet) {
                if (activity.canFinish) {
                    activity.finish()
                }
            }
        }
    }
}

class MainActivity : BaseActivity() {
    private lateinit var dataTable: MySmartTable<ProgressInfo>
    private lateinit var headerText: TextView
    private lateinit var buttonCast: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        dataTable = findViewById(R.id.data_table)
        headerText = findViewById(R.id.header_text)

        buttonCast = findViewById(R.id.button_cast)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            buttonCast.setOnClickListener {
                val intent = Intent(Settings.ACTION_CAST_SETTINGS)
                startActivity(intent)
            }
        } else {
            buttonCast.visibility = View.GONE
        }
        grantPermission()
        //displayInfo()
    }

    fun displayInfo() {
        val displayMetrics = this.resources.displayMetrics
        headerText.setText("SDK Version: ${Build.VERSION.SDK_INT} " +
                "density: ${displayMetrics.density} " +
                "densityDpi: ${displayMetrics.densityDpi} " +
                "xdpi: ${displayMetrics.xdpi} " +
                "ydpi: ${displayMetrics.ydpi} " +
                "widthPixels: ${displayMetrics.widthPixels} " +
                "heightPixels: ${displayMetrics.heightPixels} " +
                "scaledDensity: ${displayMetrics.scaledDensity} ")
        headerText.visibility = View.VISIBLE
    }

    fun afterPermissionOK() {
        Log.v(TAG, "call afterPermissionOK()")
        dataTable.setTableData(ProgressData.getInstance(this, dataTable).toTableData())
    }

    fun grantPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    Toast.makeText(this, "在应用信息中打开存储权限", Toast.LENGTH_LONG).show()
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                afterPermissionOK()
            }
        } else {
            afterPermissionOK()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(this, "已授权存储权限", Toast.LENGTH_SHORT).show()
                    afterPermissionOK()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "已拒绝存储权限，功能无法正常使用", Toast.LENGTH_LONG).show()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }
}
