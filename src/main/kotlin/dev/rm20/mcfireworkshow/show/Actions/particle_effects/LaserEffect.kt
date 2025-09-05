package dev.rm20.mcfireworkshow.show.Actions.particle_effects

import ParticleEffectData
import com.destroystokyo.paper.ParticleBuilder
import dev.rm20.mcfireworkshow.MCFireworkShow
import dev.rm20.mcfireworkshow.helpers.ParticleHelper
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable

class LaserEffect : IParticleEffect<ParticleEffectData.LaserData> {
    private val world = Bukkit.getWorld("world")
    private val plugin = MCFireworkShow.showManager.getFireworkShowPlugin()

    override fun execute(data: ParticleEffectData.LaserData) {
        val particleInfo = ParticleHelper.getParticleInfoFromString(data.particleType) ?: return

        object : BukkitRunnable() {
            var ticksElapsed = 0
            override fun run() {
                if (ticksElapsed >= data.duration) {
                    this.cancel()
                    return
                }

                val progress = if (data.duration > 1) ticksElapsed.toDouble() / (data.duration - 1) else 1.0
                val currentWidth = leap(data.startWidth, data.endWidth, progress)

                val start = Location(world, data.start.x, data.start.y, data.start.z)
                val end = Location(world, data.end.x, data.end.y, data.end.z)
                val vector = end.toVector().subtract(start.toVector())
                val length = vector.length()
                if (length < 0.1) return // Avoid division by zero
                vector.normalize()

                val step = 1.0 / data.density
                var distance = 0.0
                while (distance < length) {
                    val loc = start.clone().add(vector.clone().multiply(distance))
                    ParticleBuilder(particleInfo.getParticle())
                        .location(loc)
                        .count(5) // Spawn a small cluster to create thickness
                        .offset(currentWidth / 2, currentWidth / 2, currentWidth / 2)
                        .extra(0.0)
                        .receivers(256)
                        .force(true)
                        .data(particleInfo.data)
                        .spawn()
                    distance += step
                }
                ticksElapsed++
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }

    private fun leap(start: Double, end: Double, progress: Double): Double {
        return start + (end - start) * progress
    }
}
