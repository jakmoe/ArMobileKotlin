package com.example.helloarkotlin.artest.api

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.helloarkotlin.artest.api.data.ArStoreComponents
import com.google.gson.Gson
import com.google.gson.JsonParseException
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class ApiRepository {
    private val apiUrl = "https://250exynmi7.execute-api.us-east-2.amazonaws.com/default/ArTesting/"

    private val interceptor = HttpLoggingInterceptor().also {
        it.level = HttpLoggingInterceptor.Level.BODY
    }
    private val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

    private val webservice: Webservice = Retrofit.Builder()
        .baseUrl(apiUrl)
        .client(client)
        .build()
        .create(Webservice::class.java)

    fun getArStore(DeviceId: String): LiveData<ArStoreComponents> {
        val data = MutableLiveData<ArStoreComponents>()
        webservice.getArStore(DeviceId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.body() == null) onFailure(call, Throwable("Body could not be parsed."))
                else {
                    // Log.i("TESTINGRESPONSE", response.body()!!.string())
                    try {
                        data.value = Gson().fromJson(response.body()?.string(), ArStoreComponents::class.java)
                    } catch (e: JsonParseException) {
                        Log.e("Gson Error", e.message)
                    }
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("Repository Error", t.message)
            }
        })
        return data
    }
}