package com.ece452s24g7.mindful.database

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromString(value: String?): List<String>? {
        return value?.split(";")?.filterNot { it == "" }
    }

    @TypeConverter
    fun listToString(list: List<String>): String {
        val builder = StringBuilder()
        for (str in list) {
            builder.append(str)
            builder.append(';')
        }
        return builder.toString()
    }
}