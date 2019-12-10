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

    fun update(bookId: Int, pageCounter: Int) {
        updateAsyncTask(bookDao, bookId, pageCounter).execute()
    }

    class InsertAsyncTask(dao: IBookDao) : AsyncTask<Book, Void, Void>() {
        private val asyncTaskDao: IBookDao = dao

        override fun doInBackground(vararg params: Book): Void? {
            asyncTaskDao.insert(params[0])
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

    class updateAsyncTask: AsyncTask<Book, Void, Void> {
        val asyncTaskDao: IBookDao
        val bookId: Int
        val pageCounter: Int

        constructor(dao: IBookDao, bId: Int, pCounter: Int) {
            asyncTaskDao = dao
            bookId = bId
            pageCounter = pCounter
        }

        override fun doInBackground(vararg params: Book): Void? {
            asyncTaskDao.update(bookId, pageCounter)
            return null
        }
    }

}
