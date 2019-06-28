package com.navigator.consumerapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.navigator.consumerapp.datastorage.api.repo.ApiRepository
import com.navigator.consumerapp.datastorage.api.serialization.ArStoreComponents
import javax.inject.Inject

class ApiViewModel @Inject constructor(apiRepository: ApiRepository, DeviceId: String): ViewModel() {
    val arStore : LiveData<ArStoreComponents> = apiRepository.getArStore(DeviceId)
}