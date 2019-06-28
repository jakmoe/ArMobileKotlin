package com.example.helloarkotlin.artest.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface Webservice {
    /**
     * @GET declares an HTTP GET request
     */
    @GET("ArStore/{DeviceId}")
    fun getArStore(@Path("DeviceId") DeviceId: String): Call<ResponseBody>

}

