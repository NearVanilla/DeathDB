package com.joshdev.deathmanager.commands

import com.joshdev.deathmanager.DeathManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Suppress("unused")
class ShowDeaths : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if(sender is Player){
            val player: Player = sender
            if(player.hasPermission("deathmanager.showdeaths")){
                val targetUsername = args?.get(0)
                if(targetUsername == null){
                    val noTargetComponent = Component.text(
                        "Please provide a username of the person you would like to see deaths for.",
                        NamedTextColor.RED,
                        TextDecoration.BOLD
                    )
                    player.sendMessage(noTargetComponent)
                    return true
                }else{
                    val targetPlayer = DeathManager.pluginInstance.server.getOfflinePlayer(targetUsername)
                    if(!targetPlayer.hasPlayedBefore()){
                        val neverPlayedComponent = Component.text(
                            "This player has never been on this server before.",
                            NamedTextColor.RED,
                            TextDecoration.BOLD
                        )
                        player.sendMessage(neverPlayedComponent)
                        return true
                    }else{
                        val preparedSelectStatement = DeathManager.dbConnection.prepareStatement("SELECT * FROM deaths WHERE uniqueId = ? ORDER BY timeOfDeath DESC LIMIT 5") // Ignore SQL Dialect warning.
                        preparedSelectStatement.setString(1, targetPlayer.uniqueId.toString())
                        val results = preparedSelectStatement.executeQuery()
                        var deathListComponent = Component.text(
                            "List of Deaths\n===============\n",
                            NamedTextColor.GRAY,
                            TextDecoration.BOLD
                        )
                        val deathIndex = 1
                        while(results.next()){
                            // Convert epoch second back to human-readable time.
                            val dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(results.getInt(2).toLong()), ZoneId.systemDefault())
                            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy @ HH:mm")
                            val formattedDateTime = dateTime.format(formatter)
                            // Format position
                            val formattedPosition = "${results.getDouble(3)}, ${results.getDouble(4)}, ${results.getDouble(5)}"
                            // Create component
                            val entryComponent = Component.text(
                                "${deathIndex}) $formattedDateTime | $formattedPosition | [View Inventory]\n", // TODO Make clickable button to go to inventory.
                                NamedTextColor.GRAY,
                                TextDecoration.BOLD
                            )
                            deathListComponent = deathListComponent.append(entryComponent)
                        }
                        player.sendMessage(deathListComponent)
                        return true
                    }
                }
            }else{
                val noPermissionComponent = Component.text(
                    "You do not have permission to run this command.",
                    NamedTextColor.RED,
                    TextDecoration.BOLD
                )
                player.sendMessage(noPermissionComponent)
                return true
            }
        }else{
            sender.sendMessage("This command can only be ran by players.")
            return true
        }
    }
}