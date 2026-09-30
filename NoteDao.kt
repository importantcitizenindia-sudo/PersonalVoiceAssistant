package com.example.personalvoiceassistant

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NoteDao {
    @Insert
    suspend fun insert(note: Note)

    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    suspend fun getAll(): List<Note>

    @Query("DELETE FROM notes")
    suspend fun deleteAll()
}
