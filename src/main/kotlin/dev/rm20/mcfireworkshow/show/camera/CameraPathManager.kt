package dev.rm20.mcfireworkshow.show.camera

import Action
import ActionDeserializer
import CameraMovement
import Location
import Rotation
import Show
import com.destroystokyo.paper.ParticleBuilder
import com.google.gson.GsonBuilder
import dev.fruxz.stacked.text
import dev.rm20.mcfireworkshow.MCFireworkShow
import dev.rm20.mcfireworkshow.PREFIX
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import java.io.File
import java.util.*

object CameraPathManager {

    private val activeSessions = mutableMapOf<UUID, MutableList<CameraMovement>>()
    private val sessionShowNames = mutableMapOf<UUID, String>()
    private val activeVisualizations = mutableMapOf<UUID, BukkitTask>()
    private val plugin = MCFireworkShow.showManager.getFireworkShowPlugin()
    fun createPath(player: Player, showName: String) {
        val playerUUID = player.uniqueId
        sessionShowNames[playerUUID] = showName
        // Start with a point at the player's current location at tick 0
        val initialPoint = CameraMovement(
            location = Location(player.location.x, player.location.y, player.location.z),
            rotation = Rotation(player.location.yaw.toDouble(), player.location.pitch.toDouble(), 0.0),
            tick = 0,
            interpolation = "linear" // First point's interpolation doesn't matter
        )
        activeSessions[playerUUID] = mutableListOf(initialPoint)
        player.sendMessage(text("$PREFIX Created a new camera path for '$showName', starting at your current location."))
    }

    fun addPoint(player: Player, tickDuration: Int, interpolation: String) {
        val playerUUID = player.uniqueId
        val path = activeSessions[playerUUID]
        if (path == null) {
            player.sendMessage(text("$PREFIX You don't have an active camera path session. Use /cameratool create <showName> first."))
            return
        }
        
        val newPoint = CameraMovement(
            location = Location(player.location.x, player.location.y, player.location.z),
            rotation = Rotation(player.location.yaw.toDouble(), player.location.pitch.toDouble(), 0.0),
            tick = tickDuration,
            interpolation = interpolation
        )
        val existingPointIndex = path.indexOfFirst { it.tick == tickDuration }

        if (existingPointIndex != -1) {
            path[existingPointIndex] = newPoint
            player.sendMessage(text("$PREFIX Replaced keyframe at tick $tickDuration. Total points: ${path.size}."))
        } else {
            path.add(newPoint)
            path.sortBy { it.tick }
            player.sendMessage(text("$PREFIX Added a new keyframe at tick $tickDuration. Total points: ${path.size}."))
        }
    }

    fun discardPath(player: Player) {
        stopVisualization(player.uniqueId)
        activeSessions.remove(player.uniqueId)
        sessionShowNames.remove(player.uniqueId)
        player.sendMessage(text("$PREFIX Discarded current camera path."))
    }

    fun savePath(player: Player, dataFolder: File): Boolean {
        val playerUUID = player.uniqueId
        val path = activeSessions[playerUUID]
        val showName = sessionShowNames[playerUUID]

        if (path == null || showName == null) {
            player.sendMessage(text("$PREFIX No active camera path to save."))
            return false
        }

        val showFile = File(dataFolder, "shows/$showName.json")
        if (!showFile.exists()) {
            player.sendMessage(text("$PREFIX Show file '$showName.json' not found."))
            return false
        }
        val jsonString = showFile.readText()
        val gson = GsonBuilder()
            .registerTypeAdapter(Action::class.java, ActionDeserializer()) // Register for Action
            .create()
        val show = gson.fromJson(jsonString, Show::class.java)

        // Create a new Show instance with the updated camera movements
        val updatedShow = show.copy(cameraMovements = path)

        showFile.writeText(gson.toJson(updatedShow))

        // Clean up session
        stopVisualization(playerUUID)
        activeSessions.remove(player.uniqueId)
        sessionShowNames.remove(player.uniqueId)
        player.sendMessage(text("$PREFIX Camera path saved successfully!"))
        return true
    }

    fun toggleVisualization(player: Player) {
        val playerUUID = player.uniqueId
        val existingTask = activeVisualizations.remove(playerUUID)

        if (existingTask != null) {
            existingTask.cancel()
            player.sendMessage(text("$PREFIX Visualization stopped."))
        } else {
            val path = activeSessions[playerUUID] ?: run {
                player.sendMessage(text("$PREFIX No active path to visualize."))
                return
            }
            if (path.size < 2) {
                player.sendMessage(text("$PREFIX You need at least two points to visualize a path."))
                return
            }

            val newTask = object : BukkitRunnable() {
                override fun run() {
                    if (!player.isOnline || !activeSessions.containsKey(playerUUID)) {
                        this.cancel()
                        activeVisualizations.remove(playerUUID)
                        return
                    }
                    drawPath(player, path)
                }
            }.runTaskTimer(plugin, 0L, 20L) // Redraw every second

            activeVisualizations[playerUUID] = newTask
            player.sendMessage(text("$PREFIX Visualization started. It will update as you add points."))
        }
    }

    private fun stopVisualization(playerUUID: UUID) {
        activeVisualizations.remove(playerUUID)?.cancel()
    }

