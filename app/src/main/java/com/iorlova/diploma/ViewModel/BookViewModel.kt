package com.iorlova.diploma.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.iorlova.diploma.Repository.Book
import com.iorlova.diploma.Repository.BookDatabase
import com.iorlova.diploma.Repository.BookRepository

class BookViewModel(application: Application): AndroidViewModel(application) {

    private val mRepository: BookRepository
    val allBooks: LiveData<List<Book>>

    init {
        val booksDao = BookDatabase.getBookDatabase(application).bookDao()
        mRepository = BookRepository(booksDao)
        allBooks = mRepository.mAllBooks
    }

    fun insert(book: Book){
        mRepository.inset(book)
    }

    fun delete(book: Book) {
        mRepository.delete(book)
    }

    fun update(bookId: Int, count: Int) {
        mRepository.update(bookId, count)
    }
}