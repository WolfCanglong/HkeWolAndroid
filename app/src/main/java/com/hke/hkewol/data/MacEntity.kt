package com.hke.hkewol.data
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "macs")
data class MacEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hostKey: String,       // 对应 HostEntity.hostKey
    val macRaw: ByteArray,     // 6 字节规范化 MAC
    val macText: String,       // 规范化文本：AA:BB:CC:DD:EE:FF
    val nickname: String?,     // 可选昵称
    val lastUsedAt: Long = 0
)