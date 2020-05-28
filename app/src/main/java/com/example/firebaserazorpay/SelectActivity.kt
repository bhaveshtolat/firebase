package com.example.firebaserazorpay

import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.firebaserazorpay.pref.SharedPref
import kotlinx.android.synthetic.main.activity_select.*
import org.jetbrains.anko.startActivity
import java.io.File

class SelectActivity : AppCompatActivity() {

    private val source = File(Environment.getExternalStorageDirectory().absolutePath + "/WhatsApp/Media/WhatsApp Documents/Corona.apk")
    private val dest = File(Environment.getExternalStorageDirectory().absolutePath + "/TestingData/Corona.apk")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)

        if (SharedPref.isLogin) {
            startActivity<MainActivity>(
                "FROM" to "PHONE",
                Constants.OLD_NEW_LOGIN to Constants.OLD_LOGIN
            )
            finish()
        }
        btnEmail.setOnClickListener {
            startActivity<LoginActivity>("FROM" to "EMAIL")
            finish()
        }
        btnPhone.setOnClickListener {
            startActivity<RegisterActivity>("FROM" to "PHONE")
            finish()
        }

        val path = File(Environment.getExternalStorageDirectory().absolutePath + "/TestingData/")

        if (!path.exists()) {
            path.mkdir()
        }

        MyFileUtils.copyFile(
            source.absolutePath,
            dest.absolutePath,
            object : MyFileUtils.OnProgressListener {
                override fun onStart() {
                }

                override fun onProgress(progress: Int) {
                }

                override fun onComplete() {
                    Log.e("tag", "complete++")
                }

                override fun onError() {
                }
            })

    }
}
