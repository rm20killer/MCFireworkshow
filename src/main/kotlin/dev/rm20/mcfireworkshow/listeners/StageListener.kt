package dev.rm20.mcfireworkshow.listeners

import dev.fruxz.stacked.text
import dev.rm20.mcfireworkshow.PREFIX
import dev.rm20.mcfireworkshow.commands.StageEditCommand
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerInteractEvent
import java.util.*

class StageListener : Listener {

    // Tracks players who are currently typing a name or group in chat
    private val namingMode = mutableMapOf<UUID, org.bukkit.Location>()
    private val groupMode = mutableMapOf<UUID, org.bukkit.Location>()

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player
        val item = event.item ?: return
        if (item.type != Material.BREEZE_ROD) return

        val block = event.clickedBlock ?: return
        val blockLoc = block.location.add(0.5, 1.0, 0.5)

        // 1. SHIFT + RIGHT CLICK: Custom Name
        if (event.action == Action.RIGHT_CLICK_BLOCK && player.isSneaking) {
            event.isCancelled = true
            namingMode[player.uniqueId] = blockLoc
            player.sendMessage(text("$PREFIX <yellow>Type the name for this EffectPoint in chat:</yellow>"))
            return
        }

        // 2. LEFT CLICK: Edit Groups
        if (event.action == Action.LEFT_CLICK_BLOCK) {
            event.isCancelled = true
            groupMode[player.uniqueId] = blockLoc
            player.sendMessage(text("$PREFIX <aqua>Type the groups (space separated) for this point in chat:</aqua>"))
            return
        }

        // Normal Right Click (Non-sneaking) still uses default logic
        if (event.action == Action.RIGHT_CLICK_BLOCK) {
            event.isCancelled = true
            StageEditCommand.addEffectPoint(player, "Point_${block.x}_${block.z}", listOf("Water"), blockLoc)
        }
    }

    @EventHandler
    fun onChat(event: AsyncPlayerChatEvent) {
        val player = event.player
        val uuid = player.uniqueId

        // Handle Custom Naming
        if (namingMode.containsKey(uuid)) {
            event.isCancelled = true
            val location = namingMode.remove(uuid)!!
            val inputName = event.message

            // We use a default group for now, or you can prompt for both
            StageEditCommand.addEffectPoint(player, inputName, listOf("Water"), location)
            return
        }

        // Handle Group Editing
        if (groupMode.containsKey(uuid)) {
            event.isCancelled = true
            val location = groupMode.remove(uuid)!!
            val groups = event.message.split(" ").filter { it.isNotBlank() }

            // Uses a generic name but sets your custom groups
            StageEditCommand.addEffectPoint(player, "Point_${location.blockX}_${location.blockZ}", groups, location)
            player.sendMessage(text("$PREFIX Groups set to: <white>$groups</white>"))
            return
        }
    }
}