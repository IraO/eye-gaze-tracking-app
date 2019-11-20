package com.iorlova.diploma.Repository

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface IBookDao {

    @Query("SELECT * from book_table ORDER BY id ASC")
    fun getBooks(): LiveData<List<Book>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(book: Book)

    @Delete
    fun delete(book: Book)

    @Query("SELECT * from book_table WHERE name LIKE :substring")
    fun findBooksStartWith(substring: String): List<Book>

}
