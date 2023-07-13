/* Licensed under GNU General Public License v3.0 */
package com.joshdev.deathmanager

import com.joshdev.deathmanager.commands.RestoreInventory
import com.joshdev.deathmanager.commands.ShowDeaths
import com.joshdev.deathmanager.events.OnPlayerDeath
import com.joshdev.deathmanager.exceptions.DeathManagerException
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.util.logging.Logger

@Suppress("UNUSED") // Main class comes up as unused when it actually is, Kotlin dumb si
class DeathManager : JavaPlugin() {

    companion object {
        lateinit var pluginLogger: Logger
            private set
        lateinit var dbConnection: Connection
            private set
        lateinit var pluginInstance: DeathManager
            private set
        fun isLoggerInitialized(): Boolean {
            return ::pluginLogger.isInitialized
        }
        fun isConnectionInitialized(): Boolean {
            return ::dbConnection.isInitialized
        }
        fun isPluginInstanceInitialized(): Boolean {
            return ::pluginInstance.isInitialized
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
        val connection = DriverManager.getConnection("jdbc:sqlite:$dbPath") ?: throw DeathManagerException("Connection to database couldn't be established.")
        dbConnection = connection
        val stmt = connection.createStatement()
        stmt.execute("CREATE TABLE IF NOT EXISTS deaths(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, uniqueId TEXT NOT NULL, timeOfDeath INTEGER NOT NULL, posX REAL NOT NULL, posY REAL NOT NULL, posZ REAL NOT NULL, worldName TEXT NOT NULL, serializedInventory BLOB NOT NULL);")
        server.pluginManager.registerEvents(OnPlayerDeath(), this)
        this.getCommand("showdeaths")?.setExecutor(ShowDeaths())
        this.getCommand("restoreinventory")?.setExecutor(RestoreInventory())
        if (!isConnectionInitialized() || !isLoggerInitialized() || !isPluginInstanceInitialized()) {
            throw DeathManagerException("The Database Connection, Logger or Plugin Instance has not initialized properly.")
        }
        logger.info("DeathManager has been enabled, enjoy!") // Log that plugin is enabled si.
    }

    override fun onDisable() { logger.info("DeathManager has been disabled, goodbye!") }
}
