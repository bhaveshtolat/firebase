package com.example.firebaserazorpay

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_forgot_password.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val TAG = javaClass.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        auth = FirebaseAuth.getInstance()

        tvSend.setOnClickListener {
            if (etEmail.text.toString().isNotEmpty()) {
                auth.sendPasswordResetEmail(etEmail.text.toString())
                    .addOnSuccessListener {
                        Log.e(TAG, "Check Email")
                        toast("Check Email")
                        startActivity<LoginActivity>()
                        finish()
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "Failure ${it.message.orEmpty()}")
                    }
            } else {
                toast("Enter Email")
            }
        }
    }
}
