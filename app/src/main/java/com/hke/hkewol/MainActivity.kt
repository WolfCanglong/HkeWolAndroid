package com.hke.hkewol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.hke.hkewol.ui.MainScreen
import com.hke.hkewol.ui.MainViewModel
import com.hke.hkewol.ui.theme.HkeWolTheme

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HkeWolTheme {
                Surface {
                    val state by vm.ui.collectAsState()
                    MainScreen(
                        state = state,
                        onHostChange = vm::onHostChange,
                        onMacChange = vm::onMacChange,
                        onNameChange = vm::onNameChange,
                        onSelectHost = vm::selectHost,
                        onEditHostNickname = vm::editHostNickname,
                        onSelectMac = vm::selectMac,
                        onDeleteHost = vm::deleteHost,
                        onDeleteMac = vm::deleteMac,
                        onLocalWake = vm::sendLocalWake,
                        onNetworkWake = vm::sendNetworkWake
                    )
                }
            }
        }
    }
}
