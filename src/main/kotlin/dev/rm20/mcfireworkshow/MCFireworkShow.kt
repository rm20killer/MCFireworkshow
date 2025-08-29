package dev.rm20.mcfireworkshow

import dev.rm20.mcfireworkshow.managers.RegisterManager
import dev.rm20.mcfireworkshow.show.ShowManager
import hm.zelha.particlesfx.util.ParticleSFX
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

        fun getPluginInstance(): MCFireworkShow? {
            return if (::instance.isInitialized) {
                instance
            } else {
                null
            }
        }
    }

    init {
        instance = this
    }
    override fun onEnable() {
        instance = this
        // Plugin startup logic
        val time = measureTimeMillis {
            RegisterManager.registerListeners(this)
        }
        this.saveDefaultConfig();
        showManager = ShowManager(this)
        ParticleSFX.setPlugin(this)
        logger.info("Plugin enabled in $time ms")

    }

    override fun onDisable() {
        server.scheduler.cancelTasks(this)
        ParticleSFX.setPlugin(null)
        logger.info("Plugin disabled")
    }

}