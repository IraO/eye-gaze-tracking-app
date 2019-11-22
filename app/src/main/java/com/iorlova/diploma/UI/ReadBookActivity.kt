package com.iorlova.diploma.UI

import android.content.Intent
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextPaint
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.ViewPager
import com.iorlova.diploma.R
import com.iorlova.diploma.UI.PageSplitter.PageSplitter
import com.iorlova.diploma.UI.PageSplitter.TextPagerAdapter
import com.rtfparserkit.converter.text.StringTextConverter
import com.rtfparserkit.parser.RtfStreamSource
import java.io.*
import java.nio.charset.StandardCharsets

class ReadBookActivity : AppCompatActivity() {
    private lateinit var pagesView: ViewPager

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
        pagesView = findViewById(R.id.pages)
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x
        val height = size.y

        val pageSplitter = PageSplitter(width, height, 1f, 0)
        val textPaint = TextPaint()
        textPaint.textSize = resources.getDimension(R.dimen.text_size)

        val intent: Intent = intent
        val bookPath = intent.getStringExtra("BOOK_PATH")
        var text = readFile(bookPath)

        if (text != null) {
            if (bookPath.endsWith(".rtf")) {
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
                R.string.invalid_path,
                Toast.LENGTH_LONG).show()

            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun extractRTF(textRTF: String): String{
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

    private fun readFile(fileName: String): String? {
        var line: String? = null
        val TAG = "PAGE_SPLITTER"

        try {
            val fileInputStream =
                FileInputStream(File(fileName))

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
}
