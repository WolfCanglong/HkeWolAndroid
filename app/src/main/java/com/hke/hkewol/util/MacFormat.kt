package com.hke.hkewol.util
object MacFormat {
    fun parseMac(text: String): ByteArray {
        val hex = text.uppercase()
            .replace("-", ":")
            .replace(".", "")
            .replace(" ", "")
            .let {
                if (it.contains(":")) it.split(":").joinToString("") else it
            }
        require(hex.length == 12) { "MAC 长度应为12个十六进制字符" }
        val bytes = ByteArray(6)
        for (i in 0 until 6) {
            val b = hex.substring(i * 2, i * 2 + 2).toInt(16)
            bytes[i] = b.toByte()
        }
        return bytes
    }

    fun normalizeText(text: String): String {
        val b = parseMac(text)
        return b.joinToString(":") { "%02X".format(it.toInt() and 0xFF) }
    }
}