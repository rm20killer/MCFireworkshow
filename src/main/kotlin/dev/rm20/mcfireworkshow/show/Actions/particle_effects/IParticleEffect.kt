package dev.rm20.mcfireworkshow.show.Actions.particle_effects

import ParticleEffectData

interface IParticleEffect<T : ParticleEffectData> {
    fun execute(data: T)
}