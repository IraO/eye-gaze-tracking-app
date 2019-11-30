package com.iorlova.diploma.UI

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.SurfaceView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.iorlova.diploma.EyeDetection.OpenCVCamera
import com.iorlova.diploma.R
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.*
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

    private val openCVCamera: OpenCVCamera = OpenCVCamera()

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

            mRgba = openCVCamera.detectFace(mRgba, mGray)

            mRgba = openCVCamera.rotate(mRgba, Core.ROTATE_90_CLOCKWISE)
            mGray = openCVCamera.rotate(mGray, Core.ROTATE_90_CLOCKWISE)

        }
        return mRgba
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
