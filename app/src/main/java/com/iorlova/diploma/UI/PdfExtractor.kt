package com.iorlova.diploma.UI

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnPageScrollListener
import com.iorlova.diploma.R
import com.iorlova.diploma.ViewModel.BookViewModel

class PdfExtractor : AppCompatActivity() {
    private lateinit var bookViewModel: BookViewModel
    private var bookId: Int = 0
    private val onPageScrollListener = OnPageScrollListener { page, _ -> bookViewModel.update(bookId, page) }

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
        val bookUri = Uri.parse(intent.getStringExtra("BOOK_URI"))
        val bookPageCounter = intent.getIntExtra("BOOK_PAGE_COUNTER", 0)

        val pdfView: PDFView = findViewById(R.id.pdfView)
        val file = createNewFile(bookUri, contentResolver)

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

}
