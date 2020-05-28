package com.example.firebaserazorpay.pref

import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.gsonpref.gsonNullablePref
import com.example.firebaserazorpay.User

object SharedPref : KotprefModel() {

    var isLogin by booleanPref()

    var userId by stringPref()

    var userData by gsonNullablePref<User>()
}