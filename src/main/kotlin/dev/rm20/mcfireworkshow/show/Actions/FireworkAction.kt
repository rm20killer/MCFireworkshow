package dev.rm20.mcfireworkshow.show.Actions

import ActionData
import org.bukkit.*
import org.bukkit.entity.Firework
import org.bukkit.util.Vector


class FireworkAction {
    fun spawnFirework(fireworkData: ActionData.FireworksData) {
        // Get the world from the provided location

        val world: World? = Bukkit.getWorld("world")
        val location = Location(
            world,
            fireworkData.location.x.toDouble(),
            fireworkData.location.y.toDouble(),
            fireworkData.location.z.toDouble()
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
            fireworkData.forces?.x?.toDouble() ?: 0.0,
            fireworkData.forces?.y?.toDouble() ?: 0.0,
            fireworkData.forces?.z?.toDouble() ?: 0.0
        )
        firework.velocity = forceVector
        firework.detonate()

    }
}