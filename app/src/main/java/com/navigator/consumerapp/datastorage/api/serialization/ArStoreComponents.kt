package com.navigator.consumerapp.datastorage.api.serialization

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class ArStoreComponents(
    @SerializedName("Items") var items: List<ArStoreComponent>,
    @SerializedName("Count") var count: String?,
    @SerializedName("ScannedCount") var scannedCount: String?
)

data class ArStoreComponent(
    @SerializedName("DeviceId") var deviceId: String,
    @SerializedName("Name") var name: String,
    @SerializedName("Orientation") var orientation: JsonElement,
    @SerializedName("Scale") var scale: List<Float>,
    @SerializedName("Offset") var offset: List<Float>,
    @SerializedName("Elements") var elements: List<ArStoreElement>
)

data class ArStoreElement (
    @SerializedName("Type") var type: String,
    @SerializedName("Link") var link: String,
    @SerializedName("Offset") var offset: List<Float>,
    @SerializedName("Scale") var scale: List<Float>,
    @SerializedName("Orientation") var orientation: List<Float>,
    @SerializedName("Text") var text: String,
    @SerializedName("ModelList") var modelList: List<ArStoreModel>
)

data class ArStoreModel(
    @SerializedName("Name") var name: String,
    @SerializedName("TextureLink") var textureLink: String,
    @SerializedName("ModelLink") var modelLink:String
)