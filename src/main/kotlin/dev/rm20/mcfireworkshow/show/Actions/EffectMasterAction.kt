package dev.rm20.mcfireworkshow.show.Actions

import ActionData
import me.m64diamondstar.effectmaster.shows.EffectShow
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World

class EffectMasterAction {
    fun playEffectM(effectMasterData: ActionData.EffectMasterData)
    {

        val world: World? = Bukkit.getWorld("world")
        val location = Location(
            world,
            effectMasterData.location.x,
            effectMasterData.location.y,
            effectMasterData.location.z
        )
        val effectShow = EffectShow(effectMasterData.category, effectMasterData.name)
        if(effectShow.centerLocation == null) {
            return
        }
        effectShow.play(null, location, false)

    }


}