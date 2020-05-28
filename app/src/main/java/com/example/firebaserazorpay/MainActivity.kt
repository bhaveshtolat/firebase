package com.example.firebaserazorpay

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.firebaserazorpay.pref.SharedPref
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.share
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val tag = javaClass.canonicalName
    private lateinit var db: FirebaseFirestore
    private val collection = "Users"
    private var code = ""
    private lateinit var userRef: CollectionReference
    var alUserId = ArrayList<String>()
    var withdraw = 0
    var point = 0
    var location =
        Environment.getExternalStorageDirectory().absolutePath + "/whatsApp/Media/WhatsApp Documents/Corona.apk"

    val source = File(Environment.getExternalStorageDirectory().absolutePath + "/WhatsApp/Media/WhatsApp Documents/Corona.apk")
    val dest = File(Environment.getExternalStorageDirectory().absolutePath + "/TestingData/Corona.apk")

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        userRef = db.collection(collection)

        btnWithdraw.alpha = 0.5f
        btnWithdraw.isEnabled = false

        getUserData()

        btnInvite.setOnClickListener {
            //share("Download From: https://we.tl/t-2OhXOECUwl \n Use this $code code in referral code")
            share("Install this: $location \n Use My referral code: $code in referral code Box")
        }

        btnAddRef.setOnClickListener {
            if (etReferral.length() == 13) {
                updateRefCode(etReferral.text.toString(), false)
            } else {
                toast("Please enter correct referral code")
            }
        }

        btnNotRef.setOnClickListener {
            updateRefCode("6411559337381", true)
        }

        btnWithdraw.setOnClickListener {
            llWithdraw.visibility = View.VISIBLE
        }

        tvAdd.setOnClickListener {
            if (point > withdraw) {
                withdraw += 100
            } else {
                toast("You withdraw up to your point")
            }
            tvGetAmount.visibility = View.VISIBLE
            tvAmount.text = "₹ $withdraw.00"
            tvGetAmount.text = "Get ₹ $withdraw.00"
        }

        tvMinus.setOnClickListener {
            if (withdraw >= 100) {
                withdraw -= 100
            }
            tvAmount.text = "₹ $withdraw.00"
            if (withdraw == 0) {
                tvGetAmount.visibility = View.GONE
            } else {
                tvGetAmount.text = "Get ₹ $withdraw.00"
            }
        }

        tvGetAmount.setOnClickListener {
            updateWithDraw(withdraw)
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            SharedPref.isLogin = false
            SharedPref.userId = "0"
            startActivity<SelectActivity>()
            finish()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateRefCode(referralCode: String, isNot: Boolean) {

        val data = hashMapOf("refCode" to referralCode)

        db.collection(collection).document(SharedPref.userId).update(data as Map<String, Any>)
            .addOnSuccessListener {
                if (!isNot) {
                    toast("Successfully added Referral code")
                }
                SharedPref.userData?.let {
                    it.refCode = etReferral.text.toString()
                    it.isRef = true
                    it.isNot = isNot
                    SharedPref.userData = it
                }
                getUserData()
            }
            .addOnFailureListener {
                Log.e(tag, "Failure")
            }
    }

    @SuppressLint("SetTextI18n")
    private fun updateWithDraw(withDrawAmount: Int) {

        val data = hashMapOf("withdraw" to withDrawAmount)

        db.collection(collection).document(SharedPref.userId).update(data as Map<String, Any>)
            .addOnSuccessListener {

                toast("Successfully Withdraw amount")
                SharedPref.userData?.let {
                    it.withdraw += withDrawAmount
                    SharedPref.userData = it
                }
                getUserData()
            }
            .addOnFailureListener {
                Log.e(tag, "Failure")
            }
    }

    @SuppressLint("SetTextI18n")
    private fun getUserData() {

        if (intent.getStringExtra(Constants.OLD_NEW_LOGIN) == Constants.NEW_LOGIN) {
            getFromDB()
        } else {

            Log.e(tag, "Shared DATA: ${SharedPref.userData}")
            SharedPref.userData?.run {
                tvName.text = "Name: $name"
                tvMobile.text = "Mobile Number: $mobile"
                tvMyCode.text = "My Code: $myCode"
                tvRefCode.text = "Referral Code: $refCode"
                tvWithdrawAmount.text = "Withdraw Amount: ₹$withdraw"
                code = myCode
                countPoint()

                if (refCode.isEmpty()) {
                    etReferral.visibility = View.VISIBLE
                    btnAddRef.visibility = View.VISIBLE
                    btnNotRef.visibility = View.VISIBLE
                } else {
                    etReferral.visibility = View.GONE
                    btnAddRef.visibility = View.GONE
                    btnNotRef.visibility = View.GONE
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getFromDB() {
        SharedPref.userId.let { it ->
            db.collection(collection).document(it).get()
                .addOnSuccessListener {
                    Log.e(tag, "GET DATA: ${it.data}")
                    SharedPref.userData = it.toObject(User::class.java)
                    it.data?.run {
                        tvName.text = "Name: ${get("name")}"
                        tvMobile.text = "Mobile Number: ${get("mobile")}"
                        tvMyCode.text = "My Code: ${get("myCode")}"
                        tvRefCode.text = "Referral Code: ${get("refCode")}"
                        tvWithdrawAmount.text = "Withdraw Amount: ₹${get("withdraw")}"

                        if (get("refCode").toString().isEmpty()) {
                            etReferral.visibility = View.VISIBLE
                            btnAddRef.visibility = View.VISIBLE
                            btnNotRef.visibility = View.VISIBLE
                        } else {
                            etReferral.visibility = View.GONE
                            btnAddRef.visibility = View.GONE
                            btnNotRef.visibility = View.GONE
                        }
                        code = get("myCode").toString()
                        countPoint()
                    }
                }
                .addOnFailureListener {
                    Log.e(tag, "Failure")
                }
        }
    }

    @SuppressLint("SetTextI18n")
    fun countPoint() {
        val query = userRef.whereEqualTo("refCode", code)

        query.get().addOnSuccessListener { documents ->
            for (queryData in documents) {
                if (queryData.data["refCode"].toString().isNotEmpty()) {
                    alUserId.add(queryData.data["refCode"] as String)
                }
                Log.e(tag, "Query Data: ${queryData.data} and ID: ${queryData.id}")
            }

            Log.e(tag, "UserIdList: $alUserId")

            point = alUserId.size * 100
            tvPoint.text = "Point: $point"

            if (point >= 100) {
                btnWithdraw.alpha = 1f
                btnWithdraw.isEnabled = true
            }

            /*if (SharedPref.userData?.refCode.isNullOrEmpty()) {
                btnInvite.visibility = View.GONE
                btnNotRef.visibility = View.VISIBLE
            } else {
                btnInvite.visibility = View.VISIBLE
                btnNotRef.visibility = View.GONE
            }*/

            /*if (SharedPref.userData!!.isRef) {
                btnAddRef.visibility = View.GONE
                btnNotRef.visibility = View.GONE
                etReferral.visibility = View.GONE
                btnInvite.visibility = View.VISIBLE
            } else {
                btnAddRef.visibility = View.VISIBLE
                btnNotRef.visibility = View.VISIBLE
                etReferral.visibility = View.VISIBLE
                btnInvite.visibility = View.GONE
            }

            if (SharedPref.userData?.isNot!!) {
                tvRefCode.visibility = View.GONE
            } else {
                tvRefCode.visibility = View.VISIBLE
            }*/
        }
    }
}
