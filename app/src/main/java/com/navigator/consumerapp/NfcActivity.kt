package com.navigator.consumerapp

import android.app.Activity
import android.app.ActivityManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.ar.core.Config
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.navigator.consumerapp.datastorage.api.repo.ApiRepository
import com.navigator.consumerapp.datastorage.api.serialization.ArStoreComponents
import com.navigator.consumerapp.viewmodel.ApiViewModel
import kotlinx.android.synthetic.main.activity_main.*


class NfcActivity : AppCompatActivity() {

    private val tag = NfcActivity::class.java.simpleName
    private var mNfcAdapter: NfcAdapter? = null
    private val minOpenGLVersion = 3.0
    private var arFragment: ArFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (!checkIsSupportedDeviceOrFinish(this, mNfcAdapter)) return
        setContentView(R.layout.activity_main)
    }

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

            animation_view.playAnimation()
            var idCheckSuccessful = false
            ApiViewModel(ApiRepository(), ndefDeviceId).arStore.observe(this, Observer {
                components: ArStoreComponents ->
                idCheckSuccessful = if(components.items?.get(0)?.deviceId == ndefDeviceId) {
                    frameLayout.visibility = View.VISIBLE

                    arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment?
                    val sceneView = arFragment?.arSceneView
                    configureSceneView(arFragment?.arSceneView)

                    setupArElements(sceneView!!.scene)

                    animation_view.cancelAnimation()
                    true
                } else {
                    Toast.makeText(this, getString(R.string.NfcTagNotFound), Toast.LENGTH_LONG).show()
                    false
                }
            })
            return (idCheckSuccessful)
        } else return false
    }

    private fun setupArElements(scene: Scene) {
        val anchorNodeTestingCircle = AnchorNode()
        anchorNodeTestingCircle.localPosition = Vector3(0.0f, 0.0f, -0.6f)
        anchorNodeTestingCircle.setParent(scene)
        buildTestingCircle(anchorNodeTestingCircle)

        val anchorNodeInfoCard = AnchorNode()
        anchorNodeInfoCard.localPosition = Vector3(0.0f, 0.0f, -0.6f)
        anchorNodeInfoCard.setParent(scene)
        buildInfoCard(anchorNodeInfoCard)

        val anchorNodeMovie = AnchorNode()
        anchorNodeMovie.localPosition = Vector3(0.0f, 0.0f, -0.7f)
        anchorNodeMovie.localScale = Vector3(
            0.5f, 0.5f, 1.0f
        )
        anchorNodeMovie.setParent(scene)
        buildMovieCard(anchorNodeMovie)
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
            Toast.makeText(this, getString(R.string.NoSupportForNfc), Toast.LENGTH_LONG).show()
            finish()
            return false
        }
        val openGlVersionString = (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .deviceConfigurationInfo
            .glEsVersion
        if (java.lang.Double.parseDouble(openGlVersionString) < minOpenGLVersion) {
            Log.e(tag, getString(R.string.SceneformNeedsGreaterOpenGLVersion))
            Toast.makeText(activity, getString(R.string.SceneformNeedsGreaterOpenGLVersion), Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        return true
    }

    /**
     * Configures AR Core
     */
    private fun configureSceneView(sceneView: ArSceneView?) {
        val config = sceneView?.session?.config ?: return
        config.planeFindingMode = Config.PlaneFindingMode.DISABLED
        config.augmentedFaceMode = Config.AugmentedFaceMode.DISABLED
        config.focusMode = Config.FocusMode.AUTO
        config.cloudAnchorMode = Config.CloudAnchorMode.DISABLED
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
    }

    /**
     * Builds an AR Playback Movie out of the raw Movie
     */
    private fun buildMovieCard(anchorNode: AnchorNode) {
        // Create an ExternalTexture for displaying the contents of the video.
        val texture = ExternalTexture()

        // Create an Android MediaPlayer to capture the video on the external texture's surface.
        val mediaPlayer = MediaPlayer.create(this, R.raw.lion_chroma)
        mediaPlayer.setSurface(texture.surface)
        mediaPlayer.isLooping = true

        // Create a renderable with a material that has a parameter of type 'samplerExternal' so that
        // it can display an ExternalTexture. The material also has an implementation of a chroma key
        // filter.
        ModelRenderable.builder()
            .setSource(this, R.raw.chroma_key_video)
            .build()
            .thenAccept {
                it.material.setExternalTexture("videoTexture", texture)
                createTransformableNode(anchorNode).renderable = it
                if (!mediaPlayer.isPlaying) mediaPlayer.start()
            }
    }

    /**
     * Builds an Info Card out of the Info Card Widget XML
     */
    private fun buildInfoCard(anchorNode: AnchorNode) {
        ViewRenderable.builder()
            .setView(this, R.layout.info_card_widget)
            .build()
            .thenAccept { createTransformableNode(anchorNode).renderable = it }
            .exceptionally {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                return@exceptionally null }
    }
    /**
     * Creates a Red Circle for testing out Positioning of Elements in the augmented dimension
     */
    private fun buildTestingCircle(anchorNode: AnchorNode) {
        MaterialFactory.makeOpaqueWithColor(this,
            com.google.ar.sceneform.rendering.Color(Color.RED)
        ).thenAccept {
            createTransformableNode(anchorNode).renderable = ShapeFactory.makeSphere(0.05f, Vector3(0f, 0f, 0f), it)
        }
    }

    private fun createTransformableNode(anchorNode: AnchorNode): TransformableNode {
        val node = TransformableNode(arFragment?.transformationSystem)
        node.setParent(anchorNode)
        return node
    }
}
