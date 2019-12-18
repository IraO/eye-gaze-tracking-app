package com.iorlova.diploma.UI

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import com.iorlova.diploma.Repository.Book

class BookFactory {
    companion object {
        fun createBook(bookUri: Uri, contentResolver: ContentResolver): Book {
            val name = getName(bookUri, contentResolver)
            val format = name.substringAfterLast(".")

            return Book(name = name, format = format, uri = bookUri.toString())
        }

        private fun getName(bookUri: Uri, contentResolver: ContentResolver): String {
            val cursor = contentResolver.query(bookUri, null, null, null, null)
            val nameIndex = cursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            val bookName = cursor.getString(nameIndex)
            cursor.close()

            return bookName
        }
    }
}
