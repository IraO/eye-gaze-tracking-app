package com.iorlova.diploma.Repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Book::class], version = 1)
abstract class BookDatabase: RoomDatabase(){
    abstract fun bookDao(): IBookDao


    companion object {
        @Volatile
        private var INSTANCE: BookDatabase? = null

        fun getBookDatabase(context: Context): BookDatabase {
            return INSTANCE?: synchronized(this) {
                val inst = Room.databaseBuilder(context.applicationContext,
                    BookDatabase::class.java, "book_database")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = inst
                return inst
            }
        }
    }
}