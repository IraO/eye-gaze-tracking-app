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

    var learn_frames = 0
    lateinit var templateR: Mat
    lateinit var templateL: Mat

    private val mRelativeFaceSize = 0.2f
    private var mAbsoluteFaceSize = 0.0

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

    fun detectFace(rgba: Mat, gray: Mat): Mat {
        mRgba = rgba
        mGray = gray

        val absoluteFaceSize = mAbsoluteFaceSize  //Rgba.cols() * 0.2

        val faces = MatOfRect()
        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(
                mRgba, faces, 1.1, 2, 2,
                Size(absoluteFaceSize, absoluteFaceSize), Size()
            )
        }

        val facesArray = faces.toArray()
        for (i in facesArray.indices) {
            Imgproc.rectangle(
                mRgba,
                facesArray[i].tl(),
                facesArray[i].br(),
                Scalar(0.0, 255.0, 0.0, 255.0),
                3
            )
            xCenter = ((facesArray[i].x + facesArray[i].width + facesArray[i].x) / 2).toDouble()
            yCenter =
                ((facesArray[i].y + facesArray[i].y + facesArray[i].height) / 2).toDouble()
            val centerPoint = Point(xCenter, yCenter)

            Imgproc.circle(mRgba, centerPoint, 10, Scalar(255.0, 0.0, 0.0, 255.0), 3)
            Imgproc.putText(
                mRgba, "[" + centerPoint.x + "," + centerPoint.y + "]",
                Point(centerPoint.x + 20, centerPoint.y + 20),
                Core.FONT_HERSHEY_SIMPLEX, 0.7, Scalar(255.0, 255.0, 255.0, 255.0)
            )

            val r: Rect = facesArray[i]
            val eyeArea = Rect(
                r.x + r.width / 8, (r.y + (r.height / 4.5)).toInt(),
                r.width - 2 * r.width / 16, r.height / 3
            )

            val eyeRight = Rect(
                r.x + r.width / 16, (r.y + (r.height / 4.5)).toInt(),
                (r.width - 2 * r.width / 16) / 2, r.height / 3
            )

            val eyeLeft = Rect(
                r.x + r.width / 16 + (r.width - 2 * r.width / 16) / 2,
                (r.y + (r.height / 4.5)).toInt(), (r.width - 2 * r.width / 16) / 2, r.height / 3
            )

            Imgproc.rectangle(
                mRgba,
                eyeLeft.tl(),
                eyeLeft.br(),
                Scalar(255.0, 0.0, 0.0, 255.0),
                1
            )
            Imgproc.rectangle(
                mRgba,
                eyeRight.tl(),
                eyeRight.br(),
                Scalar(255.0, 0.0, 0.0, 255.0),
                1
            )

            templateL = getTemplate(eyeDetectorClassifier, eyeRight, 24)
            templateR = getTemplate(eyeDetectorClassifier, eyeLeft, 24)
            learn_frames++
//          matchEye(eyeLeft, templateL, method)
//          matchEye(eyeRight, templateR, method)
        }

        return mRgba
}

    fun rotate(matrix: Mat, rotateCode: Int): Mat {
//      Point pivot = new Point(modified.rows() / 2, modified.cols() / 2);
//      Mat rotationMatrix2D = Imgproc.getRotationMatrix2D(pivot, 360.0f, 1.0f);
//      Utils.matToBitmap(rotationMatrix2D, mCacheBitmap);
        val destination = Mat()
        Core.rotate(matrix, destination, rotateCode)

//      val resizeImage = Mat()
//      val sz = Size(600.0, 600.0)
//
//      Imgproc.resize(destination, resizeImage, sz)
        return destination
    }

    fun getTemplate(classifier: CascadeClassifier, area: Rect, size: Int): Mat {
        var template = Mat()
        var mROI = mGray.submat(area)
        val eyes = MatOfRect()
        val iris = Point()
        var eye_template: Rect

        classifier.detectMultiScale(
            mROI, eyes, 1.15, 2, Objdetect.CASCADE_FIND_BIGGEST_OBJECT
                    or Objdetect.CASCADE_SCALE_IMAGE, Size(30.0, 30.0), Size()
        )

        val eyesArray = eyes.toArray()
        for (i in eyesArray.indices) {
            val e: Rect = eyesArray[i]
            e.x = area.x + e.x
            e.y = area.y + e.y

            val eye_only_rectangle = Rect(
                e.tl().x.toInt(),
                (e.tl().y + e.height * 0.4).toInt(), e.width, (e.height * 0.6).toInt()
            )
            mROI = mGray.submat(eye_only_rectangle)
            val vyrez = mRgba.submat(eye_only_rectangle)


            val mmG = Core.minMaxLoc(mROI)

            Imgproc.circle(vyrez, mmG.minLoc, 2, Scalar(255.0, 255.0, 255.0, 255.0), 2)
            iris.x = mmG.minLoc.x + eye_only_rectangle.x
            iris.y = mmG.minLoc.y + eye_only_rectangle.y

            eye_template = Rect(iris.x.toInt() - size / 2, iris.y.toInt() - size / 2, size, size)
            Imgproc.rectangle(
                mRgba, eye_template.tl(), eye_template.br(),
                Scalar(255.0, 0.0, 0.0, 255.0), 2
            )

            template = (mGray.submat(eye_template)).clone()
            return template
        }
        return template
    }

    private fun matchEye(area: Rect, mTemplate: Mat, type: Int) {
        var matchLocation: Point
        val mROI: Mat = mGray.submat(area)
        val resultCols = mROI.cols() - mTemplate.cols() + 1
        val resultRows = mROI.rows() - mTemplate.rows() + 1

        if (mTemplate.cols() == 0 || mTemplate.rows() == 0) {
            return
        }

        val mResult = Mat(resultCols, resultRows, CvType.CV_8U)
        val mmres = Core.minMaxLoc(mResult)

        matchLocation = mmres.minLoc
        val matchLoc_tx = Point(matchLocation.x + area.x, matchLocation.y + area.y)
        val matchLoc_ty = Point(
            matchLocation.x + mTemplate.cols() + area.x,
            matchLocation.y + mTemplate.rows() + area.y
        )

        Imgproc.rectangle(
            mRgba, matchLoc_tx, matchLoc_ty, Scalar(
                255.0, 255.0, 0.0,
                255.0
            )
        )
        val rec = Rect(matchLoc_tx, matchLoc_ty)
    }
}