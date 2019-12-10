package com.iorlova.diploma.Repository

import android.os.AsyncTask

class BookRepository(private val bookDao: IBookDao) {
    
    val books = bookDao.getBooks()

    fun insert(book: Book) {
        InsertAsyncTask(bookDao).execute(book)
    }

    fun delete(book: Book) {
        DeleteAsyncTask(bookDao).execute(book)
    }

    class InsertAsyncTask(dao: IBookDao) : AsyncTask<Book, Void, Void>() {
        private val mAsyncTaskDao: IBookDao = dao

        override fun doInBackground(vararg params: Book): Void? {
            mAsyncTaskDao.insert(params[0])
            return null
        }
    }

    class DeleteAsyncTask(BookDao: IBookDao) : AsyncTask<Book, Void, Void>() {
        private val asyncTaskDao: IBookDao = BookDao

        override fun doInBackground(vararg params: Book): Void? {
            asyncTaskDao.delete(params[0])
            return null
        }
    }
    
}
