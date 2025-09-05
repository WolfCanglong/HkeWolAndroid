package com.hke.hkewol.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hke.hkewol.data.HostEntity
import com.hke.hkewol.data.MacEntity

private val BUTTON_BAR_HEIGHT = 48.dp
private val MESSAGE_BAR_HEIGHT = 28.dp

@Composable
fun MainScreen(
    state: UiState,
    onHostChange: (String) -> Unit,
    onMacChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onSelectHost: (String) -> Unit,
    onEditHostNickname: (String, String) -> Unit,
    onSelectMac: (MacEntity) -> Unit,
    onDeleteHost: (String) -> Unit,
    onDeleteMac: (Long) -> Unit,
    onLocalWake: () -> Unit,
    onNetworkWake: () -> Unit
) {
    var showEditHostDialog by remember { mutableStateOf(false) }
    var editHostName by remember { mutableStateOf("") }
    var showDeleteHostConfirm by remember { mutableStateOf(false) }
    var macToDelete by remember { mutableStateOf<MacEntity?>(null) }

    Column(
        Modifier
            .statusBarsPadding() // 顶部避开状态栏
            .padding(horizontal = 16.dp)
            .fillMaxSize()
    ) {
        HostSection(
            hosts = state.hosts,
            hostInput = state.hostInput,
            onHostChange = onHostChange,
            onSelectHost = onSelectHost,
            onEditClick = {
                if (state.hostInput.isNotBlank()) {
                    editHostName = state.hosts.find { it.hostKey == state.hostInput }?.display
                        ?: state.hostInput
                    showEditHostDialog = true
                }
            }
        )

        // 固定高度按钮区
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(BUTTON_BAR_HEIGHT)
        ) {
            Button(
                onClick = {
                    if (state.macInput.isBlank()) {
                        // 可改成更新 message 或 Toast
                    } else {
                        onLocalWake()
                    }
                },
                modifier = Modifier.weight(1f)
            ) { Text("本地唤醒") }

            Button(
                onClick = {
                    if (state.hostInput.isBlank()) {
                        // 可改成更新 message 或 Toast
                    } else {
                        onNetworkWake()
                    }
                },
                modifier = Modifier.weight(1f)
            ) { Text("网络唤醒") }
        }

        // 列表占据中间空间，底部留白给消息
        MacSection(
            macs = state.macs,
            macInput = state.macInput,
            nameInput = state.nameInput,
            onMacChange = onMacChange,
            onNameChange = onNameChange,
            onSelectMac = onSelectMac,
            onDeleteMacClick = { macToDelete = it },
            bottomPadding = MESSAGE_BAR_HEIGHT + 12.dp,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        // 固定高度消息区
        MessageBar(
            message = state.message,
            modifier = Modifier
                .fillMaxWidth()
                .height(MESSAGE_BAR_HEIGHT)
        )
    }

    // 主机编辑对话框
    if (showEditHostDialog) {
        AlertDialog(
            onDismissRequest = { showEditHostDialog = false },
            title = { Text("编辑主机") },
            text = {
                OutlinedTextField(
                    value = editHostName,
                    onValueChange = { editHostName = it },
                    label = { Text("昵称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onEditHostNickname(state.hostInput, editHostName)
                    showEditHostDialog = false
                }) { Text("保存") }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = { showDeleteHostConfirm = true },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) { Text("删除主机") }

                    Spacer(Modifier.width(8.dp))

                    TextButton(onClick = { showEditHostDialog = false }) {
                        Text("取消")
                    }
                }
            }
        )
    }

    // 主机删除确认
    if (showDeleteHostConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteHostConfirm = false },
            title = { Text("确认删除主机？") },
            text = { Text("删除后将无法恢复，并会移除该主机下的所有 MAC 记录。") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteHost(state.hostInput)
                    showDeleteHostConfirm = false
                    showEditHostDialog = false
                }) { Text("确认") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteHostConfirm = false }) { Text("取消") }
            }
        )
    }

    // MAC 删除确认
    if (macToDelete != null) {
        AlertDialog(
            onDismissRequest = { macToDelete = null },
            title = { Text("确认删除 MAC？") },
            text = {
                Text("将删除 ${macToDelete!!.nickname ?: macToDelete!!.macText}，此操作不可恢复。")
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteMac(macToDelete!!.id)
                    macToDelete = null
                }) { Text("确认") }
            },
            dismissButton = {
                TextButton(onClick = { macToDelete = null }) { Text("取消") }
            }
        )
    }
}

@Composable
fun HostSection(
    hosts: List<HostEntity>,
    hostInput: String,
    onHostChange: (String) -> Unit,
    onSelectHost: (String) -> Unit,
    onEditClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(
            value = hostInput,
            onValueChange = onHostChange,
            label = { Text("主机地址") },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑主机昵称")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (hosts.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(hosts) { h ->
                    AssistChip(
                        onClick = { onSelectHost(h.hostKey) },
                        label = { Text(h.display) }
                    )
                }
            }
        }
    }
}

@Composable
fun MacSection(
    macs: List<MacEntity>,
    macInput: String,
    nameInput: String,
    onMacChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onSelectMac: (MacEntity) -> Unit,
    onDeleteMacClick: (MacEntity) -> Unit,
    bottomPadding: Dp,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        OutlinedTextField(
            value = macInput,
            onValueChange = onMacChange,
            label = { Text("MAC 地址（AA:BB:CC:DD:EE:FF）") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = nameInput,
            onValueChange = onNameChange,
            label = { Text("名称（可选）") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (macs.isNotEmpty()) {
            Text("MAC 历史", style = MaterialTheme.typography.labelLarge)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = bottomPadding),
                modifier = Modifier.fillMaxSize()
            ) {
                items(macs, key = { it.id }) { m ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectMac(m) }
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    m.nickname?.takeIf { it.isNotBlank() } ?: m.macText,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (!m.nickname.isNullOrBlank()) {
                                    Text(
                                        m.macText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = { onDeleteMacClick(m) }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "删除",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBar(message: String?, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        AnimatedVisibility(visible = message != null) {
            val color = if (message?.startsWith("发送失败") == true)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.primary
            Text(message ?: "", color = color)
        }
    }
}
