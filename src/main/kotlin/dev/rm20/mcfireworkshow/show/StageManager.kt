package dev.rm20.mcfireworkshow.show

import Stage
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay

class StageManager {

    companion object {
        // This tag will be used to identify and clean up the display entities.
        const val STAGE_DISPLAY_TAG = "mcfs_stage_display_38312312_tag"
    }

    /**
     * Spawns BlockDisplay entities for every element in the stage.
     * @param stage The Stage object containing the elements to display.
     * @param world The world in which to display the elements.
     */
    fun displayStage(stage: Stage, world: World) {
        // Clear any previously displayed stage elements first
        clearDisplay(world)

        // Display Lights with Sea Lanterns
        stage.lights.forEach { light ->
            val location = Location(world, light.location.x, light.location.y, light.location.z)
            spawnDisplayEntity(location, Material.SEA_LANTERN, light.name, light.groups)
        }

        // Display Lasers with Beacons
        stage.lasers.forEach { laser ->
            val location = Location(world, laser.location.x, laser.location.y, laser.location.z)
            spawnDisplayEntity(location, Material.BEACON, laser.name, laser.groups)
        }

        // Display Effect Points with Note Blocks
        stage.effectPoints.forEach { effectPoint ->
            val location = Location(world, effectPoint.location.x, effectPoint.location.y, effectPoint.location.z)
            spawnDisplayEntity(location, Material.NOTE_BLOCK, effectPoint.name, effectPoint.groups)
        }
    }

    /**
     * Removes all stage element displays from a world.
     * @param world The world to clear the displays from.
     */
    fun clearDisplay(world: World) {
        world.entities.forEach { entity ->
            if (entity.scoreboardTags.contains(STAGE_DISPLAY_TAG)) {
                entity.remove()
            }
        }
    }

    /**
     * Spawns a BlockDisplay and a corresponding TextDisplay for a stage element.
     * @param location The location to spawn the entities.
     * @param material The material the BlockDisplay should show.
     * @param name The name of the stage element.
     * @param groups The list of groups the element belongs to.
     */
    private fun spawnDisplayEntity(location: Location, material: Material, name: String, groups: List<String>?) {
        // Spawn the Block Display
        location.world?.spawn(location, BlockDisplay::class.java) { display ->
            display.block = material.createBlockData()
            val transformation = display.transformation
            transformation.scale.set(0.35f)
            display.transformation = transformation
            display.addScoreboardTag(STAGE_DISPLAY_TAG)
        }

        // Spawn the Text Display for the name and groups, positioned slightly above the block
        val textLocation = location.clone().add(0.0, 0.5, 0.0)
        location.world?.spawn(textLocation, TextDisplay::class.java) { textDisplay ->
            // Safely handle a null or empty list for groups
            val groupText = if (!groups.isNullOrEmpty()) "§e(${groups.joinToString(", ")})" else ""
            textDisplay.text(Component.text("§a$name $groupText"))
            textDisplay.billboard = Display.Billboard.CENTER // Makes the text always face the player
            textDisplay.addScoreboardTag(STAGE_DISPLAY_TAG)
        }
    }
}