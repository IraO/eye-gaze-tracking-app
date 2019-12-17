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
        UpdateAsyncTask(bookDao, bookId, pageCounter).execute()
    }

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

class UpdateAsyncTask(dao: IBookDao, bId: Int, pCounter: Int) : AsyncTask<Book, Void, Void>() {
    private val asyncTaskDao: IBookDao = dao
    private val bookId: Int = bId
    private val pageCounter: Int = pCounter

    override fun doInBackground(vararg params: Book): Void? {
        asyncTaskDao.update(bookId, pageCounter)
        return null
    }
}

