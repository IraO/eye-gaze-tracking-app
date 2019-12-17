package com.iorlova.diploma.Repository;

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity(indices = [Index(value = ["name", "format"], unique = true)])
data class Book(
    @NotNull
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val format: String = "",
    val uri: String = "",
    val page_counter: Int = 0
)
