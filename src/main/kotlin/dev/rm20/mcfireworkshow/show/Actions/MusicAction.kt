package dev.rm20.mcfireworkshow.show.Actions


import Music
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.SoundStop
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import net.kyori.adventure.title.Title
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.checkerframework.checker.units.qual.C
import java.util.*


class MusicAction {
    fun playMusic(music: Music) {
        // Create a Sound object
        val sound: Sound = Sound.sound(
            Key.key("minecraft", music.musicID),
            Sound.Source.MASTER,
            1.0f,
            1.0f
        )
        val minimessage = MiniMessage.builder()
            .tags(
                TagResolver.builder()
                    .resolver(StandardTags.color())
                    .resolver(StandardTags.decorations())
                    .build()
            )
            .build()
        Bukkit.getOnlinePlayers().forEach { player ->
            val miniMessage = MiniMessage.miniMessage()
            var parsed= miniMessage.deserialize("<yellow>Now Playing <#4cf005>${music.Name}</#4cf005> by <aqua>${music.Author}</aqua>")
            player.sendActionBar(parsed)
            player.playSound(sound);
        }

    }

    fun stopMusic(musicName: String) {
        val stopSound: SoundStop = SoundStop.named(Key.key("minecraft", musicName))
        Bukkit.getOnlinePlayers().forEach { player ->
            player.stopSound(stopSound);
        }
    }
}