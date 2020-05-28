package com.example.firebaserazorpay

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.firebaserazorpay.pref.SharedPref
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val tag = javaClass.canonicalName
    private lateinit var db: FirebaseFirestore
    private val collection = "Users"
    private var userId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (intent.getStringExtra("FROM") == "EMAIL") {
            etEmail.hint = "Enter Email"
        } else {
            etEmail.hint = "Enter Phone number"
        }

        if (intent.getBooleanExtra("IS_VERIFY", false)) {
            login()
        }

        tvRegisterNow.setOnClickListener {
            startActivity<RegisterActivity>("FROM" to intent.getStringExtra("FROM"))
        }

        tvLogin.setOnClickListener {
            if (validation()) {
                login()
            }
        }

        tvAfterVerify.setOnClickListener {
            startActivity<LoginActivity>("IS_VERIFY" to true)
            finish()
        }

        tvForgotPassword.setOnClickListener {
            startActivity<ForgotPasswordActivity>()
        }
    }

    private fun login() {
        auth.signInWithEmailAndPassword(etEmail.text.toString(), etPassword.text.toString())
            .addOnCompleteListener(this) { task ->
                Log.d(tag, "signInWithEmail: ${etEmail.text}")
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(tag, "signInWithEmail:success")
                    val user = auth.currentUser

                    if (user != null) {
                        if (user.isEmailVerified) {
                            user.uid.let { userID ->
                                userId = userID
                                db.collection(collection).document(userID).get()
                                    .addOnSuccessListener { loginData ->
                                        Log.e(tag, "Login DATA: ${loginData.data}")
                                        toast("Login Success")

                                        SharedPref.isLogin = true
                                        SharedPref.userId = userID
                                        SharedPref.userData = loginData.toObject(User::class.java)

                                        startActivity<MainActivity>(
                                            "FROM" to "EMAIL",
                                            Constants.OLD_NEW_LOGIN to Constants.OLD_LOGIN
                                        )
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        Log.e(tag, "Failure" + it.message)
                                        toast("Something wrong")
                                    }
                            }
                        } else {

                            user.sendEmailVerification().addOnCompleteListener { task1 ->
                                if (task1.isSuccessful) {
                                    /*tvLogin.alpha = 0.5f
                                    tvLogin.isEnabled = false*/
                                    tvVerify.visibility = View.VISIBLE
                                    Handler().postDelayed({
                                        tvVerify.visibility = View.GONE
                                    }, 3000)
                                    Log.e(tag, "Check Email")
                                } else {
                                    Log.e(tag, "Failure" + task1.exception?.message)
                                    Handler().postDelayed({
                                        tvVerify.visibility = View.GONE
                                    }, 3000)
                                }
                            }
                        }
                    }

                } else {
                    Log.w(tag, "signInWithEmail:failure " + task.exception?.message.orEmpty())
                    toast("You not found in server")
                }
            }
    }

    private fun validation(): Boolean =
        when {
            etEmail.text.toString().isEmpty() -> {
                toast("Enter Email")
                false
            }
            etPassword.text.toString().isEmpty() -> {
                toast("Enter Password")
                false
            }
            else -> {
                true
            }
        }
}
