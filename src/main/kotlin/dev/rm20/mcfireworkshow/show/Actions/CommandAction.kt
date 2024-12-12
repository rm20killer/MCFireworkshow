package dev.rm20.mcfireworkshow.show.Actions

import org.bukkit.Bukkit

class CommandAction {


    fun runCommand(command: String) {

        try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
        } catch (e: Exception) {
            Bukkit.getLogger().warning("Error executing command: $command")
        }

    }
}