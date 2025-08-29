package dev.rm20.mcfireworkshow.show.Actions

import ActionData
import dev.rm20.mcfireworkshow.MCFireworkShow
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.max

class ModelAction {

    fun spawnModel(modelData: ActionData.ModelData) {
        val world = Bukkit.getWorld("world") ?: return
        val plugin = MCFireworkShow.showManager.getFireworkShowPlugin()

        // Spawn the ItemDisplay entity at the starting location
        val itemDisplay = world.spawnEntity(
            Location(world, modelData.location.x, modelData.location.y, modelData.location.z),
            EntityType.ITEM_DISPLAY
        ) as ItemDisplay

        // Configure the item with its custom model data
        val material = Material.getMaterial(modelData.material.uppercase()) ?: Material.STONE
        val itemStack = ItemStack(material)
        val itemMeta = itemStack.itemMeta
        if (modelData.itemModel != null) {
            try {
                itemMeta?.itemModel = NamespacedKey.fromString(modelData.itemModel.lowercase())
            } catch (e: IllegalArgumentException) {
                plugin.logger.warning("Invalid NamespacedKey format for item model: \"${modelData.itemModel}\". It should be like 'namespace:key'.")
            }
        }
        itemStack.itemMeta = itemMeta
        itemDisplay.setItemStack(itemStack)

        // Set default display properties (you can make these configurable later)
        itemDisplay.shadowStrength = 0f
        itemDisplay.shadowRadius = 0f
        itemDisplay.isGlowing = modelData.glowing // Example: set to true if you want an emissive effect

        // --- Animation Logic ---
        val totalDuration = modelData.duration
        val delay = max(0, modelData.delay)
        val moveTime = modelData.moveTime?.let { max(0, it) } ?: (totalDuration - delay)

        // Define start state vectors
        val startTranslation =
            Vector3f(modelData.location.x.toFloat(), modelData.location.y.toFloat(), modelData.location.z.toFloat())
        val startScale = Vector3f(modelData.scale.x.toFloat(), modelData.scale.y.toFloat(), modelData.scale.z.toFloat())
        val startRotation = Quaternionf().rotationYXZ(
            Math.toRadians(modelData.rotation.yaw).toFloat(),
            Math.toRadians(modelData.rotation.pitch).toFloat(),
            Math.toRadians(modelData.rotation.roll).toFloat()
        )

        // Define end state vectors (if they exist)
        val endTranslation = modelData.endLocation?.let { Vector3f(it.x.toFloat(), it.y.toFloat(), it.z.toFloat()) }
        val endScale = modelData.endScale?.let { Vector3f(it.x.toFloat(), it.y.toFloat(), it.z.toFloat()) }
        val endRotation = modelData.endRotation?.let {
            Quaternionf().rotationYXZ(
                Math.toRadians(it.yaw).toFloat(), // Yaw
                Math.toRadians(it.pitch).toFloat(), // Pitch
                Math.toRadians(it.roll).toFloat()  // Roll
            )
        }

        object : BukkitRunnable() {
            var ticksElapsed = 0

            override fun run() {
                // When the total duration is over, remove the entity and stop the task
                if (ticksElapsed >= totalDuration) {
                    itemDisplay.remove()
                    this.cancel()
                    return
                }

                val currentTranslation: Vector3f
                val currentScale: Vector3f
                val currentRotation: Quaternionf

                val hasMove = endTranslation != null || endScale != null || endRotation != null

                when {
                    // Phase 1: Delaying, or no movement is defined. Hold at start state.
                    ticksElapsed < delay || !hasMove -> {
                        currentTranslation = startTranslation
                        currentScale = startScale
                        currentRotation = startRotation
                    }
                    // Phase 2: Moving. Interpolate between start and end states.
                    ticksElapsed < delay + moveTime -> {
                        val moveProgress = if (moveTime > 1) (ticksElapsed - delay).toFloat() / (moveTime - 1) else 1f

                        currentTranslation = endTranslation?.let { startTranslation.lerp(it, moveProgress, Vector3f()) }
                            ?: startTranslation
                        currentScale = endScale?.let { startScale.lerp(it, moveProgress, Vector3f()) } ?: startScale
                        currentRotation =
                            endRotation?.let { startRotation.slerp(it, moveProgress, Quaternionf()) } ?: startRotation
                    }
                    // Phase 3: Finished moving. Hold at end state.
                    else -> {
                        currentTranslation = endTranslation ?: startTranslation
                        currentScale = endScale ?: startScale
                        currentRotation = endRotation ?: startRotation
                    }
                }

                // Build the transformation matrix from the calculated vectors
                val transformationMatrix = Matrix4f()
                    .translate(currentTranslation)
                    .rotate(currentRotation)
                    .scale(currentScale)

                // Apply the final transformation to the entity
                itemDisplay.setTransformationMatrix(transformationMatrix)

                ticksElapsed++
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }
}
