package com.hke.hkewol.data
import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository private constructor(context: Context) {
    private val db = Room.databaseBuilder(context, AppDatabase::class.java, "woltool.db").build()
    private val dao = db.dao()

    suspend fun getHosts() = dao.getHosts()
    // Repository.kt
    suspend fun upsertHost(hostKey: String, display: String? = null) {
        val existing = dao.getHosts().find { it.hostKey == hostKey }
        // 如果是第一次保存非空 hostKey，并且存在本地临时主机，则迁移数据
        if (hostKey.isNotBlank()) {
            migrateLocalToRealHost(hostKey)
        }
        val finalDisplay = when {
            !display.isNullOrBlank() -> display
            existing?.display?.isNotBlank() == true -> existing.display
            hostKey.isEmpty() -> "本地"
            else -> hostKey
        }
        dao.upsertHost(
            HostEntity(
                hostKey = hostKey,
                display = finalDisplay
            )
        )
    }

    /**
     * 将本地临时主机("")下的 MAC 记录迁移到新的 hostKey，并删除本地主机
     */
    private suspend fun migrateLocalToRealHost(newHostKey: String) {
        val localHostKey = "" // 你定义的本地主机 key
        val localMacs = dao.getMacsByHost(localHostKey)
        if (localMacs.isNotEmpty()) {
            // 更新 MAC 记录的 hostKey
            localMacs.forEach { mac ->
                dao.updateMac(mac.copy(hostKey = newHostKey))
            }
            // 删除本地主机记录
            dao.deleteHost(localHostKey)
        }
    }



    suspend fun deleteHost(hostKey: String) {
        dao.deleteMacsByHost(hostKey)
        dao.deleteHost(hostKey)
    }

    suspend fun getMacs(hostKey: String) = dao.getMacsByHost(hostKey)

    suspend fun upsertMac(hostKey: String, macText: String, macRaw: ByteArray, nickname: String?): MacEntity {
        val existing = dao.getMacByHostAndText(hostKey, macText)
        val now = System.currentTimeMillis()
        return if (existing != null) {
            val updated = existing.copy(nickname = nickname ?: existing.nickname, lastUsedAt = now)
            dao.updateMac(updated)
            updated
        } else {
            val id = dao.insertMac(MacEntity(hostKey = hostKey, macRaw = macRaw, macText = macText, nickname = nickname, lastUsedAt = now))
            dao.getMacByHostAndText(hostKey, macText)!!
        }
    }

    suspend fun deleteMac(macId: Long) = dao.deleteMac(macId)

    suspend fun updateHostDisplay(hostKey: String, display: String) {
        dao.updateDisplay(hostKey, display)
    }


    suspend fun setLastSuccess(hostKey: String, macId: Long) = dao.setLastSuccess(LastSuccessEntity(hostKey = hostKey, macId = macId))
    suspend fun getLastSuccess() = dao.getLastSuccess()

    companion object {
        @Volatile private var INSTANCE: Repository? = null
        fun get(context: Context): Repository = INSTANCE ?: synchronized(this) {
            Repository(context.applicationContext).also { INSTANCE = it }
        }
    }
}