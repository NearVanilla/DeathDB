package com.nearvanilla.death.database

import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.arguments.parser.ParserParameters
import cloud.commandframework.arguments.parser.StandardParameters
import cloud.commandframework.bukkit.BukkitCommandManager
import cloud.commandframework.bukkit.CloudBukkitCapabilities
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator
import cloud.commandframework.meta.CommandMeta
import cloud.commandframework.paper.PaperCommandManager
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.function.Function


object DeathDatabaseCommands {

    lateinit var manager : BukkitCommandManager<CommandSender>
    lateinit var annotationParser : AnnotationParser<CommandSender>

    fun register(plugin: PluginLifecycle){
        val executionCoordinatorFunction =
            AsynchronousCommandExecutionCoordinator.newBuilder<CommandSender>().build()

        val mapperFunction = Function.identity<CommandSender>()
        manager = PaperCommandManager(
            plugin,
            executionCoordinatorFunction,
            mapperFunction,
            mapperFunction)

        if(manager.queryCapability(CloudBukkitCapabilities.BRIGADIER)){
            manager.registerBrigadier()
        }

        if(manager.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)){
            (manager as PaperCommandManager<CommandSender>).registerAsynchronousCompletions()
        }

        val commandMetaFunction =
            Function<ParserParameters, CommandMeta> { p: ParserParameters ->
                CommandMeta.simple().with(
                    CommandMeta.DESCRIPTION,
                    p.get(StandardParameters.DESCRIPTION, "Commands for dead inventories.")
                ).build()
            }

        annotationParser = AnnotationParser(
            manager, CommandSender::class.java, commandMetaFunction)
        annotationParser.parse(this)
    }

    @CommandMethod("deathdatabase|deathdb|ddb")
    fun helpMenu(sender: CommandSender) = Unit

    @CommandMethod("deathdatabase|deathdb|ddb open|o <player>")
    @CommandPermission("deathdatabase.open")
    fun openDeadInventory(sender: CommandSender, @Argument("player") player : Player) {
        if(sender is Player){
            info("Opening dead inventory for player: ${player.name}")
            DeadInventoryScreen(sender, player).show()
        } else {
            warn("Cannot open inventory if not a player")
        }
    }

    @CommandMethod("deathdatabase|deathdb|ddb open|o offline <player>")
    @CommandPermission("deathdatabase.open.offline")
    fun openDeadInventoryOffline(sender: CommandSender, @Argument("player") player : String) {
        // Used for offline players
        val expectedPlayer : Player? = DeathDatabase.getOfflinePlayer(player)
        if(expectedPlayer == null){
            sender.sendMessage("This player either hasn't died or doesn't exist.")
            return
        }
        openDeadInventory(sender, expectedPlayer)
    }

    @CommandMethod("deathdatabase|deathdb|ddb restore|r <player>")
    @CommandPermission("deathdatabase.restore")
    fun restoreDeadInventory(sender: CommandSender, @Argument("player") player : Player) {
        if(!player.isOnline){
            sender.sendMessage("${ChatColor.RED} [DeathDatabase] - ${player.displayName} is not online!")
            return
        }
        Bukkit.getScheduler().callSyncMethod(PluginLifecycle.INSTANCE){
            val notAddedItems = player.inventory.addItem(*DeathDatabase[player].toTypedArray())
            notAddedItems.forEach { count, itemStack ->
                player.world.dropItem(player.location, itemStack)
            }
        }
    }

    @CommandMethod("deathdatabase|deathdb|ddb clear|c <player>")
    @CommandPermission("deathdatabase.clear")
    fun clearDeadInventory(sender: CommandSender, @Argument("player") player : Player) {
        DeathDatabase[player] = mutableListOf()
    }


}