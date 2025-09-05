package com.hke.hkewol.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hke.hkewol.data.HostEntity
import com.hke.hkewol.data.MacEntity
import com.hke.hkewol.data.Repository
import com.hke.hkewol.net.NetHelpers
import com.hke.hkewol.util.MacFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UiState(
    val hosts: List<HostEntity> = emptyList(),
    val macs: List<MacEntity> = emptyList(),
    val hostInput: String = "",
    val macInput: String = "",
    val nameInput: String = "",
    val sending: Boolean = false,
    val message: String? = null
)

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = Repository.get(app)
    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    init {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(hosts = repo.getHosts())
            repo.getLastSuccess()?.let { last ->
                val macs = repo.getMacs(last.hostKey)
                val mac = macs.find { it.id == last.macId }
                _ui.value = _ui.value.copy(
                    hostInput = last.hostKey,
                    macs = macs,
                    macInput = mac?.macText ?: "",
                    nameInput = mac?.nickname ?: ""
                )
            }
        }
    }

    fun onHostChange(text: String) {
        _ui.value = _ui.value.copy(hostInput = text)
        viewModelScope.launch {
            _ui.value = _ui.value.copy(macs = repo.getMacs(text.trim()))
        }
    }

    fun onMacChange(text: String) {
        _ui.value = _ui.value.copy(macInput = text)
    }

    fun onNameChange(text: String) {
        _ui.value = _ui.value.copy(nameInput = text)
    }

    fun selectHost(hostKey: String) {
        _ui.value = _ui.value.copy(hostInput = hostKey)
        viewModelScope.launch {
            _ui.value = _ui.value.copy(macs = repo.getMacs(hostKey))
        }
    }

    fun editHostNickname(hostKey: String, newName: String) {
        viewModelScope.launch {
            repo.updateHostDisplay(hostKey, newName)
            _ui.value = _ui.value.copy(hosts = repo.getHosts())
        }
    }


    fun selectMac(mac: MacEntity) {
        _ui.value = _ui.value.copy(
            macInput = mac.macText,
            nameInput = mac.nickname ?: ""
        )
    }

    fun deleteHost(hostKey: String) {
        viewModelScope.launch {
            repo.deleteHost(hostKey)
            _ui.value = _ui.value.copy(
                hosts = repo.getHosts(),
                macs = emptyList()
            )
        }
    }

    fun deleteMac(macId: Long) {
        viewModelScope.launch {
            repo.deleteMac(macId)
            _ui.value = _ui.value.copy(macs = repo.getMacs(_ui.value.hostInput.trim()))
        }
    }

    fun sendLocalWake() {
        viewModelScope.launch {
            try {
                _ui.value = _ui.value.copy(sending = true, message = null)
                val macRaw = MacFormat.parseMac(_ui.value.macInput)

                val hostKey = _ui.value.hostInput.trim()
                val currentDisplay = _ui.value.hosts.find { it.hostKey == hostKey }?.display

                // 保留昵称
                repo.upsertHost(hostKey, currentDisplay)

                val mac = repo.upsertMac(
                    hostKey = hostKey,
                    macText = MacFormat.normalizeText(_ui.value.macInput),
                    macRaw = macRaw,
                    nickname = _ui.value.nameInput.ifEmpty { null }
                )
                repo.setLastSuccess(hostKey, mac.id)

                NetHelpers.sendWolBroadcast(getApplication(), macRaw)

                _ui.value = _ui.value.copy(
                    message = "本地唤醒已发送",
                    macs = repo.getMacs(hostKey),
                    hosts = repo.getHosts()
                )
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(message = "发送失败: ${e.message}")
            } finally {
                _ui.value = _ui.value.copy(sending = false)
            }
        }
    }

    fun sendNetworkWake() {
        viewModelScope.launch {
            try {
                _ui.value = _ui.value.copy(sending = true, message = null)
                val hostKey = _ui.value.hostInput.trim()
                require(hostKey.isNotEmpty()) { "网络唤醒需要填写主机地址" }
                val macRaw = MacFormat.parseMac(_ui.value.macInput)

                val currentDisplay = _ui.value.hosts.find { it.hostKey == hostKey }?.display

                // 保留昵称
                repo.upsertHost(hostKey, currentDisplay)

                val mac = repo.upsertMac(
                    hostKey = hostKey,
                    macText = MacFormat.normalizeText(_ui.value.macInput),
                    macRaw = macRaw,
                    nickname = _ui.value.nameInput.ifEmpty { null }
                )
                repo.setLastSuccess(hostKey, mac.id)

                NetHelpers.sendNetworkWake(hostKey, macRaw)

                _ui.value = _ui.value.copy(
                    message = "网络唤醒已发送",
                    macs = repo.getMacs(hostKey),
                    hosts = repo.getHosts()
                )
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(message = "发送失败: ${e.message}")
            } finally {
                _ui.value = _ui.value.copy(sending = false)
            }
        }
    }

}
