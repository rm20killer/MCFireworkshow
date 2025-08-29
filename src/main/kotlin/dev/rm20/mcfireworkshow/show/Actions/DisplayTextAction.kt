package dev.rm20.mcfireworkshow.show.Actions

import ActionData
import dev.rm20.mcfireworkshow.MCFireworkShow
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
import org.bukkit.scheduler.BukkitRunnable
import org.joml.Vector3f


class DisplayTextAction {

    private val plugin = MCFireworkShow.showManager.getFireworkShowPlugin()
    fun handleDisplayText(data: ActionData.DisplayTextData) {
        val world = Bukkit.getWorld("world") ?: return

        object : BukkitRunnable() {
            private var textDisplay: TextDisplay? = null
            private var ticksElapsed = 0
            private val totalDuration = data.delay + (data.moveTime ?: 0) + data.duration

            override fun run() {
                if (ticksElapsed >= totalDuration) {
                    textDisplay?.remove()
                    cancel()
                    return
                }

                if (ticksElapsed >= data.delay) {
                    // Spawn or update the TextDisplay
                    if (textDisplay == null) {
                        textDisplay = world.spawn(
                            Location(world, data.location.x, data.location.y, data.location.z),
                            TextDisplay::class.java
                        ) { display ->
                            // Set initial properties
                            display.text(Component.text(data.text))
                            display.billboard = Display.Billboard.valueOf(data.billboard)
                            display.alignment = TextDisplay.TextAlignment.valueOf(data.alignment)
                            display.isShadowed = data.shadowed
                            display.isSeeThrough = data.seeThrough
                            display.isDefaultBackground = data.defaultBackground
                            display.lineWidth = data.lineWidth

                            // Set background color
                            val bgColor = data.backgroundColor
                            if (bgColor == null)
                            {
                                display.backgroundColor = Color.fromARGB(0, 255, 255, 255)
                            }
                            if (bgColor.length == 9 && bgColor.startsWith("#")) {
                                val r = bgColor.substring(1, 3).toInt(16)
                                val g = bgColor.substring(3, 5).toInt(16)
                                val b = bgColor.substring(5, 7).toInt(16)
                                val a = bgColor.substring(7, 9).toInt(16)
                                display.backgroundColor = Color.fromARGB(a, r, g, b)
                            }

                            // Set text opacity
                            if (data.textOpacity in 0..255) {
                                display.textOpacity = data.textOpacity
                            }
                        }
                    }

                    // Handle animation
                    val moveTime = data.moveTime
                    if (moveTime != null && moveTime > 0 && ticksElapsed < data.delay + moveTime) {
                        val progress = (ticksElapsed - data.delay).toDouble() / (moveTime - 1)

                        val startLoc = data.location
                        val endLoc = data.endLocation ?: startLoc
                        val currentX = lerp(startLoc.x, endLoc.x, progress)
                        val currentY = lerp(startLoc.y, endLoc.y, progress)
                        val currentZ = lerp(startLoc.z, endLoc.z, progress)

                        val startRot = data.rotation
                        val endRot = data.endRotation ?: startRot
                        val currentYaw = lerp(startRot.yaw, endRot.yaw, progress).toFloat()
                        val currentPitch = lerp(startRot.pitch, endRot.pitch, progress).toFloat()
                        val currentRoll = lerp(startRot.roll, endRot.roll, progress).toFloat()

                        textDisplay?.teleport(Location(world, currentX, currentY, currentZ))
                        textDisplay?.interpolationDuration = 1
                        textDisplay?.interpolationDelay = 0
                        textDisplay?.transformation = textDisplay?.transformation?.apply {
                            scale.set(Vector3f(data.size.toFloat(), data.size.toFloat(), data.size.toFloat()))
                            leftRotation.set(
                                org.joml.Quaternionf().rotationYXZ(
                                    Math.toRadians(currentYaw.toDouble()).toFloat(),
                                    Math.toRadians(currentPitch.toDouble()).toFloat(),
                                    Math.toRadians(currentRoll.toDouble()).toFloat()
                                )
                            )
                        }!!
                    } else {
                        // Keep at end state
                        val finalLoc = data.endLocation ?: data.location
                        val finalRot = data.endRotation ?: data.rotation
                        textDisplay?.teleport(Location(world, finalLoc.x, finalLoc.y, finalLoc.z))
                        textDisplay?.transformation = textDisplay?.transformation?.apply {
                            scale.set(Vector3f(data.size.toFloat(), data.size.toFloat(), data.size.toFloat()))
                            leftRotation.set(
                                org.joml.Quaternionf().rotationYXZ(
                                    Math.toRadians(finalRot.yaw).toFloat(),
                                    Math.toRadians(finalRot.pitch).toFloat(),
                                    Math.toRadians(finalRot.roll).toFloat()
                                )
                            )
                        }!!
                    }
                }
                ticksElapsed++
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }

    private fun lerp(start: Double, end: Double, progress: Double): Double {
        return start + (end - start) * progress
    }
}
