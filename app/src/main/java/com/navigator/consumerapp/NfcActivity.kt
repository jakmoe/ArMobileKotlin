package com.navigator.consumerapp

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.navigator.consumerapp.arview.AugmentedRealityActivity
import com.navigator.consumerapp.datastorage.api.repo.ApiRepository
import com.navigator.consumerapp.datastorage.api.serialization.ArStoreComponents
import com.navigator.consumerapp.viewmodel.ApiViewModel


class NfcActivity : AppCompatActivity() {
    private var mNfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (!checkIsSupportedDeviceOrFinish(this, mNfcAdapter)) return
    }
    fun startArSession(view: View) {}

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Only Check if Tag is of supported Tag Type
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) startIfIsSupportedTag(intent)
    }

    private fun startIfIsSupportedTag(intent: Intent): Boolean {
        val messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        // Check if Byte Array is correctly read from Parcelable (NFC Tag)
        if (messages != null) {
            val ndefMessages = arrayOfNulls<NdefMessage>(messages.size)
            // Cast Byte Array into NdefMessage for Reading
            for (i in 0 until messages.size) ndefMessages[i] = messages[i] as NdefMessage
            // Get First Record of NdefMessages for reading out our Content
            val ndefRecord = ndefMessages[0]!!.records[0]
            // Check if Type of the Record matches with expected Text Type
            val ndefDeviceId = String(ndefRecord.payload)
            // HERE ID CHECK
            var idCheckSuccessful = false
            ApiViewModel(ApiRepository(), ndefDeviceId).arStore.observe(this, Observer {
                components: ArStoreComponents ->
                idCheckSuccessful = if(components.items[0].deviceId == ndefDeviceId) {
                    startActivity(Intent(this, AugmentedRealityActivity::class.java))
                    true
                } else {
                    Toast.makeText(this, "Tag wurde nicht erkannt", Toast.LENGTH_LONG).show()
                    false
                }
            })
            return (idCheckSuccessful)
        } else return false
    }

    /**
     * Reregister the Checking for NFC Tags
     */
    override fun onResume() {
        super.onResume()
        val pendingIntent = PendingIntent.getActivity(this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)
        mNfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    /**
     * Unregister the Checking for NFC Tags
     */
    override fun onPause() {
        super.onPause()
        mNfcAdapter!!.disableForegroundDispatch(this)
    }

    /**
     * Checks for NFC Capability
     */
    private fun checkIsSupportedDeviceOrFinish(activity: Activity, mNfcAdapter: NfcAdapter?): Boolean {
        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show()
            finish()
            return false
        }
        return true
    }
}
