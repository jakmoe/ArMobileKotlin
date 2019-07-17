package com.navigator.consumerapp

import android.app.Activity
import android.app.ActivityManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.ar.core.Config
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.navigator.consumerapp.datastorage.api.repo.ApiRepository
import com.navigator.consumerapp.datastorage.api.serialization.ArStoreComponent
import com.navigator.consumerapp.datastorage.api.serialization.ArStoreComponents
import com.navigator.consumerapp.datastorage.api.serialization.ArStoreModel
import com.navigator.consumerapp.helpers.CameraPermissionHelper
import com.navigator.consumerapp.rfid.repo.RfidRepository
import com.navigator.consumerapp.viewmodel.ApiViewModel
import com.navigator.consumerapp.viewmodel.RfidViewModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.info_card_widget.view.*
import kotlinx.android.synthetic.main.object_chooser.view.*


class NfcActivity : AppCompatActivity() {
    private val tag = NfcActivity::class.java.simpleName
    private val minOpenGLVersion = 3.0
    private var mNfcAdapter: NfcAdapter? = null
    private var arFragment: ArFragment? = null
    private var startingPosition: AnchorNode? = null

    /** At creation of Activity setup Fragment */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(findViewById(R.id.toolbar))
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (!checkIsSupportedDeviceOrFinish(this, mNfcAdapter)) return
        setContentView(R.layout.activity_main)
        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment?
    }

    /** Reregister the Checking for NFC Tags */
    override fun onResume() {
        super.onResume()
        arFragment?.arSceneView?.session?.resume()
        mNfcAdapter?.enableForegroundDispatch(this, getPendingIntent(), null, null)
    }

    /** Unregister the Checking for NFC Tags */
    override fun onPause() {
        super.onPause()
        arFragment?.arSceneView?.session?.pause()
        mNfcAdapter!!.disableForegroundDispatch(this)
    }

    /** Register the Check for an RFID Tag whenever a new Tag is discovered */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) startIfIsSupportedTag(intent)
    }

    fun startNewScan(view: View) {
        changeStatusImageView(0)
        startingView.visibility = View.VISIBLE
        toolbar.visibility = View.GONE
        startingPosition?.setParent(null)
    }

    private fun startIfIsSupportedTag(intent: Intent) {
        startStopLoadingAnimation()
        RfidViewModel(RfidRepository(), intent).deviceId.observe(this, Observer {ndefDeviceId: String ->
            if (ndefDeviceId == "") {
                changeStatusImageView(2)
                showToastTagNotSupported()
            }
            ApiViewModel(ApiRepository(), ndefDeviceId).arStore.observe(this, Observer { components: ArStoreComponents ->
                if (components.items.isNotEmpty() && components.items[0].deviceId == ndefDeviceId) {
                    configureSceneView(arFragment?.arSceneView)
                    arFragment?.arSceneView?.scene!!.addOnUpdateListener { Log.i("ARTEST CAMERA", "CAMERA: W: ${arFragment?.arSceneView?.scene!!.camera.worldPosition}, L: ${arFragment?.arSceneView?.scene!!.camera.localPosition}") }

                    startingPosition = getNodeAtCameraPosition(arFragment?.arSceneView?.scene!!)
                    startingPosition?.setParent(arFragment?.arSceneView?.scene!!)

                    setupArElements(arFragment?.arSceneView?.scene, components.items[0])

                    arFragment?.arSceneView?.scene!!.addOnUpdateListener(UserDistanceChecker(arFragment?.arSceneView?.scene!!, components))

                    changeStatusImageView(1)
                }
                else {
                    changeStatusImageView(2)
                    showToastTagNotSupported()
                }
                startStopLoadingAnimation()
            })

        })
    }

    inner class UserDistanceChecker(private val scene: Scene?, private val components: ArStoreComponents): Scene.OnUpdateListener {
        private val cameraInitialPosition = scene?.camera?.localPosition
        override fun onUpdate(p0: FrameTime?) {
            // Moving away from the original scan on z-Axis
            if (scene!!.camera.localPosition.z.unaryPlus() >= 0.2+cameraInitialPosition!!.z.unaryPlus()) {
                Log.i("ARTEST", "CURRENT DISTANCE FROM ORIGIN: ${scene!!.camera.localPosition.z.unaryPlus()}")
                startingView.visibility = View.INVISIBLE
                toolbar.visibility = View.VISIBLE
                scene.removeOnUpdateListener(this@UserDistanceChecker)
            }
        }
    }

    private fun setupArElements(scene: Scene?, arStoreComponent: ArStoreComponent) {
        if (scene == null) return

        ObjectTitleTextView.text = arStoreComponent.name
        ObjectDescriptionTextView.text = arStoreComponent.details?.getOrNull(0)
        ObjectDescriptionTextView2.text = arStoreComponent.details?.getOrNull(1)

        Log.i("ARTEST", "0 = Movie, 1 = View, 2 = Choice")
        arStoreComponent.elements?.forEach { element ->
            val anchorNode = AnchorNode()
            anchorNode.setParent(startingPosition)
            anchorNode.localPosition = createVectorFromArStoreFloatList(element.offset, startingPosition!!)
            anchorNode.localScale = createVectorFromArStoreFloatList(element.scale, startingPosition!!)
            anchorNode.localRotation = createQuaternionFromArStoreFloatList(element.orientation, startingPosition!!)
            Log.i("ARTEST", "ANCHORNODE for ${element.type}: ${anchorNode.localPosition}")
            when (element.type) {
                "0" -> buildMovieCard(anchorNode, element.link)
                "1" -> buildViewCard(anchorNode, R.layout.info_card_widget, element.text!!)
                "2" -> if (element.modelList != null) buildChoiceElement(anchorNode, element.modelList!!, scene)
            }
        }

        // TODO("REMOVE SETUP OF TEST ELEMENTS")
//        val anchorNodeTestingCircle = AnchorNode()
//        anchorNodeTestingCircle.localPosition = Vector3(0.0f, 0.0f, -0.6f)
//        anchorNodeTestingCircle.setParent(scene)
//        buildTestingCircle(anchorNodeTestingCircle, 0.05f)
//        var logTimer = 0.0
//        val visibilityOffsetInMetres = 0.3
//        scene.addOnUpdateListener { anchorNodeTestingCircle.isEnabled = scene.camera.localPosition.x.unaryPlus() <= visibilityOffsetInMetres / 10 }
    }

    private fun buildChoiceElement(anchorNode: AnchorNode, modelList: List<ArStoreModel?>, scene: Scene) {
        ViewRenderable.builder()
            .setView(this, R.layout.object_chooser)
            .build()
            .thenAccept { renderable ->
                anchorNode.renderable = renderable
                var index = 0
                Picasso.get().load(modelList[index]?.textureLink).into(renderable.view.imageView as ImageView)
                renderable.view.imageButtonLeft.setOnClickListener {
                    if (index == 0) index = modelList.lastIndex+1
                    Picasso.get().load(modelList.getOrNull(--index)?.textureLink).into(renderable.view.imageView as ImageView)
                    renderable.view.imageCaption.text = modelList[index]?.name
                }
                renderable.view.imageButtonRight.setOnClickListener {
                    if (index == modelList.lastIndex) index = -1
                    Picasso.get().load(modelList.getOrNull(++index)?.textureLink).into(renderable.view.imageView as ImageView)
                    renderable.view.imageCaption.text = modelList[index]?.name
                }
            }
//        scene.addOnUpdateListener {
//            //Only Log every half Second
//            anchorNode.isEnabled = scene.camera.localPosition.x.absoluteValue >= 0.35
//        }
    }

    /** Builds an AR Playback Movie out of the raw Movie */
    private fun buildMovieCard(anchorNode: AnchorNode, link: String?) {
        val texture = ExternalTexture()
        MediaPlayer().apply {
            isLooping = true
            setDataSource(link)
            setOnPreparedListener { mediaPlayer ->
                ModelRenderable.builder()
                    .setSource(this@NfcActivity, R.raw.chroma_key_video)
                    .build()
                    .thenAccept {
                        it.material.setExternalTexture("videoTexture", texture)
                        anchorNode.renderable = it
                        setSurface(texture.surface)
                        mediaPlayer.start()
                    }
            }
            prepareAsync()
        }
    }

    /** Builds an Info Card out of the Info Card Widget XML */
    private fun buildViewCard(anchorNode: AnchorNode, view: Int, text: String) = ViewRenderable.builder().setView(this, view).build()
        .thenAccept { renderable ->
            renderable.view.textView.text = text
            anchorNode.renderable = renderable
        }
        .exceptionally { Toast.makeText(this, getString(R.string.ErrorAtViewBuilding), Toast.LENGTH_SHORT).show(); null }

    /** Creates a Red Circle for testing out Positioning of Elements in the augmented dimension */
    private fun buildTestingCircle(anchorNode: AnchorNode, radius: Float) = MaterialFactory.makeOpaqueWithColor(this, Color(Color.RED)).thenAccept {
        anchorNode.renderable = ShapeFactory.makeSphere(radius, Vector3(0f, 0f, 0f), it) }

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

    /** Configures Main Icon and Color*/
    private fun changeStatusImageView(value: Int) {
        var drawable: Drawable? = null
        when (value) {
            0 -> {
                drawable = ContextCompat.getDrawable(this, R.drawable.ic_phone)
                statusImageView.background = ContextCompat.getDrawable(this, R.drawable.button_shape_start_screen)
                userInteractionMainText.visibility = View.VISIBLE
                userInteractionDetailText.text = getString(R.string.RfidInstructions)
            }
            1 -> {
                drawable = ContextCompat.getDrawable(this, R.drawable.ic_ok)
                statusImageView.background = ContextCompat.getDrawable(this, R.drawable.button_shape_start_screen_success)
                userInteractionMainText.visibility = View.GONE
                userInteractionDetailText.text = getString(R.string.MoveAwayFromObject)
            }
            2 -> {
                drawable = ContextCompat.getDrawable(this, R.drawable.ic_not_ok)
                statusImageView.background = ContextCompat.getDrawable(this, R.drawable.button_shape_start_screen_fail)
                userInteractionDetailText.text = getString(R.string.ScanFailed)
            }
        }
        if (drawable == null) return
        statusImageView.setImageDrawable(drawable)
    }

    /** Starts or stops the Lottie Animation Cycle*/
    private fun startStopLoadingAnimation() {
        if (animation_view.isAnimating) {
            animation_view.cancelAnimation()
            animation_view.visibility = View.INVISIBLE
        } else {
            animation_view.playAnimation()
            animation_view.visibility = View.VISIBLE
        }
    }

    /** Configures AR Core*/
    private fun configureSceneView(sceneView: ArSceneView?) {
        val config = sceneView?.session?.config
        sceneView!!.planeRenderer!!.isEnabled = false
        config!!.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
        config!!.augmentedFaceMode = Config.AugmentedFaceMode.DISABLED
        config!!.focusMode = Config.FocusMode.AUTO
        config!!.cloudAnchorMode = Config.CloudAnchorMode.DISABLED
        config!!.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
    }

    private fun getNodeAtCameraPosition(scene: Scene) = AnchorNode().apply {
        this.localPosition = scene.camera.localPosition
        this.localRotation = scene.camera.localRotation
    }
    /** Toast Generator for bad Tag */
    private fun showToastTagNotSupported() = Toast.makeText(this, getString(R.string.NfcTagNotFound), Toast.LENGTH_LONG).show()
    /** Pending Intent Helper Function */
    private fun getPendingIntent(): PendingIntent = PendingIntent.getActivity(this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)
    /** TransformableNode Helper Function */
    private fun createTransformableNode(anchorNode: AnchorNode): TransformableNode = TransformableNode(arFragment?.transformationSystem).also { it.setParent(anchorNode) }
    private fun createVectorFromArStoreFloatList(list: List<Float>?, nodeAtStartingPosition: AnchorNode): Vector3 =
        if (list != null && list.size == 3) Vector3(
            nodeAtStartingPosition.localPosition.x+list[0],
            nodeAtStartingPosition.localPosition.y+list[1],
            nodeAtStartingPosition.localPosition.z+list[2]
        ) else Vector3(0f, 0f, 0f)
    private fun createQuaternionFromArStoreFloatList(
        list: List<Float>?,
        startingPosition: AnchorNode
    ): Quaternion =
        if (list != null && list.size == 4) Quaternion(
            Vector3(
                startingPosition.localRotation.x+list[0],
                startingPosition.localRotation.y+list[1],
                startingPosition.localRotation.z+list[2]
            ),
            startingPosition.localRotation.w+list[3]
        ) else Quaternion(Vector3(0f, 0f, 0f), 0f)
    fun startDebugAr(view: View) {
        toolbar.visibility = View.VISIBLE
        frameLayout.visibility = View.VISIBLE
        startingView.visibility = View.GONE
    }
}
