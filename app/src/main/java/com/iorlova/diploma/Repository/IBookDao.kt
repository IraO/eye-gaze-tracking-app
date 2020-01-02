package com.iorlova.diploma.Repository

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface IBookDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(book: Book)

    @Delete
    fun delete(book: Book)

    @Query("UPDATE book SET page_counter = :count WHERE id =:id")
    fun update(id: Int, count: Int)

    @Query("SELECT * from book ORDER BY id ASC")
    fun getBooks(): LiveData<List<Book>>

    @Query("SELECT * from book WHERE name LIKE :substring")
    fun findBooksStartWith(substring: String): List<Book>

}
