package dev.rm20.mcfireworkshow.show.Actions

import ActionData
import org.bukkit.Bukkit

class EffectMasterAction {
    fun playEffectM(effectMasterData: ActionData.EffectMasterData)
    {
        val command = "em playat ${effectMasterData.category} ${effectMasterData.name}.yml world, ${effectMasterData.location.x}, ${effectMasterData.location.y}, ${effectMasterData.location.z}"
        try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
        } catch (e: Exception) {
            Bukkit.getLogger().warning("Error executing command: $command")
        }


    }


}