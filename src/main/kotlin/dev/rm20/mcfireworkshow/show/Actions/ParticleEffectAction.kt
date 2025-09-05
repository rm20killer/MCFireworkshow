package dev.rm20.mcfireworkshow.show.Actions

import ActionData
import ParticleEffectData
import dev.rm20.mcfireworkshow.show.Actions.particle_effects.AreaEffect
import dev.rm20.mcfireworkshow.show.Actions.particle_effects.BustEffect
import dev.rm20.mcfireworkshow.show.Actions.particle_effects.*

class ParticleEffectAction {
    fun handleParticleEffect(data: ActionData.ParticleEffectActionData) {
        when (val effect = data.effect) {
            is ParticleEffectData.SpawnData -> SpawnEffect().execute(effect)
            is ParticleEffectData.BustData -> BustEffect().execute(effect)
            is ParticleEffectData.AreaData -> AreaEffect().execute(effect)
            is ParticleEffectData.LaserData -> LaserEffect().execute(effect)
            is ParticleEffectData.FountainData -> FountainEffect().execute(effect)
        }

    }
}