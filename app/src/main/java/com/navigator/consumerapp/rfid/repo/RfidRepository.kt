package com.navigator.consumerapp.rfid.repo

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class RfidRepository {

    fun getRfidDeviceId(intent: Intent): LiveData<String> {
        val data = MutableLiveData<String>()
        GlobalScope.launch {
            val ndefMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            if (ndefMessages != null) {
                data.postValue(String((ndefMessages[0] as NdefMessage).records[0].payload))
            }
        }
        return data
    }
}