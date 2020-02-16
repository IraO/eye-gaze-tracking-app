package com.iorlova.diploma.EyeDetection

import android.content.res.Resources
import android.util.Log
import com.iorlova.diploma.R
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import org.opencv.objdetect.Objdetect
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


class OpenCVCamera {
    lateinit var cascadeClassifier: CascadeClassifier
    lateinit var eyeDetectorClassifier: CascadeClassifier

    lateinit var mRgba: Mat
    lateinit var mGray: Mat

    var learnFrames = 0

    private var mAbsoluteFaceSize = 0.3

    var xCenter = -1.0
    var yCenter = -1.0

    fun initializeOpenCVDependencies(resources: Resources, cascadeDir: File, cascadeDirEye: File) {
        try {
            // Copy the resource into a temp file so OpenCV can load it
            val iResource: InputStream = resources.openRawResource(R.raw.lbpcascade_frontalface)
            val mCascadeFile = File(cascadeDir, "lbpcascade_frontalface.raw")
            val os = FileOutputStream(mCascadeFile)

            val buffer = ByteArray(4096)
            var bytesRead = iResource.read(buffer)
            while (bytesRead != -1) {
                os.write(buffer, 0, bytesRead)
                bytesRead = iResource.read(buffer)
            }
            iResource.close()
            os.close()

            // Load the cascade classifier
            val iResourceEye: InputStream =
                resources.openRawResource(R.raw.haarcascade_lefteye_2splits)
            val mCascadeFileEye = File(cascadeDirEye, "haarcascade_lefteye_2splits.raw")
            val ose = FileOutputStream(mCascadeFileEye)

            bytesRead = iResourceEye.read(buffer)
            while (bytesRead != -1) {
                ose.write(buffer, 0, bytesRead)
                bytesRead = iResourceEye.read(buffer)
            }
            iResourceEye.close()
            ose.close()

            cascadeClassifier = CascadeClassifier(mCascadeFile.absolutePath)

            // Load the cascade classifier for Eye detection
            eyeDetectorClassifier = CascadeClassifier(mCascadeFileEye.absolutePath)

        } catch (e: Exception) {
            Log.e("OpenCVActivity", "Error loading cascade", e)
        }
    }

    fun rotate(matrix: Mat, rotateCode: Int): Mat {
        val destination = Mat()
        Core.rotate(matrix, destination, rotateCode)
        return destination
    }

    fun detectFaces(rgba: Mat, gray: Mat): Array<Rect> {
        mRgba = rgba
        mGray = gray

        val absoluteFaceSize = mAbsoluteFaceSize  //Rgba.cols() * 0.2
        val faces = MatOfRect()
        cascadeClassifier.detectMultiScale(
            mRgba, faces, 1.3, 6, 2,
            Size(absoluteFaceSize, absoluteFaceSize), Size()
        )
        return faces.toArray()
    }

    fun detectEyes(face: Rect): Array<Array<Rect>>{
        val r: Rect = face
        val eyeRight = Rect(
            r.x + r.width / 16, (r.y + (r.height / 4.5)).toInt(),
            (r.width - 2 * r.width / 16) / 2, r.height / 3
        )
        val eyeLeft = Rect(
            r.x + r.width / 16 + (r.width - 2 * r.width / 16) / 2,
            (r.y + (r.height / 4.5)).toInt(), (r.width - 2 * r.width / 16) / 2, r.height / 3
        )

        Imgproc.rectangle(mRgba, eyeLeft.tl(), eyeLeft.br(),
            Scalar(255.0, 0.0, 0.0, 255.0), 1
        )
        Imgproc.rectangle(mRgba, eyeRight.tl(), eyeRight.br(),
            Scalar(255.0, 0.0, 0.0, 255.0), 1
        )

        val leftEyeDetected = detectEye(eyeDetectorClassifier, eyeLeft)
        val rightEyeDetected = detectEye(eyeDetectorClassifier, eyeRight)

        return arrayOf(leftEyeDetected, rightEyeDetected)
    }

    private fun detectEye(classifier: CascadeClassifier, area: Rect): Array<Rect> {
        val mROI = mGray.submat(area)
        val eyes = MatOfRect()
        classifier.detectMultiScale(
            mROI, eyes, 1.3, 6, Objdetect.CASCADE_FIND_BIGGEST_OBJECT
                    or Objdetect.CASCADE_SCALE_IMAGE, Size(30.0, 30.0), Size()
        )
        return eyes.toArray()
    }

    fun drawFace(face: Rect) {
        Imgproc.rectangle(mRgba, face.tl(), face.br(), Scalar(0.0, 255.0, 0.0, 255.0), 3)
        xCenter = ((face.x + face.width + face.x) / 2).toDouble()
        yCenter = ((face.y + face.y + face.height) / 2).toDouble()
        val centerPoint = Point(xCenter, yCenter)

        Imgproc.circle(mRgba, centerPoint, 10, Scalar(255.0, 0.0, 0.0, 255.0), 3)
    }

    fun drawEye(eyesArray: Rect, area: Rect, size: Int) {
        var mROI: Mat
        val iris = Point()
        var eyeTemplate: Rect

        val e: Rect = eyesArray
        e.x = area.x + e.x
        e.y = area.y + e.y

        val eyeOnlyRectangle = Rect(
            e.tl().x.toInt(),
            (e.tl().y + e.height * 0.4).toInt(), e.width, (e.height * 0.6).toInt()
        )

        mROI = mGray.submat(eyeOnlyRectangle)
        val eyeCut = mRgba.submat(eyeOnlyRectangle)

        val mmG = Core.minMaxLoc(mROI)

        Imgproc.circle(eyeCut, mmG.minLoc, 2, Scalar(255.0, 255.0, 255.0, 255.0), 2)
        iris.x = mmG.minLoc.x + eyeOnlyRectangle.x
        iris.y = mmG.minLoc.y + eyeOnlyRectangle.y

        eyeTemplate = Rect(iris.x.toInt() - size / 2, iris.y.toInt() - size / 2, size, size)
        Imgproc.rectangle(mRgba, eyeTemplate.tl(), eyeTemplate.br(),
            Scalar(255.0, 0.0, 0.0, 255.0), 2
        )
    }
}