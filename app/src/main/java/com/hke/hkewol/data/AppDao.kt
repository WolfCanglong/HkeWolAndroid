package com.hke.hkewol.data

import androidx.room.*

@Dao
interface AppDao {
    @Query("SELECT * FROM hosts ORDER BY createdAt DESC")
    suspend fun getHosts(): List<HostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHost(host: HostEntity)

    @Query("DELETE FROM hosts WHERE hostKey = :hostKey")
    suspend fun deleteHost(hostKey: String)

    @Query("SELECT * FROM macs WHERE hostKey = :hostKey ORDER BY lastUsedAt DESC")
    suspend fun getMacsByHost(hostKey: String): List<MacEntity>

    @Insert
    suspend fun insertMac(mac: MacEntity): Long

    @Update
    suspend fun updateMac(mac: MacEntity)

    @Query("DELETE FROM macs WHERE id = :macId")
    suspend fun deleteMac(macId: Long)

    @Query("DELETE FROM macs WHERE hostKey = :hostKey")
    suspend fun deleteMacsByHost(hostKey: String)

    @Query("SELECT * FROM macs WHERE hostKey=:hostKey AND macText=:macText LIMIT 1")
    suspend fun getMacByHostAndText(hostKey: String, macText: String): MacEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setLastSuccess(last: LastSuccessEntity)

    @Query("SELECT * FROM last_success WHERE singleton=1")
    suspend fun getLastSuccess(): LastSuccessEntity?

    @Query("UPDATE hosts SET display = :display WHERE hostKey = :hostKey")
    suspend fun updateDisplay(hostKey: String, display: String)

}