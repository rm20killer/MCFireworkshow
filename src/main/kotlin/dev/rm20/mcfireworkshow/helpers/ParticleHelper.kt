package dev.rm20.mcfireworkshow.helpers
import dev.rm20.mcfireworkshow.extensions.getLogger
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Particle.DustTransition
import org.bukkit.inventory.ItemStack
import java.util.*

object ParticleHelper {
    /**
     * Parses a string to create a ParticleInfo object, which holds a particle and its required data.
     * This now supports multiple particle types with special data requirements.
     *
     * @param input The string to parse, e.g., "DUST 255 0 0 1.5" or "ITEM DIAMOND".
     * @return A ParticleInfo object, or null if the format is invalid.
     */
    fun getParticleInfoFromString(input: String?): ParticleInfo? {
        if (input.isNullOrBlank()) {
            return null
        }

        val parts = input.trim().split(" +".toRegex()).toTypedArray()
        val particleName = parts[0].uppercase(Locale.getDefault())

        try {
            val particle = Particle.valueOf(particleName)

            // Use a 'when' statement to handle different particle types that require data
            return when (particle) {
                Particle.DUST -> {
                    // Format: DUST <r> <g> <b> [size]
                    if (parts.size < 4) {
                        getLogger().error("Invalid DUST format. Expected: DUST <r> <g> <b> [size], got: $input")
                        return null
                    }
                    val r = parts[1].toInt()
                    val g = parts[2].toInt()
                    val b = parts[3].toInt()
                    val size = if (parts.size > 4) parts[4].toFloat() else 1.0f
                    ParticleInfo(particle, DustOptions(Color.fromRGB(r, g, b), size))
                }

                Particle.DUST_COLOR_TRANSITION -> {
                    // Format: DUST_COLOR_TRANSITION <r1> <g1> <b1> <r2> <g2> <b2> [size]
                    if (parts.size < 7) {
                        getLogger().error("Invalid DUST_COLOR_TRANSITION format. Expected: DUST_COLOR_TRANSITION <r1> <g1> <b1> <r2> <g2> <b2> [size], got: $input")
                        return null
                    }
                    val fromColor = Color.fromRGB(parts[1].toInt(), parts[2].toInt(), parts[3].toInt())
                    val toColor = Color.fromRGB(parts[4].toInt(), parts[5].toInt(), parts[6].toInt())
                    val size = if (parts.size > 7) parts[7].toFloat() else 1.0f
                    ParticleInfo(particle, DustTransition(fromColor, toColor, size))
                }

                Particle.ITEM -> {
                    // Format: ITEM <material>
                    if (parts.size < 2) {
                        getLogger().error("Invalid ITEM format. Expected: ITEM <material>, got: $input")
                        return null
                    }
                    val material = Material.matchMaterial(parts[1]) ?: run {
                        getLogger().error("Invalid material for ITEM particle: ${parts[1]}")
                        return null
                    }
                    ParticleInfo(particle, ItemStack(material))
                }

                Particle.BLOCK, Particle.FALLING_DUST -> {
                    // Format: BLOCK <material> or FALLING_DUST <material>
                    if (parts.size < 2) {
                        getLogger().error("Invalid ${particle.name} format. Expected: ${particle.name} <material>, got: $input")
                        return null
                    }
                    val material = Material.matchMaterial(parts[1]) ?: run {
                        getLogger().error("Invalid material for ${particle.name} particle: ${parts[1]}")
                        return null
                    }
                    if (!material.isBlock) {
                        getLogger().error("Material for ${particle.name} particle must be a block: ${parts[1]}")
                        return null
                    }
                    ParticleInfo(particle, Bukkit.createBlockData(material))
                }

                Particle.SHRIEK -> {
                    // Format: SHRIEK [delay]
                    val delay = if (parts.size > 1) parts[1].toInt() else 0
                    ParticleInfo(particle, delay)
                }

                else -> {
                    // Default case for particles with no special data
                    if (particle.dataType != Void::class.java) {
                        getLogger().warn("Particle '$particleName' requires data, but is not handled by ParticleHelper. It may not display correctly.")
                    }
                    ParticleInfo(particle, null)
                }
            }
        } catch (e: IllegalArgumentException) {
            // Catches invalid particle names or number format errors
            getLogger().error("Invalid particle format for '$input': ${e.message}")
            return null
        }
    }
}