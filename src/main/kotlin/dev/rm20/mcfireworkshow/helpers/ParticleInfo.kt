package dev.rm20.mcfireworkshow.helpers
import org.bukkit.Particle;
import org.jetbrains.annotations.Nullable;
class ParticleInfo(
    private val particle: Particle,
    @get:Nullable @Nullable val data: Any?
) {

    fun getParticle(): Particle {
        return particle
    }
}