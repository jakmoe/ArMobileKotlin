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
import com.navigator.consumerapp.datastorage.api.serialization.ArStoreComponent
import com.navigator.consumerapp.datastorage.api.serialization.ArStoreComponents
import com.navigator.consumerapp.datastorage.api.serialization.ArStoreElement
import com.navigator.consumerapp.helpers.CameraPermissionHelper
import com.navigator.consumerapp.viewmodel.ApiViewModel
import kotlinx.android.synthetic.main.activity_main.*


class NfcActivity : AppCompatActivity() {
    private val tag = NfcActivity::class.java.simpleName
    private val minOpenGLVersion = 3.0
    private var mNfcAdapter: NfcAdapter? = null
    private var arFragment: ArFragment? = null

    /** At creation of Activity setup Fragment */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (!checkIsSupportedDeviceOrFinish(this, mNfcAdapter)) return
        setContentView(R.layout.activity_main)
        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment?
    }

    /** Reregister the Checking for NFC Tags */
    override fun onResume() {
        super.onResume()
        mNfcAdapter?.enableForegroundDispatch(this, getPendingIntent(), null, null)
    }

    /** Unregister the Checking for NFC Tags */
    override fun onPause() {
        super.onPause()
        mNfcAdapter!!.disableForegroundDispatch(this)
    }

    /** Register the Check for an RFID Tag whenever a new Tag is discovered */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) startIfIsSupportedTag(intent)
    }

    private fun startIfIsSupportedTag(intent: Intent) {
        animation_view.playAnimation()
        frameLayout.visibility = View.VISIBLE
        val ndefMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        if (ndefMessages != null) {
            // Cast Byte Array into NdefMessage for Reading (MIME Type text/plain = String)
            val ndefDeviceId = String((ndefMessages[0] as NdefMessage).records[0].payload)

            ApiViewModel(ApiRepository(), ndefDeviceId).arStore.observe(this, Observer { components: ArStoreComponents ->
                if (components.items.isNotEmpty() && components.items[0].deviceId == ndefDeviceId) {
                    configureSceneView(arFragment?.arSceneView)
                    setupArElements(arFragment?.arSceneView?.scene, components.items[0])
                    frameLayout.visibility = View.VISIBLE
                }
                else showToastTagNotSupported()
            })

            animation_view.cancelAnimation()
            animation_view.visibility = View.INVISIBLE
        } else showToastTagNotSupported()
    }


    private fun setupArElements(scene: Scene?, arStoreComponent: ArStoreComponent) {

        if (!arStoreComponent.elements.isNullOrEmpty()) for (element in arStoreComponent.elements!!) {
            when (element.type) {
                "0" -> setupView(element)
                "1" -> setupVideo(element)
                "2" -> setupChoiceElement(element)
            }
        }


        if (scene == null) return
        // TODO("Include Logic to use arStoreComponent from Database")


        // Scan Node
        val scanNode = getNodeAtCameraPosition(scene)


        // TODO("REMOVE SETUP OF TEST ELEMENTS")
        val anchorNodeTestingCircle = AnchorNode()
        anchorNodeTestingCircle.localPosition = Vector3(0.0f, 0.0f, -0.6f)
        anchorNodeTestingCircle.setParent(scene)
        buildTestingCircle(anchorNodeTestingCircle, 0.05f)

        var logTimer = 0.0
        val visibilityOffsetInMetres = 0.3
        scene.addOnUpdateListener {
            //Only Log every half Second
            if (logTimer > 0.5 ) {
                logEventLoop("x",scanNode.localPosition.x,scene.camera.localPosition.x)
                logTimer = 0.0
            } else logTimer += it.deltaSeconds


            anchorNodeTestingCircle.isEnabled = scene.camera.localPosition.x.unaryPlus() <= visibilityOffsetInMetres / 10
        }

        scanNode.setParent(scene)


        val anchorNodeInfoCard = AnchorNode()
        anchorNodeInfoCard.localPosition = Vector3(0.0f, 0.0f, -0.6f)
        anchorNodeInfoCard.setParent(scene)

        val anchorNodeMovie = AnchorNode()
        anchorNodeMovie.localPosition = Vector3(0.0f, 0.0f, -0.7f)
        anchorNodeMovie.localScale = Vector3(0.5f, 0.5f, 1.0f)
        anchorNodeMovie.setParent(scene)

        // buildViewCard(anchorNodeInfoCard, R.layout.info_card_widget)
        buildMovieCard(anchorNodeMovie)
    }

    private fun setupChoiceElement(element: ArStoreElement) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun setupVideo(element: ArStoreElement) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun setupView(element: ArStoreElement) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun logEventLoop(logName: String, origin: Float, offset: Float) {
        Log.d("SCENEUPDATER", "${logName.toUpperCase()} ORIGIN:   $origin")
        Log.d("SCENEUPDATER", "${logName.toUpperCase()} OFFSET:   $offset")
    }


    /** Builds an AR Playback Movie out of the raw Movie */
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

    /** Builds an Info Card out of the Info Card Widget XML */
    private fun buildViewCard(anchorNode: AnchorNode, view: Int) = ViewRenderable.builder().setView(this, view).build()
        .thenAccept { createTransformableNode(anchorNode).renderable = it }
        .exceptionally { Toast.makeText(this, getString(R.string.ErrorAtViewBuilding), Toast.LENGTH_SHORT).show(); null }

    /** Creates a Red Circle for testing out Positioning of Elements in the augmented dimension */
    private fun buildTestingCircle(anchorNode: AnchorNode, radius: Float) = MaterialFactory.makeOpaqueWithColor(this, Color(Color.RED)).thenAccept {
        createTransformableNode(anchorNode).renderable = ShapeFactory.makeSphere(radius, Vector3(0f, 0f, 0f), it) }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, getString(R.string.CameraPermissionNeeded), Toast.LENGTH_LONG).show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) CameraPermissionHelper.launchPermissionSettings(this)
            finish()
        }
    }

    /** Checks for NFC Capability */
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

    /** Configures AR Core*/
    private fun configureSceneView(sceneView: ArSceneView?) {
        val config = sceneView?.session?.config ?: return
        config.planeFindingMode = Config.PlaneFindingMode.DISABLED
        config.augmentedFaceMode = Config.AugmentedFaceMode.DISABLED
        config.focusMode = Config.FocusMode.AUTO
        config.cloudAnchorMode = Config.CloudAnchorMode.DISABLED
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
    }

    private fun getNodeAtCameraPosition(scene: Scene) = AnchorNode().apply { this.localPosition = scene.camera.localPosition }
    /** Toast Generator for bad Tag */
    private fun showToastTagNotSupported() = Toast.makeText(this, getString(R.string.NfcTagNotFound), Toast.LENGTH_LONG).show()
    /** Pending Intent Helper Function */
    private fun getPendingIntent(): PendingIntent = PendingIntent.getActivity(this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)
    /** TransformableNode Helper Function */
    private fun createTransformableNode(anchorNode: AnchorNode): TransformableNode = TransformableNode(arFragment?.transformationSystem).also { it.setParent(anchorNode) }
}
