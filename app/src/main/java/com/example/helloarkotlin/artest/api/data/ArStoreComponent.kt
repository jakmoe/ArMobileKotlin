package com.example.helloarkotlin.artest.api.data

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class ArStoreComponent(
    @SerializedName("DeviceId")
    var deviceId: String,

    @SerializedName("Name")
    var name: String,

    @SerializedName("Orientation")
    var orientation: JsonElement,

    @SerializedName("Scale")
    var scale: JsonElement,

    @SerializedName("Offset")
    var offset: JsonElement,

    @SerializedName("Elements")
    var elements: JsonElement
)