    fun loadPathForEditing(player: Player, showName: String, dataFolder: File) {
        val playerUUID = player.uniqueId
        if (activeSessions.containsKey(playerUUID)) {
            player.sendMessage(text("$PREFIX You already have an active session. Please save or discard it first."))
            return
        }

        val showFile = File(dataFolder, "shows/$showName.json")
        if (!showFile.exists()) {
            player.sendMessage(text("$PREFIX Show file '$showName.json' not found."))
            return
        }

        val jsonString = showFile.readText()
        val gson = GsonBuilder()
            .registerTypeAdapter(Action::class.java, ActionDeserializer()) // Register for Action
            .create()
        val show = gson.fromJson(jsonString, Show::class.java)

        if (show.cameraMovements.isNullOrEmpty()) {
            player.sendMessage(text("$PREFIX Show '$showName' does not have a camera path to edit. Use '/cameratool create' instead."))
            return
        }

        activeSessions[playerUUID] = show.cameraMovements.toMutableList()
        sessionShowNames[playerUUID] = showName
        player.sendMessage(text("$PREFIX Loaded camera path for '$showName' with ${show.cameraMovements.size} points. You can now add new points or save."))
        toggleVisualization(player) // Automatically turn on visualization
    }

    private fun drawPath(player: Player, path: List<CameraMovement>) {
        if (path.size < 2) return

        // Draw each segment
        for (i in 0 until path.size - 1) {
            val startPoint = path[i]
            val endPoint = path[i + 1]
            val duration = endPoint.tick - startPoint.tick
            if (duration <= 0) continue

            val interpolationType = startPoint.interpolation.lowercase().replace(" ", "")
            val pathColor = when (interpolationType) {
                "easein", "easeout", "easeinout" -> Color.YELLOW
                "catmullrom" -> Color.FUCHSIA
                else -> Color.AQUA // Linear
            }

            // Draw a line between the two points using the correct interpolation
            for (t in 0..duration step 2) {
                val interpolatedState = getInterpolatedState(path, startPoint.tick + t) ?: continue
                val location = interpolatedState.first
                ParticleBuilder(Particle.DUST)
                    .location(player.world, location.x, location.y, location.z)
                    .receivers(player)
                    .data(Particle.DustOptions(pathColor, 0.75F))
                    .count(1)
                    .spawn()
            }
        }

        // Draw markers at each keyframe
        path.forEachIndexed { index, point ->
            val color = if (index == 0) Color.GREEN else Color.RED
            val location = point.location
            ParticleBuilder(Particle.FLAME)
                .location(player.world, location.x, location.y, location.z)
                .receivers(player)
                .count(3)
                .extra(0.0)
                .spawn()
        }
    }

    fun teleportToTick(player: Player, tick: Int) {
        val path = activeSessions[player.uniqueId] ?: run {
            player.sendMessage(text("$PREFIX You don't have an active camera path session."))
            return
        }

        val state = getInterpolatedState(path, tick)
        if (state == null) {
            player.sendMessage(text("$PREFIX Cannot calculate position. Path may be too short or tick is out of bounds."))
            return
        }

        val (location, rotation) = state
        val bukkitLocation = org.bukkit.Location(
            player.world,
            location.x,
            location.y,
            location.z,
            rotation.yaw.toFloat(),
            rotation.pitch.toFloat()
        )
        player.teleport(bukkitLocation)
        player.sendMessage(text("$PREFIX Teleported to path position at tick $tick."))
    }

    private fun getInterpolatedState(path: List<CameraMovement>, tick: Int): Pair<Location, Rotation>? {
        if (path.isEmpty()) return null
        if (path.size == 1) return Pair(path.first().location, path.first().rotation)

        val nextPointIndex = path.indexOfFirst { it.tick >= tick }

        return when {
            // Before the first keyframe (or exactly on it)
            nextPointIndex == 0 -> Pair(path.first().location, path.first().rotation)

            // After the last keyframe
            nextPointIndex == -1 -> Pair(path.last().location, path.last().rotation)

            // Between two keyframes
            nextPointIndex > 0 -> {
                val prevPoint = path[nextPointIndex - 1]
                val nextPoint = path[nextPointIndex]

                val segmentDuration = nextPoint.tick - prevPoint.tick
                val timeIntoSegment = tick - prevPoint.tick
                val progress = if (segmentDuration > 0) timeIntoSegment.toDouble() / segmentDuration.toDouble() else 1.0

                val interpolationType = prevPoint.interpolation.lowercase().replace(" ", "")

                val location: Location
                val rotation: Rotation

                when (interpolationType) {
                    "easein" -> {
                        location = Interpolation.easeIn(prevPoint, nextPoint, progress)
                        rotation = Interpolation.easeInRotation(prevPoint, nextPoint, progress)
                    }

                    "easeout" -> {
                        location = Interpolation.easeOut(prevPoint, nextPoint, progress)
                        rotation = Interpolation.easeOutRotation(prevPoint, nextPoint, progress)
                    }

                    "easeinout" -> {
                        location = Interpolation.easeInOut(prevPoint, nextPoint, progress)
                        rotation = Interpolation.easeInOutRotation(prevPoint, nextPoint, progress)
                    }

                    "catmullrom" -> {
                        val p1 = prevPoint
                        val p2 = nextPoint
                        val p0 = if (nextPointIndex - 2 >= 0) path[nextPointIndex - 2] else p1
                        val p3 = if (nextPointIndex + 1 < path.size) path[nextPointIndex + 1] else p2

                        val v0 = Vector(p0.location.x, p0.location.y, p0.location.z)
                        val v1 = Vector(p1.location.x, p1.location.y, p1.location.z)
                        val v2 = Vector(p2.location.x, p2.location.y, p2.location.z)
                        val v3 = Vector(p3.location.x, p3.location.y, p3.location.z)

                        location = Interpolation.catmullRom(v0, v1, v2, v3, progress)
                        rotation = Interpolation.linearRotation(prevPoint, nextPoint, progress)
                    }

                    else -> {
                        location = Interpolation.linear(prevPoint, nextPoint, progress)
                        rotation = Interpolation.linearRotation(prevPoint, nextPoint, progress)
                    }
                }
                Pair(location, rotation)
            }

            else -> null // Should not happen
        }
    }
}
