package dev.rm20.mcfireworkshow.show.camera

import CameraMovement
import Location
import Rotation
import dev.rm20.mcfireworkshow.MCFireworkShow
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.*
import org.bukkit.Location as BukkitLocation

class CameraPlaybackTask(private val movements: List<CameraMovement>) : BukkitRunnable() {

    private var currentTick = 0
    private val totalDuration = movements.lastOrNull()?.tick ?: 0
    private val plugin = MCFireworkShow.showManager.getFireworkShowPlugin()
    private val playerOriginalStates = mutableMapOf<UUID, Pair<BukkitLocation, GameMode>>()

    fun start() {
        if (movements.isEmpty()) {
            plugin.logger.warning("No camera movements to play back.")
            return
        }
        // Store original state and put players in spectator mode
        Bukkit.getOnlinePlayers().forEach { player ->
            playerOriginalStates[player.uniqueId] = Pair(player.location, player.gameMode)
            player.gameMode = GameMode.SPECTATOR
            player.sendMessage("§aCamera playback started. You are now in spectator mode.")
            //hide all players so they can't see each other during playback
            Bukkit.getOnlinePlayers().forEach { otherPlayer ->
                if (otherPlayer != player) {
                    player.hidePlayer(plugin, otherPlayer)
                }
            }
        }
        plugin.logger.info("Starting camera playback for ${movements.size} movements over $totalDuration ticks.")
        this.runTaskTimer(plugin, 0L, 1L)
    }

    override fun run() {
        if (currentTick > totalDuration) {
            finish()
            return
        }

        // Find the index of the next keyframe
        val nextPointIndex = movements.indexOfFirst { it.tick >= currentTick }
        val world = Bukkit.getWorlds().first() // Assuming one world
        val bukkitLocation: BukkitLocation = when {
            // Case 1: We are at the very beginning of the path (before the second keyframe).
            // Teleport to the first point without interpolation.
            nextPointIndex == 0 -> {
                val firstPoint = movements.first()
                BukkitLocation(
                    world,
                    firstPoint.location.x,
                    firstPoint.location.y,
                    firstPoint.location.z,
                    firstPoint.rotation.yaw.toFloat(),
                    firstPoint.rotation.pitch.toFloat()
                )
            }
            // Case 2: Between two keyframes. Perform interpolation.
            nextPointIndex > 0 -> {
                val prevPoint = movements[nextPointIndex - 1]
                val nextPoint = movements[nextPointIndex]

                val segmentDuration = nextPoint.tick - prevPoint.tick
                val timeIntoSegment = currentTick - prevPoint.tick
                val progress = if (segmentDuration > 0) timeIntoSegment.toDouble() / segmentDuration.toDouble() else 1.0

                // Determine which interpolation method to use based on the previous point's setting
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
                        // For Catmull-Rom, we need 4 points: P0, P1, P2, P3
                        val p1 = prevPoint // Start of the current segment
                        val p2 = nextPoint // End of the current segment

                        // Get P0, handling the edge case where we are at the first segment
                        val p0 = if (nextPointIndex - 2 >= 0) movements[nextPointIndex - 2] else p1
                        // Get P3, handling the edge case where we are at the last segment
                        val p3 = if (nextPointIndex + 1 < movements.size) movements[nextPointIndex + 1] else p2

                        // Convert custom Locations to Bukkit Vectors for the catmullRom function
                        val v0 = Vector(p0.location.x, p0.location.y, p0.location.z)
                        val v1 = Vector(p1.location.x, p1.location.y, p1.location.z)
                        val v2 = Vector(p2.location.x, p2.location.y, p2.location.z)
                        val v3 = Vector(p3.location.x, p3.location.y, p3.location.z)

                        location = Interpolation.catmullRom(v0, v1, v2, v3, progress)
                        // Catmull-Rom for rotation is complex, so we fall back to easeInOut
                        rotation = Interpolation.easeInOutRotation(prevPoint, nextPoint, progress)
                    }
                    else -> { // "linear" and any other unknown types
                        location = Interpolation.linear(prevPoint, nextPoint, progress)
                        rotation = Interpolation.linearRotation(prevPoint, nextPoint, progress)
                    }
                }

                BukkitLocation(world, location.x, location.y, location.z, rotation.yaw.toFloat(), rotation.pitch.toFloat())
            }
            // Case 3: No future keyframe found (nextPointIndex is -1), we are past the end.
            else -> {
                finish()
                return
            }
        }

        // Teleport all spectators to the calculated location
        Bukkit.getOnlinePlayers().forEach { player ->
            if (player.gameMode == GameMode.SPECTATOR) {
                player.teleport(bukkitLocation)
            }
        }

        currentTick++
    }

    fun finish() {
        plugin.logger.info("Camera playback finished after $currentTick ticks.")
        // Restore players to their original state
        playerOriginalStates.forEach { (uuid, state) ->
            val player = Bukkit.getPlayer(uuid)
            player?.teleport(state.first)
            player?.gameMode = state.second
            player?.sendMessage("§aCamera playback finished. You are now back in your original game mode.")
            // Show all players again
            Bukkit.getOnlinePlayers().forEach { otherPlayer ->
                if (otherPlayer != player) {
                    player?.showPlayer(plugin, otherPlayer)
                }
            }
        }
        this.cancel()
    }
}
