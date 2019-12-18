package com.iorlova.diploma.UI

import android.content.Context
import android.content.Intent
import com.iorlova.diploma.Repository.Book
import com.iorlova.diploma.ViewModel.BookViewModel

class BookCoordinator {
    companion object {
        fun loadBook(activity: MainActivity, intent: Intent) {
            activity.startActivity(intent)
        }

        fun removeBook(position: Int, bookViewModel: BookViewModel, MainActivity: Context) {
            val builder = android.app.AlertDialog.Builder(MainActivity)

            builder.setTitle("Confirm")
            builder.setMessage("Are you sure you want to delete?")
            builder.setPositiveButton("YES") { _, _ ->
                val book = bookViewModel.books.value!![position]
                bookViewModel.delete(book)
            }
            builder.setNegativeButton("NO") { dialog, _ ->
                dialog.dismiss()
            }
            val alert = builder.create()
            alert.show()
        }

        fun createIntentByBookFormat(format: String, applicationContext: Context): Intent {
            return when (format) {
                BookFormat.PDF.format -> Intent(applicationContext, PdfExtractor::class.java)
                else -> Intent(applicationContext, ReadBookActivity::class.java)
            }
        }

        fun putExtrasToIntent(intent: Intent, book: Book) {
            intent.putExtra("BOOK_ID", book.id.toString())
            intent.putExtra("BOOK_URI", book.uri)
            intent.putExtra("BOOK_FORMAT", book.format)
            intent.putExtra("BOOK_PAGE_COUNTER", book.page_counter)
        }
    }
}
