package com.iorlova.diploma.UI

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.SurfaceView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.iorlova.diploma.EyeDetection.OpenCVCamera
import com.iorlova.diploma.R
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Rect
import java.io.File
import kotlin.math.round


class TestEyeDetection : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private val PERMISSION_REQUEST_CODE = 200

    lateinit var cameraBridgeViewBase: CameraBridgeViewBase
    private lateinit var loader: BaseLoaderCallback

    lateinit var mRgba: Mat
    lateinit var mGray: Mat

    private val mRelativeFaceSize = 0.2f
    private var mAbsoluteFaceSize = 0.0

    var isDialogOn = false
    var countNoDetectedFace = 0
    var countNoDetectedEyes = 0

    private val openCVCamera: OpenCVCamera = OpenCVCamera()

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        window.decorView.systemUiVisibility = (
                // Set the content to appear under the system bars so that the
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_eye_detection)

        cameraBridgeViewBase = findViewById(R.id.cameraView)
        cameraBridgeViewBase.visibility = SurfaceView.VISIBLE
        cameraBridgeViewBase.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT)
        cameraBridgeViewBase.setCvCameraViewListener(this)

        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CAMERA
                )
            ) {
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    PERMISSION_REQUEST_CODE
                )
            }
        }

        loader = object : BaseLoaderCallback(this) {
            override fun onManagerConnected(status: Int) {
                when (status) {
                    LoaderCallbackInterface.SUCCESS -> {
                        //System.loadLibrary("opencv_java3")
                        val cascadeDir: File = getDir("cascade", Context.MODE_PRIVATE)
                        val cascadeDirEye: File = getDir("cascade", Context.MODE_PRIVATE)
                        openCVCamera.initializeOpenCVDependencies(resources, cascadeDir, cascadeDirEye)
                        cameraBridgeViewBase.enableView()
                    }
                    else -> super.onManagerConnected(status)
                }
            }
        }
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        if (inputFrame != null) {
            mRgba = inputFrame.rgba()
            mRgba = openCVCamera.rotate(mRgba, Core.ROTATE_90_COUNTERCLOCKWISE)

            mGray = inputFrame.gray()
            mGray = openCVCamera.rotate(mGray, Core.ROTATE_90_COUNTERCLOCKWISE)

            if (mAbsoluteFaceSize == 0.0) {
                val height = mGray.cols()
                if (round(height * mRelativeFaceSize) > 0) {
                    mAbsoluteFaceSize = round(height * mRelativeFaceSize).toDouble()
                }
            }
            var message = ""

            val detectedFaces = openCVCamera.detectFaces(mRgba, mGray)

            if (detectedFaces.isEmpty()) {
                countNoDetectedFace += 1
            } else {
                countNoDetectedFace = 0
            }

            for (face in detectedFaces) {
                openCVCamera.drawFace(face)

                val detectedEyes = openCVCamera.detectEyes(face)
                val leftEye = detectedEyes[0]
                val rightEye = detectedEyes[1]

                if(leftEye.isEmpty() or rightEye.isEmpty()) {
                    countNoDetectedEyes +=1
                } else {
                    val eyeLeft = Rect(
                        face.x + face.width / 16 + (face.width - 2 * face.width / 16) / 2,
                        (face.y + (face.height / 4.5)).toInt(), (face.width - 2 * face.width / 16) / 2, face.height / 3
                    )
                    val eyeRight = Rect(
                        face.x + face.width / 16, (face.y + (face.height / 4.5)).toInt(),
                        (face.width - 2 * face.width / 16) / 2, face.height / 3
                    )

                    for (eye in leftEye) {
                        openCVCamera.drawEye(eye, eyeLeft, 24)
                    }
                    for (eye in rightEye) {
                        openCVCamera.drawEye(eye, eyeRight, 24)
                    }

                    mRgba = openCVCamera.mRgba
                    openCVCamera.learnFrames++
                    countNoDetectedEyes = 0
                }
            }
            if (!isDialogOn && (countNoDetectedFace == 20 || countNoDetectedEyes == 15)) {
                this.runOnUiThread {
                    isDialogOn = true
                    if (countNoDetectedFace >= 20) {
                        message = "Can't find face"
                    } else if (countNoDetectedEyes >= 15) {
                        message = "Eyes are not visible"
                    }
                    openDialog(message)
                }
            }

            mRgba = openCVCamera.rotate(mRgba, Core.ROTATE_90_CLOCKWISE)
            mGray = openCVCamera.rotate(mGray, Core.ROTATE_90_CLOCKWISE)
        }
        return mRgba
    }

    private fun openDialog(message: String) {
        val builder = AlertDialog.Builder(this@TestEyeDetection, R.style.ReadingGoalsWindow)

        val messageTextView = TextView(builder.context)
        messageTextView.gravity = Gravity.CENTER
        messageTextView.setTextColor(Color.WHITE)
        messageTextView.text = message

        builder.setView(messageTextView)
        builder.setPositiveButton("Continue") { dialog, which ->
            isDialogOn = false
            countNoDetectedFace = 0
            countNoDetectedEyes = 0
        }
        val alert = builder.create()
        alert.show()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        mGray = Mat(width, height, CvType.CV_8UC4)
        mRgba = Mat(width, height, CvType.CV_8UC4)

    }

    override fun onCameraViewStopped() {
        mGray.release()
        mRgba.release()
    }

    override fun onPause() {
        super.onPause()
        cameraBridgeViewBase.disableView()
    }

    override fun onResume() {
        super.onResume()

        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(applicationContext, "Can't Load OpenCV", Toast.LENGTH_SHORT).show()
        } else {
            loader.onManagerConnected(BaseLoaderCallback.SUCCESS)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView()
        }
    }
}
