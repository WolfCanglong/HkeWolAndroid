package com.hke.hkewol.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "last_success")
data class LastSuccessEntity(
    @PrimaryKey val singleton: Int = 1,
    val hostKey: String,
    val macId: Long
)