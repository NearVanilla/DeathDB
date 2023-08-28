/* Licensed under GNU General Public License v3.0 */
package com.nearvanilla.deathmanager.commands

import com.nearvanilla.deathmanager.DeathManager
import com.nearvanilla.deathmanager.libs.Serialization
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
                if (args == null || args.count() != 2) {
                    val invalidArgsComponent = Component.text(
                        "One or more of the required arguments have not been provided.",
                        NamedTextColor.RED,
                        TextDecoration.BOLD,
                    )
                    player.sendMessage(invalidArgsComponent)
                    return true
                } else {
                    val targetUsername = args[0]
                    val convertedTargetIndex = args[1].toIntOrNull()
                    if (convertedTargetIndex == null) {
                        val invalidIndexComponent = Component.text(
                            "The index you have provided is not valid.",
                            NamedTextColor.RED,
                            TextDecoration.BOLD,
                        )
                        player.sendMessage(invalidIndexComponent)
                        return true
                    } else if (convertedTargetIndex > 5 || convertedTargetIndex < 1) {
                        val invalidRangeComponent = Component.text(
                            "Your index must be between 1 and 5.",
                            NamedTextColor.RED,
                            TextDecoration.BOLD,
                        )
                        player.sendMessage(invalidRangeComponent)
                        return true
                    }
                    val targetOfflinePlayer = DeathManager.pluginInstance.server.getOfflinePlayer(targetUsername)
                    if (!targetOfflinePlayer.isOnline) {
                        val offlineComponent = Component.text(
                            "This player is offline.",
                            NamedTextColor.RED,
                            TextDecoration.BOLD,
                        )
                        player.sendMessage(offlineComponent)
                        return true
                    } else if (!targetOfflinePlayer.hasPlayedBefore()) {
                        val neverPlayedComponent = Component.text(
                            "This player has never been on this server before.",
                            NamedTextColor.RED,
                            TextDecoration.BOLD,
                        )
                        player.sendMessage(neverPlayedComponent)
                        return true
                    } else {
                        val results = DeathManager.dbWrapper.getPlayerInformation(targetOfflinePlayer as Player)
                        var resultsIndex = 1
                        var serializedInventory: String? = null
                        while (results.next()) {
                            if (resultsIndex == convertedTargetIndex) {
                                serializedInventory = results.getString("serializedInventory")
                                break
                            }
                            resultsIndex += 1
                        }
                        if (serializedInventory == null) {
                            val noInventoryComponent = Component.text(
                                "No inventory found.",
                                NamedTextColor.RED,
                                TextDecoration.BOLD,
                            )
                            player.sendMessage(noInventoryComponent)
                            return true
                        } else {
                            val targetOnlinePlayer = targetOfflinePlayer.player
                            if (targetOnlinePlayer != null) {
                                val deserializedInventory = Serialization.Deserialize(serializedInventory)
                                val currentInventory = targetOnlinePlayer.inventory.contents
                                targetOnlinePlayer.inventory.clear()
                                for (item in currentInventory) {
                                    if (item != null) {
                                        targetOnlinePlayer.world.dropItem(targetOnlinePlayer.location, item)
                                    }
                                }
                                targetOnlinePlayer.inventory.contents = deserializedInventory
                                val successComponent = Component.text(
                                    "Sucessfully restored inventory.",
                                    NamedTextColor.GREEN,
                                    TextDecoration.BOLD,
                                )
                                player.sendMessage(successComponent)
                                return true
                            } else {
                                val invalidPlayerComponent = Component.text(
                                    "Failed to find the player.",
                                    NamedTextColor.RED,
                                    TextDecoration.BOLD,
                                )
                                player.sendMessage(invalidPlayerComponent)
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
