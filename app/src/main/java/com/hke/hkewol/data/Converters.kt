package com.hke.hkewol.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromBytes(bytes: ByteArray): String = bytes.joinToString(",") { it.toUByte().toString() }

    @TypeConverter
    fun toBytes(text: String): ByteArray =
        if (text.isEmpty()) byteArrayOf() else text.split(",").map { it.toInt().toByte() }.toByteArray()
}