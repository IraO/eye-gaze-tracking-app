package com.iorlova.diploma.UI

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnPageScrollListener
import com.iorlova.diploma.R
import com.iorlova.diploma.UI.ReadingGoal.BaseReadingGoal
import com.iorlova.diploma.UI.ReadingGoal.CounterReadingGoal
import com.iorlova.diploma.UI.ReadingGoal.TimerReadingGoal
import com.iorlova.diploma.ViewModel.BookViewModel
import java.io.File
import java.io.InputStream

class PdfExtractor : AppCompatActivity() {

    private lateinit var bookViewModel: BookViewModel
    private var bookId: Int = 0
    private var readingGoal: BaseReadingGoal? = null
    private val onPageScrollListener = OnPageScrollListener { page, _ ->
        bookViewModel.update(bookId, page)
        if (readingGoal != null && readingGoal!!.goalId == 1) {
            readingGoal!!.update(page)
            if (readingGoal!!.isTriggered()) {
                openDialog(readingGoal!!.alert())
            }
        }    }

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
        setContentView(R.layout.activity_pdf_extractor)
        bookViewModel = ViewModelProviders.of(this).get(BookViewModel::class.java)

        bookId = intent.getStringExtra("BOOK_ID").toInt()
        val pdfView: PDFView = findViewById(R.id.pdfView)
        val file = createTemporaryFile()
        val bookPageCounter = intent.getIntExtra("BOOK_PAGE_COUNTER", 0)

        val goalId = intent.extras!!.getInt("GOAL_ID")
        val goalVal = intent.extras!!.getString("GOAL_VAL")

        when (goalId) {
            0 -> {
                readingGoal = TimerReadingGoal(goalVal!!)
                readingGoal!!.alert()
            }
            1 -> {
                readingGoal = CounterReadingGoal(bookPageCounter, goalVal!!.toInt())
                readingGoal!!.alert()
            }
        }

        pdfView.fromFile(file)
            .enableSwipe(true)
            .enableDoubletap(true)
            .defaultPage(bookPageCounter)
            .onPageScroll(onPageScrollListener)
            .swipeHorizontal(true)
            .pageSnap(true)
            .autoSpacing(true)
            .pageFling(true)
            .load()
    }

    private fun openDialog(message: String) {
        val builder = AlertDialog.Builder(this@PdfExtractor, R.style.ReadingGoalsWindow)
        val view = layoutInflater.inflate(R.layout.dialog_reading_mode, null)
        builder.setView(view)

        val messageText: TextView = view.findViewById(R.id.reading_goal)
        messageText.text = message

        builder.setPositiveButton("Finish") {dialog, which ->
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }
        builder.setNegativeButton("Continue") { dialog, which ->
            readingGoal!!.goalId = -1
        }
        val alert = builder.create()
        alert.show()
    }

    private fun File.copyInputStreamToFile(inputStream: InputStream) {
        this.outputStream().use { fileOut ->
            inputStream.copyTo(fileOut)
        }
    }

    private fun createTemporaryFile(): File {
        val bookUri = Uri.parse(intent.getStringExtra("BOOK_URI"))
        val file = File(Environment.getExternalStorageDirectory().toString() + "/" + File.separator + "tmpBook")
        val bookInputStream = contentResolver.openInputStream(bookUri)

        file.createNewFile()
        file.copyInputStreamToFile(bookInputStream!!)
        file.deleteOnExit()
        return file
    }
}
