package com.example.firebaserazorpay

import androidx.multidex.MultiDexApplication
import com.chibatching.kotpref.Kotpref
import com.chibatching.kotpref.gsonpref.gson
import com.google.gson.Gson

class MyApp : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        Kotpref.init(this)
        Kotpref.gson = Gson()
    }
}