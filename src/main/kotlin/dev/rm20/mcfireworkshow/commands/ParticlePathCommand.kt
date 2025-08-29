package dev.rm20.mcfireworkshow.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import dev.rm20.mcfireworkshow.MCFireworkShow
import dev.rm20.mcfireworkshow.interfaces.BrigCommand
import dev.rm20.mcfireworkshow.show.particles.ParticlePathManager
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

class ParticlePathCommand : BrigCommand {
    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("pathtool")
            .requires { it.sender is Player }
            .then(
                Commands.literal("create")
                    .then(
                        Commands.argument("showName", StringArgumentType.word())
                            .then(
                                Commands.argument("pathName", StringArgumentType.greedyString())
                                    .executes(this::executeCreate)
                            )
                    )
            )
            .then(
                Commands.literal("edit")
                    .then(
                        Commands.argument("showName", StringArgumentType.word())
                            .then(
                                Commands.argument("pathName", StringArgumentType.greedyString())
                                    .executes(this::executeEdit)
                            )
                    )
            )
            .then(
                Commands.literal("addpoint")
                    .then(
                        Commands.argument("tick", IntegerArgumentType.integer(0))
                            .executes { ctx -> executeAddPoint(ctx, 0.0f, null) }
                            .then(
                                Commands.argument("height", FloatArgumentType.floatArg())
                                    .executes { ctx -> executeAddPoint(ctx, FloatArgumentType.getFloat(ctx, "height"), null) }
                                    .then(
                                        Commands.argument("particleOverride", StringArgumentType.greedyString())
                                            .executes { ctx -> executeAddPoint(ctx, FloatArgumentType.getFloat(ctx, "height"), StringArgumentType.getString(ctx, "particleOverride")) }
                                    )
                            )
                    )
            )
            .then(
                Commands.literal("gui")
                    .executes(this::executeGui)
            )
            .then(
                Commands.literal("save")
                    .executes(this::executeSave)
            )
            .then(
                Commands.literal("discard")
                    .executes(this::executeDiscard)
            )
            .build()
    }

    private fun executeCreate(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        val showName = StringArgumentType.getString(ctx, "showName")
        val pathName = StringArgumentType.getString(ctx, "pathName")
        ParticlePathManager.createPath(player, showName, pathName)
        return Command.SINGLE_SUCCESS
    }

    private fun executeEdit(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        val showName = StringArgumentType.getString(ctx, "showName")
        val pathName = StringArgumentType.getString(ctx, "pathName")
        ParticlePathManager.loadPathForEditing(player, showName, pathName, MCFireworkShow.instance.dataFolder)
        return Command.SINGLE_SUCCESS
    }

    private fun executeAddPoint(ctx: CommandContext<CommandSourceStack>, height: Float, particleOverride: String?): Int {
        val player = ctx.source.sender as Player
        val tick = IntegerArgumentType.getInteger(ctx, "tick")
        ParticlePathManager.addPoint(player, tick, height.toDouble(), particleOverride)
        return Command.SINGLE_SUCCESS
    }

    private fun executeGui(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        ParticlePathManager.openPathGui(player)
        return Command.SINGLE_SUCCESS
    }

    private fun executeSave(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        ParticlePathManager.savePath(player, MCFireworkShow.instance.dataFolder)
        return Command.SINGLE_SUCCESS
    }

    private fun executeDiscard(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        ParticlePathManager.discardPath(player)
        return Command.SINGLE_SUCCESS
    }
}
