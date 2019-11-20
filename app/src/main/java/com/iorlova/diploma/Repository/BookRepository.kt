package com.iorlova.diploma.Repository

import android.os.AsyncTask
import androidx.lifecycle.LiveData

class BookRepository(private val bookDao: IBookDao) {
    
    val mAllBooks = bookDao.getBooks()

    fun getAllBooks() = mAllBooks

    fun inset(book: Book) {
        insertAsyncTask(bookDao).execute(book)
    }

    class insertAsyncTask: AsyncTask<Book, Void, Void> {
        val mAsyncTaskDao: IBookDao

        constructor(dao: IBookDao) {
            mAsyncTaskDao = dao
        }

        override fun doInBackground(vararg params: Book): Void? {
            mAsyncTaskDao.insert(params[0])
            return null
        }
        
    }
    
}
