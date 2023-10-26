/* Licensed under GNU General Public License v3.0 */
package com.nearvanilla.deathmanager

import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.arguments.parser.ParserParameters
import cloud.commandframework.arguments.parser.StandardParameters
import cloud.commandframework.execution.CommandExecutionCoordinator
import cloud.commandframework.meta.CommandMeta
import cloud.commandframework.paper.PaperCommandManager
import com.nearvanilla.deathmanager.commands.RestoreInventory
import com.nearvanilla.deathmanager.commands.ShowDeaths
import com.nearvanilla.deathmanager.events.OnPlayerDeath
import com.nearvanilla.deathmanager.exceptions.DeathManagerException
import com.nearvanilla.deathmanager.libs.DatabaseWrapper
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.function.Function
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

    // Cloud Stuff
    private lateinit var commandManager: PaperCommandManager<CommandSender>
    private lateinit var annotationParser: AnnotationParser<CommandSender>
    private lateinit var commandMetaFunction: Function<ParserParameters, CommandMeta>

    override fun onEnable() {
        logger.info("Setting up DeathManager...")
        commandManager = PaperCommandManager(
            this,
            CommandExecutionCoordinator.simpleCoordinator(),
            Function.identity(),
            Function.identity(),
        )
        commandMetaFunction =
            Function<ParserParameters, CommandMeta> { p: ParserParameters ->
                CommandMeta.simple() // This will allow you to decorate commands with descriptions
                    .with(
                        CommandMeta.DESCRIPTION,
                        p.get(StandardParameters.DESCRIPTION, "No description"),
                    )
                    .build()
            }
        annotationParser = AnnotationParser(
            commandManager,
            CommandSender::class.java,
            commandMetaFunction,
        )
        annotationParser.parse(RestoreInventory())
        annotationParser.parse(ShowDeaths())
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
        if (!isWrapperInitialized() || !isLoggerInitialized() || !isPluginInstanceInitialized()) {
            throw DeathManagerException("The Database Wrapper, Logger or Plugin Instance has not initialized properly.")
        }
        logger.info("DeathManager has been enabled, enjoy!") // Log that plugin is enabled si.
    }

    override fun onDisable() { logger.info("DeathManager has been disabled, goodbye!") }
}
