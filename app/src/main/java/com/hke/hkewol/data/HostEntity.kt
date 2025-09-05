package com.hke.hkewol.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hosts")
data class HostEntity(
    @PrimaryKey val hostKey: String, // "" 表示本地主机
    val display: String,             // 展示文本（为空则显示“本地”）
    val createdAt: Long = System.currentTimeMillis()
)