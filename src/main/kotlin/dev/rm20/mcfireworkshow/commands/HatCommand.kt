package dev.rm20.mcfireworkshow.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.tree.LiteralCommandNode
import dev.fruxz.stacked.text
import dev.rm20.mcfireworkshow.PREFIX
import dev.rm20.mcfireworkshow.interfaces.BrigCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

class HatCommand: BrigCommand {
    override fun register(): LiteralCommandNode<CommandSourceStack> {

        return Commands.literal("hat")
            .executes { ctx ->
                Hat(ctx.source.sender)
                Command.SINGLE_SUCCESS
            }
            .build()
    }

    private fun Hat(sender: CommandSender)
    {
        val player: Player = sender as Player
        val item: ItemStack = player.getItemInHand()
        if (item.type === Material.AIR) {
            sender.sendMessage(text("$PREFIX Cant equip air"))
            return
        }


        val inventory: PlayerInventory = player.getInventory()
        val helmet: ItemStack? = inventory.helmet
        val hat = item.clone()
        hat.amount = 1

        if (helmet != null)
        {
            if (helmet.type !== Material.AIR) {
                if (inventory.firstEmpty() != -1) {
                    inventory.addItem(helmet)
                } else {
                    player.getWorld().dropItemNaturally(player.getLocation(), helmet)
                    sender.sendMessage(text("$PREFIX Dropped helmet, inv is full"))
                }
            }
        }


        inventory.helmet = hat
        removeExact(inventory, hat)
        sender.sendMessage(text("$PREFIX Hat equipped!"))
    }

    private fun removeExact(inventory: PlayerInventory, hat: ItemStack) {
        val contents = inventory.contents
        for (i in contents.indices) {
            val item = contents[i]
            if (item != null && item.isSimilar(hat)) { // Use isSimilar to compare item data
                if (item.amount > 1) {
                    item.amount = item.amount - 1
                } else {
                    inventory.setItem(i, null)
                }
                break
            }
        }
    }
}
