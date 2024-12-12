package dev.rm20.mcfireworkshow.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.tree.LiteralCommandNode
import dev.fruxz.stacked.text
import dev.rm20.mcfireworkshow.PREFIX
import dev.rm20.mcfireworkshow.interfaces.BrigCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands

class TemplateCommand : BrigCommand {

    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("template")
            .executes { ctx ->
                ctx.source.sender.sendMessage(text("$PREFIX Hello World!"))
                Command.SINGLE_SUCCESS
            }
            .build()
    }

}