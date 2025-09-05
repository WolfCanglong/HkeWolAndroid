package com.hke.hkewol.data
import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository private constructor(context: Context) {
    private val db = Room.databaseBuilder(context, AppDatabase::class.java, "woltool.db").build()
    private val dao = db.dao()

    suspend fun getHosts() = dao.getHosts()
    suspend fun upsertHost(hostKey: String) = dao.upsertHost(
        HostEntity(
            hostKey = hostKey,
            display = if (hostKey.isEmpty()) "本地" else hostKey
        )
    )
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