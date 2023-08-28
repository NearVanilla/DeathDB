/* Licensed under GNU General Public License v3.0 */
package com.nearvanilla.deathmanager

import com.nearvanilla.deathmanager.commands.RestoreInventory
import com.nearvanilla.deathmanager.commands.ShowDeaths
import com.nearvanilla.deathmanager.events.OnPlayerDeath
import com.nearvanilla.deathmanager.exceptions.DeathManagerException
import com.nearvanilla.deathmanager.libs.DatabaseWrapper
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.logging.Logger

@Suppress("UNUSED") // Main class comes up as unused when it actually is, Kotlin dumb si
class DeathManager : JavaPlugin() {

    companion object {
        lateinit var pluginLogger: Logger
            private set
        lateinit var dbWrapper: DatabaseWrapper
            private set
        lateinit var pluginInstance: DeathManager
            private set
        fun isLoggerInitialized(): Boolean {
            return Companion::pluginLogger.isInitialized
        }
        fun isWrapperInitialized(): Boolean {
            return Companion::dbWrapper.isInitialized
        }
        fun isPluginInstanceInitialized(): Boolean {
            return Companion::pluginInstance.isInitialized
        }
    }

    override fun onEnable() {
        logger.info("Setting up DeathManager...")
        pluginLogger = logger
        pluginInstance = this
        if (!dataFolder.exists()) {
            dataFolder.mkdir()
        }
        val dbPath = dataFolder.absolutePath + "/data.db"
        val dbFile = File(dbPath)
        if (!dbFile.exists()) {
            dbFile.createNewFile()
        }
        dbWrapper = DatabaseWrapper(dbPath)
        dbWrapper.createDeathsTable()
        server.pluginManager.registerEvents(OnPlayerDeath(), this)
        this.getCommand("showdeaths")?.setExecutor(ShowDeaths())
        this.getCommand("restoreinventory")?.setExecutor(RestoreInventory())
        if (!isWrapperInitialized() || !isLoggerInitialized() || !isPluginInstanceInitialized()) {
            throw DeathManagerException("The Database Wrapper, Logger or Plugin Instance has not initialized properly.")
        }
        logger.info("DeathManager has been enabled, enjoy!") // Log that plugin is enabled si.
    }

    override fun onDisable() { logger.info("DeathManager has been disabled, goodbye!") }
}
