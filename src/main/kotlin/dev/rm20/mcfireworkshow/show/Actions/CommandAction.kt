package dev.rm20.mcfireworkshow.show.Actions

import org.bukkit.Bukkit


/**
 * Handles the execution of commands.
 */
class CommandAction {

    /**
     * Executes a command as the console sender.
     * @param command The command to execute.
     */
    fun runCommand(command: String) {

        try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
        } catch (e: Exception) {
            Bukkit.getLogger().warning("Error executing command: $command")
        }

    }
}