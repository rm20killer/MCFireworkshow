package dev.rm20.mcfireworkshow.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import dev.fruxz.stacked.text
import dev.rm20.mcfireworkshow.MCFireworkShow
import dev.rm20.mcfireworkshow.PREFIX
import dev.rm20.mcfireworkshow.interfaces.BrigCommand
import dev.rm20.mcfireworkshow.show.camera.CameraPathManager
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

class CameraCommand : BrigCommand {
    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("cameratool")
            .requires { it.sender is Player }
            .then(
                Commands.literal("create")
                    .then(
                        Commands.argument("showName", StringArgumentType.greedyString())
                            .executes(this::executeCreate)
                    )
            )
            .then(
                Commands.literal("edit")
                    .then(
                        Commands.argument("showName", StringArgumentType.greedyString())
                            .executes(this::executeEdit)
                    )
            )
            .then(
                Commands.literal("tp")
                    .then(
                        Commands.argument("tick", IntegerArgumentType.integer(0))
                            .executes(this::executeTeleportToTick)
                    )
            )
            .then(
                Commands.literal("addpoint")
                .then(
                    Commands.argument("tick", StringArgumentType.word())
                    .then(
                        Commands.argument("interpolation", StringArgumentType.word())
                        .suggests { _, builder ->
                            builder.suggest("linear")
                            builder.suggest("easeIn")
                            builder.suggest("easeOut")
                            builder.suggest("easeInOut")
                            builder.suggest("catmullRom")
                            builder.buildFuture()
                        }
                        .executes(this::executeAddPoint)
                    )
                )
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

    private fun executeTeleportToTick(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        val tick = IntegerArgumentType.getInteger(ctx, "tick")
        CameraPathManager.teleportToTick(player, tick)
        return Command.SINGLE_SUCCESS
    }

    private fun executeCreate(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        val showName = StringArgumentType.getString(ctx, "showName")
        CameraPathManager.createPath(player, showName)
        player.sendMessage(text("$PREFIX Started creating a camera path for show: '$showName'. Use /cameratool addpoint to add keyframes."))
        return Command.SINGLE_SUCCESS
    }

    private fun executeEdit(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        val showName = StringArgumentType.getString(ctx, "showName")
        CameraPathManager.loadPathForEditing(player, showName, MCFireworkShow.instance.dataFolder)
        return Command.SINGLE_SUCCESS
    }

    private fun executeAddPoint(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        val tickDuration = StringArgumentType.getString(ctx, "tick").toIntOrNull()
        val interpolation = StringArgumentType.getString(ctx, "interpolation")

        if (tickDuration == null || tickDuration <= -1) {
            player.sendMessage(text("$PREFIX Invalid tick. It must be a positive number."))
            return 0
        }

        CameraPathManager.addPoint(player, tickDuration, interpolation)
        return Command.SINGLE_SUCCESS
    }

    private fun executeSave(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        val success = CameraPathManager.savePath(player, MCFireworkShow.showManager.getFireworkShowPlugin().dataFolder)
        if (success) {
            player.sendMessage(text("$PREFIX Camera path saved successfully!"))
        }
        return Command.SINGLE_SUCCESS
    }

    private fun executeDiscard(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        CameraPathManager.discardPath(player)
//        player.sendMessage(text("$PREFIX Discarded current camera path."))
        return Command.SINGLE_SUCCESS
    }
}
