package com.example.helloarkotlin.artest.api.data

import com.google.gson.annotations.SerializedName

data class ArStoreComponents(
    @SerializedName("Items")
    var items: List<ArStoreComponent>,

    @SerializedName("Count")
    var count: String?,

    @SerializedName("ScannedCount")
    var scannedCount: String?
)