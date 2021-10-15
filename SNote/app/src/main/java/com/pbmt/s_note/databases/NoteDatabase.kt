package com.pbmt.s_note.databases

import android.content.Context
import android.provider.ContactsContract
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pbmt.s_note.dao.NoteDao
import com.pbmt.s_note.entities.Notes

@Database(entities = [Notes::class],version = 1,exportSchema = false)
abstract class NoteDatabase :RoomDatabase(){

    companion object {
        var notesDatabase: NoteDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): NoteDatabase {
            if (notesDatabase == null) {
                notesDatabase = Room.databaseBuilder(
                    context
                    , NoteDatabase::class.java
                    , "notes.db"
                ).build()
            }
            return notesDatabase!!
        }

    }

    abstract fun noteDao(): NoteDao
}