package com.ece452s24g7.mindful.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Query("SELECT * FROM entry ORDER BY date DESC")
    fun getAll(): Flow<List<Entry>>

    @Query("SELECT * FROM entry WHERE date >= :firstDayLong AND date <= :lastDayLong ORDER BY date DESC")
    fun getEntriesInMonth(firstDayLong: Long, lastDayLong: Long): Flow<List<Entry>>

    @Insert
    fun insert(entry: Entry)

    @Update
    fun update(entry: Entry)

    @Delete
    fun delete(entry: Entry)
}