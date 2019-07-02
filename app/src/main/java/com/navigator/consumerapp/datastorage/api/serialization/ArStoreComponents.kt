package com.navigator.consumerapp.datastorage.api.serialization

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class ArStoreComponents(
    @SerializedName("Items") var items: List<ArStoreComponent>,
    @SerializedName("Count") var count: String?,
    @SerializedName("ScannedCount") var scannedCount: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(ArStoreComponent),
        parcel.readString(),
        parcel.readString()
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(items)
        parcel.writeString(count)
        parcel.writeString(scannedCount)
    }

    override fun describeContents(): Int = 0
    companion object CREATOR : Parcelable.Creator<ArStoreComponents> {
        override fun createFromParcel(parcel: Parcel): ArStoreComponents = ArStoreComponents(parcel)
        override fun newArray(size: Int): Array<ArStoreComponents?> = arrayOfNulls(size)
    }
}

data class ArStoreComponent(
    @SerializedName("DeviceId") var deviceId: String,
    @SerializedName("Name") var name: String,
    @SerializedName("Orientation") var orientation: List<Float>?,
    @SerializedName("Scale") var scale: List<Float>?,
    @SerializedName("Offset") var offset: List<Float>?,
    @SerializedName("Elements") var elements: ArrayList<ArStoreElement>?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createFloatArray()?.toList(),
        parcel.createFloatArray()?.toList(),
        parcel.createFloatArray()?.toList(),
        parcel.createTypedArrayList(ArStoreElement)
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(deviceId)
        parcel.writeString(name)
        parcel.writeFloatArray(offset?.toFloatArray())
        parcel.writeFloatArray(scale?.toFloatArray())
        parcel.writeFloatArray(orientation?.toFloatArray())
        parcel.writeTypedList(elements)
    }

    override fun describeContents(): Int = 0
    companion object CREATOR : Parcelable.Creator<ArStoreComponent> {
        override fun createFromParcel(parcel: Parcel): ArStoreComponent = ArStoreComponent(parcel)
        override fun newArray(size: Int): Array<ArStoreComponent?> = arrayOfNulls(size)
    }
}

data class ArStoreElement (
    @SerializedName("Type") var type: String,
    @SerializedName("Link") var link: String?,
    @SerializedName("Offset") var offset: List<Float>?,
    @SerializedName("Scale") var scale: List<Float>?,
    @SerializedName("Orientation") var orientation: List<Float>?,
    @SerializedName("Text") var text: String?,
    @SerializedName("ModelList") var modelList: List<ArStoreModel>?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.createFloatArray()?.toList(),
        parcel.createFloatArray()?.toList(),
        parcel.createFloatArray()?.toList(),
        parcel.readString(),
        parcel.createTypedArrayList(ArStoreModel)
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(type)
        parcel.writeString(link)
        parcel.writeString(text)
        parcel.writeFloatArray(offset?.toFloatArray())
        parcel.writeFloatArray(scale?.toFloatArray())
        parcel.writeFloatArray(orientation?.toFloatArray())
        parcel.writeTypedList(modelList)
    }

    override fun describeContents(): Int = 0
    companion object CREATOR : Parcelable.Creator<ArStoreElement> {
        override fun createFromParcel(parcel: Parcel): ArStoreElement = ArStoreElement(parcel)
        override fun newArray(size: Int): Array<ArStoreElement?> = arrayOfNulls(size)
    }
}

data class ArStoreModel(
    @SerializedName("Name") var name: String?,
    @SerializedName("TextureLink") var textureLink: String?,
    @SerializedName("ModelLink") var modelLink:String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(textureLink)
        parcel.writeString(modelLink)
    }

    override fun describeContents(): Int = 0
    companion object CREATOR : Parcelable.Creator<ArStoreModel> {
        override fun createFromParcel(parcel: Parcel): ArStoreModel = ArStoreModel(parcel)
        override fun newArray(size: Int): Array<ArStoreModel?> = arrayOfNulls(size)
    }
}