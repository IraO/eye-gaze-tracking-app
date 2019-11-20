package com.iorlova.diploma.Repository;

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity(tableName = "book_table")
data class Book(
    @NotNull
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val format: String = "",
    val path: String = "",
    val page_counter: Int = 0
)