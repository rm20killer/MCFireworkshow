package dev.rm20.mcfireworkshow.show.Actions.particle_effects

import ParticleEffectData
import com.destroystokyo.paper.ParticleBuilder
import dev.rm20.mcfireworkshow.MCFireworkShow
import dev.rm20.mcfireworkshow.helpers.ParticleHelper
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.random.Random


class FountainEffect : IParticleEffect<ParticleEffectData.FountainData> {

    private val world = Bukkit.getWorld("world")
    private val plugin = MCFireworkShow.showManager.getFireworkShowPlugin()

    override fun execute(data: ParticleEffectData.FountainData) {
        val trailParticleInfo = ParticleHelper.getParticleInfoFromString(data.particleType) ?: return
        val itemMaterial = Material.matchMaterial(data.material.uppercase()) ?: Material.STONE
        val fountainLocation = Location(world, data.location.x, data.location.y, data.location.z)
        val baseVelocity = Vector(data.velocity.x, data.velocity.y, data.velocity.z)

        object : BukkitRunnable() {
            var tickCounter = 0
            val activeItems = mutableListOf<Item>()

            override fun run() {
                // Phase 1: Spawn new items for the duration of the effect
                if (tickCounter < data.duration) {
                    repeat(data.amount) {
                        val item = spawnFountainItem(fountainLocation, itemMaterial, data.itemModel)

                        // Apply randomized velocity
                        if (data.randomizer != 0.0) {
                            val randomX = (Random.nextDouble() * 2 - 1) * data.randomizer
                            val randomY = (Random.nextDouble() * 2 - 1) * data.randomizer
                            val randomZ = (Random.nextDouble() * 2 - 1) * data.randomizer
                            item.velocity = baseVelocity.clone().add(Vector(randomX, randomY, randomZ))
                        } else {
                            item.velocity = baseVelocity
                        }

                        activeItems.add(item)

                        // Schedule the item's removal after its lifetime expires
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, {
                            if (item.isValid) {
                                item.remove()
                            }
                        }, data.lifetime.toLong())
                    }
                }

                // Phase 2: Apply particle trails to all active items
                activeItems.removeIf { !it.isValid } // Clean up removed items
                for (item in activeItems) {
                    ParticleBuilder(trailParticleInfo.getParticle())
                        .location(item.location)
                        .count(1)
                        .extra(0.0)
                        .force(true)
                        .data(trailParticleInfo.data)
                        .spawn()
                }

                // Phase 3: Check if the effect is finished
                if (tickCounter >= data.duration && activeItems.isEmpty()) {
                    this.cancel()
                }

                tickCounter++
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }

    private fun spawnFountainItem(location: Location, material: Material, customModelData: String?): Item {
        val item = location.world!!.spawnEntity(location, EntityType.ITEM) as Item
        item.pickupDelay = Integer.MAX_VALUE
        item.isPersistent = false
        // Tag the item to identify it and prevent merging
        item.persistentDataContainer.set(
            NamespacedKey(plugin, "mcfs-fountain-item"),
            PersistentDataType.BOOLEAN, true
        )

        val itemStack = ItemStack(material)
        item.itemStack = itemStack
        return item
    }
}