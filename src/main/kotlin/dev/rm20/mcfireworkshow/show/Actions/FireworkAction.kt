package dev.rm20.mcfireworkshow.show.Actions

import ActionData
import org.bukkit.*
import org.bukkit.entity.Firework
import org.bukkit.util.Vector

/**
 * Handles the spawning and detonation of fireworks.
 */
class FireworkAction {

    /**
     * Spawns a firework with the specified properties and detonates it.
     * @param fireworkData The data containing the firework's location, NBT data, and optional forces.
     */
    fun spawnFirework(fireworkData: ActionData.FireworksData) {
        // Get the world from the provided location

        val world: World? = Bukkit.getWorld("world")
        val location = Location(
            world,
            fireworkData.location.x,
            fireworkData.location.y,
            fireworkData.location.z
        )


        // Create the Firework entity
        val firework = world?.spawn(location, Firework::class.java) ?: return

        // Get the FireworkMeta
        val meta = firework.fireworkMeta
        meta.power = 0;
        // Create and add explosions from fireworkData
        for (explosionData in fireworkData.nbt.Fireworks.Explosions) {
            val type = FireworkEffect.Type.valueOf(explosionData.Type)
            val explosion = FireworkEffect.builder()
                .withColor(explosionData.Colors.map { Color.fromRGB(it) })
                .with(type)
                .flicker(explosionData.flicker == 1)
                .trail(explosionData.trail == 1)
                .build()
            meta.addEffect(explosion)
        }

        // Apply the meta to the firework
        firework.fireworkMeta = meta

        //apply force
        val forceVector = Vector(
            fireworkData.forces?.x ?: 0.0,
            fireworkData.forces?.y ?: 0.0,
            fireworkData.forces?.z ?: 0.0
        )
        val i = fireworkData.forces?.i ?: 1.0 // Default to 1.0 if i is null

        firework.velocity = Vector(
            forceVector.x * i,
            forceVector.y * i,
            forceVector.z * i
        )
        firework.velocity = forceVector
        // Detonate the firework immediately
        firework.detonate()

    }
}