package dev.rm20.mcfireworkshow

import dev.rm20.mcfireworkshow.managers.RegisterManager
import dev.rm20.mcfireworkshow.show.ShowManager
import org.bukkit.plugin.java.JavaPlugin
import kotlin.system.measureTimeMillis

class MCFireworkShow : JavaPlugin() {

    companion object {
        lateinit var showManager: ShowManager
        lateinit var instance: MCFireworkShow
            private set

        fun reload() {
            instance.reloadConfig()
        }
    }

    init {
        instance = this
    }
    override fun onEnable() {

        // Plugin startup logic
        val time = measureTimeMillis {
            RegisterManager.registerListeners(this)
        }
        this.saveDefaultConfig();
        showManager = ShowManager(this)
        logger.info("Plugin enabled in $time ms")


    }

}