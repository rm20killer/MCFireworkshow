package dev.rm20.mcfireworkshow.show.Actions

import ActionData
import ParticleEffectData
import com.destroystokyo.paper.ParticleBuilder
import dev.rm20.mcfireworkshow.MCFireworkShow
import dev.rm20.mcfireworkshow.helpers.ParticleHelper
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class ParticleEffectAction {
    private val world = Bukkit.getWorld("world")
    val plugin = MCFireworkShow.showManager.getFireworkShowPlugin()
    fun handleParticleEffect(data: ActionData.ParticleEffectActionData) {
        when (val effect = data.effect) {
            is ParticleEffectData.BustData -> handleBust(effect)
            is ParticleEffectData.AreaData -> handleArea(effect)
            is ParticleEffectData.LaserData -> handleLaser(effect)
            is ParticleEffectData.SpawnData -> handleSpawn(effect)
            else -> {
                plugin.logger.warning("Unknown particle effect type: ${effect::class.java.simpleName}")
            }
        }
    }
    private fun handleSpawn(data: ParticleEffectData.SpawnData) {
        val particleInfo = ParticleHelper.getParticleInfoFromString(data.particleType) ?: return
        val center = Location(world, data.location.x, data.location.y, data.location.z)
        ParticleBuilder(particleInfo.getParticle())
            .location(center)
            .count(data.amount)
            .offset(data.delta.x,data.delta.y,data.delta.z)
            .receivers(256)
            .force(true)
            .extra(data.speed)
            .data(particleInfo.data)
            .spawn()
    }
    private fun handleBust(data: ParticleEffectData.BustData) {
        val particleInfo = ParticleHelper.getParticleInfoFromString(data.particleType) ?: return
        val center = Location(world, data.location.x, data.location.y, data.location.z)
        ParticleBuilder(particleInfo.getParticle())
            .location(center)
            .count(data.amount)
            .offset(data.radius * data.scale.x, data.radius* data.scale.y, data.radius* data.scale.z)
            .receivers(256)
            .force(true)
            .data(particleInfo.data)
            .spawn()
    }


    private fun handleArea(data: ParticleEffectData.AreaData) {
        val particleInfo = ParticleHelper.getParticleInfoFromString(data.particleType) ?: return

        val minX = min(data.pos1.x, data.pos2.x)
        val minY = min(data.pos1.y, data.pos2.y)
        val minZ = min(data.pos1.z, data.pos2.z)
        val maxX = max(data.pos1.x, data.pos2.x)
        val maxY = max(data.pos1.y, data.pos2.y)
        val maxZ = max(data.pos1.z, data.pos2.z)

        val volume = (maxX - minX) * (maxY - minY) * (maxZ - minZ)
        val particleCount = (volume * data.density).toInt()
        val particle = particleInfo.getParticle()
        for (i in 0 until particleCount) {
            val x = Random.nextDouble(minX, maxX)
            val y = Random.nextDouble(minY, maxY)
            val z = Random.nextDouble(minZ, minZ)
            ParticleBuilder(particle)
                .location(Location(world, x, y, z))
                .count(1) // Spawn one particle at a time
                .receivers(256)
                .data(particleInfo.data)
                .force(true)

        }
    }

    private fun handleLaser(data: ParticleEffectData.LaserData) {
        val plugin = MCFireworkShow.showManager.getFireworkShowPlugin()
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

    private fun handleFountain(data: ParticleEffectData.FountainData)
    private fun leap(start: Double, end: Double, progress: Double): Double {
        return start + (end - start) * progress
    }
}