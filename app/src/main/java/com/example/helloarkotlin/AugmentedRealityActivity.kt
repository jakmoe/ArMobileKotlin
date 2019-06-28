package com.example.helloarkotlin

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Config
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class AugmentedRealityActivity : AppCompatActivity() {

    private val tag = MainActivity::class.java.simpleName
    private val minOpenGLVersion = 3.0


    private var arFragment: ArFragment? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkIsSupportedDeviceOrFinish(this)) return

        setContentView(R.layout.activity_augmented_reality)

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment?
        val sceneView = arFragment!!.arSceneView
        // configureSceneView(sceneView)

        //GET DATABASE INFORMATION
        // val pose = sceneView.arFrame?.camera?.pose
        // val locationPose = Pose.makeTranslation(100.0f, 4.0f, 0.0f)//define a translation
        // val targetPose = pose?.compose(locationPose) //make a new pose based on camera pose
        // val anchor = sceneView.session?.createAnchor(targetPose)
        val anchorNodeTestingCircle = AnchorNode()
        anchorNodeTestingCircle.localPosition = Vector3(0.0f, 0.0f, -0.6f)
        anchorNodeTestingCircle.setParent(sceneView.scene)
        buildTestingCircle(anchorNodeTestingCircle)

        val anchorNodeInfoCard = AnchorNode()
        anchorNodeInfoCard.localPosition = Vector3(0.0f, 0.0f, -0.6f)
        anchorNodeInfoCard.setParent(sceneView.scene)
        buildInfoCard(anchorNodeInfoCard)

        val anchorNodeMovie = AnchorNode()
        anchorNodeMovie.localPosition = Vector3(0.0f, 0.0f, -0.7f)
        anchorNodeMovie.localScale = Vector3(
            0.5f, 0.5f, 1.0f
        )
        anchorNodeMovie.setParent(sceneView.scene)
        buildMovieCard(anchorNodeMovie)

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
                Toast.makeText(this@AugmentedRealityActivity, "Error", Toast.LENGTH_SHORT).show()
                return@exceptionally null }
    }

    /**
     * Creates a Red Circle for testing out Positioning of Elements in the augmented dimension
     */
    private fun buildTestingCircle(anchorNode: AnchorNode) {
        MaterialFactory.makeOpaqueWithColor(this,
            Color(Color.RED)
        ).thenAccept {
            createTransformableNode(anchorNode).renderable = ShapeFactory.makeSphere(0.05f, Vector3(0f, 0f, 0f), it)
        }
    }

    private fun createTransformableNode(anchorNode: AnchorNode): TransformableNode {
        val node = TransformableNode(arFragment!!.transformationSystem)
        node.setParent(anchorNode)
        return node
    }

    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     * Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
     * Finishes the activity if Sceneform can not run
     */
    private fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
        val openGlVersionString = (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .deviceConfigurationInfo
            .glEsVersion
        if (java.lang.Double.parseDouble(openGlVersionString) < minOpenGLVersion) {
            Log.e(tag, "Sceneform requires OpenGL ES 3.0 later")
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
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
}
