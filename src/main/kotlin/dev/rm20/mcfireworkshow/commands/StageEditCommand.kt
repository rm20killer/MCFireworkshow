package dev.rm20.mcfireworkshow.commands;

import EffectPoint
import LaserPoint
import Light
import Location
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import dev.fruxz.stacked.text
import dev.rm20.mcfireworkshow.PREFIX
import dev.rm20.mcfireworkshow.interfaces.BrigCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.File

class StageEditCommand : BrigCommand {
    // Temporary lists to hold stage components before saving
    companion object {
        // Keeping these in the companion object ensures the Listener and Command share data
        val pendingLights = mutableListOf<Light>()
        val pendingLasers = mutableListOf<LaserPoint>()
        val pendingEffectPoints = mutableListOf<EffectPoint>()

        fun addEffectPoint(player: Player, name: String, groups: List<String>, loc: org.bukkit.Location) {
            val effectPoint = EffectPoint(name, Location(loc.x, loc.y, loc.z), groups)
            pendingEffectPoints.add(effectPoint)
            player.sendMessage(text("$PREFIX Added Effect Point '$name' via Wand."))
        }
    }

    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("stagetool")
            .requires { it.sender is Player }
            .then(
                Commands.literal("tool")
                    .executes(this::executeGiveTool)
            )
            // --- LIGHTS ---
            .then(
                Commands.literal("addlight")
                    .then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("groups", StringArgumentType.greedyString())
                            .executes(this::executeAddLight)))
            )
            // --- LASERS ---
            .then(
                Commands.literal("addlaser")
                    .then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("groups", StringArgumentType.greedyString())
                            .executes(this::executeAddLaser)))
            )
            // --- EFFECT POINTS (Fountains, etc) ---
            .then(
                Commands.literal("addeffectpoint")
                    .then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("groups", StringArgumentType.greedyString())
                            .executes(this::executeAddEffectPoint)))
            )
            // --- UTILS ---
            .then(
                Commands.literal("clear")
                    .executes {
                        pendingLights.clear()
                        pendingLasers.clear()
                        pendingEffectPoints.clear()
                        it.source.sender.sendMessage(text("$PREFIX All pending stage elements cleared."))
                        Command.SINGLE_SUCCESS
                    }
            )
            .then(
                Commands.literal("save")
                    .then(Commands.argument("showName", StringArgumentType.word())
                        .executes(this::executeSave))
            )
            .build()
    }

    fun executeAddLight(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        val name = StringArgumentType.getString(ctx, "name")
        val groups = StringArgumentType.getString(ctx, "groups").split(" ").filter { it.isNotBlank() }
        val loc = player.location

        pendingLights.add(Light(name, Location(loc.x, loc.y, loc.z), groups))
        player.sendMessage(text("$PREFIX Light '$name' added to group(s) $groups."))
        return Command.SINGLE_SUCCESS
    }

    fun executeAddLaser(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        val name = StringArgumentType.getString(ctx, "name")
        val groups = StringArgumentType.getString(ctx, "groups").split(" ").filter { it.isNotBlank() }
        val loc = player.location

        pendingLasers.add(LaserPoint(name, Location(loc.x, loc.y, loc.z), groups))
        player.sendMessage(text("$PREFIX Laser '$name' added to group(s) $groups."))
        return Command.SINGLE_SUCCESS
    }

    fun executeAddEffectPoint(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        val name = StringArgumentType.getString(ctx, "name")
        val groups = StringArgumentType.getString(ctx, "groups").split(" ").filter { it.isNotBlank() }
        val loc = player.location

        pendingEffectPoints.add(EffectPoint(name, Location(loc.x, loc.y, loc.z), groups))
        player.sendMessage(text("$PREFIX Effect Point '$name' added to group(s) $groups."))
        return Command.SINGLE_SUCCESS
    }

    private fun executeGiveTool(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        val item = ItemStack(Material.BREEZE_ROD)
        val meta = item.itemMeta
        meta.displayName(text("<gradient:aqua:white>Stage Creator Wand</gradient>"))
        meta.lore(listOf(text("Right-click a block to add an EffectPoint"), text("Default Group: 'Water'")))
        item.itemMeta = meta

        player.inventory.addItem(item)
        player.sendMessage(text("$PREFIX You received the Stage Creator Wand!"))
        return Command.SINGLE_SUCCESS
    }

    private fun executeSave(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        val showName = StringArgumentType.getString(ctx, "showName")
        val gson = GsonBuilder().setPrettyPrinting().create()

        val directory = File("plugins/MCFireworkShow/shows")
        val file = File(directory, "$showName.json")

        try {
            val rootObject: JsonObject = if (file.exists()) {
                // Load existing JSON if it exists
                JsonParser.parseString(file.readText()).asJsonObject
            } else {
                // Create a shell if it's a new show
                JsonObject().apply {
                    addProperty("showName", showName)
                    add("stage", JsonObject())
                }
            }

            // Get the "stage" section
            val stageJson = rootObject.getAsJsonObject("stage") ?: JsonObject()

            // 1. Update Lights
            val existingLights = stageJson.getAsJsonArray("lights")?.map { gson.fromJson(it, Light::class.java) } ?: emptyList()
            val mergedLights = existingLights + pendingLights

            // 2. Update Lasers
            val existingLasers = stageJson.getAsJsonArray("lasers")?.map { gson.fromJson(it, LaserPoint::class.java) } ?: emptyList()
            val mergedLasers = existingLasers + pendingLasers

            // 3. Update Effect Points
            val existingEffects = stageJson.getAsJsonArray("effectPoints")?.map { gson.fromJson(it, EffectPoint::class.java) } ?: emptyList()
            val mergedEffects = existingEffects + pendingEffectPoints

            // Re-map back to JsonArrays and set them in the stage object
            stageJson.add("lights", gson.toJsonTree(mergedLights))
            stageJson.add("lasers", gson.toJsonTree(mergedLasers))
            stageJson.add("effectPoints", gson.toJsonTree(mergedEffects))

            // Ensure the origin location is set if it was missing
            if (!stageJson.has("location")) {
                val loc = player.location
                stageJson.add("location", gson.toJsonTree(Location(loc.x, loc.y, loc.z)))
            }

            rootObject.add("stage", stageJson)

            // Save back to disk
            file.writeText(gson.toJson(rootObject))

            // Cleanup
            pendingLights.clear()
            pendingLasers.clear()
            pendingEffectPoints.clear()

            player.sendMessage(text("$PREFIX Successfully updated $showName.json with new elements!"))
            return 1

        } catch (e: Exception) {
            player.sendMessage(text("$PREFIX <red>Error appending to file: ${e.message}"))
            e.printStackTrace()
            return 0
        }
    }
}