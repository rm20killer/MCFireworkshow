package dev.rm20.mcfireworkshow.show.Actions

import ActionData
import dev.rm20.mcfireworkshow.MCFireworkShow
import dev.rm20.mcfireworkshow.helpers.ParticleHelper
import dev.rm20.mcfireworkshow.helpers.ParticleInfo
import dev.rm20.mcfireworkshow.show.Effects.TextEffects
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.scheduler.BukkitRunnable
import java.awt.Font
import kotlin.math.max

/**
 * Handles the displaying of particle text, with support for movement, rotation, delay, and move time.
 */
class TextAction {
    fun displayText(textData: ActionData.ParticleTextData) {
        val font = Font("Consolas", Font.PLAIN, 32)
        val textToDisplay = textData.text
        if (textData.size <= 0) {
            Bukkit.getLogger().warning("Text size is zero or negative, cannot display text.")
            return
        }

        val particleInfo: ParticleInfo = ParticleHelper.getParticleInfoFromString(textData.particleType)
            ?: throw IllegalArgumentException("Invalid particle: ${textData.particleType}")
        val particle = particleInfo.getParticle()

        val world: World? = Bukkit.getWorld("world")
        val startLocation = Location(world, textData.location.x, textData.location.y, textData.location.z)
        val endLocation = textData.endLocation?.let { Location(world, it.x, it.y, it.z) }

        val plugin = MCFireworkShow.showManager.getFireworkShowPlugin()
        if (!plugin.isEnabled) {
            Bukkit.getLogger().severe("Plugin is not enabled, cannot run text action.")
            return
        }

        // UPDATED: Get delay and calculate effective move time
        val totalDuration = textData.duration
        val delay = max(0, textData.delay)
        // If moveTime isn't specified, it defaults to the remaining duration after the delay.
        val moveTime = textData.moveTime?.let { max(0, it) } ?: (totalDuration - delay)

        if (delay + moveTime > totalDuration) {
            Bukkit.getLogger().warning("Text Action has an invalid timing: delay + moveTime > totalDuration. Animation may be cut short.")
        }

        object : BukkitRunnable() {
            var ticksElapsed = 0

            override fun run() {
                if (ticksElapsed >= totalDuration) {
                    this.cancel()
                    return
                }

                // Determine the current state: delaying, moving, or finished moving
                when {
                    // Phase 1: In delay period. Display at start position.
                    ticksElapsed < delay || endLocation == null -> {
                        TextEffects.displayParticleText(
                            particle, startLocation, textToDisplay, font,
                            textData.size, textData.rotation.yaw, textData.rotation.pitch, textData.rotation.roll
                        )
                    }

                    // Phase 2: In movement period.
                    ticksElapsed < delay + moveTime -> {
                        // Calculate progress of the move itself (0.0 to 1.0)
                        val moveProgress = if (moveTime > 1) (ticksElapsed - delay).toDouble() / (moveTime - 1) else 1.0

                        val currentLocation = interpolateLocation(startLocation, endLocation, moveProgress)
                        val currentYaw = textData.endRotation?.yaw?.let { interpolateValue(textData.rotation.yaw, it, moveProgress) } ?: textData.rotation.yaw
                        val currentPitch = textData.endRotation?.pitch?.let { interpolateValue(textData.rotation.pitch, it, moveProgress) } ?: textData.rotation.pitch
                        val currentRoll = textData.endRotation?.roll?.let { interpolateValue(textData.rotation.roll, it, moveProgress) } ?: textData.rotation.roll

                        TextEffects.displayParticleText(
                            particle, currentLocation, textToDisplay, font,
                            textData.size, currentYaw, currentPitch, currentRoll
                        )
                    }

                    // Phase 3: Movement is finished. Display at end position.
                    else -> {
                        val finalYaw = textData.endRotation?.yaw ?: textData.rotation.yaw
                        val finalPitch = textData.endRotation?.pitch ?: textData.rotation.pitch
                        val finalRoll = textData.endRotation?.roll ?: textData.rotation.roll
                        TextEffects.displayParticleText(
                            particle, endLocation, textToDisplay, font,
                            textData.size, finalYaw, finalPitch, finalRoll
                        )
                    }
                }
                ticksElapsed++
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }

    private fun interpolateLocation(start: Location, end: Location, progress: Double): Location {
        val x = start.x + (end.x - start.x) * progress
        val y = start.y + (end.y - start.y) * progress
        val z = start.z + (end.z - start.z) * progress
        return Location(start.world, x, y, z)
    }

    private fun interpolateValue(start: Double, end: Double, progress: Double): Double {
        return start + (end - start) * progress
    }
}