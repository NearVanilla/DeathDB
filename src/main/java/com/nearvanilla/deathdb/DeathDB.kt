/* Licensed under GNU General Public License v3.0 */
package com.nearvanilla.deathdb

import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.arguments.parser.ParserParameters
import cloud.commandframework.arguments.parser.StandardParameters
import cloud.commandframework.execution.CommandExecutionCoordinator
import cloud.commandframework.meta.CommandMeta
import cloud.commandframework.paper.PaperCommandManager
import com.nearvanilla.deathdb.commands.RestoreInventory
import com.nearvanilla.deathdb.commands.ShowDeaths
import com.nearvanilla.deathdb.events.OnPlayerDeath
import com.nearvanilla.deathdb.exceptions.DeathDBException
import com.nearvanilla.deathdb.libs.DatabaseWrapper
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.function.Function
import java.util.logging.Logger

@Suppress("UNUSED") // Main class comes up as unused when it actually is, Kotlin dumb si
class DeathDB : JavaPlugin() {

    companion object {
        lateinit var pluginLogger: Logger
            private set
        lateinit var dbWrapper: DatabaseWrapper
            private set
        lateinit var pluginInstance: DeathDB
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
        logger.info("Setting up DeathDB...")
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
            throw DeathDBException("The Database Wrapper, Logger or Plugin Instance has not initialized properly.")
        }
        logger.info("DeathDB has been enabled, enjoy!") // Log that plugin is enabled si.
    }

    override fun onDisable() { logger.info("DeathDB has been disabled, goodbye!") }
}
