package com.iorlova.diploma.Repository;

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity(tableName = "book_table", indices = [Index(value = ["checksum"], unique = true)])
data class Book(
    @NotNull
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val format: String = "",
    val path: String = "",
    val page_counter: Int = 0,
    val checksum: String = ""
)
