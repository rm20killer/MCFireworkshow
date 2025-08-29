package dev.rm20.mcfireworkshow.show.Effects


import ParticlePath
import dev.rm20.mcfireworkshow.MCFireworkShow
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.math.sign
import org.bukkit.Location as BukkitLocation

/**
 * A task that handles the playback of predefined particle paths during a show.
 * It interpolates between keyframes (PathPoint) to create smooth, continuous particle trails.
 * @param paths A list of all ParticlePath objects defined in the show.
 */
class ParticlePathPlaybackTask(private val paths: List<ParticlePath>) : BukkitRunnable() {
    private var currentTick = 0
    private val world = Bukkit.getWorld("world")
    // Stores the last known position for each path to draw a continuous line from.
    // The key is the path name (String), and the value is the Bukkit Location.
    private val lastPositions = mutableMapOf<String, BukkitLocation>()

    // Pre-process paths by sorting their points by tick for efficient lookup during playback.
    private val sortedPaths = paths.map { path ->
        path.copy(points = path.points.sortedBy { it.tick })
    }

    override fun run() {
        if (world == null) {
            cancel()
            return
        }

        // Process each defined path every tick.
        sortedPaths.forEach { path ->
            val startPoint = path.points.lastOrNull { it.tick <= currentTick }
            val endPoint = path.points.firstOrNull { it.tick > currentTick }

            if (startPoint != null && endPoint != null) {
                val segmentDuration = endPoint.tick - startPoint.tick
                if (segmentDuration <= 0) return@forEach

                val progress = (currentTick - startPoint.tick).toDouble() / segmentDuration

                val startLoc = BukkitLocation(world, startPoint.location.x, startPoint.location.y, startPoint.location.z)
                val endLoc = BukkitLocation(world, endPoint.location.x, endPoint.location.y, endPoint.location.z)
                var curveFactor = 0.0
                val curveMagnitude = abs(endPoint.curved.toDouble())

                if (curveMagnitude > 0) {
                    val endPointIndex = path.points.indexOf(endPoint)
                    val nextPoint = if (endPointIndex != -1 && endPointIndex + 1 < path.points.size) {
                        path.points[endPointIndex + 1]
                    } else {
                        null
                    }
                    if (nextPoint != null) {
                        val nextLoc = BukkitLocation(world, nextPoint.location.x, nextPoint.location.y, nextPoint.location.z)
                        val v1x = endLoc.x - startLoc.x
                        val v1z = endLoc.z - startLoc.z
                        val v2x = nextLoc.x - endLoc.x
                        val v2z = nextLoc.z - endLoc.z
                        val crossProductY = (v1x * v2z) - (v1z * v2x)
                        val turnDirection = sign(crossProductY)
                        curveFactor = turnDirection * curveMagnitude
                    }
                }
                val archHeight = startPoint.height
                val basePosVector: Vector

                if (curveFactor != 0.0) {
                    val p0 = startLoc.toVector()
                    val p2 = endLoc.toVector()

                    val midPoint = p0.clone().midpoint(p2)
                    val direction = p2.clone().subtract(p0).normalize()

                    var up = Vector(0, 1, 0)
                    if (abs(direction.y) > 0.99) {
                        up = Vector(1, 0, 0)
                    }
                    val perpVector = direction.crossProduct(up).normalize()
                    val p1 = midPoint.add(perpVector.multiply(curveFactor))

                    //Bezier formula: B(t) = (1-t)^2*P0 + 2(1-t)t*P1 + t^2*P2
                    val oneMinusProgress = 1.0 - progress
                    val term1 = p0.multiply(oneMinusProgress * oneMinusProgress)
                    val term2 = p1.multiply(2.0 * oneMinusProgress * progress)
                    val term3 = p2.multiply(progress * progress)
                    basePosVector = term1.add(term2).add(term3)
                } else {
                    basePosVector = startLoc.toVector().add(endLoc.toVector().subtract(startLoc.toVector()).multiply(progress))
                }
                val finalPosVector = basePosVector.clone()
                if (archHeight > 0) {
                    val verticalOffset = 4 * archHeight * progress * (1 - progress)
                    finalPosVector.y += verticalOffset
                }

                val currentPos = finalPosVector.toLocation(world)
                val lastPos = lastPositions[path.name]
                if (lastPos != null) {
                    val particleType = startPoint.particleTypeOverride ?: path.particleType
                    ParticleShapes.drawLine(particleType, lastPos, currentPos, 2.0)
                }
                lastPositions[path.name] = currentPos
            } else {
                lastPositions.remove(path.name)
            }
        }

        // Stop the task if we have passed the final keyframe of all paths.
        val maxTick = paths.flatMap { it.points }.maxOfOrNull { it.tick } ?: 0
        if (currentTick > maxTick) {
            cancel()
            return
        }

        currentTick++
    }

    /**
     * Starts the playback task.
     * It initializes the starting positions for any paths that begin at tick 0
     * and schedules the task to run every tick.
     */
    fun start() {
        // Initialize lastPositions for any paths that start at tick 0.
        sortedPaths.forEach { path ->
            path.points.find { it.tick == 0 }?.let { startPoint ->
                lastPositions[path.name] = BukkitLocation(world, startPoint.location.x, startPoint.location.y, startPoint.location.z)
            }
        }
        this.runTaskTimer(MCFireworkShow.showManager.getFireworkShowPlugin(), 0L, 1L)
    }
}