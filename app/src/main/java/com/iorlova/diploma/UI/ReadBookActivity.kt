package com.iorlova.diploma.UI

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.text.TextPaint
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.iorlova.diploma.EyeDetection.OpenCVCamera
import com.iorlova.diploma.R
import com.iorlova.diploma.UI.PageSplitter.PageSplitter
import com.iorlova.diploma.UI.PageSplitter.TextPagerAdapter
import com.iorlova.diploma.ViewModel.BookViewModel
import com.rtfparserkit.converter.text.StringTextConverter
import com.rtfparserkit.parser.RtfStreamSource
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import java.io.*
import java.nio.charset.StandardCharsets
import kotlin.math.round

class ReadBookActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 200
    }

    lateinit var cameraBridgeViewBase: CameraBridgeViewBase
    lateinit var mRgba: Mat
    lateinit var mGray: Mat

    private val openCVCamera: OpenCVCamera = OpenCVCamera()
    private val mRelativeFaceSize = 0.2f
    private var mAbsoluteFaceSize = 0.0
    private lateinit var loader: BaseLoaderCallback
    private lateinit var pagesView: ViewPager

    private lateinit var bookViewModel: BookViewModel
    var bookId: Int = 0

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
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
        setContentView(R.layout.activity_read_book)
        bookViewModel = ViewModelProviders.of(this).get(BookViewModel::class.java)

        pagesView = findViewById(R.id.pages)
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x
        val height = size.y

        val pageSplitter = PageSplitter(width, height, 1f, 0)
        val textPaint = TextPaint()
        textPaint.textSize = resources.getDimension(R.dimen.text_size)

        bookId = intent.getStringExtra("BOOK_ID").toInt()
        val bookUri = Uri.parse(intent.getStringExtra("BOOK_URI"))
        val bookFormat = intent.getStringExtra("BOOK_FORMAT")
        val pageCount = intent.getIntExtra("BOOK_PAGE_COUNTER", 0)
        var text = readFile(bookUri)

        if (text != null) {
            if (bookFormat == BookFormat.RTF.format) {
                text = extractRTF(text)
            }

            val textLines = text.split("\n")
            for (line in textLines) {
                pageSplitter.append(line, textPaint)
            }
            pagesView.adapter = TextPagerAdapter(supportFragmentManager, pageSplitter.pages)
        } else {
            Toast.makeText(
                applicationContext,
                R.string.invalid_format,
                Toast.LENGTH_LONG
            ).show()

            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }
        pagesView.currentItem = pageCount

        pagesView.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                bookViewModel.update(bookId, position)
            }
        })

        cameraBridgeViewBase = findViewById(R.id.cameraViewBook)
        cameraBridgeViewBase.visibility = SurfaceView.INVISIBLE
        cameraBridgeViewBase.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT)
        cameraBridgeViewBase.setCvCameraViewListener(this)

        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    PERMISSION_REQUEST_CODE)
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

    private fun extractRTF(textRTF: String): String {
        var text = ""
        val stream = ByteArrayInputStream(textRTF.toByteArray(StandardCharsets.UTF_8))

        val converter = StringTextConverter()
        try {
            converter.convert(RtfStreamSource(stream))
            val extractedText = converter.getText()
            text = extractedText
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return text
    }

    private fun readFile(bookUri: Uri): String? {
        val TAG = "PAGE_SPLITTER"
        var line: String? = null

        try {

            val file = createNewFile(bookUri, contentResolver)
            val fileInputStream = FileInputStream(file)

            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val stringBuilder = StringBuilder()

            line = bufferedReader.readLine()
            while (line != null) {
                stringBuilder.append(line + System.getProperty("line.separator"))
                line = bufferedReader.readLine()
            }
            fileInputStream.close()
            line = stringBuilder.toString()

            bufferedReader.close()
        } catch (ex: FileNotFoundException) {
            Log.d(TAG, ex.message)
        } catch (ex: IOException) {
            Log.d(TAG, ex.message)
        }

        return line
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
        cameraBridgeViewBase.disableView()
    }
}
