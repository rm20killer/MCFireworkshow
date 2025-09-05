package dev.rm20.mcfireworkshow.show.Actions.particle_effects

import ParticleEffectData
import com.destroystokyo.paper.ParticleBuilder
import dev.rm20.mcfireworkshow.helpers.ParticleHelper
import org.bukkit.Bukkit
import org.bukkit.Location

class SpawnEffect : IParticleEffect<ParticleEffectData.SpawnData> {
    private val world = Bukkit.getWorld("world")

    override fun execute(data: ParticleEffectData.SpawnData) {
        val particleInfo = ParticleHelper.getParticleInfoFromString(data.particleType) ?: return
        val center = Location(world, data.location.x, data.location.y, data.location.z)
        ParticleBuilder(particleInfo.getParticle())
            .location(center)
            .count(data.amount)
            .offset(data.delta.x, data.delta.y, data.delta.z)
            .receivers(256)
            .force(true)
            .extra(data.speed)
            .data(particleInfo.data)
            .spawn()
    }
}
