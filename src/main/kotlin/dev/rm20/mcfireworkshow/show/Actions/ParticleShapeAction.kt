package dev.rm20.mcfireworkshow.show.Actions

import ActionData
import Shape
import dev.rm20.mcfireworkshow.MCFireworkShow
import dev.rm20.mcfireworkshow.show.Effects.ParticleShapes
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.math.max

class ParticleShapeAction {
    private val world = Bukkit.getWorld("world")
    val plugin = MCFireworkShow.showManager.getFireworkShowPlugin()

    fun handleParticleShape(shapeData: ActionData.ParticleShapeData) {
        val particleTypeString = shapeData.particleType // Get the particle string

        // This task will keep the shape alive for its duration
        object : BukkitRunnable() {
            var ticksElapsed = 0
            var previousProgress = 0.0
            override fun run() {
                if (ticksElapsed >= shapeData.duration) {
                    this.cancel()
                    return
                }

                // Delegate to the correct drawing function based on the shape type
                when (val shape = shapeData.shape) {
                    //Line
                    is Shape.Line -> {
                        val delay = max(0, shape.delay)
                        val animationTime = shape.moveTime?.let { max(0, it) }
                        val pos1 = Location(world, shape.start.x, shape.start.y, shape.start.z)
                        val pos2 = Location(world, shape.end.x, shape.end.y, shape.end.z)
                        // If there's no animation, draw it instantly on the first tick
                        if (animationTime == null) {
                            if (ticksElapsed == 0) {
                                ParticleShapes.drawLine(particleTypeString, pos1, pos2, shapeData.density)
                            }
                        }
                        // Handle animation
                        else {
                            if (ticksElapsed >= delay && ticksElapsed < delay + animationTime) {
                                val progress = if (animationTime > 1) (ticksElapsed - delay).toDouble() / (animationTime - 1) else 1.0
                                val startProgress = if (shape.keepTrail) 0.0 else previousProgress
                                // Pass the particle string directly
                                ParticleShapes.drawAnimatedLine(particleTypeString, pos1, pos2, shapeData.density, startProgress, progress)
                                previousProgress = progress
                            }
                        }
                    }
                    // Arch
                    is Shape.Arch -> {
                        val delay = max(0, shape.delay)
                        val animationTime = shape.moveTime?.let { max(0, it) }
                        val pos1 = Location(world, shape.start.x, shape.start.y, shape.start.z)
                        val pos2 = Location(world, shape.end.x, shape.end.y, shape.end.z)
                        // If there's no animation, draw it instantly on the first tick
                        if (animationTime == null) {
                            if (ticksElapsed == 0) {
                                ParticleShapes.drawArch(particleTypeString, pos1, pos2, shape.height, shapeData.density)
                            }
                        }
                        // Handle animation
                        else {
                            if (ticksElapsed >= delay && ticksElapsed < delay + animationTime) {
                                val progress = if (animationTime > 1) (ticksElapsed - delay).toDouble() / (animationTime - 1) else 1.0
                                val startProgress = if (shape.keepTrail) 0.0 else previousProgress
                                // Pass the particle string directly
                                ParticleShapes.drawAnimatedArch(particleTypeString, pos1, pos2, shape.height, shapeData.density, startProgress, progress)
                                previousProgress = progress
                            }
                        }
                    }
                    is Shape.Circle -> {
                        val delay = max(0, shape.delay)
                        if (ticksElapsed >= delay) {
                            val rotation = Vector(shape.rotation.yaw, shape.rotation.pitch, shape.rotation.roll)
                            val startCentre = Location(world, shape.centre.x, shape.centre.y, shape.centre.z)

                            val endRotation = Vector(
                                shape.endRotation?.yaw ?: shape.rotation.yaw,
                                shape.endRotation?.pitch ?: shape.rotation.pitch,
                                shape.endRotation?.roll ?: shape.rotation.roll
                            )
                            val elapsedTicksAfterDelay = ticksElapsed - delay

                            // Progress for radius/rotation/centre animation
                            val moveTime = shape.moveTime?.let { max(1, it) }
                            val animationProgress = if (moveTime != null && moveTime > 0) {
                                (elapsedTicksAfterDelay.toDouble() / (moveTime - 1)).coerceAtMost(1.0)
                            } else {
                                1.0
                            }

                            val currentCentre = if (shape.endCentre != null && moveTime != null && moveTime > 0) {
                                val endCentreLoc = Location(world, shape.endCentre.x, shape.endCentre.y, shape.endCentre.z)
                                val startVec = startCentre.toVector()
                                val endVec = endCentreLoc.toVector()
                                val direction = endVec.subtract(startVec)
                                if (world != null) {
                                    startVec.add(direction.multiply(animationProgress)).toLocation(world)
                                } else {
                                    startCentre
                                }
                            } else {
                                startCentre
                            }

                            // Progress for circumference drawing animation
                            val drawTime = shape.drawTime?.let { max(1, it) }
                            val currentCircumferenceProgress = if (drawTime != null && drawTime > 0) {
                                (elapsedTicksAfterDelay.toDouble() / (drawTime - 1)).coerceAtMost(1.0)
                            } else {
                                1.0
                            }

                            // Determine the start progress for the circumference based on keepTrail
                            val startCircumferenceProgress = if (shape.keepTrail) 0.0 else previousProgress

                            ParticleShapes.drawAnimatedCircle(
                                particleTypeString,
                                currentCentre,
                                shape.radius,
                                rotation,
                                shape.endRadius,
                                endRotation,
                                shapeData.density,
                                animationProgress,
                                startCircumferenceProgress,
                                currentCircumferenceProgress
                            )

                            // Update previous progress for the next tick
                            previousProgress = currentCircumferenceProgress
                        }
                    }
                    is Shape.Sphere -> {
                        val delay = max(0, shape.delay)
                        if(ticksElapsed >= delay) {
                            val startCentre = Location(world, shape.centre.x, shape.centre.y, shape.centre.z)
                            val moveTime = shape.moveTime?.let { max(1, it) }
                            val progress = if(moveTime != null && moveTime > 0) {
                                ((ticksElapsed - delay).toDouble() / (moveTime - 1)).coerceAtMost(1.0)
                            } else {
                                1.0
                            }
                            val currentCentre = if (shape.endCentre != null && moveTime != null && moveTime > 0) {
                                val endCentreLoc = Location(world, shape.endCentre.x, shape.endCentre.y, shape.endCentre.z)
                                val startVec = startCentre.toVector()
                                val endVec = endCentreLoc.toVector()
                                val direction = endVec.subtract(startVec)
                                if (world != null) {
                                    startVec.add(direction.multiply(progress)).toLocation(world)
                                } else {
                                    startCentre
                                }
                            } else {
                                startCentre
                            }
                            ParticleShapes.drawAnimatedSphere(particleTypeString, currentCentre, shape.radius, shape.endRadius, shapeData.density, progress)
                        }
                    }
                    is Shape.Hemisphere -> {
                        val delay = max(0, shape.delay)
                        if(ticksElapsed >= delay) {
                            val centre = Location(world, shape.centre.x, shape.centre.y, shape.centre.z)
                            val rotation = Vector(shape.rotation.yaw, shape.rotation.pitch, shape.rotation.roll)
                            val moveTime = shape.moveTime?.let { max(1, it) }
                            val progress = if(moveTime != null && moveTime > 0) {
                                ((ticksElapsed - delay).toDouble() / (moveTime - 1)).coerceAtMost(1.0)
                            } else {
                                1.0
                            }
                            ParticleShapes.drawAnimatedHemisphere(particleTypeString, centre, shape.radius, rotation, shape.endRadius, shapeData.density, progress)
                        }
                    }
                    is Shape.Cube -> {
                        val delay = max(0, shape.delay)
                        if(ticksElapsed >= delay) {
                            val startCentre = Location(world, shape.centre.x, shape.centre.y, shape.centre.z)
                            val scale = Vector(shape.scale.x, shape.scale.y, shape.scale.z)
                            val endScale = shape.endScale?.let { Vector(it.x, it.y, it.z) } ?: scale
                            val rotation = Vector(shape.rotation.yaw, shape.rotation.pitch, shape.rotation.roll)
                            val moveTime = shape.moveTime?.let { max(1, it) }
                            val progress = if(moveTime != null && moveTime > 0) {
                                ((ticksElapsed - delay).toDouble() / (moveTime - 1)).coerceAtMost(1.0)
                            } else {
                                1.0
                            }
                            val currentCentre = if (shape.endCentre != null && moveTime != null && moveTime > 0) {
                                val endCentreLoc = Location(world, shape.endCentre.x, shape.endCentre.y, shape.endCentre.z)
                                val startVec = startCentre.toVector()
                                val endVec = endCentreLoc.toVector()
                                val direction = endVec.subtract(startVec)
                                if (world != null) {
                                    startVec.add(direction.multiply(progress)).toLocation(world)
                                } else {
                                    startCentre
                                }
                            } else {
                                startCentre
                            }
                            ParticleShapes.drawAnimatedCube(particleTypeString, currentCentre, scale, rotation, endScale, shapeData.density, progress)
                        }
                    }
                    // Add cases for other shapes here
                    else -> {
                        plugin.logger.warning("Unsupported shape type: ${shape::class.simpleName}")
                    }
                }

                ticksElapsed++
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }
}