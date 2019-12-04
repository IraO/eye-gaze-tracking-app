package com.iorlova.diploma.Repository

import android.os.AsyncTask
import androidx.lifecycle.LiveData

class BookRepository(private val bookDao: IBookDao) {
    
    val mAllBooks = bookDao.getBooks()

    fun getAllBooks() = mAllBooks

    fun inset(book: Book) {
        insertAsyncTask(bookDao).execute(book)
    }

    fun delete(book: Book) {
        deleteAsyncTask(bookDao).execute(book)
    }

    fun update(bookId: Int, pageCounter: Int) {
        updateAsyncTask(bookDao, bookId, pageCounter).execute()
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

    class deleteAsyncTask: AsyncTask<Book, Void, Void> {
        val mAsyncTaskDao: IBookDao

        constructor(dao: IBookDao) {
            mAsyncTaskDao = dao
        }

        override fun doInBackground(vararg params: Book): Void? {
            mAsyncTaskDao.delete(params[0])
            return null
        }
    }

    class updateAsyncTask: AsyncTask<Book, Void, Void> {
        val mAsyncTaskDao: IBookDao
        val bookId: Int
        val pageCounter: Int

        constructor(dao: IBookDao, bId: Int, pCounter: Int) {
            mAsyncTaskDao = dao
            bookId = bId
            pageCounter = pCounter
        }

        override fun doInBackground(vararg params: Book): Void? {
            mAsyncTaskDao.update(bookId, pageCounter)
            return null
        }
    }

}
