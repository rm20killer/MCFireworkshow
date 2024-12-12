package dev.rm20.mcfireworkshow.commands

import dev.rm20.mcfireworkshow.PREFIX
import dev.rm20.mcfireworkshow.interfaces.BrigCommand
import dev.rm20.mcfireworkshow.show.ShowManager
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import dev.fruxz.stacked.text
import dev.rm20.mcfireworkshow.MCFireworkShow
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import java.io.File
import java.util.*
import java.util.concurrent.CompletableFuture

class StartShowCommand : BrigCommand {

    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("start-show")
            .then(Commands.argument("showName", StringArgumentType.word())
                .executes(this::executeStartShow))
            .build()
    }
    private fun executeStartShow(ctx: CommandContext<CommandSourceStack>): Int {
        val showName = StringArgumentType.getString(ctx, "showName")
        val sender = ctx.source.sender

        sender.sendMessage(text("$PREFIX Starting Show: $showName"))
        MCFireworkShow.showManager.startShow(showName, sender)
        return Command.SINGLE_SUCCESS
    }



}
