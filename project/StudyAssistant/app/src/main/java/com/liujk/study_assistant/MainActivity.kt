package com.liujk.study_assistant

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.liujk.study_assistant.data.Config
import com.liujk.study_assistant.data.ProcessData
import com.liujk.study_assistant.data.ProcessInfo
import com.liujk.study_assistant.utils.DocumentsUtils
import com.liujk.study_assistant.view.MySmartTable
import java.io.File


const val TAG = "liujk_log"

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
    private lateinit var dataTable: MySmartTable<ProcessInfo>
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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun showOpenDocumentTree(rootPath: File) {
        var intent: Intent? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            val sm: StorageManager = this.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val volume = sm.getStorageVolume(rootPath)
            if (volume != null) {
                intent = volume.createAccessIntent(null)
            }
        }
        var showNotify = false
        if (intent == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                showNotify = true
                intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                val treeUri = DocumentsUtils.getTreeUriForStorage(this, rootPath)
                if (treeUri != null) {
                    intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, treeUri)
                }
            }
        }
        if (showNotify) {
            AlertDialog.Builder(this)
                .setMessage("如果需要自动创建配置文件，请在接下来的界面下方点击允许访问")
                .setTitle("提示")
                .setNegativeButton("确定") { _: DialogInterface, _: Int ->
                    startActivityForResult(intent, MY_REQUEST_OPEN_DOCUMENT_TREE)
                }
                .show()
        } else if (intent != null) {
            startActivityForResult(intent, MY_REQUEST_OPEN_DOCUMENT_TREE)
        }
    }

    lateinit var needWriteDriverPath: File
    private fun loadConfigFromFile() {
        val noConfigFile = Config.loadConfig(this)
        Log.v(TAG, "noConfigFile is $noConfigFile")
        if (noConfigFile) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                needWriteDriverPath = Config.getNeedWriteDriverPath(this)
                if (DocumentsUtils.isOnExtSdCard(needWriteDriverPath, this)) {
                    showOpenDocumentTree(needWriteDriverPath)
                    return
                }
            }
            afterSetStorage()
        }
    }

    private fun afterSetStorage() {
        Log.v(TAG, "Config.writeBuildInConfig()")
        Config.writeBuildInConfig(this)
    }

    private fun setUriForStorage(uri: Uri?) {
        if (uri != null) {
            DocumentsUtils.saveTreeUri(this, needWriteDriverPath.path, uri)
            afterSetStorage()
        } else {
            Log.v(TAG, "setUriForStorage(), but uri is null")
        }
    }

    private fun afterPermissionOK() {
        Log.v(TAG, "call afterPermissionOK()")
        loadConfigFromFile()
        dataTable.tableData = ProcessData.getInstance(this).toTableData()
    }

    private var requestPermissionOK: () -> Unit = {afterPermissionOK()}
    private var currentRequestCode = MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE

    private fun grantPermission() {
        grantPermission(Manifest.permission.READ_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            grantPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
                afterPermissionOK()
            }
        }
    }

    private fun grantPermission(permission: String, requestCode: Int, permissionOK : () -> Unit) {
        requestPermissionOK = permissionOK
        currentRequestCode = requestCode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        permission)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    Toast.makeText(this, "在应用信息中打开存储权限", Toast.LENGTH_LONG).show()
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                        arrayOf(permission),
                        requestCode)

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                permissionOK()
            }
        } else {
            permissionOK()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            currentRequestCode -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(this, "已授权存储权限", Toast.LENGTH_SHORT).show()
                    requestPermissionOK()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            MY_REQUEST_OPEN_DOCUMENT_TREE -> if (data != null && data.data != null) {
                Log.v(TAG, "onActivityResult($requestCode), data.data is ${data.data}")
                setUriForStorage(data.data as Uri?)
            }
            else -> {
            }
        }
    }

    companion object{
        private const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 100
        private const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 101
        private const val MY_REQUEST_OPEN_DOCUMENT_TREE = 102
    }
}
