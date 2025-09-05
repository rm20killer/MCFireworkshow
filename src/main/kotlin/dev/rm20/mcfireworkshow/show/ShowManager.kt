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
import dev.rm20.mcfireworkshow.show.Actions.*
import dev.rm20.mcfireworkshow.show.Effects.ParticlePathPlaybackTask
import dev.rm20.mcfireworkshow.show.camera.CameraPlaybackTask
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.scheduler.BukkitRunnable
import java.io.File


class ShowManager(private val fireworkPlugin: MCFireworkShow) {
    //set up stage manager
    private val stageManager = StageManager()
    private val effectManager = EffectManager(fireworkPlugin, this)
    private var currentShow: Show? = null
    /**
     * Starts a firework show by loading and parsing the show file, and then scheduling the execution of frames.
     * @param showName The name of the show to start.
     * @param sender The command sender who initiated the show.
     */
    fun startShow(showName: String, sender: CommandSender, path: Boolean) {
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
//        Bukkit.getLogger().warning("$fireworkShow")

        //3. Extract show data
        val frames = fireworkShow.frames
        val biggestFrameSize = frames.keys.maxOfOrNull { it.toIntOrNull() ?: 0 }
        if (biggestFrameSize == null) {
            Bukkit.getLogger().info("Error with JSON")
            sender.sendMessage(text("$PREFIX Error with JSON"))
            return
        }

        // Display show information to the sender
        sender.sendMessage(text(BLOCK_PREFIX))
        sender.sendMessage(text("<color:#4a628f>>></color> <color:#b2c2d4>Name:  <white>${fireworkShow.showName}"))
        sender.sendMessage(text("<color:#4a628f>>></color> <color:#b2c2d4>frames Loaded: <white>${frames.size}"))
        sender.sendMessage(text("<color:#4a628f>>></color> <color:#b2c2d4>Total Action: <white>${getTotalActions(fireworkShow)}"))
        sender.sendMessage(text("<color:#4a628f>>></color> <color:#b2c2d4>Total time: <white>$biggestFrameSize <color:#b2c2d4>ticks"))
        sender.sendMessage(text("<color:#4a628f>>></color> <color:#b2c2d4>Total Lasers: <white>${fireworkShow.stage.lasers.size} <color:#b2c2d4>Lasers"))
        currentShow=fireworkShow

        // Start playing music associated with the show
        val musicAction = MusicAction()
        musicAction.playMusic(fireworkShow.music)

        if (!fireworkShow.particlePaths.isNullOrEmpty()) {
            sender.sendMessage(text("<color:#4a628f>>></color> <color:#b2c2d4>Particle Paths: <white>Detected"))
            ParticlePathPlaybackTask(fireworkShow.particlePaths).start()
        } else {
            sender.sendMessage(text("<color:#4a628f>>></color> <color:#b2c2d4>Particle Paths: <gray>Not Found"))
        }
        var camPath: CameraPlaybackTask? = null
        if(path)
        {
            if (!fireworkShow.cameraMovements.isNullOrEmpty()) {
                sender.sendMessage(text("<color:#4a628f>>></color> <color:#b2c2d4>Camera Path: <white>Detected"))
                camPath = CameraPlaybackTask(fireworkShow.cameraMovements)
                camPath.start()
            } else {
                sender.sendMessage(text("<color:#4a628f>>></color> <color:#b2c2d4>Camera Path: <gray>Not Found"))
            }
        }
        else
        {
            sender.sendMessage(text("<color:#4a628f>>></color> <color:#b2c2d4>Camera Path: <gray>Disabled"))
        }


        // Schedule the execution of frames using BukkitRunnable
        object : BukkitRunnable() {
            var currentFrame = 0
            override fun run() {
//                Bukkit.getLogger().info("frame: $currentFrame")
                if (currentFrame > biggestFrameSize) {
                    camPath?.finish()
                    musicAction.stopMusic(fireworkShow.music.musicID)
                    cancel()
                    return
                }
                val frame = frames[currentFrame.toString()]
                if (frame != null) {
                    playFrame(frame.actions, fireworkShow)
                }
                currentFrame++
            }
        }.runTaskTimer(fireworkPlugin, 0L, 1L)
    }

