package dev.rm20.mcfireworkshow.show.Actions


import Music
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.SoundStop
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import org.bukkit.Bukkit

/**
 * Handles music-related actions, such as playing and stopping music.
 */
class MusicAction {


    /**
     * Plays music for all online players.
     * @param music The music object containing the music ID, name, and author.
     */
    fun playMusic(music: Music) {
        // Create a Sound object
        val sound: Sound = Sound.sound(
            Key.key(music.musicID),
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

    /**
     * Stops music for all online players.
     * @param musicName The name of the music to stop.
     */
    fun stopMusic(musicName: String) {
        val stopSound: SoundStop = SoundStop.named(Key.key(musicName))
        Bukkit.getOnlinePlayers().forEach { player ->
            player.stopSound(stopSound);
        }
    }
}