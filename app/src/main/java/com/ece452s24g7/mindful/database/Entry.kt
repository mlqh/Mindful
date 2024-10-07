package com.ece452s24g7.mindful.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class Entry(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "date") val date: Date,
    @ColumnInfo(name = "text") val text: String?,
    @ColumnInfo(name = "imageURIs") val imageURIs: List<String>,
    @ColumnInfo(name = "videoURIs") val videoURIs: List<String>,
    @ColumnInfo(name = "audioPath") val audioPath: String?,
    @ColumnInfo(name = "location") val location: String?
)
