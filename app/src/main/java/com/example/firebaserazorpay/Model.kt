package com.example.firebaserazorpay

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("name")
    val name: String,
    @SerializedName("mobile")
    val mobile: String,
    @SerializedName("refCode")
    var refCode: String,
    @SerializedName("refId")
    val refId: String,
    @SerializedName("myCode")
    val myCode: String,
    @SerializedName("point")
    val point: String,
    @SerializedName("withdraw")
    var withdraw: Int,
    @SerializedName("isRef")
    var isRef: Boolean,
    @SerializedName("isNot")
    var isNot: Boolean
) {
    constructor() : this("", "", "", "", "", "", 0, false, false)
}
