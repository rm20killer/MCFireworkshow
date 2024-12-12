package dev.rm20.mcfireworkshow.commands

import dev.rm20.mcfireworkshow.PREFIX
import dev.rm20.mcfireworkshow.interfaces.BrigCommand
import com.mojang.brigadier.Command
import com.mojang.brigadier.tree.LiteralCommandNode
import dev.fruxz.stacked.text
import dev.rm20.mcfireworkshow.MCFireworkShow
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands



class ReloadCommand : BrigCommand  {
    override fun register(): LiteralCommandNode<CommandSourceStack> {

        return Commands.literal("FS-reload")
            .executes { ctx ->
                ctx.source.sender.sendMessage(text("$PREFIX Reloading!"))
                MCFireworkShow.reload()
                Command.SINGLE_SUCCESS
            }
            .build()
    }
}

