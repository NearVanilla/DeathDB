package com.joshdev.deathmanager

import com.joshdev.deathmanager.commands.RestoreInv
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

    companion object{

        // For logging shit
        var pluginLogger: Logger? = null
            private set

        // For saving shit
        var dbConnection: Connection? = null
            private set

    }

    override fun onEnable() {

        pluginLogger = logger // Set plugin logger object.

        // Database Connection Setup

        if(!dataFolder.exists()){ // If data folder doesn't exist.

            dataFolder.mkdir() // Create it.

        } // End of if statement.

        val dbPath = dataFolder.absolutePath + "/data.db" // Get absolute path of data folder and concatenate db file.
        val dbFile = File(dbPath) // Create file object from path.

        if(!dbFile.exists()){ // If file doesn't exist.

            dbFile.createNewFile() // Create it.

        } // End of if statement.

        // Use elvis operator to establish connection to database or throw exception.
        val connection = DriverManager.getConnection("jdbc:sqlite:$dbPath") ?: throw DeathManagerException("Connection to database couldn't be established.") // Get the SQLite Connection to the database.

        dbConnection = connection // Set connection object.

        server.pluginManager.registerEvents(OnPlayerDeath(), this) // Register on player death event.
        this.getCommand("showdeaths")?.setExecutor(ShowDeaths()) // Register show deaths command.
        this.getCommand("restoreinv")?.setExecutor(RestoreInv()) // Register restore inv command.

        logger.info("DeathManager has been enabled, enjoy!") // Log that plugin is enabled si.

    }

    override fun onDisable() {

        logger.info("DeathManager has been disabled, goodbye!")

    }

}