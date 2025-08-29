package dev.rm20.mcfireworkshow.show.Actions

import ActionData
import LaserPoint
import dev.rm20.mcfireworkshow.MCFireworkShow
import fr.skytasul.guardianbeam.Laser
import org.bukkit.Bukkit
import org.bukkit.Location

class GuardianLaserAction {
    companion object {
        val activeLasers = mutableMapOf<String, Laser>()
    }

    fun handleLaserAction(laserData: ActionData.GuardianLaserData, lasers: List<LaserPoint>) {
        val plugin = MCFireworkShow.showManager.getFireworkShowPlugin()

        val laserPoints: List<LaserPoint> = when {
            laserData.name != null -> lasers.filter { it.name == laserData.name }
            laserData.group != null -> lasers.filter { it.groups.contains(laserData.group) }
            else -> {
                plugin.logger.warning("Laser action has no target group or name.")
                emptyList()
            }
        }

        if( laserPoints.isEmpty() ) {
            plugin.logger.warning("No laser points found for the specified group or name.")
            return
        }
        for (point in laserPoints) {
            //Turn off laser
            if (laserData.state.equals("off", ignoreCase = true)) {
                activeLasers[point.name]?.let {
                    it.stop()
                    activeLasers.remove(point.name)
                }
                continue
            }

            val startLocation = Location(Bukkit.getWorld("world"), point.location.x, point.location.y, point.location.z)
            val endLocation = Location(startLocation.world, laserData.endPos.x, laserData.endPos.y, laserData.endPos.z)
            if(laserData.relative)
            {
                endLocation.add(startLocation)
            }
            val existingLaser = activeLasers[point.name]

            // If the laser is already active, we will move it to the new end location.
            if (existingLaser != null) {
                // MOVE action.
                val moveDuration = laserData.duration.toInt()
                try {
                    existingLaser.moveEnd(endLocation, moveDuration, null)
                } catch (e: ReflectiveOperationException) {
                    plugin.logger.severe("Failed to move laser '${point.name}': ${e.message}")
                    e.printStackTrace()
                }
            } // If the laser is not active, we will create a new one.
            else {
                try {
                    val newLaser = Laser.GuardianLaser(startLocation, endLocation, -1, laserData.distance)
                    newLaser.start(plugin)
                    activeLasers[point.name] = newLaser
                } catch (e: ReflectiveOperationException) {
                    plugin.logger.severe("Failed to create laser '${point.name}': ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }
}