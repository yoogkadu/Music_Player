package com.example.musicplayer.database.utils

import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson

class Converters {
    private val gson = Gson()
    @TypeConverter
    fun fromFloatArray(value: FloatArray?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toFloatArray(value: String?): FloatArray? {
        if (value == null) return null
        val type = object : TypeToken<FloatArray>() {}.type
        return gson.fromJson(value, type)
    }

    // Standard Long to Date converters for timestamps
    @TypeConverter
    fun fromTimestamp(value: Long?): java.util.Date? {
        return value?.let { java.util.Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: java.util.Date?): Long? {
        return date?.time
    }
}