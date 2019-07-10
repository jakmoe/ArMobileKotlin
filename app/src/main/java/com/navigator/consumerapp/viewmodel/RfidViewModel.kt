package com.navigator.consumerapp.viewmodel

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.navigator.consumerapp.rfid.repo.RfidRepository
import javax.inject.Inject

class RfidViewModel @Inject constructor(rfidRepository: RfidRepository, intent: Intent): ViewModel() {
    val deviceId : LiveData<String> = rfidRepository.getRfidDeviceId(intent)
}