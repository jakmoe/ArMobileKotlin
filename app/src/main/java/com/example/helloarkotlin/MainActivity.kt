package com.example.helloarkotlin

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.helloarkotlin.artest.api.ApiRepository
import com.example.helloarkotlin.artest.api.ApiViewModel
import com.example.helloarkotlin.artest.api.data.ArStoreComponents


class MainActivity : AppCompatActivity() {

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

        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            startifIsSupportedTag(intent)
        }
    }

    private fun startifIsSupportedTag(intent: Intent): Boolean {
        val messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        // Check if Byte Array is correctly read from Parcelable (NFC Tag)
        if (messages != null) {
            val ndefMessages = arrayOfNulls<NdefMessage>(messages.size)
            // Cast Byte Array into NdefMessage for Reading
            for (i in 0 until messages.size) ndefMessages[i] = messages[i] as NdefMessage
            // Get First Record of NdefMessages for reading out our Content
            val ndefRecord = ndefMessages[0]!!.records[0]
            // Check if Type of the Record matches with expected Text Type
            val id = String(ndefRecord.payload)
            // HERE ID CHECK


            // TODO: Check up on Name after RFID is rewritten
            ApiViewModel(ApiRepository(), "A912371").arStore.observe(this, Observer {
                components: ArStoreComponents ->
                val name = components.items[0].name
                Log.i("TESTER", "Observed Result $name")

                startActivity(Intent(this, AugmentedRealityActivity::class.java).apply {
                    putExtra(EXTRA_MESSAGE, "Testing Intent")
                })
            })




            // IF ID FITS GO FURTHER, IF NOT GO BAD
            Toast.makeText(this, "ID is $id", Toast.LENGTH_LONG).show()
            return true
        }
        return false
    }

    /**
     * Reregister the Checking for NFC Tags
     */
    override fun onResume() {
        super.onResume()
        val pendingIntent = PendingIntent.getActivity(this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)
        mNfcAdapter!!.enableForegroundDispatch(this, pendingIntent, null, null)
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
