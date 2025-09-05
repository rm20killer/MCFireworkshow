package dev.rm20.mcfireworkshow.show.Actions.particle_effects

import ParticleEffectData
import com.destroystokyo.paper.ParticleBuilder
import dev.rm20.mcfireworkshow.helpers.ParticleHelper
import dev.rm20.mcfireworkshow.show.Actions.particle_effects.IParticleEffect
import org.bukkit.Bukkit
import org.bukkit.Location
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class AreaEffect : IParticleEffect<ParticleEffectData.AreaData> {
    private val world = Bukkit.getWorld("world")

    override fun execute(data: ParticleEffectData.AreaData) {
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
            val z = Random.nextDouble(minZ, maxZ)
            ParticleBuilder(particle)
                .location(Location(world, x, y, z))
                .count(1)
                .receivers(256)
                .data(particleInfo.data)
                .force(true)
                .spawn()
        }
    }
}
