package com.example.helloarkotlin.artest.api

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.helloarkotlin.artest.api.data.ArStoreComponents
import javax.inject.Inject


class ApiViewModel @Inject constructor(
    apiRepository: ApiRepository,
    DeviceId: String
): ViewModel() {
    val arStore : LiveData<ArStoreComponents> = apiRepository.getArStore(DeviceId)
}