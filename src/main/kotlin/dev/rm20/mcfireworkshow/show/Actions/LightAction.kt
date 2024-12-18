package dev.rm20.mcfireworkshow.show.Actions

import ActionData
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World

class LightAction {
    fun LightController(lightData: ActionData.LightData)
    {
        val world: World? = Bukkit.getWorld("world")
        val location = org.bukkit.Location(
            world,
            lightData.location.x,
            lightData.location.y,
            lightData.location.z,
        )

        Bukkit.getLogger().info("changing lit of: $location which is ${(location.block.getType())}")
        if(location.block.getType()==Material.REDSTONE_LAMP)
        {
            val data = location.block.blockData as org.bukkit.block.data.Lightable
            data.isLit = lightData.lit
            location.block.blockData = data
        }

    }
}