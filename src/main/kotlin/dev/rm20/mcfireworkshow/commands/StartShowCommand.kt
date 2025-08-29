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
import org.bukkit.entity.Player

class StartShowCommand : BrigCommand {

    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("start-show")
            .requires { it.sender is Player }
            .then(
                Commands.argument("showName", StringArgumentType.word())
                .then(
                    Commands.argument("path", StringArgumentType.word())
                        .suggests { _, builder ->
                            builder.suggest("true")
                            builder.suggest("false")
                            builder.buildFuture()
                        }.executes(this::executeStartShow))
                )
            .build()
    }

    private fun executeStartShow(ctx: CommandContext<CommandSourceStack>): Int {
        val showName = StringArgumentType.getString(ctx, "showName")
        // Get the path argument and convert it to a boolean
        // If the path argument is not provided, default to true
        val path = StringArgumentType.getString(ctx, "path").toBoolean()
        val sender = ctx.source.sender
        sender.sendMessage(text("$PREFIX Starting Show: $showName"))
        MCFireworkShow.showManager.startShow(showName, sender, path)
        return Command.SINGLE_SUCCESS
    }


}
