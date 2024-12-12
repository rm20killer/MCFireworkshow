package dev.rm20.mcfireworkshow.interfaces

import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack

interface BrigCommand {

    fun register(): LiteralCommandNode<CommandSourceStack>

    val aliases: List<String>
        get() = listOf()

    val description: String
        get() = ""

}