package dev.rm20.mcfireworkshow.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.tree.LiteralCommandNode
import dev.fruxz.stacked.text
import dev.rm20.mcfireworkshow.interfaces.BrigCommand
import dev.rm20.mcfireworkshow.show.Effects.GuardianBeamEffect
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands

class TestParticleCommand : BrigCommand  {
    override fun register(): LiteralCommandNode<CommandSourceStack> {

        return Commands.literal("FS-Test")
            .executes { ctx ->
                ctx.source.sender.sendMessage(text("Testing particles"))
//                val font = Font("Consolas", Font.PLAIN, 32)
//                val particle = Particle.FLAME
//                val textToDisplay = "Hello!"
//                val location = ctx.source.location
//                TextEffects.displayParticleText(particle, location, textToDisplay, font)
                val location = ctx.source.location
                val endLocation = location.clone().add(10.0, 10.0, 10.0)
                GuardianBeamEffect.displayBeam(
                    location,
                    endLocation,
                    100, // Duration in ticks
                    10,
                    false
                )
                Command.SINGLE_SUCCESS
            }
            .build()
    }
}

