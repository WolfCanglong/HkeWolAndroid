package com.hke.hkewol.net

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.*

object NetHelpers {

    // 获取当前网卡的广播地址
    private fun getBroadcastAddress(context: Context): InetAddress {
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
        val dhcp = wm.dhcpInfo ?: return InetAddress.getByName("255.255.255.255")
        val broadcast = (dhcp.ipAddress and dhcp.netmask) or (dhcp.netmask.inv())
        val quads = ByteArray(4)
        for (k in 0..3) quads[k] = ((broadcast shr k * 8) and 0xFF).toByte()
        return InetAddress.getByAddress(quads)
    }

    // 标准 WOL Magic Packet
    suspend fun sendWolBroadcast(context: Context, mac: ByteArray, port: Int = 2223) = withContext(Dispatchers.IO) {
        require(mac.size == 6)
        val magic = ByteArray(6) { 0xFF.toByte() } + ByteArray(16 * 6) { 0 }
        for (i in 0 until 16) {
            System.arraycopy(mac, 0, magic, 6 + i * 6, 6)
        }
        DatagramSocket().use { socket ->
            socket.broadcast = true
            val broadcastAddr = getBroadcastAddress(context)
            val packet = DatagramPacket(magic, magic.size, broadcastAddr, port)
            socket.send(packet)
        }
    }

    // 网络唤醒
    suspend fun sendNetworkWake(host: String, mac: ByteArray, port: Int = 2223) = withContext(Dispatchers.IO) {
        require(mac.size == 6)
        val data = ByteArray(7)
        data[0] = 0xFA.toByte()
        System.arraycopy(mac, 0, data, 1, 6)
        val address = InetAddress.getByName(host)
        DatagramSocket().use { socket ->
            val packet = DatagramPacket(data, data.size, address, port)
            socket.send(packet)
        }
    }
}