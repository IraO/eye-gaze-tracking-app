package com.iorlova.diploma.UI

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.iorlova.diploma.R
import com.iorlova.diploma.ViewModel.BookViewModel
import java.io.File

class PdfExtractor : AppCompatActivity() {
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
        setContentView(R.layout.activity_pdf_extractor)

        val intent: Intent = intent
        val bookPath = intent.getStringExtra("BOOK_PATH")
        val pageCount = intent.getIntExtra("BOOK_PAGE_COUNTER", 0)
        bookId = intent.getStringExtra("BOOK_ID").toInt()

        bookViewModel = ViewModelProviders.of(this).get(BookViewModel::class.java)

        val pdfView: PDFView = findViewById(R.id.pdfView)
        var file = File(bookPath)
        pdfView.fromFile(file)
            .enableSwipe(true) // allows to block changing pages using swipe
            .enableDoubletap(true)
            .defaultPage(pageCount)
            .onPageChange(onPageScrollListener) //.onTap(onTapListener)
            .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
            .load()
    }

    private val onPageScrollListener = object: OnPageChangeListener {
        override fun onPageChanged(page: Int, pageCount: Int) {
            bookViewModel.update(bookId, pageCount)
        }
    }
}
