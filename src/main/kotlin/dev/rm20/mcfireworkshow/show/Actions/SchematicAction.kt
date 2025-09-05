package dev.rm20.mcfireworkshow.show.Actions

import ActionData
import SchematicAnimation
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.world.World
import com.sk89q.worldedit.world.block.BlockState
import dev.fruxz.ascend.extension.isNull
import dev.rm20.mcfireworkshow.MCFireworkShow
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import kotlin.io.path.pathString
import kotlin.math.ceil

/**
 * Handles the placement of schematics using the FastAsyncWorldEdit API.
 * Includes support for animating the placement of blocks over a specified duration.
 */
class SchematicAction {
    private val plugin = MCFireworkShow.showManager.getFireworkShowPlugin()
    private val world = Bukkit.getWorld("world")!!

    fun place(data: ActionData.PlaceSchematicData) {
        if (Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") == null) {
            plugin.logger.severe("FastAsyncWorldEdit is not installed! The 'place_schematic' action cannot be executed.")
            return
        }
        val schematicsDir = WorldEdit.getInstance().schematicsFolderPath
        val schematicFile = File(schematicsDir.pathString, "${data.schematicName}.schem")

        if (!schematicFile.exists()) {
            plugin.logger.warning("Schematic '${data.schematicName}.schem' not found in the FAWE schematics folder.")
            return
        }

        try {
            val format = ClipboardFormats.findByFile(schematicFile) ?: run {
                plugin.logger.warning("Could not determine format for schematic '${data.schematicName}'.")
                return
            }
            val clipboard: Clipboard = format.getReader(schematicFile.inputStream()).read()
            val pasteLocation = BlockVector3.at(data.location.x, data.location.y, data.location.z)

            // If no animation is specified, perform an instant paste.
            if (data.animation == null || data.animation.duration <= 1) {
                clipboard.paste(WorldEdit.getInstance().newEditSession(world as World?), pasteLocation, true)
                return
            }

            animatePlacement(clipboard, pasteLocation, data.animation)

        } catch (e: Exception) {
            plugin.logger.severe("An error occurred while placing schematic '${data.schematicName}': ${e.message}")
            e.printStackTrace()
        }
    }

    private fun animatePlacement(clipboard: Clipboard, pasteLocation: BlockVector3, animation: SchematicAnimation) {
        // Get all blocks from the clipboard and convert to a mutable list
        val blocks = mutableListOf<Pair<BlockVector3, BlockState>>()
        clipboard.region.forEach { pos ->
            val block = clipboard.getBlock(pos)
            if (!block.blockType.isNull) {
                blocks.add(pos to block)
            }
        }

        // Sort the list of blocks based on the specified animation mode
        when (animation.mode.lowercase()) {
            "bottom_up" -> blocks.sortBy { it.first.y() }
            "top_down" -> blocks.sortByDescending { it.first.y() }
            "sweep_z" -> blocks.sortWith(compareBy({ it.first.y() }, { it.first.z() }, { it.first.x() }))
            "sweep_x" -> blocks.sortWith(compareBy({ it.first.y() }, { it.first.x() }, { it.first.z() }))
            "random" -> blocks.shuffle()
            // "none" or any other value will result in the default order
        }

        val blocksPerTick = ceil(blocks.size.toDouble() / animation.duration).toInt()
        var placedBlocks = 0

        object : BukkitRunnable() {
            override fun run() {
                if (placedBlocks >= blocks.size) {
                    this.cancel()
                    return
                }

                val editSession = WorldEdit.getInstance().newEditSession(world as World?)
                val blocksToPlaceThisTick = blocks.drop(placedBlocks).take(blocksPerTick)

                for ((relativePos, block) in blocksToPlaceThisTick) {
                    val worldPos = pasteLocation.add(relativePos)
                    editSession.setBlock(worldPos, block)
                }

                editSession.flushQueue()
                placedBlocks += blocksToPlaceThisTick.size
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }
}