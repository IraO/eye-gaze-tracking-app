package com.iorlova.diploma.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.iorlova.diploma.Repository.Book
import com.iorlova.diploma.Repository.BookDatabase
import com.iorlova.diploma.Repository.BookRepository

class BookViewModel(application: Application) : AndroidViewModel(application) {

    private val bookRepository: BookRepository
    val books: LiveData<List<Book>>

    init {
        val booksDao = BookDatabase.getBookDatabase(application).bookDao()
        bookRepository = BookRepository(booksDao)
        books = bookRepository.books
    }

    fun insert(book: Book) {
        bookRepository.insert(book)
    }

    fun delete(book: Book) {
        bookRepository.delete(book)
    }

    fun update(bookId: Int, count: Int) {
        bookRepository.update(bookId, count)
    }
}