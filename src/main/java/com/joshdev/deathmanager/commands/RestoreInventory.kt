/* Licensed under GNU General Public License v3.0 */
package com.joshdev.deathmanager.commands

import com.joshdev.deathmanager.DeathManager
import com.joshdev.deathmanager.libs.Serialization
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class RestoreInventory : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender is Player) {
            val player: Player = sender
            if (!player.hasPermission("deathmanager.restoreinventory")) {
                val invalidPermissionComponent = Component.text(
                    "You do not have permission to run this command.",
                    NamedTextColor.RED,
                    TextDecoration.BOLD,
                )
                player.sendMessage(invalidPermissionComponent)
                return true
            } else {
                val targetUsername = args?.get(0)
                val targetIndex = args?.get(1)
                if (targetUsername == null || targetIndex == null) {
                    val invalidArgsComponent = Component.text(
                        "One or more of the required arguments have not been provided.",
                        NamedTextColor.RED,
                        TextDecoration.BOLD,
                    )
                    player.sendMessage(invalidArgsComponent)
                    return true
                } else {
                    val targetPlayer = DeathManager.pluginInstance.server.getOfflinePlayer(targetUsername)

                    if (!targetPlayer.isOnline) {
                        val offlineComponent = Component.text(
                            "This player is offline.",
                            NamedTextColor.RED,
                            TextDecoration.BOLD,
                        )
                        player.sendMessage(offlineComponent)
                        return true
                    } else if (!targetPlayer.hasPlayedBefore()) {
                        val neverPlayedComponent = Component.text(
                            "This player has never been on this server before.",
                            NamedTextColor.RED,
                            TextDecoration.BOLD,
                        )
                        player.sendMessage(neverPlayedComponent)
                        return true
                    } else {
                        val preparedSelectStatement = DeathManager.dbConnection.prepareStatement("SELECT * FROM deaths WHERE uniqueId = ? ORDER BY timeOfDeath DESC LIMIT 5") // Ignore SQL Dialect warning.
                        preparedSelectStatement.setString(1, targetPlayer.uniqueId.toString())
                        val results = preparedSelectStatement.executeQuery()
                        if (results == null) {
                            val noResultsComponent = Component.text(
                                "Couldn't find any deaths for this user, have they died on the server yet?",
                                NamedTextColor.RED,
                                TextDecoration.BOLD,
                            )
                            player.sendMessage(noResultsComponent)
                            return true
                        } else {
                            var resultIndex = 1
                            var resultInventory: String? = null
                            while (results.next()) {
                                if (resultIndex.toString() == targetIndex) {
                                    resultInventory = results.getString("serializedInventory")
                                }
                                resultIndex++
                            }
                            if (resultInventory == null) {
                                val noResultComponent = Component.text(
                                    "Could not find a record with that index.",
                                    NamedTextColor.RED,
                                    TextDecoration.BOLD,
                                )
                                player.sendMessage(noResultComponent)
                                return true
                            } else {
                                val deserializedInventory = Serialization.Deserialize(resultInventory)
                                for (itemStack in deserializedInventory) {
                                    targetPlayer.player?.inventory?.addItem(itemStack)
                                }
                                val successComponent = Component.text(
                                    "This players inventory is successfully restored!",
                                    NamedTextColor.GREEN,
                                    TextDecoration.BOLD,
                                )
                                player.sendMessage(successComponent)
                                return true
                            }
                        }
                    }
                }
            }
        } else {
            sender.sendMessage("This command is for Players only.")
            return true
        }
    }
}
