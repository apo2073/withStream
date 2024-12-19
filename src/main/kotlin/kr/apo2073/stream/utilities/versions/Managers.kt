package kr.apo2073.stream.utilities.versions

import kr.apo2073.stream.Stream
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.title.Title
import org.bukkit.entity.Player
import java.time.Duration

object Managers {
    val prefix = MiniMessage.miniMessage().deserialize("<b><gradient:#AEAEAE:#5FE2C5>[ withStream ]</gradient></b> ")
    /*
        Component.text("§x§A§E§A§E§A§E§l[ §x§A§2§B§6§B§2§lw§x§9§C§B§A§B§3§li§x§9§6§B§E§B§5§lt§x§9§0§C§2§B§7§lh§x§8§A§C§6§B§9§lS§x§8§3§C§A§B§A§lt§x§7§D§C§E§B§C§lr§x§7§7§D§2§B§E§le§x§7§1§D§6§C§0§la§x§6§B§D§A§C§1§lm §x§5§F§E§2§C§5§l]§r§l ")
*/

    fun showTitle(mainTitle: String, subTitle: String, player: Player) {
        try {
            player.showTitle(
                Title.title(
                    Component.text(mainTitle),
                    Component.text(subTitle),
                    Title.Times.times(Duration.ZERO, Duration.ofSeconds(5), Duration.ZERO)
                )
            )
        } catch (e: NoSuchMethodError) {
            try {
                val craftPlayerClass = Class.forName(NMSVersion.getPackageName() + "entity.CraftPlayer")
                val componentClass = Class.forName(NMSVersion.getComponentPackage())

                val craftPlayer = craftPlayerClass.cast(player)
                val getHandle = craftPlayerClass.getMethod("getHandle")
                val playerHandle = getHandle.invoke(craftPlayer)
                val playerConnection = playerHandle.javaClass.getDeclaredField("playerConnection")
                playerConnection.isAccessible = true
                val connection = playerConnection.get(playerHandle)

                val chatSerializerClass = Class.forName(NMSVersion.getComponentPackage() + "\$ChatSerializer")
                val titleComponent = chatSerializerClass.getMethod("a", String::class.java)
                    .invoke(null, "{\"text\":\"$mainTitle\"}")
                val subtitleComponent = chatSerializerClass.getMethod("a", String::class.java)
                    .invoke(null, "{\"text\":\"$subTitle\"}")

                val titlePacketClass = Class.forName(NMSVersion.getNMSPackageName() + "PacketPlayOutTitle")
                val enumTitleActionClass =
                    Class.forName(NMSVersion.getNMSPackageName() + "PacketPlayOutTitle\$EnumTitleAction")

                val titleConstructor = titlePacketClass.getConstructor(
                    enumTitleActionClass,
                    componentClass,
                    Integer.TYPE, Integer.TYPE, Integer.TYPE
                )

                val titleAction = enumTitleActionClass.getField("TITLE").get(null)
                val subtitleAction = enumTitleActionClass.getField("SUBTITLE").get(null)

                val titlePacket = titleConstructor.newInstance(titleAction, titleComponent, 0, 70, 20)
                val subtitlePacket = titleConstructor.newInstance(subtitleAction, subtitleComponent, 0, 70, 20)

                val sendPacket =
                    connection.javaClass.getMethod("sendPacket", Class.forName(NMSVersion.getPacketClass()))

                if (Stream.instance.config.getBoolean("donation.view-title")) {
                    sendPacket.invoke(connection, titlePacket)
                    sendPacket.invoke(connection, subtitlePacket)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendMessage(text: Component, player: Player) {
        try {
            player.sendMessage(text)
        } catch (e: NoSuchMethodError) {
            try {
                val craftPlayerClass = Class.forName(NMSVersion.getPackageName() + "entity.CraftPlayer")

                val craftPlayer = craftPlayerClass.cast(player)
                val getHandle = craftPlayerClass.getMethod("getHandle")
                val playerHandle = getHandle.invoke(craftPlayer)
                val playerConnection = playerHandle.javaClass.getDeclaredField("playerConnection")
                playerConnection.isAccessible = true
                val connection = playerConnection.get(playerHandle)

                val chatSerializerClass = Class.forName(NMSVersion.getComponentPackage() + "\$ChatSerializer")
                val plainText = PlainTextComponentSerializer.plainText().serialize(text)
                val messageComponent = chatSerializerClass.getMethod("a", String::class.java)
                    .invoke(null, "{\"text\":\"${plainText}\"}")

                val chatPacketClass = Class.forName(NMSVersion.getNMSPackageName() + "PacketPlayOutChat")
                val chatMessageTypeClass = Class.forName(NMSVersion.getNMSPackageName() + "ChatMessageType")
                val chatPacket = chatPacketClass.getConstructor(
                    Class.forName(NMSVersion.getNMSPackageName() + "IChatBaseComponent"),
                    chatMessageTypeClass,
                    java.util.UUID::class.java
                ).newInstance(
                    messageComponent,
                    chatMessageTypeClass.getField("SYSTEM").get(null),
                    java.util.UUID.randomUUID()
                )

                val sendPacket = connection.javaClass.getMethod(
                    "sendPacket",
                    Class.forName(NMSVersion.getNMSPackageName() + "Packet")
                )
                sendPacket.invoke(connection, chatPacket)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private const val LOGO = """
        
            .__  __  .__      _________ __                                 
    __  _  _|__|/  |_|  |__  /   _____//  |________   ____ _____    _____  
    \ \/ \/ /  \   __\  |  \ \_____  \\   __\_  __ \_/ __ \\__  \  /     \ 
     \     /|  ||  | |   Y  \/        \|  |  |  | \/\  ___/ / __ \|  Y Y  \
      \/\_/ |__||__| |___|  /_______  /|__|  |__|    \___  >____  /__|_|  /
                          \/        \/                   \/     \/      \/
    """

    private val VERSION_INFO = """
            Version: ${Stream.instance.getVersion()}
            Author: apo2073
    """

    fun printLogo() {
        try {
            Stream.instance.server.consoleSender.sendMessage(
                Component.text(LOGO).append(
                    Component.text(VERSION_INFO, NamedTextColor.GREEN)
                )
            )
        } catch (e: NoSuchMethodError) {
            Stream.instance.logger.info(
                """
            $LOGO
            §a$VERSION_INFO
        """.trimIndent()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun Player.performCommandAsOP(command: String) {
        val iisOP = this.isOp
        this.isOp = true
        this.performCommand(command)
        this.isOp = iisOP
    }
}