    /**
     * Plays a single frame of the show by executing the actions within the frame.
     * @param actions The list of actions to execute for the frame.
     */
    private fun playFrame(actions: List<Action>, show: Show) {
        Bukkit.getLogger().info("frame to RUN: $actions")
        actions.forEach { action ->
            executeAction(action)
        }
    }
    /**
     * Executes a single action. This logic is now centralized so it can be called
     * from a Show or a saved Effect.
     * @param action The action to execute.
     * @param show The parent show context, which can be null if called from an effect.
     */
    fun executeAction(action: Action) {
        if(currentShow==null)
        {
            return
        }
        when (action.data) {
            is ActionData.PlayEffectData -> {
                effectManager.playEffect(action.data)
            }
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
            is ActionData.LightData -> {
                val lightAction = LightAction()
                val light = action.data
                lightAction.LightController(light)
            }
            is ActionData.GuardianLaserData -> {
                val guardianLaserAction = GuardianLaserAction()
                val laserData = action.data
                guardianLaserAction.handleLaserAction(laserData, currentShow!!.stage.lasers)
            }
            is ActionData.CrystalLaserData -> {
                val crystalLaserAction = CrystalLaserAction()
                val laserData = action.data
                crystalLaserAction.handleLaserAction(laserData, currentShow!!.stage.lasers)
            }
            is ActionData.MusicData -> {
                val music = action.data
                Bukkit.getOnlinePlayers().forEach { player ->
                    val miniMessage = MiniMessage.miniMessage()
                    var parsed= miniMessage.deserialize("<yellow>Now Playing <#4cf005>${music.Name}</#4cf005> by <aqua>${music.Author}</aqua>")
                    player.sendActionBar(parsed)
                }
            }
            is ActionData.EffectMasterData -> {
                val effectMasterAction = EffectMasterAction()
                val effect = action.data
                effectMasterAction.playEffectM(effect)
            }
            is ActionData.ParticleTextData -> {
                val textAction = TextAction()
                val textData = action.data
                textAction.displayText(textData)
            }
            is ActionData.DisplayTextData -> {
                val particleAction = DisplayTextAction()
                val particleData = action.data
                particleAction.handleDisplayText(particleData)
            }
            is ActionData.ModelData -> {
                val modelAction = ModelAction()
                val modelData = action.data
                modelAction.spawnModel(modelData)
            }
            is ActionData.ParticleShapeData -> {
                val particleShapeAction = ParticleShapeAction()
                val shapeData = action.data
                particleShapeAction.handleParticleShape(shapeData)
            }
            is ActionData.ParticleEffectActionData -> {
                val particleEffectAction = ParticleEffectAction()
                val particleData = action.data
                particleEffectAction.handleParticleEffect(particleData)
            }
            is ActionData.PlaceSchematicData -> {
                val schematicAction = SchematicAction()
                schematicAction.place(action.data)
            }
            else -> {
                return
            }
            }
    }
    /**
     * Calculates the total number of actions in a show.
     * @param show The show object.
     * @return The total number of actions in the show.
     */
    fun getTotalActions(show: Show): Int {
        return show.frames.values.sumOf { it.actions.size }
    }

    fun getFireworkShowPlugin(): MCFireworkShow {
        return fireworkPlugin
    }

    fun displayStage(showName: String, sender: CommandSender) {
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
        //3. Extract show data
        val stage = fireworkShow.stage
        if (stage == null) {
            Bukkit.getLogger().info("Error with JSON, no stage found")
            sender.sendMessage(text("$PREFIX Error with JSON, no stage found"))
            return
        }
        // Display show information to the sender
        val world = Bukkit.getWorld("world")
        if (world == null) {
            Bukkit.getLogger().info("World not found")
            sender.sendMessage(text("$PREFIX World not found"))
            return
        }
        stageManager.displayStage(stage, world)

    }

    fun ClearDisplay(sender: CommandSender) {
        val world = Bukkit.getWorld("world")
        if (world == null) {
            Bukkit.getLogger().info("World not found")
            sender.sendMessage(text("$PREFIX World not found"))
            return
        }
        // Clear the stage display
        stageManager.clearDisplay(world)
        sender.sendMessage(text("$PREFIX Stage display cleared."))
    }


}
