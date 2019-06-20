package com.example.helloarkotlin

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.core.Pose
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.ux.TransformableNode


class MainActivity : AppCompatActivity() {
    private val tag = MainActivity::class.java.simpleName
    private val minOpenGLVersion = 3.0

    private var arFragment: ArFragment? = null

    override// CompletableFuture requires api level 24
    // FutureReturnValueIgnored is not valid
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkIsSupportedDeviceOrFinish(this)) return

        setContentView(R.layout.activity_main)
        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment?
        val sceneView = arFragment!!.arSceneView

        // val pose = sceneView.arFrame?.camera?.pose
        // val locationPose = Pose.makeTranslation(100.0f, 4.0f, 0.0f)//define a translation
        // val targetPose = pose?.compose(locationPose) //make a new pose based on camera pose
        // val anchor = sceneView.session?.createAnchor(targetPose)
        val anchorNode = AnchorNode()
        anchorNode.setParent(sceneView.scene)
        anchorNode.localPosition = Vector3(0.0f, 0.0f, -0.6f)

        val node = TransformableNode(arFragment!!.transformationSystem)
        node.setParent(anchorNode)
        MaterialFactory.makeOpaqueWithColor(this,
            com.google.ar.sceneform.rendering.Color(Color.RED)
        ).thenAccept {
            node.renderable = ShapeFactory.makeSphere(0.1f, Vector3(0f, 0f, 0f), it)
        }
        node.select()
        /*
        arFragment!!.setOnTapArPlaneListener { hitResult: HitResult, _, _ ->

                // Create the Anchor.
                val anchor = hitResult.createAnchor()
                val anchorNode = AnchorNode(anchor)
                anchorNode.setParent(arFragment!!.arSceneView.scene)

                // Create the transformable node and add it to the anchor.
                val node = TransformableNode(arFragment!!.transformationSystem)
                node.setParent(anchorNode)
                MaterialFactory.makeOpaqueWithColor(this,
                    com.google.ar.sceneform.rendering.Color(Color.RED)
                ).thenAccept {
                    node.renderable = ShapeFactory.makeSphere(0.1f, Vector3(0.1f, 0.1f, 0.1f), it)
                }
                node.select()
            }
            */
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
}
