package dev.rm20.mcfireworkshow.show.particles

import Location
import ParticlePath
import PathPoint
import Show
import com.google.gson.GsonBuilder
import dev.fruxz.stacked.text
import dev.rm20.mcfireworkshow.MCFireworkShow
import dev.rm20.mcfireworkshow.PREFIX
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.File
import java.util.*

object ParticlePathManager {
    private val activeSessions = mutableMapOf<UUID, PathEditorSession>()
    private val plugin = MCFireworkShow.showManager.getFireworkShowPlugin()
    data class PathEditorSession(
        val playerUUID: UUID,
        var showName: String,
        var pathName: String,
        var particlePath: ParticlePath,
        val showFile: File
    )

    fun createPath(player: Player, showName: String, pathName: String) {
        if (activeSessions.containsKey(player.uniqueId)) {
            player.sendMessage(text("$PREFIX You are already editing a path. Use /pathtool save or /pathtool discard first."))
            return
        }
        val showFile = File(plugin.dataFolder, "shows/$showName.json")
        if (!showFile.exists()) {
            player.sendMessage(text("$PREFIX Show file '$showName.json' not found."))
            return
        }

        val newPath = ParticlePath(pathName, "minecraft:end_rod", mutableListOf())
        val session = PathEditorSession(player.uniqueId, showName, pathName, newPath, showFile)
        activeSessions[player.uniqueId] = session
        player.sendMessage(text("$PREFIX Started creating a new particle path '$pathName' for show '$showName'."))
    }

    fun loadPathForEditing(player: Player, showName: String, pathName: String, dataFolder: File) {
        if (activeSessions.containsKey(player.uniqueId)) {
            player.sendMessage(text("$PREFIX You are already editing a path. Use /pathtool save or /pathtool discard first."))
            return
        }

        val showFile = File(dataFolder, "shows/$showName.json")
        if (!showFile.exists()) {
            player.sendMessage(text("$PREFIX Show file not found: ${showFile.path}"))
            return
        }

        val show = loadShow(showFile)
        val path = show.particlePaths?.find { it.name == pathName }
        if (path == null) {
            player.sendMessage(text("$PREFIX Particle path '$pathName' not found in show '$showName'."))
            return
        }

        val session = PathEditorSession(player.uniqueId, showName, pathName, path, showFile)
        activeSessions[player.uniqueId] = session
        player.sendMessage(text("$PREFIX Loaded particle path '$pathName' for editing."))
        openPathGui(player)
    }

    fun addPoint(player: Player, tick: Int, height: Double, particleOverride: String?) {
        val session = activeSessions[player.uniqueId]
        if (session == null) {
            player.sendMessage(text("$PREFIX You are not currently editing a particle path."))
            return
        }
        val location = player.location
        val newPoint = PathPoint(
            Location(location.x, location.y, location.z),
            tick,
            height,
            particleOverride,
            0
        )
        session.particlePath.points = session.particlePath.points + newPoint
        player.sendMessage(text("$PREFIX Added a new point to path '${session.pathName}' at tick $tick."))
        openPathGui(player) // Refresh GUI
    }

    fun openPathGui(player: Player) {
        val session = activeSessions[player.uniqueId]
        if (session == null) {
            player.sendMessage(text("$PREFIX You are not currently editing a particle path."))
            return
        }

        val path = session.particlePath
        val inventory = Bukkit.createInventory(null, 54, Component.text("Path: ${path.name}"))

        path.points.sortedBy { it.tick }
        path.points.forEachIndexed { index, point ->
            if (index < 54) {
                inventory.setItem(index, createPointItemStack(point))
            }
        }
        player.openInventory(inventory)
    }

    private fun createPointItemStack(point: PathPoint): ItemStack {
        val item = ItemStack(Material.ENDER_PEARL)
        val meta = item.itemMeta
        meta.displayName(Component.text("Tick: ${point.tick}", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false))
        val lore = mutableListOf<Component>()
        lore.add(Component.text("Location:", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))
        lore.add(Component.text("  X: ${"%.2f".format(point.location.x)}", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
        lore.add(Component.text("  Y: ${"%.2f".format(point.location.y)}", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
        lore.add(Component.text("  Z: ${"%.2f".format(point.location.z)}", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
        if (point.height != 0.0) {
            lore.add(Component.text("Height: ${point.height}", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false))
        }
        if (point.particleTypeOverride != null) {
            lore.add(Component.text("Particle: ${point.particleTypeOverride}", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false))
        }
        meta.lore(lore)
        item.itemMeta = meta
        return item
    }


    fun savePath(player: Player, dataFolder: File): Boolean {
        val session = activeSessions[player.uniqueId]
        if (session == null) {
            player.sendMessage(text("$PREFIX You are not currently editing a path."))
            return false
        }

        val show = loadShow(session.showFile)
        val paths = show.particlePaths?.toMutableList() ?: mutableListOf()
        val existingPathIndex = paths.indexOfFirst { it.name == session.particlePath.name }

        if (existingPathIndex != -1) {
            paths[existingPathIndex] = session.particlePath
        } else {
            paths.add(session.particlePath)
        }

        val updatedShow = show.copy(particlePaths = paths)
        saveShow(updatedShow, session.showFile)
        activeSessions.remove(player.uniqueId)
        player.sendMessage(text("$PREFIX Particle path '${session.pathName}' saved successfully!"))
        return true
    }

    fun discardPath(player: Player) {
        if (activeSessions.remove(player.uniqueId) != null) {
            player.sendMessage(text("$PREFIX Discarded current particle path edits."))
        } else {
            player.sendMessage(text("$PREFIX You are not currently editing a path."))
        }
    }

    private fun loadShow(file: File): Show {
        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.fromJson(file.readText(), Show::class.java)
    }

    private fun saveShow(show: Show, file: File) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        file.writeText(gson.toJson(show))
    }
}
