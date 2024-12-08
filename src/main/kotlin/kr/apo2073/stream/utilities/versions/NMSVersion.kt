package kr.apo2073.stream.utilities.versions

import org.bukkit.Bukkit

object NMSVersion {
    private val version: String = Bukkit.getServer().javaClass.getPackage().name.split(".")[3]
    private val majorVersion: Int = version.split("_")[1].toInt()

    fun getPackageName(): String = "org.bukkit.craftbukkit.$version."

    fun getNMSPackageName(): String = when {
        majorVersion < 17 -> "net.minecraft.server.$version."
        else -> "net.minecraft.network."
    }

    fun getComponentPackage(): String = when {
        majorVersion < 17 -> getNMSPackageName() + "IChatBaseComponent"
        else -> "net.minecraft.network.chat.Component"
    }

    fun getPacketClass(): String = when {
        majorVersion < 17 -> getNMSPackageName() + "Packet"
        else -> "net.minecraft.network.protocol.Packet"
    }

    fun getConnectionFieldName(): String = when {
        majorVersion < 17 -> "playerConnection"
        else -> "connection"
    }

    fun getSendPacketMethodName(): String = when {
        majorVersion < 17 -> "sendPacket"
        else -> "send"
    }

    fun isLegacyVersion(): Boolean = majorVersion < 17
}