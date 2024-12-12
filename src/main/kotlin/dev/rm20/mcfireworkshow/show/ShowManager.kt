package dev.rm20.mcfireworkshow.show


import Action
import ActionData
import ActionDeserializer
import Show
import com.google.gson.GsonBuilder
import dev.fruxz.stacked.text
import dev.rm20.mcfireworkshow.BLOCK_PREFIX
import dev.rm20.mcfireworkshow.MCFireworkShow
import dev.rm20.mcfireworkshow.PREFIX
import dev.rm20.mcfireworkshow.show.Actions.CommandAction
import dev.rm20.mcfireworkshow.show.Actions.FireworkAction
import dev.rm20.mcfireworkshow.show.Actions.MusicAction
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.scheduler.BukkitRunnable
import java.io.File



class ShowManager(private val fireworkPlugin: MCFireworkShow) {
    fun startShow(showName: String, sender: CommandSender) {
        // 1. Load the show file (e.g., "shows/Grand Finale.yml")
        val showFile = File(fireworkPlugin.dataFolder, "shows/$showName.json")
        if (!showFile.exists()) {
            Bukkit.getLogger().warning("Show file not found: ${showFile.path}")
            sender.sendMessage(text("$PREFIX Show file not found: ${showFile.path}"))
            return
        }

        sender.sendMessage(text("$PREFIX loading: ${showFile.path}"))
        // 2. Parse the file (use a library like SnakeYAML)
        val jsonString = showFile.readText()
        val gson = GsonBuilder()
            .registerTypeAdapter(Action::class.java, ActionDeserializer()) // Register for Action
            .create()
        val fireworkShow = gson.fromJson(jsonString, Show::class.java)


        Bukkit.getLogger().warning("loading: ${showFile.path}")
        Bukkit.getLogger().warning("$fireworkShow")

        //3. Extract show data
        val frames = fireworkShow.frames
        val biggestFrameSize = frames.keys.maxOfOrNull { it.toIntOrNull() ?: 0 }
        if (biggestFrameSize == null) {
            Bukkit.getLogger().info("Error with JSON")
            sender.sendMessage(text("$PREFIX Error with JSON"))
            return
        }

        sender.sendMessage(text(BLOCK_PREFIX))
        sender.sendMessage(text("<color:#4a628f>>></color> <color:#b2c2d4>Name:  <white>${fireworkShow.showName}"))
        sender.sendMessage(text("<color:#4a628f>>></color> <color:#b2c2d4>frames Loaded: <white>${frames.size}"))
        sender.sendMessage(text("<color:#4a628f>>></color> <color:#b2c2d4>Total Action: <white>${getTotalActions(fireworkShow)}"))
        sender.sendMessage(text("<color:#4a628f>>></color> <color:#b2c2d4>Total time: <white>$biggestFrameSize <color:#b2c2d4>ticks"))

//        Bukkit.getLogger().info("frame: $frames")
        var musicAction = MusicAction()
        musicAction.playMusic(fireworkShow.music)
        object : BukkitRunnable() {
            var currentFrame = 0
            override fun run() {
//                Bukkit.getLogger().info("frame: $currentFrame")
                if (currentFrame > biggestFrameSize) {
                    musicAction.stopMusic(fireworkShow.music.musicID)
                    cancel()
                    return
                }
                val frame = frames[currentFrame.toString()]
                if (frame != null) {
                    playFrame(frame.actions)
                }
                currentFrame++
            }
        }.runTaskTimer(fireworkPlugin, 0L, 1L)
    }

    private fun playFrame(actions: List<Action>) {

        Bukkit.getLogger().info("frame to RUN: $actions")
        actions.forEach { action ->
            when (action.data) {
                is ActionData.CommandData -> {
                    val commandAction = CommandAction()
                    val command = action.data.command
                    commandAction.runCommand(command)
                    Bukkit.getLogger().info("running $command")
                }
                is ActionData.FireworksData -> {
                    val fireworkAction = FireworkAction()
                    val firework = action.data
                    fireworkAction.spawnFirework(firework)
                }

                else -> {
                    return
                }
            }
        }

    }
    fun getTotalActions(show: Show): Int {
        return show.frames.values.sumOf { it.actions.size }
    }



}
