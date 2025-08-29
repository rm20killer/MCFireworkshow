package dev.rm20.mcfireworkshow.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import dev.fruxz.stacked.text
import dev.rm20.mcfireworkshow.MCFireworkShow
import dev.rm20.mcfireworkshow.PREFIX
import dev.rm20.mcfireworkshow.interfaces.BrigCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands

class StageCommand: BrigCommand {
    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("stage-display")
            .executes(this::executeClearStage)
            .then(Commands.argument("showName", StringArgumentType.word())
                .executes(this::executeDisplayStage))
            .build()
    }
    private fun executeDisplayStage(ctx: CommandContext<CommandSourceStack>): Int {
        val showName = StringArgumentType.getString(ctx, "showName")
        val sender = ctx.source.sender

        sender.sendMessage(text("$PREFIX Displaying Stage for Show: $showName"))
        MCFireworkShow.showManager.displayStage(showName, sender)
        return Command.SINGLE_SUCCESS
    }

    private fun executeClearStage(ctx: CommandContext<CommandSourceStack>): Int {
        val sender = ctx.source.sender
        sender.sendMessage(text("$PREFIX Clearing Stage"))
        MCFireworkShow.showManager.ClearDisplay(sender)
        return Command.SINGLE_SUCCESS
    }
}