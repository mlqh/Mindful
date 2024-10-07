package com.ece452s24g7.mindful.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import java.io.File

@Database(entities = [Entry::class], version = 1)
@TypeConverters(Converters::class)
abstract class EntryDatabase : RoomDatabase() {
    abstract fun dao(): EntryDao

    companion object {
        @Volatile
        private var INSTANCE: EntryDatabase? = null

        fun getInstance(context: Context, forceUpdate: Boolean = false): EntryDatabase {
            if ((INSTANCE == null) or forceUpdate) {
                synchronized(this) {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        EntryDatabase::class.java,
                        "entry.db"
                    ).allowMainThreadQueries() // Synchronous operations for now, implement async later
                        .fallbackToDestructiveMigration()
                        .setJournalMode(JournalMode.TRUNCATE) // done to avoid needing to close db on backup
                        .build()
                    INSTANCE = instance
                }
            }

            return INSTANCE!!
        }

        // used when restoring a db backup
        fun overwriteDatabase(context: Context, databaseFile: File) {
            var instance = getInstance(context)

            // overwriting the db with builder + createFromFile() refused to work
            databaseFile.copyTo(context.getDatabasePath("entry.db"), true)

            // rebuild the room db now that the underlying file has been overwritten
            getInstance(context, true)
        }
    }
}