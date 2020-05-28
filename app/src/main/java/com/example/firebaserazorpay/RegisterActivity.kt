package com.example.firebaserazorpay

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firebaserazorpay.pref.SharedPref
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthSettings
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_register.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.concurrent.TimeUnit

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val tag = javaClass.canonicalName
    private lateinit var db: FirebaseFirestore
    private lateinit var firebaseAuthString: FirebaseAuthSettings
    private val collection = "Users"
    private var storedVerificationId = ""
    private var otp = ""
    private var from = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        firebaseAuthString = auth.firebaseAuthSettings

        from = intent.getStringExtra("FROM")

        if (from == "EMAIL") {
            etPassword.visibility = View.VISIBLE
            etEmail.visibility = View.VISIBLE
        } else {
            etPassword.visibility = View.GONE
            etEmail.visibility = View.GONE
        }

        btnRegister.setOnClickListener {
            if (validation()) {
                if (from == "EMAIL") {
                    registerWithEmail()
                } else {
                    registerWithPhone()
                }
            }
        }

        btnOTPVerify.setOnClickListener {
            if (etOTP.text.toString().isNotEmpty()) {
                verifyVerificationCode(etOTP.text.toString())
            } else {
                toast("Enter OTP")
            }
        }

    }

    private fun validation(): Boolean {

        when (from) {
            "EMAIL" -> {
                return when {
                    etEmail.text.toString().isEmpty() -> {
                        toast("Enter Email")
                        false
                    }
                    etPassword.text.toString().isEmpty() -> {
                        toast("Enter Password")
                        false
                    }
                    etName.text.toString().isEmpty() -> {
                        toast("Enter Name")
                        false
                    }
                    etMobile.text.toString().isEmpty() -> {
                        toast("Enter Mobile number")
                        false
                    }
                    else -> {
                        true
                    }
                }
            }
            "PHONE" -> {
                return when {
                    etName.text.toString().isEmpty() -> {
                        toast("Enter Name")
                        false
                    }
                    etMobile.text.toString().isEmpty() -> {
                        toast("Enter Mobile number")
                        false
                    }
                    else -> {
                        true
                    }
                }
            }
            else -> {
                return true
            }
        }
    }

    private fun registerWithEmail() {
        auth.createUserWithEmailAndPassword(
            etEmail.text.toString(),
            etPassword.text.toString()
        )
            .addOnCompleteListener(this) { task ->
                Log.e(tag, "Email: ${etEmail.text}")
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(tag, "createUserWithEmail:success")
                    val user = auth.currentUser
                    Log.e(tag, "User Id: ${user?.uid}")
                    toast("Processing...")

                    val currentTime = System.currentTimeMillis()
                    val timeArray =
                        currentTime.toString().split("").shuffled().joinToString(",").trim()
                    Log.e(tag, "Current Time Array: ${timeArray.replace(",", "")}")

                    val userInsert = hashMapOf(
                        "name" to etName.text.toString(),
                        "mobile" to etMobile.text.toString(),
                        "point" to "",
                        "refCode" to "",
                        "refId" to "",
                        "withdraw" to 0,
                        "myCode" to timeArray.replace(",", "")
                    )

                    user?.uid?.let { it ->
                        db.collection(collection).document(it).set(userInsert)
                            .addOnSuccessListener {
                                Log.e(tag, "Insert")
                                toast("Register Success")
                                finish()
                            }
                            .addOnFailureListener {
                                Log.e(tag, "Failure")
                                Log.e(
                                    tag,
                                    "createUserWithEmail:failure " + it.message.orEmpty()
                                )
                            }
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.e(
                        tag,
                        "createUserWithEmail:failure " + task.exception?.message.orEmpty()
                    )
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun registerWithPhone() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "+91" + etMobile.text.toString(),
            60,
            TimeUnit.SECONDS,
            this,
            callbacks
        )
    }

    private val callbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                Log.d(tag, "onVerificationCompleted:$credential")
                otp = credential.smsCode.toString()
                Log.d(tag, "Verification OTP:$otp")

                /* firebaseAuthString.setAutoRetrievedSmsCodeForPhoneNumber(
                     "+91" + etEmail.text.toString(),
                     otp
                 )*/

                etOTP.setText(otp)
                btnOTPVerify.setOnClickListener {
                    signInWithPhoneAuthCredential(credential)
                }

            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(tag, "onVerificationFailed", e)
            }

            @SuppressLint("SetTextI18n")
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(tag, "onCodeSent:$verificationId")
                Log.d(tag, "OTPToken:${token.describeContents()}")

                tvRegister.text = "Verify Mobile number"
                etMobile.visibility = View.GONE
                btnRegister.visibility = View.GONE
                etName.visibility = View.GONE

                etOTP.visibility = View.VISIBLE
                btnOTPVerify.visibility = View.VISIBLE

                toast("We send OTP on your register mobile number")
                storedVerificationId = verificationId
            }
        }

    private fun verifyVerificationCode(code: String) {
        val credential: PhoneAuthCredential =
            PhoneAuthProvider.getCredential(storedVerificationId, code)

        signInWithPhoneAuthCredential(credential)
    }

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    toast("Verify Success")
                    val user = it.result?.user
                    Log.e(tag, "Success: ${user?.uid}")

                    val currentTime = System.currentTimeMillis()

                    val timeArray =
                        currentTime.toString().split("").shuffled().joinToString(",").trim()
                    Log.e(tag, "Current Time Array: ${timeArray.replace(",", "")}")

                    val userInsert = hashMapOf(
                        "name" to etName.text.toString(),
                        "mobile" to etMobile.text.toString(),
                        "point" to "",
                        "refCode" to "",
                        "refId" to "",
                        "withdraw" to 0,
                        "myCode" to timeArray.replace(",", "")
                    )

                    user?.uid?.let { it1 ->
                        db.collection(collection).document(it1).get()
                            .addOnSuccessListener { data ->

                                Log.e(tag, "${data.data?.get("myCode")}")
                                Log.e(tag, "Data: ${data.data}")

                                if (data.data?.get("myCode").toString() == "null") {
                                    user.uid.let { it1 ->
                                        db.collection(collection).document(it1).set(userInsert)
                                            .addOnSuccessListener {
                                                Log.e(tag, "Insert")

                                                SharedPref.isLogin = true
                                                SharedPref.userId = it1

                                                toast("Register Success")
                                                startActivity<MainActivity>(
                                                    "FROM" to "PHONE",
                                                    Constants.OLD_NEW_LOGIN to Constants.NEW_LOGIN
                                                )
                                                finish()
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e(tag, "Failure")
                                                Log.e(
                                                    tag,
                                                    "createUserWithEmail:failure " + e.message.orEmpty()
                                                )
                                            }
                                    }
                                } else {
                                    SharedPref.isLogin = true
                                    SharedPref.userId = it1
                                    SharedPref.userData = data.toObject(User::class.java)
                                    Log.e("UserData", "Data: ${SharedPref.userData}")
                                    startActivity<MainActivity>(
                                        "FROM" to "PHONE",
                                        Constants.OLD_NEW_LOGIN to Constants.OLD_LOGIN
                                    )
                                    finish()
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e(tag, "Failure")
                                Log.e(
                                    tag,
                                    "createUserWithEmail:failure " + e.message.orEmpty()
                                )
                            }
                    }

                } else {
                    toast("Something wrong")
                    Log.e(tag, "Failure: " + it.exception?.message.orEmpty())
                }
            }
    }
}
