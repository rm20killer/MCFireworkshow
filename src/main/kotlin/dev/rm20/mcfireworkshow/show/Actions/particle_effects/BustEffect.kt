package dev.rm20.mcfireworkshow.show.Actions.particle_effects

import ParticleEffectData
import com.destroystokyo.paper.ParticleBuilder
import dev.rm20.mcfireworkshow.helpers.ParticleHelper
import dev.rm20.mcfireworkshow.show.Actions.particle_effects.IParticleEffect
import org.bukkit.Bukkit
import org.bukkit.Location

class BustEffect : IParticleEffect<ParticleEffectData.BustData> {
    private val world = Bukkit.getWorld("world")

    override fun execute(data: ParticleEffectData.BustData) {
        val particleInfo = ParticleHelper.getParticleInfoFromString(data.particleType) ?: return
        val center = Location(world, data.location.x, data.location.y, data.location.z)
        ParticleBuilder(particleInfo.getParticle())
            .location(center)
            .count(data.amount)
            .offset(data.radius * data.scale.x, data.radius * data.scale.y, data.radius * data.scale.z)
            .receivers(256)
            .force(true)
            .data(particleInfo.data)
            .spawn()
    }
}
